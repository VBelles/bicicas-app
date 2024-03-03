package com.tcn.bicicas

import app.cash.turbine.test
import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.main.buildHttpClient
import com.tcn.bicicas.pin.PinModuleImpl
import com.tcn.bicicas.pin.presentation.PinState
import com.tcn.bicicas.pin.presentation.PinViewModel
import com.tcn.bicicas.responses.PIN_AUTH_RESPONSE
import com.tcn.bicicas.responses.TWO_FACTOR_RESPONSE
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.respondJson
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private fun pinModule(clock: Clock = Clock { 0 }): PinModuleImpl {
        val mockEngine = MockEngine.config {
            addHandler { request ->
                val (user, pass) = request.url.parameters["username"] to request.url.parameters["password"]
                when (request.url.encodedPath) {
                    "/oauth/token" -> when {
                        user == "user" && pass == "pass" -> respondJson(PIN_AUTH_RESPONSE)
                        else -> respondBadRequest()
                    }

                    "/dashboard" -> respondJson(TWO_FACTOR_RESPONSE)
                    else -> respondBadRequest()
                }
            }
        }
        val dispatcher = mainCoroutineRule.dispatcher
        return PinModuleImpl(
            httpClient = { buildHttpClient(mockEngine) },
            storeManager = { StoreManager(dispatcher, "", clock::millis, FakeFileSystem()) },
            clock = clock,
            oauthBaseUrl = "http://0.0.0.0",
            oauthClientId = "clientId",
            oauthClientSecret = "clientSecret"
        )
    }

    @Test
    fun when_login_is_performed_then_state_is_loading() = runTest {
        val viewModel: PinViewModel = pinModule().pinViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onLogin("user", "pass")
            val loadingState = awaitItem() as PinState.LoggedOut
            assertTrue(loadingState.loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_success_login_is_performed_then_state_is_success() = runTest {
        val viewModel: PinViewModel = pinModule().pinViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onLogin("user", "pass")
            awaitItem() // Loading
            val state = awaitItem() as PinState.LoggedIn // Success login
            assertEquals(state.pinResult.user, "1234")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_login_is_performed_with_wrong_credentials_then_state_has_error() = runTest {
        val viewModel: PinViewModel = pinModule().pinViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onLogin("user", "wrongpass")
            val state = (awaitItem() as PinState.LoggedOut).takeIf { it.loginError != null }
                ?: awaitItem() as PinState.LoggedOut
            assertEquals(PinState.LoginError.WrongUserPass, state.loginError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_success_login_then_pin_progress_is_refreshed_automatically() = runTest {
        val viewModel: PinViewModel = pinModule { currentTime }.pinViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onLogin("user", "pass")
            awaitItem() // Loading
            val state = awaitItem() as PinState.LoggedIn // Success login
            assertEquals(30_000L, state.pinResult.remainTime)
            cancelAndIgnoreRemainingEvents()
        }
        advanceTimeBy(15_000)
        viewModel.state.test {
            awaitItem() // Previous state
            val state = awaitItem() as PinState.LoggedIn
            assertEquals(15_000L, state.pinResult.remainTime)
            cancelAndIgnoreRemainingEvents()
        }
        advanceTimeBy(15_000)
        viewModel.state.test {
            awaitItem() // Previous state
            val state = awaitItem() as PinState.LoggedIn
            assertEquals(30_000, state.pinResult.remainTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_logout_is_performed_then_pin_state_is_not_refreshed() = runTest {
        val viewModel = pinModule().pinViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onLogin("user", "pass")
            awaitItem() // Loading
            awaitItem() as PinState.LoggedIn // Success login
            viewModel.onLogout()
            awaitItem() as PinState.LoggedOut // Logged out
            cancelAndIgnoreRemainingEvents()
        }
    }
}
package com.tcn.bicicas

import android.content.SharedPreferences
import app.cash.turbine.test
import com.tcn.bicicas.data.Clock
import com.tcn.bicicas.di.networkModule
import com.tcn.bicicas.di.pinModule
import com.tcn.bicicas.ui.pin.PinState
import com.tcn.bicicas.ui.pin.PinViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.MemorySharedPreferences
import com.tcn.bicicas.utils.enqueueJson
import com.tcn.bicicas.utils.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PinTest : KoinTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockWebServer = MockWebServer()

    private var time: Long = 0

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module { single { Clock { time } } },
            networkModule,
            module { single<SharedPreferences> { MemorySharedPreferences() } },
            pinModule(mockWebServer.url("/").toString()),
        )
    }

    @Test
    fun when_login_is_performed_then_state_is_loading() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json")
        mockWebServer.enqueueJson("two_factor_response.json")

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            assertTrue(awaitItem().loading)
            assertFalse(awaitItem().loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_success_login_is_performed_then_state_is_success() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json")
        mockWebServer.enqueueJson("two_factor_response.json")

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            awaitItem() // Loading
            val state = awaitItem() // Success login
            assertTrue(state.loggedIn)
            assertEquals(state.userNumber, "1234")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_login_is_performed_with_wrong_credentials_then_state_has_error() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json", 401)

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            awaitItem() // Loading
            val state = awaitItem() // Failure login
            assertFalse(state.loggedIn)
            assertEquals(PinState.LoginError.WrongUserPass, state.loginError)
        }
    }

    @Test
    fun when_success_login_then_pin_progress_is_refreshed_automatically() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json")
        mockWebServer.enqueueJson("two_factor_response.json")
        time = 0

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            awaitItem() // Loading
            awaitItem() // Success login

            var state = awaitItem()
            assertNotNull(state.pin)
            assertEquals("30", state.timeText)
            assertEquals(1f, state.progress)

            time += 15_000
            mainCoroutineRule.scheduler.advanceTimeBy(15_000)
            state = awaitItem()

            assertEquals("15", state.timeText)
            assertEquals(0.5f, state.progress)

            time += 15_000
            mainCoroutineRule.scheduler.advanceTimeBy(15_000)
            state = awaitItem()

            assertEquals("30", state.timeText)
            assertEquals(1f, state.progress)
        }
    }

    @Test
    fun when_success_login_then_pin_is_refreshed_only_every_30s() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json")
        mockWebServer.enqueueJson("two_factor_response.json")
        time = 0

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            awaitItem() // Loading
            awaitItem() // Success login

            val pin = awaitItem().pin
            assertNotNull(pin)

            time += 15_000
            mainCoroutineRule.scheduler.advanceTimeBy(15_000)
            assertEquals(pin, awaitItem().pin)

            time += 15_000
            mainCoroutineRule.scheduler.advanceTimeBy(15_000)
            assertNotEquals(pin, awaitItem().pin)
        }
    }

    @Test
    fun when_logout_is_performed_then_pin_state_is_not_refreshed() = runBlocking {
        val pinViewModel: PinViewModel = koinTestRule.get()
        mockWebServer.enqueueJson("pin_auth_response.json")
        mockWebServer.enqueueJson("two_factor_response.json")
        time = 0

        pinViewModel.pinState.test {
            awaitItem() // Initial empty state
            pinViewModel.login("user", "pass")
            awaitItem() // Loading
            awaitItem() // Success login
            awaitItem() // Pin updated

            pinViewModel.logout()
            val logoutState = awaitItem()
            /*            assertNull(logoutState.pin)
                        assertNull(logoutState.nextPin)
                        assertNull(logoutState.userNumber)*/
            assertFalse(logoutState.loggedIn)

            // Advance time and expect state to not be updated
            time += 15_000
            mainCoroutineRule.scheduler.advanceTimeBy(15_000)
            expectNoEvents()
        }
    }

}
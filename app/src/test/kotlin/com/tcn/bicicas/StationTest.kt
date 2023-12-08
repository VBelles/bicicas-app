package com.tcn.bicicas

import app.cash.turbine.test
import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.main.buildHttpClient
import com.tcn.bicicas.responses.STATIONS_SUCCESS_RESPONSE
import com.tcn.bicicas.responses.STATIONS_SUCCESS_RESPONSE_2
import com.tcn.bicicas.stations.StationsModule
import com.tcn.bicicas.stations.StationsModuleImpl
import com.tcn.bicicas.stations.presentation.StationsState
import com.tcn.bicicas.stations.presentation.StationsViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.respondJson
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StationTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private fun stationsModule(
        activeFlow: Flow<Boolean>,
        benchStatusResponse: () -> String? = ::STATIONS_SUCCESS_RESPONSE,
        clock: Clock = Clock { 0 },
    ): StationsModule {
        val mockEngine = MockEngine.config {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/bench_status" -> benchStatusResponse()?.let { respondJson(it) }
                        ?: respondBadRequest()

                    else -> respondBadRequest()
                }
            }
        }
        val dispatcher = mainCoroutineRule.dispatcher
        return StationsModuleImpl(
            clock = clock,
            storeManager = { StoreManager(dispatcher, "", clock::millis, FakeFileSystem()) },
            httpClient = { buildHttpClient(mockEngine) },
            stationsBaseUrl = "http://0.0.0.0",
            dateParser = { clock.millis() },
            activeFlow = activeFlow,
        )
    }


    @Test
    fun when_view_model_is_initialized_then_empty_state_is_emitted() = runTest {
        val viewModel = stationsModule(flowOf(false)).stationsViewModel
        viewModel.state.test {
            assertEquals(StationsState(hasLoaded = false), awaitItem())
            assertEquals(StationsState(hasLoaded = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_app_is_not_active_then_state_is_not_refreshed_automatically() = runTest {
        val viewModel = stationsModule(flowOf(false)).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            advanceTimeBy(30_001)
            expectNoEvents() // No new events
        }
    }

    @Test
    fun when_app_is_active_then_state_is_refreshed_automatically() = runTest {
        val activeFlow = MutableStateFlow(true)
        var response = STATIONS_SUCCESS_RESPONSE
        val viewModel = stationsModule(activeFlow, { response }).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            assertTrue(awaitItem().stations.isNotEmpty())
            response = STATIONS_SUCCESS_RESPONSE_2
            advanceTimeBy(30_000)
            assertTrue(awaitItem().stations.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_stations_are_being_refreshed_then_state_is_loading() = runTest {
        val viewModel = stationsModule(flowOf(false)).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            viewModel.onRefresh()
            assertTrue(awaitItem().isLoading)
            awaitItem() // Data fetched
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun when_refresh_is_success_then_state_is_updated() = runTest {
        val viewModel = stationsModule(flowOf(false)).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            viewModel.onRefresh()
            awaitItem() // Loading true
            assertTrue(awaitItem().stations.isNotEmpty()) // Success refresh
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_favorite_is_toggled_then_state_is_updated() = runTest {
        val viewModel = stationsModule(flowOf(false)).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            viewModel.onRefresh()
            awaitItem() // Loading true
            val station = awaitItem().also { println(it) }.stations.first()
            awaitItem() // Loading false
            assertFalse(station.favorite)
            viewModel.onFavoriteClicked(station.id)
            val updatedStation = awaitItem().also { println(it) }.stations.first()
            assertTrue(updatedStation.favorite)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun when_refresh_has_failure_then_error_event_is_emitted() = runTest {
        val viewModel: StationsViewModel = stationsModule(flowOf(false), { null }).stationsViewModel
        viewModel.state.test {
            awaitItem() // Initial empty state
            awaitItem() // Loaded state
            viewModel.onRefresh()
            awaitItem() // Loading true
            assertTrue(awaitItem().hasError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_stations_are_updated_then_stations_are_sorted_by_favorite_and_id() = runTest {
        val viewModel: StationsViewModel = stationsModule(flowOf(false)).stationsViewModel

        viewModel.state.test {
            awaitItem() // Initial empty state
            viewModel.onRefresh()
            awaitItem() // Loading true
            awaitItem() // Data fetched

            var ids = awaitItem().stations.map { it.id }
            assertEquals(listOf("01", "02", "03"), ids)

            viewModel.onFavoriteClicked("03")
            ids = awaitItem().stations.map { it.id }
            assertEquals(listOf("03", "01", "02"), ids)

            viewModel.onFavoriteClicked("02")
            ids = awaitItem().stations.map { it.id }
            assertEquals(listOf("02", "03", "01"), ids)

            viewModel.onFavoriteClicked("01")
            ids = awaitItem().stations.map { it.id }
            assertEquals(listOf("01", "02", "03"), ids)

            cancelAndIgnoreRemainingEvents()
        }

    }
}
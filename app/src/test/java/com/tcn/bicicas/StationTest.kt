package com.tcn.bicicas

import android.content.SharedPreferences
import app.cash.turbine.test
import com.tcn.bicicas.di.clockModule
import com.tcn.bicicas.di.networkModule
import com.tcn.bicicas.di.stationModule
import com.tcn.bicicas.ui.stations.StationsState
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.MemorySharedPreferences
import com.tcn.bicicas.utils.enqueueJson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StationTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockWebServer = MockWebServer()

    private val activeFlow = MutableSharedFlow<Boolean>(replay = 1)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            clockModule,
            networkModule,
            module { single<Flow<Boolean>> { activeFlow } },
            module { single<SharedPreferences> { MemorySharedPreferences() } },
            stationModule(mockWebServer.url("/").toString()),
        )
    }


    @Test
    fun when_view_model_is_initialized_then_empty_state_is_emitted() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        stationsViewModel.state.test {
            assertEquals(StationsState(emptyList(), null, false), awaitItem())
        }
    }

    @Test
    fun when_app_is_not_active_then_state_is_not_refreshed_automatically() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")
        activeFlow.emit(false) // Set app as not active
        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            mainCoroutineRule.scheduler.advanceTimeBy(30_001)
            expectNoEvents() // No new events
        }
    }

    @Test
    fun when_app_is_active_then_state_is_refreshed_automatically() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")
        mockWebServer.enqueueJson("stations_success_response_2.json")

        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            activeFlow.emit(true) // Set app as active to start auto refresh
            mainCoroutineRule.scheduler.advanceTimeBy(30_001)
            assertTrue(awaitItem().stations.isNotEmpty())
            mainCoroutineRule.scheduler.advanceTimeBy(30_001)
            assertTrue(awaitItem().stations.isNotEmpty())
        }
    }

    @Test
    fun when_stations_are_being_refreshed_then_state_is_loading() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")

        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            stationsViewModel.onRefresh()
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun when_refresh_is_success_then_state_is_updated() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")

        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            stationsViewModel.onRefresh()
            awaitItem() // Loading true
            assertTrue(awaitItem().stations.isNotEmpty()) // Success refresh

            stationsViewModel.errorEvent.test {
                expectNoEvents() // Error is not emitted
            }

            expectNoEvents()
        }

    }

    @Test
    fun when_refresh_has_failure_then_error_event_is_emitted() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueue(MockResponse().setHttp2ErrorCode(500))

        stationsViewModel.errorEvent.test {
            stationsViewModel.onRefresh()
            awaitItem()
            expectNoEvents()
        }
    }


    @Test
    fun when_favorite_is_toggled_then_state_is_updated() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")

        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            stationsViewModel.onRefresh()
            awaitItem() // Loading true
            val station = awaitItem().stations.first()
            assertFalse(station.favorite)
            stationsViewModel.onFavoriteClicked(station.id)
            val updatedStation = awaitItem().stations.first()
            assertTrue(updatedStation.favorite)
            expectNoEvents()
        }
    }

    @Test
    fun when_stations_are_updated_then_stations_are_sorted_by_favorite_and_id() = runBlocking {
        val stationsViewModel: StationsViewModel = koinTestRule.koin.get()
        mockWebServer.enqueueJson("stations_success_response.json")

        stationsViewModel.state.test {
            awaitItem() // Initial empty state
            stationsViewModel.onRefresh()
            awaitItem() // Loading true

            var ids = awaitItem().stations.map { it.id }
            assertContentEquals(listOf("01", "02", "03"), ids)

            stationsViewModel.onFavoriteClicked("03")
            ids = awaitItem().stations.map { it.id }
            assertContentEquals(listOf("03", "01", "02"), ids)

            stationsViewModel.onFavoriteClicked("02")
            ids = awaitItem().stations.map { it.id }
            assertContentEquals(listOf("02", "03", "01"), ids)

            stationsViewModel.onFavoriteClicked("01")
            ids = awaitItem().stations.map { it.id }
            assertContentEquals(listOf("01", "02", "03"), ids)

            expectNoEvents()
        }

    }

}
package com.searchhh.app.ui.home

import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.AppError
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.FakeRecentQueryDao
import com.searchhh.app.data.FakeSavedDao
import com.searchhh.app.data.FakeSearchRepository
import com.searchhh.app.data.model.SearchPage
import com.searchhh.app.data.repository.RecentQueriesRepository
import com.searchhh.app.data.repository.SavedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dispatchers = object : DispatcherProvider {
        override val io = testDispatcher
        override val default = testDispatcher
        override val main = testDispatcher
        override val mainImmediate = testDispatcher
    }

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeSearchRepository = FakeSearchRepository()): HomeViewModel =
        HomeViewModel(
            searchRepository = repo,
            savedRepository = SavedRepository(FakeSavedDao()),
            recentQueriesRepository = RecentQueriesRepository(FakeRecentQueryDao()),
            dispatchers = dispatchers,
        )

    @Test
    fun `loads discovery feed on creation`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(2, state.trending.size)
        assertEquals("Campus Coffee Run", state.trending.first().title)
        assertEquals(2, state.categories.size)
        assertNotNull(state.backend)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `exposes error state when the backend is unreachable`() = runTest(testDispatcher) {
        val failing = FakeSearchRepository(failure = ApiResult.Failure(AppError.OFFLINE))
        val vm = viewModel(failing)
        advanceUntilIdle()

        assertEquals(AppError.OFFLINE, vm.state.value.error)
        assertTrue(vm.state.value.trending.isEmpty())
    }

    @Test
    fun `query changes are kept in state`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onQueryChange("cof")
        assertEquals("cof", vm.state.value.query)

        vm.onQueryChange("")
        advanceUntilIdle()
        assertTrue(vm.state.value.suggestions.isEmpty())
    }

    @Test
    fun `debounced suggestions arrive after the pause`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onQueryChange("cof")
        advanceUntilIdle()
        assertEquals(listOf("coffee", "coffee beans"), vm.state.value.suggestions)
    }

    @Test
    fun `submitting a query trims whitespace and clears suggestions`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onQueryChange("  coffee  ")
        advanceUntilIdle()

        vm.onQuerySubmitted()
        assertTrue(vm.state.value.suggestions.isEmpty())
        assertEquals("  coffee  ", vm.state.value.query)
    }

    @Test
    fun `empty page is reported through state`() = runTest(testDispatcher) {
        val empty = FakeSearchRepository(page = SearchPage(total = 0, results = emptyList()))
        val vm = viewModel(empty)
        advanceUntilIdle()

        assertTrue(vm.state.value.trending.isEmpty())
        assertEquals(0, vm.state.value.trending.size)
    }
}

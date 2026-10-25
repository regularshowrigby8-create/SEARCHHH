package com.searchhh.app.ui.funding

import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.FakeSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FundingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val dispatchers = object : DispatcherProvider {
        override val io = dispatcher
        override val default = dispatcher
        override val main = dispatcher
        override val mainImmediate = dispatcher
    }

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads a match on creation`() = runTest(dispatcher) {
        val vm = FundingViewModel(FakeSearchRepository(), dispatchers)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.matches.size)
        assertEquals(1, state.nextActions.size)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `completing training moves the grant into the eligible bucket`() = runTest(dispatcher) {
        val vm = FundingViewModel(FakeSearchRepository(), dispatchers)
        advanceUntilIdle()
        assertEquals(0, vm.state.value.eligible.size)

        vm.setTrainingCompleted(true)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.eligible.size)
        assertTrue(vm.state.value.needsSteps.isEmpty())
    }

    @Test
    fun `age is clamped to a sane range`() = runTest(dispatcher) {
        val vm = FundingViewModel(FakeSearchRepository(), dispatchers)
        advanceUntilIdle()

        vm.setAge(5)
        assertEquals(15, vm.state.value.age)

        vm.setAge(90)
        assertEquals(65, vm.state.value.age)
    }
}

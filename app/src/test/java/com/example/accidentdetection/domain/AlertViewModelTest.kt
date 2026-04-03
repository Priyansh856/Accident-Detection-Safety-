package com.example.accidentdetection.domain

import com.example.accidentdetection.ui.alert.AlertViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `countdown times out after ten seconds`() = runTest {
        val viewModel = AlertViewModel()

        viewModel.startCountdown()
        advanceTimeBy(10_100)
        runCurrent()

        assertEquals(0, viewModel.countdown.value)
        assertTrue(viewModel.timedOut.value)
    }

    @Test
    fun `cancel stops timeout flow`() = runTest {
        val viewModel = AlertViewModel()

        viewModel.startCountdown()
        advanceTimeBy(2_000)
        runCurrent()
        viewModel.cancelAlert()
        advanceTimeBy(10_000)
        runCurrent()

        assertFalse(viewModel.timedOut.value)
    }
}

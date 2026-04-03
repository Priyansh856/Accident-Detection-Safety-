package com.example.accidentdetection.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {

    private val _countdown = MutableStateFlow(TOTAL_SECONDS)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _timedOut = MutableStateFlow(false)
    val timedOut: StateFlow<Boolean> = _timedOut.asStateFlow()
    private var countdownJob: Job? = null

    fun startCountdown() {
        if (countdownJob != null) return
        countdownJob = viewModelScope.launch {
            for (seconds in TOTAL_SECONDS downTo 1) {
                _countdown.value = seconds
                delay(1000)
            }
            _countdown.value = 0
            _timedOut.value = true
        }
    }

    fun cancelAlert() {
        countdownJob?.cancel()
        countdownJob = null
        _timedOut.value = false
    }

    companion object {
        const val TOTAL_SECONDS = 10
    }
}

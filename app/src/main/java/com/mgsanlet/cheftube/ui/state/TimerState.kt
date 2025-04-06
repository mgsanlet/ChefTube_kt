package com.mgsanlet.cheftube.ui.state

sealed class TimerState {
    object Running : TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
    object Initial : TimerState()
}
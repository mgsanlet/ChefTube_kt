package com.mgsanlet.cheftube.ui.viewmodel.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.data.model.RecipeDto
import com.mgsanlet.cheftube.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    var recipeState = MutableLiveData<RecipeState>()

    // Estado del cronómetro
    private var timer: CountDownTimer? = null
    var timeLeftInMillis: Long = 0

    val timerState = MutableLiveData<TimerState>()
    val timeLeft = MutableLiveData<String>()

    init {

        timerState.value = TimerState.Initial
    }

    fun loadRecipe(recipeId: String) {
        Log.i("DETAIL", recipeId)
        if (recipeId.isEmpty()) {
            recipeState.value = RecipeState.Error("Invalid recipe ID")
            return
        }

        viewModelScope.launch {
            try {
                recipeState.value = RecipeState.Loading
                val result = withContext(Dispatchers.IO) {
                    recipeRepository.getById(recipeId)
                }
                if (result != null) {
                    recipeState.value = RecipeState.Success(result)
                } else {
                    recipeState.value = RecipeState.Error("Recipe not found")
                }
            } catch (e: Exception) {
                recipeState.value = RecipeState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setTime(timeInMillis: Long) {
        timeLeftInMillis = timeInMillis
        timeLeft.value = formatTime(timeInMillis)
        timerState.value = TimerState.Initial
    }

    // Métodos públicos para controlar el cronómetro
    fun startTimer(timeInMillis: Long) {
        if (timeLeftInMillis < 1000) return

        timer?.cancel()
        timer = null

        timeLeftInMillis = timeInMillis
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                timeLeft.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                timerState.value = TimerState.Finished
            }
        }.start()
        timerState.value = TimerState.Running
    }

    fun pauseTimer() {
        timer?.cancel()
        timer = null

        timerState.value = TimerState.Paused
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000).toInt() / 60
        val seconds = (millis / 1000).toInt() % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object{
        const val ARG_RECIPE = "recipeId"
    }
}

sealed class TimerState {
    data object Running : TimerState()
    data object Paused : TimerState()
    data object Finished : TimerState()
    data object Initial : TimerState()
}

sealed class RecipeState {
    data object Loading : RecipeState()
    data class Success(val recipe: RecipeDto) : RecipeState()
    data class Error(val message: String) : RecipeState()
}
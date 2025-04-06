package com.mgsanlet.cheftube.ui.viewmodel.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.data.repository.RecipeRepository
import com.mgsanlet.cheftube.ui.state.RecipeState
import com.mgsanlet.cheftube.ui.state.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailViewModel(recipeId: String) : ViewModel() {
    private var recipeRepository = RecipeRepository()
    var recipeState = MutableLiveData<RecipeState>()
    private val _recipeId: String = recipeId
    // Estado del cronómetro
    private var timer: CountDownTimer? = null
    var timeLeftInMillis: Long = 0
    private var isTimerRunning = false

    val timerState = MutableLiveData<TimerState>()
    val timeLeft = MutableLiveData<String>()

    init {
        loadRecipe()
        timerState.value = TimerState.Initial
    }

    fun loadRecipe() {
        val currentRecipeId = _recipeId
        Log.i("DETAIL", _recipeId)
        if (currentRecipeId.isEmpty()) {
            recipeState.value = RecipeState.Error("Invalid recipe ID")
            return
        }

        viewModelScope.launch {
            try {
                recipeState.value = RecipeState.Loading
                val result = withContext(Dispatchers.IO) {
                    recipeRepository.getById(_recipeId) //TODO log
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
        if (timeLeftInMillis <= 0) return

        timer?.cancel()
        timer = null

        timeLeftInMillis = timeInMillis
        isTimerRunning = true
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                timeLeft.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                isTimerRunning = false
                timerState.value = TimerState.Finished
            }
        }.start()
        timerState.value = TimerState.Running
    }

    fun pauseTimer() {
        timer?.cancel()
        timer = null
        isTimerRunning = false
        timerState.value = TimerState.Paused
    }

    fun resetTimer() {
        timer?.cancel()
        timer = null
        timeLeftInMillis = 0
        isTimerRunning = false
        timerState.value = TimerState.Initial
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000).toInt() / 60
        val seconds = (millis / 1000).toInt() % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

@Suppress("UNCHECKED_CAST")
class RecipeDetailViewModelFactory(private val recipeId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>): T {
        return RecipeDetailViewModel(recipeId) as T
    }
}
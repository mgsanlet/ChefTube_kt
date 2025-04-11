package com.mgsanlet.cheftube.ui.viewmodel.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailViewModel(recipeId: String, app: ChefTubeApplication) : ViewModel() {
    private var recipeRepository = app.recipeRepository
    var recipeState = MutableLiveData<RecipeState>()
    private val _recipeId: String = recipeId

    // Estado del cronómetro
    private var timer: CountDownTimer? = null
    var timeLeftInMillis: Long = 0

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
                delay(3000L)
                val result = withContext(Dispatchers.IO) {
                    recipeRepository.getById(_recipeId)
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

    fun resetTimer() {
        timer?.cancel()
        timer = null
        timeLeftInMillis = 0
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
class RecipeDetailViewModelFactory(
    private val recipeId: String,
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipeDetailViewModel(recipeId, app) as T
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
    data class Success(val recipe: Recipe) : RecipeState()
    data class Error(val message: String) : RecipeState()
}
package com.mgsanlet.cheftube.ui.viewmodel.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipeByIdUseCase
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private  val getRecipeById: GetRecipeByIdUseCase
) : ViewModel() {

    private val _recipeState = MutableLiveData<RecipeState>()
    val recipeState: LiveData<RecipeState> = _recipeState

    // Estado del cronómetro
    private var _timer: CountDownTimer? = null

    var timeLeftInMillis: Long = 0

    private val _timerState = MutableLiveData<TimerState>()
    val timerState : LiveData<TimerState> = _timerState

    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String> = _timeLeft

    init {
        _timerState.value = TimerState.Initial
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                _recipeState.value = RecipeState.Loading
                val result = withContext(Dispatchers.IO) {
                    getRecipeById(recipeId)
                }
                result.fold(
                    onSuccess = { recipe ->
                        _recipeState.value = RecipeState.Success(recipe)
                    },
                    onError = { error ->
                        _recipeState.value = RecipeState.Error(error)
                    }
                )
            } catch (e: Exception) {
                _recipeState.value =
                    RecipeState.Error(RecipeError.Unknown(e.message))
            }
        }
    }

    fun setTime(timeInMillis: Long) {
        timeLeftInMillis = timeInMillis
        _timeLeft.value = formatTime(timeInMillis)
        _timerState.value = TimerState.Initial
    }

    // Métodos públicos para controlar el cronómetro
    fun startTimer(timeInMillis: Long) {
        if (timeLeftInMillis < 1000) return

        _timer?.cancel()
        _timer = null

        timeLeftInMillis = timeInMillis
        _timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                _timeLeft.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                _timerState.value = TimerState.Finished
            }
        }.start()
        _timerState.value = TimerState.Running
    }

    fun pauseTimer() {
        _timer?.cancel()
        _timer = null

        _timerState.value = TimerState.Paused
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000).toInt() / 60
        val seconds = (millis / 1000).toInt() % 60
        return String.format("%02d:%02d", minutes, seconds)
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
    data class Error(val error: RecipeError) : RecipeState()
}
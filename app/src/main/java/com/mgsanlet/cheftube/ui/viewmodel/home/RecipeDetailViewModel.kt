package com.mgsanlet.cheftube.ui.viewmodel.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.domain.usecase.recipe.AlternateFavouriteRecipeUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.DeleteCommentUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.DeleteRecipeUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipeByIdUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.PostCommentUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.IsCurrentUserAdminUseCase
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeById: GetRecipeByIdUseCase,
    private val getCurrentUserData: GetCurrentUserDataUseCase,
    private val alternateFavouriteRecipe: AlternateFavouriteRecipeUseCase,
    private val postComment: PostCommentUseCase,
    private val isCurrentUserAdminUseCase: IsCurrentUserAdminUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase
) : ViewModel() {

    private val _recipeState = MutableLiveData<RecipeState>()
    val recipeState: LiveData<RecipeState> = _recipeState

    var authorId: String? = null

    var isFavourite: Boolean = false
    var isRecipeByAuthor: Boolean = false


    // Estado del cronómetro
    private var _timer: CountDownTimer? = null

    var timeLeftInMillis: Long = 0

    private val _isUserAdmin = MutableLiveData<Boolean>()
    val isUserAdmin: LiveData<Boolean> = _isUserAdmin

    private val _timerState = MutableLiveData<TimerState>()
    val timerState: LiveData<TimerState> = _timerState

    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String> = _timeLeft

    init {
        _timerState.value = TimerState.Initial
        isCurrentUserAdmin()
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                _recipeState.value = RecipeState.Loading
                // Forzamos un yield para permitir que el estado Loading se emita
                yield()
                
                val result = withContext(Dispatchers.IO) {
                    getRecipeById(recipeId)
                }
                
                result.fold(
                    onSuccess = { recipe ->
                        authorId = recipe.author?.id
                        // Llamamos a las funciones suspend y esperamos su resultado
                        val isFav = isRecipeFavourite(recipe.id)
                        val isByAuthor = isRecipeByAuthor(recipe.id)
                        
                        // Actualizamos las propiedades en el hilo principal
                        withContext(Dispatchers.Main) {
                            isFavourite = isFav
                            isRecipeByAuthor = isByAuthor
                            _recipeState.value = RecipeState.Success(recipe)
                        }
                    },
                    onError = { error ->
                        _recipeState.value = RecipeState.Error(error)
                    }
                )
            } catch (e: Exception) {
                _recipeState.value = RecipeState.Error(RecipeError.Unknown(e.message))
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

    fun alternateFavourite(favourite: Boolean) {
        if (_recipeState.value is RecipeState.Success) {
            val recipe = (_recipeState.value as RecipeState.Success).recipe
            viewModelScope.launch {
                alternateFavouriteRecipe(recipe.id, favourite).fold(
                    onSuccess = {
                        isFavourite = favourite
                        _recipeState.value =
                            RecipeState.Success(
                                recipe.copy(
                                    favouriteCount =
                                        if (favourite) recipe.favouriteCount + 1
                                        else recipe.favouriteCount - 1
                                )
                            )
                    },
                    onError = { _recipeState.value = RecipeState.Error(RecipeError.Unknown()) }
                )
            }
        }
    }

    fun isCurrentUserAdmin() {
        viewModelScope.launch {
            isCurrentUserAdminUseCase().fold(
                onSuccess = { _isUserAdmin.value = it },
                onError = { _isUserAdmin.value = false }
            )
        }
    }

    internal suspend fun isRecipeFavourite(recipeId: String): Boolean {
        return getCurrentUserData().fold(
            onSuccess = { user -> user.favouriteRecipes.contains(recipeId) },
            onError = {
                _recipeState.value = RecipeState.Error(RecipeError.Unknown())
                false
            }
        )
    }

    internal suspend fun isRecipeByAuthor(recipeId: String): Boolean {
        return getCurrentUserData().fold(
            onSuccess = { user -> user.createdRecipes.contains(recipeId) },
            onError = {
                _recipeState.value = RecipeState.Error(RecipeError.Unknown())
                false
            }
        )
    }

    fun postComment(comment: String) {
        if (_recipeState.value is RecipeState.Success) {
            viewModelScope.launch {
                getCurrentUserData().fold(
                    onSuccess = { user ->
                        val currentRecipe = (_recipeState.value as RecipeState.Success).recipe
                        val newComment = DomainComment(
                            author = user,
                            content = comment,
                            timestamp = System.currentTimeMillis()
                        )
                        val updatedComments = currentRecipe.comments + newComment
                        val updatedRecipe = currentRecipe.copy(comments = updatedComments)
                        postComment(updatedRecipe.id, comment, user).fold(
                            onSuccess = {
                                _recipeState.value = RecipeState.Success(updatedRecipe)
                            },
                            onError = {
                                _recipeState.value = RecipeState.Error(RecipeError.Unknown())
                            }
                        )
                    },
                    onError = { _recipeState.value = RecipeState.Error(RecipeError.Unknown()) }
                )
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            deleteRecipeUseCase(recipe.id).fold(
                onSuccess = {
                    _recipeState.value = RecipeState.DeleteSuccess
                },
                onError = { _recipeState.value = RecipeState.Error(RecipeError.Unknown()) }
            )
        }
    }

    fun deleteComment(comment: DomainComment) {
        if (_recipeState.value !is RecipeState.Success) return
        
        val currentRecipe = (_recipeState.value as RecipeState.Success).recipe

        viewModelScope.launch {
            deleteCommentUseCase(
                recipeId = currentRecipe.id,
                commentTimestamp = comment.timestamp,
                userId = comment.author.id
            ).fold(
                onSuccess = {
                    // Update the recipe state with the comment removed
                    val updatedComments = currentRecipe.comments.filterNot {
                        it.author.id == comment.author.id && it.timestamp == comment.timestamp
                    }
                    _recipeState.value = RecipeState.Success(
                        currentRecipe.copy(comments = updatedComments)
                    )
                },
                onError = { error ->
                    _recipeState.value = RecipeState.Error(error as? RecipeError ?: RecipeError.Unknown())
                }
            )
        }
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
        data object DeleteSuccess : RecipeState()
    }

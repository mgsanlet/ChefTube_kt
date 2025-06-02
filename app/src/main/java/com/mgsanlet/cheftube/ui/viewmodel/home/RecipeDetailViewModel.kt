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

/**
 * ViewModel para la pantalla de detalle de receta.
 *
 * Maneja la lógica relacionada con la visualización y gestión de recetas,
 * incluyendo la carga de datos, gestión de favoritos, comentarios,
 * temporizador de cocina y operaciones CRUD sobre la receta.
 *
 * @property getRecipeById Caso de uso para obtener una receta por su ID
 * @property getCurrentUserData Caso de uso para obtener datos del usuario actual
 * @property alternateFavouriteRecipe Caso de uso para alternar favorito
 * @property postComment Caso de uso para publicar un comentario
 * @property isCurrentUserAdminUseCase Caso de uso para verificar rol de administrador
 * @property deleteRecipeUseCase Caso de uso para eliminar una receta
 * @property deleteCommentUseCase Caso de uso para eliminar un comentario
 */
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

    /** Estado interno mutable de la receta */
    private val _recipeState = MutableLiveData<RecipeState>()
    
    /** Estado observable de la receta */
    val recipeState: LiveData<RecipeState> = _recipeState

    /** ID del autor de la receta */
    var authorId: String? = null

    /** Indica si la receta está marcada como favorita */
    var isFavourite: Boolean = false
    
    /** Indica si el usuario actual es el autor de la receta */
    var isRecipeByAuthor: Boolean = false

    /** Temporizador para el modo de cocina */
    private var _timer: CountDownTimer? = null

    /** Tiempo restante en milisegundos */
    var timeLeftInMillis: Long = 0

    /** Estado interno del rol de administrador */
    private val _isUserAdmin = MutableLiveData<Boolean>()
    
    /** Estado observable que indica si el usuario es administrador */
    val isUserAdmin: LiveData<Boolean> = _isUserAdmin

    /** Estado interno del temporizador */
    private val _timerState = MutableLiveData<TimerState>()
    
    /** Estado observable del temporizador */
    val timerState: LiveData<TimerState> = _timerState

    /** Tiempo restante formateado */
    private val _timeLeft = MutableLiveData<String>()
    
    /** Tiempo restante formateado como String */
    val timeLeft: LiveData<String> = _timeLeft

    init {
        _timerState.value = TimerState.Initial
        isCurrentUserAdmin()
    }

    /**
     * Carga los datos de una receta por su ID.
     * Actualiza el estado de la UI según el resultado.
     *
     * @param recipeId ID de la receta a cargar
     */
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

    /**
     * Establece el tiempo inicial del temporizador.
     *
     * @param timeInMillis Tiempo en milisegundos
     */
    fun setTime(timeInMillis: Long) {
        timeLeftInMillis = timeInMillis
        _timeLeft.value = formatTime(timeInMillis)
        _timerState.value = TimerState.Initial
    }

    /**
     * Inicia el temporizador con el tiempo especificado.
     * Si ya hay un temporizador en ejecución, lo cancela primero.
     *
     * @param timeInMillis Tiempo en milisegundos para el temporizador
     */
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

    /**
     * Pausa el temporizador actual.
     * Mantiene el tiempo restante para poder reanudar más tarde.
     */
    fun pauseTimer() {
        _timer?.cancel()
        _timer = null

        _timerState.value = TimerState.Paused
    }

    /**
     * Formatea el tiempo en milisegundos a un string legible (MM:SS).
     *
     * @param millis Tiempo en milisegundos
     * @return String formateado como MM:SS
     */
    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000).toInt() / 60
        val seconds = (millis / 1000).toInt() % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Alterna el estado de favorito de la receta actual.
     *
     * @param favourite true para marcar como favorito, false para quitar de favoritos
     */
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

    /**
     * Verifica si el usuario actual tiene rol de administrador.
     * Actualiza el estado [isUserAdmin] con el resultado.
     */
    fun isCurrentUserAdmin() {
        viewModelScope.launch {
            isCurrentUserAdminUseCase().fold(
                onSuccess = { _isUserAdmin.value = it },
                onError = { _isUserAdmin.value = false }
            )
        }
    }

    /**
     * Verifica si la receta está marcada como favorita por el usuario actual.
     *
     * @param recipeId ID de la receta a verificar
     * @return true si la receta es favorita, false en caso contrario
     */
    internal suspend fun isRecipeFavourite(recipeId: String): Boolean {
        return getCurrentUserData().fold(
            onSuccess = { user -> user.favouriteRecipes.contains(recipeId) },
            onError = {
                _recipeState.value = RecipeState.Error(RecipeError.Unknown())
                false
            }
        )
    }

    /**
     * Verifica si el usuario actual es el autor de la receta.
     *
     * @param recipeId ID de la receta a verificar
     * @return true si el usuario es el autor, false en caso contrario
     */
    internal suspend fun isRecipeByAuthor(recipeId: String): Boolean {
        return getCurrentUserData().fold(
            onSuccess = { user -> user.createdRecipes.contains(recipeId) },
            onError = {
                _recipeState.value = RecipeState.Error(RecipeError.Unknown())
                false
            }
        )
    }

    /**
     * Publica un nuevo comentario en la receta actual.
     *
     * @param comment Contenido del comentario a publicar
     */
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

    /**
     * Elimina la receta actual.
     * Actualiza el estado a [RecipeState.DeleteSuccess] si tiene éxito.
     *
     * @param recipe Receta a eliminar
     */
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

    /**
     * Elimina un comentario de la receta actual.
     * Actualiza el estado con la receta actualizada si tiene éxito.
     *
     * @param comment Comentario a eliminar
     */
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
    /**
     * Estados posibles del temporizador de cocina.
     */
    sealed class TimerState {
        /** Temporizador en ejecución */
        data object Running : TimerState()
        
        /** Temporizador en pausa */
        data object Paused : TimerState()
        
        /** Temporizador finalizado */
        data object Finished : TimerState()
        
        /** Estado inicial del temporizador */
        data object Initial : TimerState()
    }

    /**
     * Estados posibles de la UI para la pantalla de detalle de receta.
     */
    sealed class RecipeState {
        /** Cargando datos de la receta */
        data object Loading : RecipeState()
        
        /** Receta cargada exitosamente */
        data class Success(val recipe: Recipe) : RecipeState()
        
        /** Error al cargar o modificar la receta */
        data class Error(val error: RecipeError) : RecipeState()
        
        /** Receta eliminada exitosamente */
        data object DeleteSuccess : RecipeState()
    }

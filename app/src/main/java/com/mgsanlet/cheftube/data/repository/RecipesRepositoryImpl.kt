package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.CommentResponse
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.data.util.Constants
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.DomainResult.Error
import com.mgsanlet.cheftube.domain.util.DomainResult.Success
import com.mgsanlet.cheftube.domain.util.FilterCriterion
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [RecipesRepository] que gestiona las operaciones relacionadas con recetas.
 * Incluye caché en memoria para mejorar el rendimiento.
 *
 * @property api Cliente de Firebase para operaciones de base de datos
 * @property recipesCache Almacenamiento en caché de las recetas para acceso rápido
 */
@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : RecipesRepository {
    var recipesCache: List<DomainRecipe>? = null

    /**
     * Limpia la caché de recetas.
     * Útil para forzar una actualización de datos en la próxima solicitud.
     */
    override suspend fun clearCache() {
        recipesCache = null
    }

    /**
     * Obtiene todas las recetas disponibles.
     * Primero intenta devolver las recetas de la caché si están disponibles.
     *
     * @return [DomainResult] con la lista de recetas o un error
     */
    override suspend fun getAll(): DomainResult<List<DomainRecipe>, RecipeError> {
        recipesCache?.let {
            if (it.isNotEmpty()) return Success(it)
        }
        // Si el caché es nulo
        return when (val result = api.getAllRecipes()) {
            is Success -> {
                val domainRecipes = result.data.map { it.toDomainRecipe() }

                if (domainRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    recipesCache = domainRecipes
                    Success(domainRecipes)
                }
            }

            is Error -> Error(result.error)
        }
    }

    /**
     * Filtra las recetas según los criterios de búsqueda proporcionados.
     *
     * @param params Parámetros de búsqueda que incluyen criterio y valores
     * @return [DomainResult] con la lista de recetas filtradas o un error
     */
    override suspend fun filterRecipes(params: SearchParams):
            DomainResult<List<DomainRecipe>, RecipeError> {
        return when (val result = getAll()) {
            is Success -> {
                val filteredRecipes = when (params.criterion) {
                    FilterCriterion.TITLE -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.title.lowercase().contains(lowercaseQuery)
                        }
                    }
                    FilterCriterion.INGREDIENT -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.ingredients.any { ingredient ->
                                ingredient.lowercase().contains(lowercaseQuery)
                            }
                        }
                    }
                    FilterCriterion.DURATION -> {
                        val min = params.minDuration.toIntOrNull() ?: 0
                        val max = params.maxDuration.toIntOrNull() ?: Int.MAX_VALUE
                        result.data.filter { recipe ->
                            val duration = recipe.durationMinutes
                            duration in min..max
                        }
                    }
                    FilterCriterion.CATEGORY -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.categories.any { category ->
                                category.lowercase().contains(lowercaseQuery)
                            }
                        }
                    }
                    FilterCriterion.DIFFICULTY -> {
                        result.data.filter { recipe ->
                            recipe.difficulty == params.difficulty
                        }
                    }
                }

                if (filteredRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    Success(filteredRecipes)
                }
            }
            is Error -> Error(result.error)
        }
    }

    /**
     * Obtiene una receta por su ID.
     * Primero busca en la caché antes de hacer una solicitud a la red.
     *
     * @param recipeId ID de la receta a buscar
     * @return [DomainResult] con la receta encontrada o un error
     */
    override suspend fun getById(recipeId: String): DomainResult<DomainRecipe, RecipeError> {
        recipesCache?.let {
            val recipe = it.find { recipe -> recipe.id == recipeId }
            if (recipe != null) return Success(recipe)
        }
        // Si el caché es nulo
        return when (val result = api.getRecipeById(recipeId)) {
            is Success -> {
                try {
                    Success(result.data.toDomainRecipe())
                } catch (e: Exception) {
                    Error(RecipeError.Unknown(e.message))
                }
            }

            is Error -> {
                Error(result.error)
            }
        }
    }

    /**
     * Obtiene múltiples recetas por sus IDs.
     * Primero intenta obtenerlas de la caché.
     *
     * @param recipeIds Lista de IDs de recetas a buscar
     * @return [DomainResult] con la lista de recetas encontradas o un error
     */
    override suspend fun getByIds(recipeIds: ArrayList<String>):
            DomainResult<List<DomainRecipe>, RecipeError> {
        recipesCache?.let {
            return Success(it.filter { recipe -> recipeIds.contains(recipe.id) })
        }
        // Si el caché es nulo
        return when (val result = api.getRecipesByIds(recipeIds)) {
            is Success -> {
                val filteredRecipes = result.data.map { it.toDomainRecipe() }

                if (filteredRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    Success(filteredRecipes)
                }
            }

            is Error -> Error(result.error)
        }
    }

    /**
     * Actualiza el contador de favoritos de una receta.
     * También actualiza la caché si existe.
     *
     * @param recipeId ID de la receta a actualizar
     * @param isNewFavourite true si se está añadiendo a favoritos, false si se está quitando
     * @return [DomainResult] con el resultado de la operación
     */
    override suspend fun updateFavouriteCount(
        recipeId: String,
        isNewFavourite: Boolean
    ): DomainResult<Unit, RecipeError> {
        val result = api.updateRecipeFavouriteCount(recipeId, isNewFavourite)

        // Actualizar el cache si existe
        recipesCache?.let { cache ->
            val index = cache.indexOfFirst { it.id == recipeId }
            if (index != -1) {
                val currentRecipe = cache[index]
                val updatedRecipe = currentRecipe.copy(
                    favouriteCount = if (isNewFavourite) currentRecipe.favouriteCount + 1
                    else currentRecipe.favouriteCount - 1
                )
                recipesCache = cache.toMutableList().apply {
                    set(index, updatedRecipe)
                }
            }
        }

        return result
    }

    /**
     * Guarda una nueva receta o actualiza una existente.
     * Si la receta no tiene ID, se genera una nueva.
     *
     * @param newRecipeData Datos de la receta a guardar
     * @param newImage Imagen de la receta en bytes (opcional)
     * @param currentUserData Datos del usuario que está guardando la receta
     * @return [DomainResult] con el ID de la receta si es nueva, o null si es una actualización
     */
    override suspend fun saveRecipe(
        newRecipeData: DomainRecipe,
        newImage: ByteArray?,
        currentUserData: DomainUser
    ): DomainResult<String?, RecipeError> {

        var newId: String? = null
        var finalId = newRecipeData.id
        if (finalId.isBlank()) {
            finalId = UUID.randomUUID().toString()
            newId = finalId
        }
        val result = api.saveRecipe(finalId, newRecipeData, newImage, currentUserData)
        if (result is Success) {
            recipesCache?.let { cache ->

                val finalRecipeData = newRecipeData.copy(
                    id = finalId,
                    imageUrl = api.getStorageUrlFromPath(
                        Constants.Storage.getRecipeImagePath(finalId)),
                    author = currentUserData
                )

                val index = cache.indexOfFirst { it.id == finalId }
                recipesCache = if (index != -1) {
                    cache.toMutableList().apply {
                        set(index, finalRecipeData)
                    }
                } else {
                    cache.toMutableList().apply {
                        add(finalRecipeData)
                    }
                }
            }
            return Success(newId)
        } else {
            return Error((result as Error).error)
        }
    }

    /**
     * Publica un nuevo comentario en una receta.
     *
     * @param recipeId ID de la receta donde se publicará el comentario
     * @param commentContent Contenido del comentario
     * @param currentUserData Datos del usuario que está publicando el comentario
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun postComment(
        recipeId: String,
        commentContent: String,
        currentUserData: DomainUser
    ): DomainResult<Unit, RecipeError> {
        return api.postComment(recipeId, commentContent, currentUserData)
    }
    
    /**
     * Elimina una receta y todas sus referencias asociadas.
     *
     * @param recipeId ID de la receta a eliminar
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun deleteRecipe(recipeId: String): DomainResult<Unit, RecipeError> {
        return api.deleteRecipeAndReferences(recipeId)
    }
    
    /**
     * Elimina un comentario de una receta.
     * Actualiza la caché local si la eliminación es exitosa.
     *
     * @param recipeId ID de la receta que contiene el comentario
     * @param commentTimestamp Marca de tiempo del comentario a eliminar
     * @param userId ID del usuario que realizó el comentario
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun deleteComment(
        recipeId: String,
        commentTimestamp: Long,
        userId: String
    ): DomainResult<Unit, RecipeError> {
        // Borra el comentario de Firebase
        val result = api.deleteComment(recipeId, commentTimestamp, userId)
        
        // Actualiza la caché local si la eliminación fue exitosa
        if (result is Success) {
            recipesCache?.let { cache ->
                val recipeIndex = cache.indexOfFirst { it.id == recipeId }
                if (recipeIndex != -1) {
                    val updatedRecipe = cache[recipeIndex].copy(
                        comments = cache[recipeIndex].comments.filterNot {
                            it.author.id == userId && it.timestamp == commentTimestamp
                        }
                    )
                    recipesCache = cache.toMutableList().apply {
                        set(recipeIndex, updatedRecipe)
                    }
                }
            }
        }
        
        return result
    }

    /**
     * Convierte un [RecipeResponse] a un [DomainRecipe].
     * Incluye la conversión de la URL de la imagen y los datos del autor.
     *
     * @receiver Respuesta de la API a convertir
     * @return [DomainRecipe] con los datos de la receta
     */
    private suspend fun RecipeResponse.toDomainRecipe(): DomainRecipe {
        return DomainRecipe(
            id = id,
            title = title,
            videoUrl = videoUrl,
            imageUrl = api.getStorageUrlFromPath(Constants.Storage.getRecipeImagePath(id)),
            ingredients = ingredients,
            steps = steps,
            categories = categories,
            comments = comments.map { it.toDomainComment() },
            favouriteCount = favouriteCount,
            durationMinutes = durationMinutes,
            difficulty = difficulty,
            author = DomainUser(
                id = this.authorId,
                email = this.authorEmail,
                username = this.authorName,
                profilePictureUrl = if (this.authorHasProfilePicture) 
                    api.getStorageUrlFromPath(
                        Constants.Storage.getProfilePicturePath(this.authorId))
                else ""
            )
        )
    }

    /**
     * Convierte un [CommentResponse] a un [DomainComment].
     *
     * @receiver Respuesta de la API a convertir
     * @return [DomainComment] con los datos del comentario
     */
    private suspend fun CommentResponse.toDomainComment(): DomainComment {
        return DomainComment(
            author = DomainUser(
                id = this.authorId,
                email = this.authorEmail,
                username = this.authorName,
                profilePictureUrl = if (this.authorHasProfilePicture) 
                    api.getStorageUrlFromPath(Constants.Storage.getProfilePicturePath(this.authorId))
                else ""
            ),
            content = this.content,
            timestamp = this.timestamp
        )
    }
}

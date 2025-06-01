package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Interfaz que define las operaciones disponibles para la gestión de recetas.
 * Todas las operaciones son suspendidas para permitir operaciones asíncronas.
 */
interface RecipesRepository {
    /**
     * Limpia la caché de recetas.
     */
    suspend fun clearCache()
    
    /**
     * Obtiene una receta por su ID.
     *
     * @param recipeId ID de la receta a buscar
     * @return [DomainResult] con la receta encontrada o error
     */
    suspend fun getById(recipeId: String): DomainResult<Recipe, RecipeError>
    
    /**
     * Obtiene todas las recetas disponibles.
     *
     * @return [DomainResult] con la lista de recetas o error
     */
    suspend fun getAll(): DomainResult<List<Recipe>, RecipeError>
    
    /**
     * Obtiene múltiples recetas por sus IDs.
     *
     * @param recipeIds Lista de IDs de recetas a buscar
     * @return [DomainResult] con la lista de recetas encontradas o error
     */
    suspend fun getByIds(recipeIds: ArrayList<String>): DomainResult<List<Recipe>, RecipeError>
    
    /**
     * Actualiza el contador de favoritos de una receta.
     *
     * @param recipeId ID de la receta a actualizar
     * @param isNewFavourite true para incrementar el contador, false para decrementarlo
     * @return [DomainResult] de la operación o error
     */
    suspend fun updateFavouriteCount(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, RecipeError>
    
    /**
     * Guarda una nueva receta o actualiza una existente.
     *
     * @param newRecipeData Datos de la receta a guardar
     * @param newImage Imagen de la receta en formato ByteArray (opcional)
     * @param currentUserData Datos del usuario que crea/actualiza la receta
     * @return ID de la receta guardada o error
     */
    suspend fun saveRecipe(newRecipeData: Recipe, newImage: ByteArray?, currentUserData: DomainUser): DomainResult<String?, RecipeError>
    
    /**
     * Elimina una receta.
     *
     * @param recipeId ID de la receta a eliminar
     * @return [DomainResult] de la operación o error
     */
    suspend fun deleteRecipe(recipeId: String): DomainResult<Unit, RecipeError>
    
    /**
     * Filtra recetas según los parámetros de búsqueda.
     *
     * @param params Parámetros de búsqueda
     * @return Lista de recetas que coinciden con los filtros o error
     */
    suspend fun filterRecipes(params: SearchParams): DomainResult<List<Recipe>, RecipeError>
    
    /**
     * Publica un comentario en una receta.
     *
     * @param recipeId ID de la receta
     * @param commentContent Contenido del comentario
     * @param currentUserData Datos del usuario que publica el comentario
     * @return [DomainResult] de la operación o error
     */
    suspend fun postComment(recipeId: String, commentContent: String, currentUserData: DomainUser): DomainResult<Unit, RecipeError>
    
    /**
     * Elimina un comentario de una receta.
     *
     * @param recipeId ID de la receta que contiene el comentario
     * @param commentTimestamp Marca de tiempo del comentario a eliminar
     * @param userId ID del usuario que realizó el comentario
     * @return [DomainResult] de la operación o error
     */
    suspend fun deleteComment(recipeId: String, commentTimestamp: Long, userId: String): DomainResult<Unit, RecipeError>
}
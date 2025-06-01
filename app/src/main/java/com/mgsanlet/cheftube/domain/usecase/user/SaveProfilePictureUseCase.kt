package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para guardar o actualizar la imagen de perfil del usuario actual.
 * También se encarga de limpiar la caché de recetas para reflejar los cambios.
 *
 * @property usersRepository Repositorio de usuarios para guardar la imagen
 * @property recipeRepository Repositorio de recetas para limpiar la caché
 */
class SaveProfilePictureUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val recipeRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para guardar la imagen de perfil.
     *
     * @param profilePicture Imagen de perfil en formato ByteArray
     * @return [DomainResult] con Unit en caso de éxito o [UserError] si hay un error
     */
    suspend operator fun invoke(profilePicture: ByteArray): DomainResult<Unit, UserError> {
        val result = usersRepository.saveProfilePicture(profilePicture)
        if (result is DomainResult.Success) {
            recipeRepository.clearCache()
        }
        return result
    }
}

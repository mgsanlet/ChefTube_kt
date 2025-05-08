package com.mgsanlet.cheftube.data.repository

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseUserApi
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val mainApi: FirebaseApi, private val userApi: FirebaseUserApi
) : UsersRepository {

    private var currentUserCache: DomainUser? = null

    override suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError> {
        currentUserCache?.let {
            return DomainResult.Success(currentUserCache!!)
        } ?: when (val result = getUserDataById(mainApi.auth.currentUser!!.uid)) {
            is DomainResult.Success -> {
                currentUserCache = result.data
                return DomainResult.Success(result.data)
            }

            is DomainResult.Error -> return DomainResult.Error(result.error)
        }
    }

    override suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError> {
        return when (val result = userApi.getUserDataById(userId)) {
            is DomainResult.Success -> {
                DomainResult.Success(
                    DomainUser(
                        id = userId,
                        username = result.data.username,
                        email = result.data.email,
                        bio = result.data.bio,
                        createdRecipes = result.data.createdRecipes,
                        favouriteRecipes = result.data.favouriteRecipes,
                        followersIds = result.data.followersIds,
                        followingIds = result.data.followingIds
                    )
                )
            }

            is DomainResult.Error -> DomainResult.Error(result.error)
        }
    }

    override suspend fun createUser(
        username: String, email: String, password: String
    ): DomainResult<Unit, UserError> {
        try {
            val isAvailableResult = userApi.isAvailableUsername(username)
            if (isAvailableResult is DomainResult.Error) {
                return isAvailableResult
            }
            mainApi.auth.createUserWithEmailAndPassword(email, password).await()
            val user = mainApi.auth.currentUser ?: throw Exception("User not found after creation")

            val insertDataResult = userApi.insertUserData(user.uid, username, email)
            if (insertDataResult is DomainResult.Error) {
                return insertDataResult
            }

            currentUserCache = DomainUser(
                id = user.uid, username = username, email = email
            )
            return DomainResult.Success(Unit)
        } catch (e: Exception) {
            return if (e is FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> DomainResult.Error(UserError.EmailInUse)
                    "WEAK_PASSWORD" -> DomainResult.Error(UserError.InvalidPasswordPattern)
                    else -> DomainResult.Error(UserError.Unknown(e.message))
                }
            } else {
                DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun loginUser(
        email: String, password: String
    ): DomainResult<Unit, UserError> {
        try {
            mainApi.auth.signInWithEmailAndPassword(email, password).await()
            val user = mainApi.auth.currentUser ?: throw Exception("User not found after login")
            when (val result = userApi.getUserDataById(user.uid)) {
                is DomainResult.Success -> {
                    currentUserCache = DomainUser(
                        id = user.uid,
                        username = result.data.username,
                        email = email,
                        bio = result.data.bio,
                        createdRecipes = result.data.createdRecipes,
                        favouriteRecipes = result.data.favouriteRecipes,
                        followersIds = result.data.followersIds,
                        followingIds = result.data.followingIds
                    )
                    return DomainResult.Success(Unit)
                }

                is DomainResult.Error -> return DomainResult.Error(result.error)
            }
        } catch (e: Exception) {
            return when (e) {
                is FirebaseAuthInvalidUserException -> DomainResult.Error(UserError.UserNotFound)
                is FirebaseAuthInvalidCredentialsException -> DomainResult.Error(UserError.WrongCredentials)
                else -> DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun updateCurrentUserData(new: DomainUser): DomainResult<Unit, UserError> {
        currentUserCache?.let {
            var result = updateUserData(new)
            if (result is DomainResult.Success) {
                currentUserCache = new
                return DomainResult.Success(Unit)
            } else {
                return result
            }
        } ?: throw Exception("User not found after login")
    }

    override suspend fun updateUserData(new: DomainUser): DomainResult<Unit, UserError> {
        //Obtenemos los datos antiguos según el Id
        var oldUserData: DomainUser? = null
        getUserDataById(new.id).fold(onSuccess = { userData ->
            oldUserData = userData
        }, onError = { error ->
            return DomainResult.Error(error)
        })

        oldUserData?.let { old ->
            // Comprobamos que no se esté intentando cambiar el username a uno ya existente
            if (new.username != old.username && userApi.isAvailableUsername(new.username) is DomainResult.Error) {
                return DomainResult.Error(UserError.UsernameInUse)
            }

            // Actualizamos los datos que pueden cambiar
            val updatedUserData = old.copy(
                username = new.username.ifBlank { old.username },
                bio = new.bio.ifBlank { old.bio },
                followersIds = new.followersIds,
                followingIds = new.followingIds
            )
            return userApi.updateUserData(old.id, updatedUserData)

        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {

        mainApi.auth.currentUser?.let {
            var result = getCurrentUserData() //Cacheamos el usuario
            result.fold(
                onSuccess = {
                    return DomainResult.Success(Unit)
                }, onError = { error ->
                    logout()
                    return DomainResult.Error(error)
                })
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override fun logout() {
        currentUserCache = null
        mainApi.auth.signOut()
    }

    override suspend fun updateFavouriteRecipes(
        recipeId: String, isNewFavourite: Boolean
    ): DomainResult<Unit, UserError> {
        var userResult = getCurrentUserData()
        if (userResult is DomainResult.Error) {
            return DomainResult.Error(userResult.error)
        }
        var updateResult = userApi.updateFavouriteRecipes(
            (userResult as DomainResult.Success).data.id, recipeId, isNewFavourite
        )
        updateResult.fold(
            onSuccess = {
                // Actualizar el caché
                currentUserCache = null
                userResult = getCurrentUserData()
                if (userResult is DomainResult.Error) {
                    return DomainResult.Error(userResult.error)
                }
                return DomainResult.Success(Unit)
            }, onError = { error ->
                return DomainResult.Error(error)
            }
        )
    }
}

package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.model.toDomainUser
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.error.ChefTubeError
import com.mgsanlet.cheftube.utils.error.UserError
import com.mgsanlet.cheftube.utils.Constants.Tag
import com.mgsanlet.cheftube.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {

    override suspend fun createUser(
        username: String,
        email: String,
        password: String
    ): Resource<DomainUser> {
        return try {
            if (userLocalDataSource.getUserByName(username) != null) {
                Resource.Error(UserError.UsernameAlreadyInUse)

            } else if (userLocalDataSource.getUserByEmail(email) != null) {
                Resource.Error(UserError.EmailAlreadyInUse)

            } else {
                val newUser = User.create(username, email, password)

                if (userLocalDataSource.insertUser(newUser)) {
                    Resource.Success(newUser.toDomainUser())
                } else {
                    Resource.Error(ChefTubeError.UnknownError(Tag.SIGN_UP))
                }
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }

    override suspend fun loginUser(emailOrUsername: String, password: String): Resource<DomainUser> {
        return try {
            val user = userLocalDataSource.getUserByEmailOrUsername(emailOrUsername)
            when {
                user == null -> {
                    Resource.Error(UserError.UserNotFound)
                }

                !user.verifyPassword(password) -> {
                    Resource.Error(UserError.WrongPassword)
                }

                else -> {
                    Resource.Success(user.toDomainUser())
                }
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }

    override suspend fun updateUser(user: User, oldPassword: String): Resource<DomainUser> {
        return try {

            // Obtener usuario actual
            val currentUser = userLocalDataSource.getUserById(user.id) ?: return Resource.Error(
                UserError.UserNotFound
            )

            // Verificar la contraseña antigua
            if (!currentUser.verifyPassword(oldPassword)) {
                return Resource.Error(UserError.WrongPassword)
            }

            if (userLocalDataSource.updateUser(user)) {
                Resource.Success(user.toDomainUser())
            } else {
                Resource.Error(ChefTubeError.UnknownError(Tag.PROFILE))
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }

    override suspend fun getUserById(userId: String): Resource<DomainUser> {
        return try {
            val user = userLocalDataSource.getUserById(userId)
            if (user != null) {
                Resource.Success(user.toDomainUser())
            } else {
                Resource.Error(UserError.UserNotFound)
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }

    override suspend fun getUserByName(username: String): Resource<DomainUser> {
        return try {
            val user = userLocalDataSource.getUserByName(username)
            if (user != null) {
                Resource.Success(user.toDomainUser())
            } else {
                Resource.Error(UserError.UserNotFound)
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }

    override suspend fun getUserByEmail(userEmail: String): Resource<DomainUser> {
        return try {
            val user = userLocalDataSource.getUserByEmail(userEmail)
            if (user != null) {
                Resource.Success(user.toDomainUser())
            } else {
                Resource.Error(UserError.UserNotFound)
            }
        } catch (e: Exception) {
            Resource.Error(ChefTubeError.UnknownError(e.message))
        }
    }
}

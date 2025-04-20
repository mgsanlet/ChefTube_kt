package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.domain.util.Error
import com.mgsanlet.cheftube.domain.util.Result
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(emailOrUsername: String, password: String): Result<DomainUser, Error> {
        return userRepository.loginUser(emailOrUsername, password)
    }
}
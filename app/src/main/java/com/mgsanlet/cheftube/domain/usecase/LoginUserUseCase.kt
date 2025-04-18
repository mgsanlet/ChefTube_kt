package com.mgsanlet.cheftube.domain.usecase

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.UserManager
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) {
    suspend operator fun invoke(emailOrUsername: String, password: String): Result<DomainUser> {
        val result = userRepository.loginUser(emailOrUsername, password)
        if (result.isSuccess) { userManager.currentUser = result.getOrNull() }
        return  result
    }
}
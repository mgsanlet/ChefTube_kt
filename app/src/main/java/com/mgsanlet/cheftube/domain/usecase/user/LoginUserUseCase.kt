package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository.UserError
import com.mgsanlet.cheftube.domain.util.Error
import com.mgsanlet.cheftube.domain.util.Result
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(emailOrUsername: String, password: String): Result<DomainUser, UserError> {
        return usersRepository.loginUser(emailOrUsername, password)
    }
}
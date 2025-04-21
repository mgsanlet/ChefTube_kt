package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository.UserError
import com.mgsanlet.cheftube.domain.util.Error
import com.mgsanlet.cheftube.domain.util.Result
import java.util.UUID
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): Result<DomainUser, UserError> {
        return usersRepository.createUser(UUID.randomUUID().toString(), username, email, password)
    }
}
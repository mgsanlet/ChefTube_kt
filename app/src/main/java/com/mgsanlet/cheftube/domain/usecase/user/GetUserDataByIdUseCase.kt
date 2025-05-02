package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class GetUserDataByIdUseCase @Inject constructor(
    private val userRepository: UsersRepository
) {
    suspend operator fun invoke(userId: String): DomainResult<DomainUser, UserError>{
        return userRepository.getUserDataById(userId)
    }

}
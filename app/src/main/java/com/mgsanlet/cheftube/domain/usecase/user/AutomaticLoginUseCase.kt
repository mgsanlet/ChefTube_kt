package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import javax.inject.Inject

class AutomaticLoginUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(): Boolean {
        return usersRepository.tryAutoLogin()
    }
}
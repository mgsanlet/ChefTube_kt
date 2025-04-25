package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val usersRepository: UsersRepository) {
    operator fun invoke(){
        usersRepository.logout()
    }
}

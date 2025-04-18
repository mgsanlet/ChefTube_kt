package com.mgsanlet.cheftube.domain.usecase

import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.UserManager
import javax.inject.Inject

class AutomaticLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) {
    suspend operator fun invoke(): Boolean {
        var result = false
        val persistentUserId = userManager.getPersistentUserId()
        persistentUserId?.let {
            userRepository.getUserById(it).fold(
                onSuccess = { user ->
                    userManager.currentUser = user
                    result = true
                }, onError = {
                    userManager.currentUser = null
                })
        }
        return result
    }
}
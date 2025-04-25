package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.domain.util.DomainResult
import javax.inject.Inject

class IsSessionKeptUseCase@Inject constructor(
    private val usersRepository: UsersRepository
) {
    operator fun invoke(): DomainResult<Boolean, UserError> = usersRepository.isSessionKept()
}
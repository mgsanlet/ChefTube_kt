package com.mgsanlet.cheftube.domain.util

object Constants {
    const val PASSWORD_MIN_LENGTH = 6
    const val USERNAME_MIN_LENGTH = 4
    const val USERNAME_MAX_LENGTH = 16
    const val PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).+$"
    const val SUPPORT_EMAIL = "support@cheftube.com"
}

enum class FilterCriterion {
    TITLE,
    INGREDIENT,
    DURATION,
    CATEGORY,
    DIFFICULTY
}
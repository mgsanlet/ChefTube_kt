package com.mgsanlet.cheftube.domain.model

data class DomainUser(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val createdRecipes: List<String> = emptyList(),
    val favouriteRecipes: List<String> = emptyList(),
    val followersIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList(),
    val inactiveDays: Int = 0
) {
    /**
     * Verifica si el usuario está inactivo (más de 30 días sin iniciar sesión)
     */
    fun isInactive(): Boolean = inactiveDays > 30
}
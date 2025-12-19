package com.koin.domain.user

/**
 * Pure domain representation of a user profile.
 */
data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val avatarUri: String? = null,
    val bio: String? = null,
)

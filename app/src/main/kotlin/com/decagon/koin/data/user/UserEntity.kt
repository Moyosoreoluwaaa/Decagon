package com.koin.data.user

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local representation of a user profile stored in Room.
 *
 * Keeping this minimal for now (no auth tokens, remote ids, etc.)
 * so that we can extend later without breaking existing local-only logic.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val avatarUri: String? = null,
    val bio: String? = null,
)

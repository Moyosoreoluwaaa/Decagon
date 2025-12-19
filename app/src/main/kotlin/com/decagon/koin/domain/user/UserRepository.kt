package com.koin.domain.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun users(): Flow<List<User>>
    fun user(id: Long): Flow<User?>
    suspend fun upsert(user: User): Long
    suspend fun delete(id: Long)
}

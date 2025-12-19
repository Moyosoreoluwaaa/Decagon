package com.koin.data.user

import com.koin.domain.user.User
import com.koin.domain.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalUserRepositoryImpl (
    private val userDao: UserDao,
) : UserRepository {

    override fun users(): Flow<List<User>> = userDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    override fun user(id: Long): Flow<User?> = userDao.getById(id).map { it?.toDomain() }

    override suspend fun upsert(user: User): Long {
        return userDao.upsert(user.toEntity())
    }

    override suspend fun delete(id: Long) {
        userDao.deleteById(id)
    }
}

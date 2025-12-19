package com.koin.data.user

import com.koin.domain.user.User

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    avatarUri = avatarUri,
    bio = bio
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    avatarUri = avatarUri,
    bio = bio
)

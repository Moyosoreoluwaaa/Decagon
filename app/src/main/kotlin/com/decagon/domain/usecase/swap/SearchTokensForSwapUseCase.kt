package com.decagon.domain.usecase.swap

import com.decagon.core.util.LoadingState
import com.decagon.data.mapper.toTokenInfo
import com.decagon.domain.model.TokenInfo
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.first

class SearchTokensForSwapUseCase(
    private val discoverRepo: DiscoverRepository
) {
    suspend operator fun invoke(query: String): Result<List<TokenInfo>> {
        return try {
            val tokens = discoverRepo.searchAllTokens(query).first()
            when (tokens) {
                is LoadingState.Success -> Result.success(
                    tokens.data.map { it.toTokenInfo() }
                )
                is LoadingState.Error -> Result.failure(tokens.throwable)
                else -> Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
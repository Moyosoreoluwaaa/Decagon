package com.wallet.data.mappers

import com.wallet.data.local.database.entities.TokenEntity
import com.octane.wallet.domain.models.Token
import com.wallet.data.remote.dto.TokenDto
import timber.log.Timber


/**
 * ✅ FIXED: Token mappers with proper logo URL extraction
 *
 * Data Flow:
 * CoinGecko API → TokenDto → TokenEntity (Database) → Token (Domain)
 */

// ==================== DTO → ENTITY ====================

/**
 * Convert CoinGecko API DTO to Room Entity.
 *
 * CRITICAL: The `image` field from CoinGecko contains the logo URL!
 */
fun TokenDto.toEntity(): TokenEntity {
    Timber.d("🔄 Mapping TokenDto: $symbol - Image URL: $image")

    return TokenEntity(
        id = id,
        symbol = symbol,
        name = name,
        logoUrl = image, // ✅ Direct from API
        currentPrice = currentPrice,
        priceChange24h = priceChange24h ?: 0.0,
        marketCap = marketCap,
        volume24h = totalVolume,
        rank = marketCapRank ?: 999,
        isVerified = true,
        tags = "",
        mintAddress = platforms?.get("solana"),
        lastUpdated = System.currentTimeMillis()
    ).also {
        Timber.d("✅ TokenEntity: ${it.symbol} - Logo: ${it.logoUrl}")
    }
}

// ==================== ENTITY → DOMAIN ====================

/**
 * Convert Room Entity to Domain Model.
 */
fun TokenEntity.toDomain(): Token {
    return Token(
        id = id,
        symbol = symbol,
        name = name,
        logoUrl = logoUrl, // ✅ Pass through to domain
        currentPrice = currentPrice,
        priceChange24h = priceChange24h,
        marketCap = marketCap,
        volume24h = volume24h,
        rank = rank,
        isVerified = isVerified,
        tags = tags.split(",").filter { it.isNotBlank() },
        mintAddress = mintAddress
    )
}

// ==================== LIST EXTENSIONS ====================

fun List<TokenDto>.toEntities(): List<TokenEntity> = map { it.toEntity() }

fun List<TokenEntity>.toDomainTokens(): List<Token> = map { it.toDomain() }
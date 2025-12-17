package com.octane.wallet.data.mappers

import com.octane.wallet.data.local.database.entities.PerpEntity
import com.octane.wallet.data.remote.dto.drift.DriftContractDto
import com.octane.wallet.domain.models.Perp
import timber.log.Timber

/**
 * ✅ FIXED: Perp mappers with DYNAMIC logo resolution.
 * No longer limited to hardcoded list!
 */

// ==================== DTO → ENTITY (WITH RESOLVER) ====================

/**
 * Convert Drift API DTO to Room Entity.
 *
 * IMPORTANT: Logo URL must be resolved separately via TokenLogoResolver.
 * This mapper just prepares the entity structure.
 */
suspend fun DriftContractDto.toEntity(
    logoUrl: String? = null  // ⭐ Pass resolved logo from repository
): PerpEntity {
    Timber.d("📄 Mapping PerpDto: $tickerId - Logo: ${logoUrl ?: "NONE"}")

    return PerpEntity(
        id = tickerId,
        symbol = tickerId,
        name = "$baseCurrency-$quoteCurrency Perpetual",

        // ✅ Use passed logo URL (resolved by TokenLogoResolver)
        logoUrl = logoUrl,

        // Prices (convert from String)
        indexPrice = indexPrice.toDoubleOrNull() ?: 0.0,
        markPrice = lastPrice.toDoubleOrNull() ?: 0.0,

        // Funding (convert from String)
        fundingRate = fundingRate.toDoubleOrNull() ?: 0.0,
        nextFundingTime = nextFundingRateTimestamp.toLongOrNull() ?: 0L,

        // Volume & OI (convert from String)
        openInterest = openInterest.toDoubleOrNull() ?: 0.0,
        volume24h = quoteVolume.toDoubleOrNull() ?: 0.0,

        // Price change
        priceChange24h = calculatePriceChange24h(),

        // Metadata
        leverage = "20x",
        exchange = "Drift",
        lastUpdated = System.currentTimeMillis()
    ).also {
        Timber.d("✅ PerpEntity created: ${it.symbol}")
    }
}

// ==================== ENTITY → DOMAIN ====================

fun PerpEntity.toDomain(): Perp {
    return Perp(
        id = id,
        symbol = symbol,
        name = name,
        logoUrl = logoUrl, // ✅ Pass through to domain
        indexPrice = indexPrice,
        markPrice = markPrice,
        fundingRate = fundingRate,
        nextFundingTime = nextFundingTime,
        openInterest = openInterest,
        volume24h = volume24h,
        priceChange24h = priceChange24h,
        leverage = leverage,
        exchange = exchange,
        isLong = priceChange24h >= 0
    )
}

// ==================== LIST EXTENSIONS ====================

fun List<PerpEntity>.toDomainPerps(): List<Perp> = map { it.toDomain() }

// ==================== HELPERS ====================

private fun DriftContractDto.calculatePriceChange24h(): Double {
    val current = lastPrice.toDoubleOrNull() ?: return 0.0
    val highPrice = high.toDoubleOrNull() ?: return 0.0
    val lowPrice = low.toDoubleOrNull() ?: return 0.0

    if (lowPrice == 0.0) return 0.0

    val midpoint = (highPrice + lowPrice) / 2.0
    if (midpoint == 0.0) return 0.0

    return ((current - midpoint) / midpoint) * 100.0
}
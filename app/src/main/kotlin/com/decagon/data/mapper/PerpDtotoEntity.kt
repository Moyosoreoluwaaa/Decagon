package com.decagon.data.mapper

import com.decagon.data.local.entity.PerpEntity
import com.decagon.data.remote.dto.drift.DriftContractDto
import com.decagon.data.service.PerpLogoProvider
import com.decagon.domain.model.Perp
import timber.log.Timber

/**
 * ✅ FIXED: Perp mappers with INSTANT logo resolution.
 * No API calls, no delays!
 */

// ==================== DTO → ENTITY ====================

/**
 * Convert Drift API DTO to Room Entity.
 *
 * ✅ NEW: Logo resolved instantly via PerpLogoProvider (no API call).
 */
fun DriftContractDto.toEntity(): PerpEntity {
    // ✅ INSTANT logo resolution (O(1) map lookup)
    val logoUrl = PerpLogoProvider.getLogoUrl(tickerId)

    if (logoUrl == null) {
        Timber.w("⚠️ No logo found for perp: $tickerId")
    } else {
        Timber.d("✅ Logo resolved: $tickerId -> ${logoUrl.take(50)}...")
    }

    return PerpEntity(
        id = tickerId,
        symbol = tickerId,
        name = "$baseCurrency-$quoteCurrency Perpetual",
        logoUrl = logoUrl, // ✅ Instant, no delay

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
        Timber.d("📝 PerpEntity created: ${it.symbol} - Logo: ${it.logoUrl != null}")
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
package com.octane.wallet.data.mappers

import com.octane.wallet.data.local.database.entities.DAppEntity
import com.octane.wallet.data.remote.dto.DAppDto
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.DAppCategory
import timber.log.Timber

/**
 * ✅ FIXED: DApp mappers with proper logo URL handling
 *
 * Data Flow:
 * DeFiLlama API → DAppDto → DAppEntity (Database) → DApp (Domain)
 */

// ==================== DTO → ENTITY ====================

fun DAppDto.toEntity(): DAppEntity {
    // ✅ DeFiLlama provides logo URLs via their CDN
    val logoUrl = logo ?: "https://icons.llama.fi/icons/protocols/${slug}?w=48"

    Timber.d("🔄 Mapping DAppDto: $name - Logo: $logoUrl")

    return DAppEntity(
        id = id,
        name = name,
        description = description ?: "No description available",

        // ✅ CRITICAL FIX: Use DeFiLlama's logo or CDN fallback
        logoUrl = logoUrl,

        category = category,
        url = url ?: "https://defillama.com/protocol/$slug",
        tvl = tvl,
        volume24h = null, // Not in /protocols endpoint
        users24h = null, // Not in /protocols endpoint
        isVerified = audits != null && audits != "0",
        chains = chains.joinToString(",").ifEmpty { chain ?: "Solana" },
        rating = 0.0,
        tags = (oracles ?: emptyList()).joinToString(","),
        lastUpdated = System.currentTimeMillis()
    ).also {
        Timber.d("✅ DAppEntity created: ${it.name} - Logo: ${it.logoUrl}")
    }
}

// ==================== ENTITY → DOMAIN ====================

fun DAppEntity.toDomain(): DApp {
    return DApp(
        id = id,
        name = name,
        description = description,
        logoUrl = logoUrl, // ✅ Pass through to domain
        category = parseDAppCategory(category),
        url = url,
        tvl = tvl,
        volume24h = volume24h,
        users24h = users24h,
        isVerified = isVerified,
        chains = chains.split(",").filter { it.isNotBlank() },
        rating = rating,
        tags = tags.split(",").filter { it.isNotBlank() }
    )
}

// ==================== CATEGORY PARSING ====================

private fun parseDAppCategory(category: String): DAppCategory {
    return when (category.lowercase().replace(" ", "")) {
        "dex", "decentralizedexchange" -> DAppCategory.DEFI
        "lending", "borrowing" -> DAppCategory.DEFI
        "yield", "yieldaggregator", "yield aggregator" -> DAppCategory.DEFI
        "liquidstaking", "liquid staking" -> DAppCategory.DEFI
        "nft", "nftmarketplace", "nftlending" -> DAppCategory.NFT
        "gaming", "gamblefi" -> DAppCategory.GAMING
        "socialfi", "social" -> DAppCategory.SOCIAL
        "bridge", "crosschain", "cross-chain" -> DAppCategory.BRIDGE
        "wallet" -> DAppCategory.WALLET
        "derivatives", "options", "perpetuals" -> DAppCategory.DEFI
        else -> DAppCategory.OTHER
    }
}

// ==================== LIST EXTENSIONS ====================

fun List<DAppDto>.toEntities(): List<DAppEntity> {
    Timber.d("📦 Converting ${this.size} DAppDTOs to Entities")
    val entities = map { it.toEntity() }

    // Log first 3 for verification
    entities.take(3).forEach {
        Timber.d("  • ${it.name}: ${it.logoUrl?.take(50)}...")
    }

    return entities
}

fun List<DAppEntity>.toDomainDApps(): List<DApp> = map { it.toDomain() }
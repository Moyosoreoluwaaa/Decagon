package com.decagon.core.crypto

import org.sol4k.PublicKey
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * SPL Token Program utilities for Decagon Wallet
 * 
 * Provides functionality for:
 * - Associated Token Account (ATA) derivation
 * - Token account creation instructions
 * - Common token mint addresses
 * 
 * SPL Token is Solana's standard for fungible and non-fungible tokens.
 * Every token (USDC, USDT, etc.) requires an Associated Token Account
 * to receive transfers.
 * 
 * Official Docs: https://spl.solana.com/token
 */
object DecagonSplTokenProgram {

    init {
        Timber.d("DecagonSplTokenProgram initialized")
    }

    /**
     * SPL Token Program ID (same on mainnet/devnet/testnet)
     */
    val TOKEN_PROGRAM_ID = PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

    /**
     * Associated Token Account Program ID
     */
    val ASSOCIATED_TOKEN_PROGRAM_ID = PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")

    /**
     * System Program ID (for account creation)
     */
    val SYSTEM_PROGRAM_ID = PublicKey("11111111111111111111111111111111")

    /**
     * Native SOL mint address (for wrapped SOL operations)
     */
    val NATIVE_MINT = PublicKey("So11111111111111111111111111111111111111112")

    /**
     * Derives Associated Token Account (ATA) address
     *
     * ATAs are deterministic addresses derived from:
     * - Wallet address
     * - Token mint address
     * - ATA program ID
     *
     * This ensures each wallet has exactly ONE token account per token.
     *
     * @param walletAddress User's main Solana wallet address
     * @param tokenMintAddress SPL token mint address (e.g., USDC mint)
     * @return Derived ATA address
     *
     * Example:
     * ```
     * val userWallet = PublicKey("YourWalletAddress")
     * val usdcMint = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
     * val ata = DecagonSplTokenProgram.findAssociatedTokenAddress(userWallet, usdcMint)
     * // Returns: User's USDC token account address
     * ```
     */
    fun findAssociatedTokenAddress(
        walletAddress: PublicKey,
        tokenMintAddress: PublicKey
    ): PublicKey {
        Timber.d("Deriving ATA for wallet: ${walletAddress.toBase58().take(8)}... mint: ${tokenMintAddress.toBase58().take(8)}...")

        // The ProgramDerivedAddress is a data class with (PublicKey, Int)
        val (address, _) = PublicKey.findProgramDerivedAddress(
            holderAddress = walletAddress,
            tokenMintAddress = tokenMintAddress,
            programId = TOKEN_PROGRAM_ID
        )

        Timber.v("Derived ATA: ${address.toBase58()}")
        return address
    }
    /**
     * Creates instruction to initialize an Associated Token Account
     *
     * MUST be called before receiving SPL tokens for the first time.
     * The instruction is idempotent - safe to call even if ATA exists.
     *
     * @param payer Account paying for rent (usually user's main wallet)
     * @param associatedTokenAddress ATA address (from findAssociatedTokenAddress)
     * @param owner Token account owner (usually same as payer)
     * @param mint Token mint address
     * @return Instruction data ready for transaction
     *
     * Cost: ~0.002 SOL (rent-exempt minimum for token account)
     *
     * Example:
     * ```
     * val instruction = DecagonSplTokenProgram.createAssociatedTokenAccountInstruction(
     *     payer = userWallet,
     *     associatedTokenAddress = ata,
     *     owner = userWallet,
     *     mint = usdcMint
     * )
     * // Add to transaction, sign, and send
     * ```
     */
    fun createAssociatedTokenAccountInstruction(
        payer: PublicKey,
        associatedTokenAddress: PublicKey,
        owner: PublicKey,
        mint: PublicKey
    ): DecagonTokenInstruction {
        Timber.d("Creating ATA instruction for mint: ${mint.toBase58().take(8)}...")

        // Instruction has no data (discriminator handled by program)
        val data = byteArrayOf()

        return DecagonTokenInstruction(
            programId = ASSOCIATED_TOKEN_PROGRAM_ID,
            accounts = listOf(
                DecagonAccountMeta(payer, isSigner = true, isWritable = true),
                DecagonAccountMeta(associatedTokenAddress, isSigner = false, isWritable = true),
                DecagonAccountMeta(owner, isSigner = false, isWritable = false),
                DecagonAccountMeta(mint, isSigner = false, isWritable = false),
                DecagonAccountMeta(SYSTEM_PROGRAM_ID, isSigner = false, isWritable = false),
                DecagonAccountMeta(TOKEN_PROGRAM_ID, isSigner = false, isWritable = false)
            ),
            data = data
        )
    }

    /**
     * Creates instruction to transfer SPL tokens
     *
     * @param source Source token account (sender's ATA)
     * @param destination Destination token account (receiver's ATA)
     * @param owner Source account owner (must sign)
     * @param amount Amount to transfer (in token's smallest unit)
     * @return Instruction data ready for transaction
     *
     * Example:
     * ```
     * val instruction = DecagonSplTokenProgram.createTransferInstruction(
     *     source = senderUsdc,
     *     destination = receiverUsdc,
     *     owner = senderWallet,
     *     amount = 1_000_000 // 1 USDC (6 decimals)
     * )
     * ```
     */
    fun createTransferInstruction(
        source: PublicKey,
        destination: PublicKey,
        owner: PublicKey,
        amount: Long
    ): DecagonTokenInstruction {
        Timber.d("Creating transfer instruction: amount=$amount")

        // Instruction discriminator: 3 (Transfer)
        // Followed by u64 amount
        val data = ByteBuffer.allocate(9).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(3.toByte())
            putLong(amount)
        }.array()

        return DecagonTokenInstruction(
            programId = TOKEN_PROGRAM_ID,
            accounts = listOf(
                DecagonAccountMeta(source, isSigner = false, isWritable = true),
                DecagonAccountMeta(destination, isSigner = false, isWritable = true),
                DecagonAccountMeta(owner, isSigner = true, isWritable = false)
            ),
            data = data
        )
    }

    /**
     * Common token mints on Solana mainnet
     *
     * Verified token addresses from:
     * https://github.com/solana-labs/token-list
     */
    object DecagonCommonTokens {
        // Stablecoins
        val USDC = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
        val USDT = PublicKey("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB")
        val USDS = PublicKey("USDSwr9ApdHk5bvJKMjzff41FfuX8bSxdKcR81vTwcA")
        val PYUSD = PublicKey("2b1kV6DkPAnxd5ixfnxCpjxmKwqjjaYmCZfHsFu24GXo")

        // Popular DEX tokens
        val JUP = PublicKey("JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN")
        val RAY = PublicKey("4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R")
        val ORCA = PublicKey("orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE")

        // Memecoins (verified)
        val BONK = PublicKey("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263")
        val WIF = PublicKey("EKpQGSJtjMFqKZ9KQanSqYXRcF8fBopzLHYxdM65zcjm")
        val POPCAT = PublicKey("7GCihgDB8fe6KNjn2MYtkzZcRjQy3t9GHdC8uHYmW2hr")

        // Wrapped assets
        val WBTC = PublicKey("3NZ9JMVBmGAqocybic2c7LQCJScmgsAZ6vQqTDzcqmJh")
        val WETH = PublicKey("7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs")

        /**
         * Checks if a mint address is a known stablecoin
         */
        fun isStablecoin(mint: PublicKey): Boolean {
            return mint == USDC || mint == USDT || mint == USDS || mint == PYUSD
        }

        /**
         * Gets token symbol from mint address
         */
        fun getSymbol(mint: PublicKey): String? {
            return when (mint) {
                USDC -> "USDC"
                USDT -> "USDT"
                USDS -> "USDS"
                PYUSD -> "PYUSD"
                JUP -> "JUP"
                RAY -> "RAY"
                ORCA -> "ORCA"
                BONK -> "BONK"
                WIF -> "WIF"
                POPCAT -> "POPCAT"
                WBTC -> "WBTC"
                WETH -> "WETH"
                NATIVE_MINT -> "SOL"
                else -> null
            }
        }
    }
}

/**
 * Represents a Solana instruction for token operations
 * 
 * Instructions are atomic operations that modify on-chain state.
 * Multiple instructions can be combined into a single transaction.
 */
data class DecagonTokenInstruction(
    val programId: PublicKey,
    val accounts: List<DecagonAccountMeta>,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as DecagonTokenInstruction
        
        if (programId != other.programId) return false
        if (accounts != other.accounts) return false
        if (!data.contentEquals(other.data)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = programId.hashCode()
        result = 31 * result + accounts.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Account metadata for instructions
 * 
 * Specifies account permissions and signing requirements
 * for a particular instruction.
 * 
 * @param pubkey Account's public key
 * @param isSigner Account must sign the transaction
 * @param isWritable Instruction may modify this account
 */
data class DecagonAccountMeta(
    val pubkey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)

/**
 * Token account data structure
 * 
 * Represents the on-chain state of an SPL token account.
 * This is the data stored at an ATA address.
 */
data class DecagonTokenAccount(
    val mint: PublicKey,
    val owner: PublicKey,
    val amount: Long,
    val delegate: PublicKey? = null,
    val state: DecagonTokenAccountState,
    val isNative: Boolean = false,
    val delegatedAmount: Long = 0,
    val closeAuthority: PublicKey? = null
)

/**
 * Token account state
 */
enum class DecagonTokenAccountState {
    UNINITIALIZED,
    INITIALIZED,
    FROZEN
}

/**
 * Extension functions for token operations
 */

/**
 * Converts token amount from human-readable to smallest unit
 * 
 * Example:
 * ```
 * 1.5.toTokenAmount(6) // Returns 1_500_000 (for USDC with 6 decimals)
 * ```
 */
fun Double.toDecagonTokenAmount(decimals: Int): Long {
    return (this * Math.pow(10.0, decimals.toDouble())).toLong()
}

/**
 * Converts token amount from smallest unit to human-readable
 * 
 * Example:
 * ```
 * 1_500_000L.fromTokenAmount(6) // Returns 1.5 (USDC)
 * ```
 */
fun Long.fromDecagonTokenAmount(decimals: Int): Double {
    return this / Math.pow(10.0, decimals.toDouble())
}

/**
 * Formats token amount for display
 * 
 * Example:
 * ```
 * 1234567L.formatDecagonTokenAmount(6, "USDC") // "1.234567 USDC"
 * ```
 */
fun Long.formatDecagonTokenAmount(decimals: Int, symbol: String): String {
    val amount = this.fromDecagonTokenAmount(decimals)
    return "%.${decimals}f %s".format(amount, symbol)
}
package com.octane.wallet.core.extension

/**
 * Truncate wallet address for display.
 * Pattern: abcd...wxyz
 */
fun String.truncateAddress(
    prefixLength: Int = 4,
    suffixLength: Int = 4
): String {
    if (length <= prefixLength + suffixLength + 3) return this
    return "${take(prefixLength)}...${takeLast(suffixLength)}"
}

/**
 * Truncate to max length with ellipsis.
 */
fun String.truncateToLength(maxLength: Int): String {
    if (length <= maxLength) return this
    return take(maxLength - 3) + "..."
}

/**
 * Check if string is a valid Solana address.
 */
fun String.isSolanaAddress(): Boolean {
    if (length !in 32..44) return false
    val base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$".toRegex()
    return matches(base58Regex)
}

/**
 * Check if string is a SNS domain.
 */
fun String.isSnsDomain(): Boolean {
    return endsWith(".sol") && length > 4
}

/**
 * Remove all whitespace.
 */
fun String.removeWhitespace(): String =
    replace("\\s".toRegex(), "")

/**
 * Capitalize first letter.
 */
fun String.capitalizeFirst(): String {
    if (isEmpty()) return this
    return replaceFirstChar { it.uppercase() }
}

/**
 * Extract numbers from string.
 */
fun String.extractNumbers(): String {
    return filter { it.isDigit() || it == '.' }
}

/**
 * Mask sensitive data (show last N characters).
 */
fun String.maskSensitive(
    visibleChars: Int = 4,
    maskChar: Char = '*'
): String {
    if (length <= visibleChars) return this
    val masked = maskChar.toString().repeat(length - visibleChars)
    return masked + takeLast(visibleChars)
}

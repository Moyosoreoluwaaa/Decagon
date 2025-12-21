package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography

/**
 * Reusable header component for list sections within a LazyColumn/LazyRow.
 * Displays "Search Results" or the provided [sectionTitle] and an optional Info button.
 *
 * @param searchQuery The current search query string.
 * @param sectionTitle The title to display when the search query is blank (e.g., "Trending Tokens").
 * @param onActionClick Optional click handler for the right-aligned action button (e.g., Info icon).
 */
@Composable
fun ListSectionHeader(
    searchQuery: String,
    sectionTitle: String,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title logic: "Search Results" or the sectionTitle
        Text(
            text = if (searchQuery.isNotBlank()) "Search Results" else sectionTitle,
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.weight(1f))

        // Action Button: Only render if an action is provided
        if (onActionClick != null) {

            IconButton(
                onClick = onActionClick,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Information for $sectionTitle",
                    tint = AppColors.TextSecondary
                )
            }
        }
    }
}
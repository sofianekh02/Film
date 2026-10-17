package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    onDismiss: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateWatchlist: () -> Unit,
    onToggleLanguage: () -> Unit,
    currentLanguage: String
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111724),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "TOP MOVIES",
                style = CinemaTypography.titleLarge,
                color = CinemaRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MenuItemRow(
                icon = Icons.Default.Home,
                title = if (currentLanguage == "ar-SA") "الرئيسية" else "Home",
                onClick = {
                    onDismiss()
                    onNavigateHome()
                },
                testTag = "menu_nav_home"
            )

            MenuItemRow(
                icon = Icons.Default.Search,
                title = if (currentLanguage == "ar-SA") "البحث" else "Search",
                onClick = {
                    onDismiss()
                    onNavigateSearch()
                },
                testTag = "menu_nav_search"
            )

            MenuItemRow(
                icon = Icons.Default.Bookmark,
                title = if (currentLanguage == "ar-SA") "قائمتي" else "My Watchlist",
                onClick = {
                    onDismiss()
                    onNavigateWatchlist()
                },
                testTag = "menu_nav_watchlist"
            )

            HorizontalDivider(
                color = Color(0xFF232D40),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            MenuItemRow(
                icon = Icons.Default.Language,
                title = if (currentLanguage == "en-US") "العربية (Arabic)" else "English (English)",
                onClick = {
                    onToggleLanguage()
                    onDismiss()
                },
                testTag = "menu_nav_language"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = ElectricBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

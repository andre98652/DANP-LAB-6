package com.example.lab6.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab6.features.pantry.domain.FreshnessStatus
import com.example.lab6.ui.theme.*

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle) en UI
 * Cada componente Compose en este archivo tiene una única responsabilidad visual concreta.
 * Son desacoplados y configurables mediante parámetros estándar, lo que promueve su reusabilidad.
 */

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun FreshnessBadge(status: FreshnessStatus) {
    val (text, bgColor, textColor) = when (status) {
        FreshnessStatus.FRESH -> Triple("Fresco", FoodFresh.copy(alpha = 0.15f), FoodFresh)
        FreshnessStatus.EXPIRING_SOON -> Triple("Por Vencer", FoodExpiringSoon.copy(alpha = 0.15f), FoodExpiringSoon)
        FreshnessStatus.EXPIRED -> Triple("Vencido", FoodExpired.copy(alpha = 0.15f), FoodExpired)
        FreshnessStatus.SAFE -> Triple("Seguro", FoodSafeBlue.copy(alpha = 0.15f), FoodSafeBlue)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun MatchPercentageBadge(percentage: Int) {
    val (bgColor, textColor) = when {
        percentage == 100 -> Pair(FoodFresh.copy(alpha = 0.15f), FoodFresh)
        percentage >= 50 -> Pair(FoodExpiringSoon.copy(alpha = 0.15f), FoodExpiringSoon)
        else -> Pair(FoodExpired.copy(alpha = 0.15f), FoodExpired)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$percentage% Disponible",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

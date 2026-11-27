package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MonoTheme
import java.util.Locale

enum class CloneIconStyle(val key: String, val label: String) {
    DEFAULT("DEFAULT", "Original"),
    MONO_GREY("MONO_GREY", "Monochrome"),
    MONO_INVERT("MONO_INVERT", "Inverted"),
    MONO_OUTLINE("MONO_OUTLINE", "Framed")
}

@Composable
fun MonoBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val accent = MonoTheme.colors.accent
    val muted = MonoTheme.colors.textSecondary
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sq = w * 0.64f
        val radius = CornerRadius(w * 0.16f, w * 0.16f)

        drawRoundRect(
            color = muted.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.28f, h * 0.28f),
            size = Size(sq, sq),
            cornerRadius = radius,
            style = Stroke(width = w * 0.08f)
        )
        drawRoundRect(
            color = accent,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(sq, sq),
            cornerRadius = radius
        )
    }
}

@Composable
fun MonoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MonoTheme.colors
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun MonoSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = MonoTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = colors.secondaryBackground,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonoStatusPill(
    text: String,
    toneColor: Color = MonoTheme.colors.textSecondary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = toneColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, toneColor.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = toneColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AppIconAvatar(
    packageName: String,
    appName: String,
    iconStyleKey: String = CloneIconStyle.DEFAULT.key,
    badgeLabel: String? = null,
    loadIcon: suspend (String) -> ImageBitmap?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val colors = MonoTheme.colors
    val iconBitmap by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = loadIcon(packageName)
    }

    val colorFilter = when (iconStyleKey) {
        CloneIconStyle.MONO_GREY.key -> ColorFilter.colorMatrix(
            ColorMatrix().apply { setToSaturation(0f) }
        )
        CloneIconStyle.MONO_INVERT.key -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        else -> null
    }

    Box(modifier = modifier.size(size + 6.dp)) {
        Box(
            modifier = Modifier
                .size(size)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
                .then(
                    if (iconStyleKey == CloneIconStyle.MONO_OUTLINE.key) {
                        Modifier.border(1.5.dp, colors.accent, RoundedCornerShape(12.dp))
                    } else {
                        Modifier.border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = iconBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "$appName icon",
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(9.dp))
                )
            } else {
                Text(
                    text = appName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (!badgeLabel.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.accent)
                    .border(1.dp, colors.primaryBackground, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badgeLabel.take(5).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatByteSize(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.0f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}

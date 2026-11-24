package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalGlassPalette

@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_drift")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_aurora_backdrop),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.42f,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.cyanGlow.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.18f + 0.12f * drift), h * 0.16f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * (0.18f + 0.12f * drift), h * 0.16f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.violetGlow.copy(alpha = 0.26f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.85f - 0.14f * drift), h * 0.48f),
                    radius = w * 0.80f
                ),
                radius = w * 0.80f,
                center = Offset(w * (0.85f - 0.14f * drift), h * 0.48f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.cyanGlow.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.35f, h * (0.88f - 0.08f * drift)),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.35f, h * (0.88f - 0.08f * drift))
            )
        }

        // Subtle vignette scrim to maintain high text legibility across all panels
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.canvasBackground.copy(alpha = 0.45f),
                            palette.canvasBackground.copy(alpha = 0.28f),
                            palette.canvasBackground.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    elevated: Boolean = false,
    accentGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val surfaceTop = if (elevated) palette.surfaceHigh else palette.surfaceMedium
    val surfaceBottom = if (elevated) palette.surfaceMedium else palette.surfaceLow

    val borderBrush = if (accentGlow) {
        Brush.linearGradient(
            colors = listOf(
                palette.cyanGlow.copy(alpha = 0.65f),
                palette.highlightBorder,
                palette.violetGlow.copy(alpha = 0.55f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                palette.highlightBorder,
                palette.subtleBorder,
                palette.cyanGlow.copy(alpha = 0.22f)
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.cyanGlow.copy(alpha = 0.35f)),
                role = Role.Button,
                onClick = onClick
            )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(surfaceTop, surfaceBottom)
                )
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassSegmentedTab(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "segment_tab"
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surfaceLow)
            .border(1.dp, palette.subtleBorder, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val textColor by animateColorAsState(
                targetValue = if (selected) palette.onAccentDark else palette.textSecondary,
                label = "tab_text_color"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (selected) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    colors = listOf(palette.cyanGlow, palette.violetGlow)
                                )
                            )
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = palette.cyanGlow.copy(alpha = 0.3f)),
                        role = Role.Tab
                    ) { onSelect(index) }
                    .testTag("${testTagPrefix}_$index")
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    prefixText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = ""
) {
    val palette = LocalGlassPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = if (errorText != null) palette.errorCoral else palette.cyanGlow
                )
            },
            prefix = prefixText?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.cyanGlow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            trailingIcon = trailingContent,
            isError = errorText != null,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = palette.fieldContainer.copy(alpha = 0.72f),
                unfocusedContainerColor = palette.fieldContainer.copy(alpha = 0.50f),
                errorContainerColor = palette.errorCoral.copy(alpha = 0.10f),
                focusedBorderColor = palette.cyanGlow,
                unfocusedBorderColor = palette.subtleBorder,
                errorBorderColor = palette.errorCoral,
                focusedLabelColor = palette.cyanGlow,
                unfocusedLabelColor = palette.textSecondary,
                errorLabelColor = palette.errorCoral,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = palette.cyanGlow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = palette.errorCoral,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun GlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    val palette = LocalGlassPalette.current
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(palette.cyanGlow, palette.violetGlow)
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = palette.surfaceLow,
            contentColor = palette.onAccentDark,
            disabledContentColor = palette.textMuted
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .drawBehind {
                if (enabled) {
                    drawCircle(
                        color = palette.cyanGlow.copy(alpha = 0.20f),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.5f, size.height * 0.65f)
                    )
                }
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (enabled) gradientBrush else Brush.horizontalGradient(
                    listOf(palette.surfaceLow, palette.surfaceLow)
                )
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = palette.onAccentDark,
                    strokeWidth = 2.5.dp
                )
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GlassGoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    testTag: String = "google_signin_button"
) {
    val palette = LocalGlassPalette.current
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.94f),
        contentColor = palette.onAccentDark,
        border = BorderStroke(1.dp, Color.White),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = palette.onAccentDark,
                    strokeWidth = 2.5.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(palette.cyanGlow.copy(alpha = 0.25f), palette.violetGlow.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Google",
                        tint = palette.onAccentDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.onAccentDark
                )
            }
        }
    }
}

data class NavTabItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun GlassBottomNavBar(
    items: List<NavTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.deepLayer.copy(alpha = 0.88f),
                            palette.canvasBackground.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            palette.highlightBorder,
                            palette.subtleBorder,
                            palette.cyanGlow.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val contentColor by animateColorAsState(
                    targetValue = if (selected) palette.cyanGlow else palette.textMuted,
                    label = "nav_color"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (selected) {
                                Modifier
                                    .background(palette.cyanGlow.copy(alpha = 0.14f))
                                    .border(
                                        1.dp,
                                        palette.cyanGlow.copy(alpha = 0.35f),
                                        RoundedCornerShape(18.dp)
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = palette.cyanGlow.copy(alpha = 0.3f)),
                            role = Role.Tab
                        ) { onSelect(index) }
                        .testTag("bottom_nav_$index")
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GlassStatusPill(
    text: String,
    accentColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 10.dp
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accentColor.copy(alpha = 0.16f))
            .border(1.dp, accentColor.copy(alpha = 0.42f), RoundedCornerShape(50))
            .padding(horizontal = horizontalPadding, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

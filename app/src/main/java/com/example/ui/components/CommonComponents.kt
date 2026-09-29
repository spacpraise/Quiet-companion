package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkWhite
import com.example.ui.theme.PetalAccent
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PetalDot(
    modifier: Modifier = Modifier,
    size: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PetalAccent)
    )
}

@Composable
fun QuietTopBar(
    title: String,
    subtitle: String? = null,
    onUserClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Surface(
        color = CanvasSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    PetalDot(size = 6.dp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    actions()
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(InkBlack)
                            .clickable { onUserClick() }
                            .testTag("user_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Account",
                            tint = InkWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOutline,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun QuietBottomNavBar(
    currentScreen: Screen,
    unreadNotifCount: Int = 2,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        color = CanvasSurface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderOutline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Today,
                label = "Today",
                isSelected = currentScreen == Screen.TODAY,
                onClick = { onNavigate(Screen.TODAY) },
                testTag = "nav_today"
            )
            BottomNavItem(
                icon = Icons.Outlined.Checklist,
                label = "Routine",
                isSelected = currentScreen == Screen.ROUTINE || currentScreen == Screen.TASK_CREATION,
                onClick = { onNavigate(Screen.ROUTINE) },
                testTag = "nav_routine"
            )
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Library",
                isSelected = currentScreen == Screen.LIBRARY || currentScreen == Screen.BOOK_DETAIL || currentScreen == Screen.READER,
                onClick = { onNavigate(Screen.LIBRARY) },
                testTag = "nav_library"
            )
            BottomNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "Companion",
                isSelected = currentScreen == Screen.COMPANION,
                onClick = { onNavigate(Screen.COMPANION) },
                testTag = "nav_companion"
            )
            BottomNavItem(
                icon = Icons.Default.Notifications,
                label = "Alerts",
                isSelected = currentScreen == Screen.ACTIVITY,
                badgeCount = unreadNotifCount,
                onClick = { onNavigate(Screen.ACTIVITY) },
                testTag = "nav_alerts"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) InkBlack else TextOutline,
                modifier = Modifier.size(22.dp)
            )
            if (badgeCount > 0 && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PetalAccent)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) InkBlack else TextOutline
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            PetalDot(size = 4.dp)
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Reusable Empty State Card matching the Zen Editorial design system
 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextOutline,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                PetalDot(size = 5.dp)
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            if (actionLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = InkWhite
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Shimmering loading placeholder skeleton card
 */
@Composable
fun LoadingSkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh.copy(alpha = alpha))
            .border(1.dp, BorderOutline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest.copy(alpha = alpha))
            )
        }
    }
}

/**
 * Celebration Completion Dialog for Book/Cadence completion
 */
@Composable
fun CelebrationCompletionDialog(
    type: String, // "CADENCE" or "BOOK"
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainerLowest,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (type == "BOOK") Icons.Default.Celebration else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (type == "BOOK") "Volume Completed" else "Cadence Achieved",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PetalDot(size = 8.dp)
                }

                Text(
                    text = if (type == "BOOK")
                        "You finished reading this volume. Quiet, intentional reading preserved."
                    else
                        "All intentional blocks completed for today. Cadence fulfilled and mind at rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Stat pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (type == "BOOK") "Reading Streak: 5 Days" else "Cadence Harmony: 100%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onPrimaryAction()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = InkWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (type == "BOOK") "Record Reflection" else "Rest Your Mind",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun FloatingToast(
    message: String?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && !message.isNullOrBlank(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(CircleShape)
                .background(InkBlack)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetalDot(size = 6.dp)
                Text(
                    text = message ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkWhite
                )
            }
        }
    }
}

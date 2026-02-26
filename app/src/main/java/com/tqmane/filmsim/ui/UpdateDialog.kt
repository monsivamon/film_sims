package com.tqmane.filmsim.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tqmane.filmsim.R
import com.tqmane.filmsim.ui.theme.LiquidColors
import com.tqmane.filmsim.util.ReleaseInfo

// ═══════════════════════════════════════════════════════════════════════════════
// UPDATE DIALOG — Modern glassmorphic Compose redesign
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun UpdateDialog(
    release: ReleaseInfo,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    // Pulsing amber ring around the update icon
    val infiniteTransition = rememberInfiniteTransition(label = "update_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1E28), Color(0xFF0D0D13))
                        )
                    )
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(28.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ─── Pulsing Icon ─────────────────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    // Outer glow ring (animated)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(LiquidColors.AccentPrimary.copy(alpha = pulseAlpha * 0.35f))
                    )
                    // Inner icon circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LiquidColors.AccentPrimary.copy(alpha = 0.14f))
                            .border(
                                1.5.dp,
                                LiquidColors.AccentPrimary.copy(alpha = pulseAlpha),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = null,
                            tint = LiquidColors.AccentPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ─── Title ────────────────────────────────────────────────────
                Text(
                    stringResource(R.string.update_available),
                    color = LiquidColors.TextHighEmphasis,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(Modifier.height(8.dp))

                // ─── Version badge ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(LiquidColors.AccentPrimary.copy(alpha = 0.14f))
                        .border(
                            1.dp,
                            LiquidColors.AccentPrimary.copy(alpha = 0.30f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    Text(
                        stringResource(R.string.new_version_available, release.version),
                        color = LiquidColors.AccentPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // ─── Release notes ────────────────────────────────────────────
                if (release.releaseNotes.isNotBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "RELEASE NOTES",
                            color = LiquidColors.TextLowEmphasis,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            release.releaseNotes,
                            color = LiquidColors.TextMediumEmphasis,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ─── Action buttons ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Later (ghost)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x13FFFFFF))
                            .border(1.dp, Color(0x1EFFFFFF), RoundedCornerShape(14.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.later),
                            color = LiquidColors.TextMediumEmphasis,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Update now (accent gradient)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        LiquidColors.GradientAccentStart,
                                        LiquidColors.GradientAccentEnd
                                    )
                                )
                            )
                            .clickable { onUpdate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.update_now),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

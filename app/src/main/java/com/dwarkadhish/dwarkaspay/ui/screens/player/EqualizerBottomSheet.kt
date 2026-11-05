package com.dwarkadhish.dwarkaspay.ui.screens.player

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.model.EqualizerSettings
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurface
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    settings: EqualizerSettings,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onUpdateSettings: (EqualizerSettings) -> Unit
) {
    val presetMap = mapOf(
        "Midnight Bass" to listOf(7, 5, 1, 3, 5),
        "Vocal Boost" to listOf(1, 3, 6, 5, 2),
        "Electronic" to listOf(6, 4, -1, 3, 6),
        "Rock" to listOf(5, 3, -1, 4, 6),
        "Acoustic" to listOf(4, 3, 2, 4, 3),
        "Flat" to listOf(0, 0, 0, 0, 0)
    )

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepElevatedSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OutlineColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Row with Switch & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Audio Equalizer",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Viora Midnight Sound Stage",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftLavender
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MidnightBlack,
                            checkedTrackColor = ElectricViolet,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = CardSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Presets Horizontal Row
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = ElectricViolet,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetMap.forEach { (presetName, levels) ->
                    val isSelected = settings.presetName == presetName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ElectricViolet else CardSurface)
                            .clickable(enabled = settings.isEnabled) {
                                onUpdateSettings(
                                    settings.copy(
                                        presetName = presetName,
                                        bandLevels = levels
                                    )
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = presetName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MidnightBlack else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5-Band Graphic Equalizer Frequencies
            Text(
                text = "FREQUENCY BANDS (dB)",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = ElectricViolet,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                settings.bandLevels.forEachIndexed { index, level ->
                    val freq = bandFrequencies.getOrElse(index) { "${index + 1}" }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (level > 0) "+$level" else "$level",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (level != 0) ElectricViolet else TextTertiary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Vertical progress simulator box
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(CardSurface)
                                .padding(2.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fraction = ((level + 10) / 20f).coerceIn(0.05f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((86 * fraction).dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (settings.isEnabled) ElectricViolet else OutlineColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = freq,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SoftLavender,
                            textAlign = TextAlign.Center
                        )

                        // Quick buttons to adjust band
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "▲",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(enabled = settings.isEnabled && level < 10) {
                                        val newLevels = settings.bandLevels.toMutableList()
                                        newLevels[index] = (level + 1).coerceAtMost(10)
                                        onUpdateSettings(
                                            settings.copy(
                                                presetName = "Custom",
                                                bandLevels = newLevels
                                            )
                                        )
                                    }
                                    .padding(4.dp)
                            )
                            Text(
                                text = "▼",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(enabled = settings.isEnabled && level > -10) {
                                        val newLevels = settings.bandLevels.toMutableList()
                                        newLevels[index] = (level - 1).coerceAtLeast(-10)
                                        onUpdateSettings(
                                            settings.copy(
                                                presetName = "Custom",
                                                bandLevels = newLevels
                                            )
                                        )
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound Effects: Bass Boost & 3D Virtualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bass Boost",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${settings.bassBoost}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.bassBoost.toFloat(),
                onValueChange = {
                    onUpdateSettings(settings.copy(bassBoost = it.toInt()))
                },
                valueRange = 0f..100f,
                enabled = settings.isEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricViolet,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = CardSurface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3D Virtualizer Surround",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${settings.virtualizer}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.virtualizer.toFloat(),
                onValueChange = {
                    onUpdateSettings(settings.copy(virtualizer = it.toInt()))
                },
                valueRange = 0f..100f,
                enabled = settings.isEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricViolet,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = CardSurface
                )
            )
        }
    }
}

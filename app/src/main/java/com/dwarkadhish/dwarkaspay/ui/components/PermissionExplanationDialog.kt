package com.dwarkadhish.dwarkaspay.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurface
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary

@Composable
fun PermissionExplanationDialog(
    icon: ImageVector,
    title: String,
    description: String,
    benefits: List<String> = emptyList(),
    confirmButtonText: String = "Allow",
    dismissButtonText: String = "Not Now",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepElevatedSurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CardSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (benefits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftLavender
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = confirmButtonText,
                    color = MidnightBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissButtonText,
                    color = TextSecondary
                )
            }
        }
    )
}

@Composable
fun PermissionPermanentlyDeniedDialog(
    title: String = "Permission Required",
    description: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepElevatedSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = { openAppSettings() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Open Settings",
                    color = MidnightBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        }
    )
}

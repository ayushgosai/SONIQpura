package com.dwarkadhish.dwarkaspay.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dwarkadhish.dwarkaspay.model.AuthUser
import com.dwarkadhish.dwarkaspay.ui.theme.*

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.CameraAlt
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: AuthUser?,
    authRepository: AuthRepository? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val displayName = user?.displayName ?: "SONIQ Listener"
    val email = user?.email ?: ""
    val photoUri = user?.profilePhotoUri ?: user?.avatarUrl
    val birthdate = user?.birthdate
    val age = user?.age
    val gender = user?.gender
    val isVerified = user?.isEmailVerified == true
    val isGoogleUser = user?.providerId == "google.com"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && user != null && authRepository != null) {
            val localUri = try {
                val dir = java.io.File(context.filesDir, "profiles").apply { mkdirs() }
                val destFile = java.io.File(dir, "avatar_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(destFile)
            } catch (e: Exception) {
                uri
            }
            authRepository.updateUserProfile(
                email = user.email,
                profilePhotoUri = localUri.toString(),
                birthdate = user.birthdate,
                age = user.age,
                gender = user.gender
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero gradient header background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ─── Avatar + Name Section ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Profile picture with camera badge
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(124.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(ElectricViolet, SoftLavender, ElectricViolet)
                                ),
                                shape = CircleShape
                            )
                            .clickable(enabled = authRepository != null) {
                                photoPickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ElectricViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = ElectricViolet
                                )
                            }
                        }
                    }

                    // Camera change badge
                    if (authRepository != null) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElectricViolet)
                                .border(2.dp, MidnightBlack, CircleShape)
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Change profile photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email with verified badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Provider badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isGoogleUser) Color(0xFF4285F4).copy(alpha = 0.18f)
                            else ElectricViolet.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isGoogleUser) "Google Account" else "Email Account",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isGoogleUser) Color(0xFF4285F4) else ElectricViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Profile Details Card ───────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                ProfileSectionHeader(title = "Personal Information")
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        ProfileInfoRow(
                            icon = Icons.Rounded.Person,
                            label = "Full Name",
                            value = displayName
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        ProfileInfoRow(
                            icon = Icons.Rounded.Email,
                            label = "Email",
                            value = email
                        )

                        if (birthdate != null) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            ProfileInfoRow(
                                icon = Icons.Rounded.CalendarToday,
                                label = "Date of Birth",
                                value = birthdate
                            )
                        }

                        if (age != null) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            ProfileInfoRow(
                                icon = Icons.Rounded.Cake,
                                label = "Age",
                                value = "$age years"
                            )
                        }

                        if (gender != null) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            ProfileInfoRow(
                                icon = Icons.Rounded.Person4,
                                label = "Gender",
                                value = gender
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Membership Card
                ProfileSectionHeader(title = "Membership")
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    ElectricViolet.copy(alpha = 0.25f),
                                    SoftLavender.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = ElectricViolet.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "SONIQ Free",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Member since 2026 · Upgrade to Premium for lossless audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricViolet,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

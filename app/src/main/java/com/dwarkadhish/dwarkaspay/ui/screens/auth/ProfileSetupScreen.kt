package com.dwarkadhish.dwarkaspay.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    email: String,
    name: String,
    password: String,
    authRepository: AuthRepository,
    onProfileSetupComplete: (String) -> Unit, // navigate to VerifyOtp with email
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Profile photo
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Birthdate state
    var birthdate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Age (auto-calculated from birthdate)
    var calculatedAge by remember { mutableStateOf<Int?>(null) }

    // Gender selection
    val genderOptions = listOf("Male", "Female", "Non-Binary", "Prefer not to say")
    var selectedGender by remember { mutableStateOf<String?>(null) }

    // Loading / error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Photo picker launcher with permanent local storage save
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
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
            selectedPhotoUri = localUri
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            val day = cal.get(Calendar.DAY_OF_MONTH)
                            val month = cal.get(Calendar.MONTH) + 1
                            val year = cal.get(Calendar.YEAR)
                            birthdate = "%02d/%02d/%04d".format(day, month, year)
                            // Calculate age
                            val today = Calendar.getInstance()
                            var age = today.get(Calendar.YEAR) - year
                            if (today.get(Calendar.MONTH) + 1 < month ||
                                (today.get(Calendar.MONTH) + 1 == month && today.get(Calendar.DAY_OF_MONTH) < day)
                            ) age--
                            calculatedAge = age.coerceAtLeast(0)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm", color = ElectricViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DeepElevatedSurface,
                titleContentColor = TextPrimary,
                headlineContentColor = ElectricViolet,
                weekdayContentColor = TextSecondary,
                subheadContentColor = TextPrimary,
                navigationContentColor = ElectricViolet,
                yearContentColor = TextPrimary,
                disabledYearContentColor = TextTertiary,
                currentYearContentColor = ElectricViolet,
                selectedYearContentColor = MidnightBlack,
                selectedYearContainerColor = ElectricViolet,
                dayContentColor = TextPrimary,
                disabledDayContentColor = TextTertiary,
                selectedDayContentColor = MidnightBlack,
                selectedDayContainerColor = ElectricViolet,
                todayContentColor = ElectricViolet,
                todayDateBorderColor = ElectricViolet
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    containerColor = DeepElevatedSurface,
                    selectedDayContentColor = MidnightBlack,
                    selectedDayContainerColor = ElectricViolet,
                    todayContentColor = ElectricViolet,
                    todayDateBorderColor = ElectricViolet
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        // Background gradient accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.18f),
                            MidnightBlack
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "VIORA MUSIC",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = ElectricViolet
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Hey ${name.split(" ").firstOrNull() ?: "there"}! Add a photo and a few details to personalise your Viora experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Profile Photo Picker ───────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(ElectricViolet, SoftLavender, ElectricViolet)
                            ),
                            shape = CircleShape
                        )
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ElectricViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = ElectricViolet,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                // Camera FAB
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Pick photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = if (selectedPhotoUri != null) "Tap to change photo" else "Tap to add photo",
                style = MaterialTheme.typography.labelSmall,
                color = SoftLavender,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Birthdate Picker ───────────────────────────────────
            SectionLabel(text = "Date of Birth")
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepElevatedSurface)
                    .border(
                        width = 1.dp,
                        color = if (birthdate.isNotEmpty()) ElectricViolet.copy(alpha = 0.5f)
                                else DividerColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = if (birthdate.isNotEmpty()) ElectricViolet else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = if (birthdate.isNotEmpty()) birthdate else "Select your birthdate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (birthdate.isNotEmpty()) TextPrimary else TextTertiary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (calculatedAge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Age ${calculatedAge}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricViolet
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Gender Selector ────────────────────────────────────
            SectionLabel(text = "Gender")
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genderOptions.take(2).forEach { gender ->
                    GenderChip(
                        label = gender,
                        isSelected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genderOptions.drop(2).forEach { gender ->
                    GenderChip(
                        label = gender,
                        isSelected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Error Banner ───────────────────────────────────────
            AnimatedVisibility(visible = errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ErrorRed.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ─── Continue to Verification ───────────────────────────
            Button(
                onClick = {
                    // Validation — gender is required; others optional
                    if (selectedGender == null) {
                        errorMessage = "Please select your gender to continue."
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    coroutineScope.launch {
                        // 1. Register the user account with profile details (sends real OTP to Gmail inbox)
                        val result = authRepository.signUpWithEmail(
                            name = name,
                            email = email,
                            password = password,
                            profilePhotoUri = selectedPhotoUri?.toString(),
                            birthdate = birthdate.ifEmpty { null },
                            age = calculatedAge,
                            gender = selectedGender
                        )
                        result.fold(
                            onFailure = { err ->
                                isLoading = false
                                errorMessage = err.message ?: "Registration failed. Please try again."
                            },
                            onSuccess = {
                                isLoading = false
                                // 2. Navigate to OTP verification
                                onProfileSetupComplete(email)
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricViolet,
                    disabledContainerColor = ElectricViolet.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Continue to Verification →",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip option — for optional profile
            TextButton(onClick = {
                errorMessage = null
                isLoading = true
                coroutineScope.launch {
                    val result = authRepository.signUpWithEmail(name, email, password)
                    isLoading = false
                    result.fold(
                        onSuccess = { onProfileSetupComplete(email) },
                        onFailure = { err ->
                            errorMessage = err.message ?: "Registration failed."
                        }
                    )
                }
            }) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        ),
        color = TextSecondary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GenderChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = tween(150),
        label = "chip_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) ElectricViolet.copy(alpha = 0.18f)
                else DeepElevatedSurface
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) ElectricViolet else DividerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) ElectricViolet else TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

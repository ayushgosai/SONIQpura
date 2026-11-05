package com.dwarkadhish.dwarkaspay.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletGradient
import com.dwarkadhish.dwarkaspay.ui.theme.HeroCardGradient
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.SuccessGreen
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

data class PremiumPlan(
    val id: String,
    val name: String,
    val price: String,
    val period: String,
    val trialBadge: String,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

@Composable
fun PremiumScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlanId by remember { mutableStateOf("individual") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val plans = listOf(
        PremiumPlan(
            id = "individual",
            name = "Individual",
            price = "₹119",
            period = "month after trial",
            trialBadge = "Free for 1 month",
            description = "1 Premium account for solo listening with full freedom.",
            features = listOf(
                "Ad-free music listening",
                "Download to listen offline",
                "High-fidelity 320 kbps audio",
                "Unlimited skips and reordering",
                "Listen together in real time"
            ),
            isPopular = true
        ),
        PremiumPlan(
            id = "duo",
            name = "Duo",
            price = "₹149",
            period = "month after trial",
            trialBadge = "Free for 1 month",
            description = "2 Premium accounts for couples or friends under one roof.",
            features = listOf(
                "2 separate Premium accounts",
                "Duo Mix: shared auto-generated playlist",
                "Ad-free music listening",
                "Download songs on both devices",
                "High-fidelity 320 kbps audio"
            )
        ),
        PremiumPlan(
            id = "family",
            name = "Family",
            price = "₹179",
            period = "month after trial",
            trialBadge = "Free for 1 month",
            description = "Up to 6 Premium accounts for family members living together.",
            features = listOf(
                "Up to 6 separate accounts",
                "Block explicit music filters",
                "Family Mix curated for everyone",
                "Ad-free music offline",
                "Separate playlists and favorites"
            )
        ),
        PremiumPlan(
            id = "student",
            name = "Student",
            price = "₹59",
            period = "month after trial",
            trialBadge = "Free for 1 month",
            description = "Special discount for verified university students.",
            features = listOf(
                "1 verified student account",
                "Ad-free high-fidelity sound",
                "Download to listen on campus offline",
                "Full catalog access",
                "Annual verification required"
            )
        )
    )

    if (showCheckoutDialog) {
        val selectedPlan = plans.find { it.id == selectedPlanId } ?: plans.first()
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            containerColor = DeepElevatedSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Viora ${selectedPlan.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "${selectedPlan.trialBadge}, then ${selectedPlan.price} / ${selectedPlan.period}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SoftLavender
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Google Play Billing integration is connected via server subscription entitlements. Cancel anytime in Google Play Store subscriptions settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCheckoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Subscribe with Play Store", color = MidnightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepElevatedSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = SoftLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }

        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(HeroCardGradient)
                    .padding(24.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightBlack.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIMITED TRIAL OFFER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                            color = SoftLavender
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Experience sound without limits.",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "3 months of Premium free. Stream over 100M songs with lossless 320 kbps fidelity and zero interruptions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavender
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    VioraGradientButton(
                        text = "Get 3 Months Free",
                        onClick = {
                            selectedPlanId = "individual"
                            showCheckoutDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Terms and conditions apply. Free for 3 months, then ₹119/month after. Cancel anytime.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Why Go Premium Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "WHY JOIN PREMIUM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 3.sp),
                    color = ElectricViolet
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumFeatureCard(
                        icon = Icons.Rounded.Block,
                        title = "Ad-free music",
                        subtitle = "Listen without stops",
                        modifier = Modifier.weight(1f)
                    )
                    PremiumFeatureCard(
                        icon = Icons.Rounded.Download,
                        title = "Download songs",
                        subtitle = "Listen anywhere offline",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumFeatureCard(
                        icon = Icons.Rounded.GraphicEq,
                        title = "320 kbps Lossless",
                        subtitle = "Studio master sound",
                        modifier = Modifier.weight(1f)
                    )
                    PremiumFeatureCard(
                        icon = Icons.Rounded.SkipNext,
                        title = "Unlimited skips",
                        subtitle = "Just press next",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Available Subscription Plans
        item {
            Text(
                text = "AVAILABLE PLANS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 3.sp),
                color = ElectricViolet,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Plan Cards
        items(plans.size) { index ->
            val plan = plans[index]
            PlanCard(
                plan = plan,
                onSelectPlan = {
                    selectedPlanId = plan.id
                    showCheckoutDialog = true
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DeepElevatedSurface)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ElectricViolet.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    onSelectPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (plan.isPopular) ElectricViolet else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DeepElevatedSurface)
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (plan.isPopular) ElectricViolet else CardSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = plan.trialBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (plan.isPopular) MidnightBlack else SoftLavender
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${plan.price} / ${plan.period}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = SoftLavender
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = plan.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        plan.features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        VioraGradientButton(
            text = "Get ${plan.name}",
            onClick = onSelectPlan
        )
    }
}

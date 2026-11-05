package com.dwarkadhish.dwarkaspay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepIndigo
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet

@Composable
fun VioraArtwork(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 0.dp,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val fallbackGradient = Brush.linearGradient(
        colors = listOf(
            ElectricViolet.copy(alpha = 0.6f),
            DeepIndigo.copy(alpha = 0.4f),
            CardSurfaceVariant
        )
    )

    Box(
        modifier = modifier
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
            .clip(shape)
            .background(fallbackGradient),
        contentAlignment = Alignment.Center
    ) {
        if (!artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

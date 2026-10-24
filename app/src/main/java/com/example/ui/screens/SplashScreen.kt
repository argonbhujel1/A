package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.SplashConfig
import com.example.ui.theme.*
import java.io.File

@Composable
fun BAMSplashScreen(
    countdown: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = rememberInfiniteTransition(label = "bam_pulse")
    val pulse by scaleAnim.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bam_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF050811),
                        Color(0xFF020408)
                    )
                )
            )
            .padding(24.dp)
            .testTag("bam_splash_screen")
    ) {
        // Skip Button top right
        Button(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp)
                .testTag("skip_bam_splash_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300D4FF)),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Skip (${countdown}s)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Center BAM Badge and Himalayan mountain logo
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF7C3AED),
                                Color(0xFF00E5FF)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Render uploaded BAM logo
                val context = LocalContext.current
                val localImageFile = File("/public/assets/aistudio/bam22_079953b9.png")
                if (localImageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(localImageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "BAM Studio Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback to bundled resource
                    Image(
                        painter = painterResource(id = R.drawable.bam_studio_logo),
                        contentDescription = "BAM Studio Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BAM DEVELOPERS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0x3300D4FF),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "BUILD • DESIGN • DEPLOY",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Technology Partner for Megha Hotel, Urlabari",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Credit
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Engineered by BAM Studio",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xCCFFFFFF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
            Text(
                text = "https://bamstudio.argan.com.np/",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun MegaHotelSplashScreen(
    splashConfig: SplashConfig,
    countdown: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = rememberInfiniteTransition(label = "megha_pulse")
    val pulse by scaleAnim.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "megha_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EmeraldPrimary,
                        EmeraldDark
                    )
                )
            )
            .padding(24.dp)
            .testTag("megha_splash_screen")
    ) {
        // Skip Button top right
        Button(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp)
                .testTag("skip_megha_splash_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x33FFFFFF)
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Skip (${countdown}s)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Center Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(Color(0x22D4AF37))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EmeraldDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (splashConfig.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(splashConfig.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Megha Hotel Logo",
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Megha Hotel",
                            tint = GoldAccent,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = splashConfig.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = splashConfig.subtitle,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = GoldLight,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Urlabari, Morang, Nepal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Credit
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = splashConfig.creditText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xCCFFFFFF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Technology by BAM Studio",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldAccent,
                    fontSize = 11.sp
                )
            )
        }
    }
}

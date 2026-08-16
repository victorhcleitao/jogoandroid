package com.example.settlementrpg.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Dicas de lore clássicas de RPG
val loadingTips = listOf(
    "Heróis descansam mais rápido perto de uma fogueira acesa.",
    "A Ferraria melhora o equipamento — e o equipamento melhora a sobrevivência.",
    "Nem todo contrato vale a pena aceitar sem ouro de sobra no cofre.",
    "Orcs só respeitam guildas de Rank D ou superior.",
    "Goblin Saqueadores são traiçoeiros — equipe seus heróis antes de caçá-los.",
    "O Mercador vende materiais do armazém automaticamente para gerar ouro passiva.",
    "Você pode descartar contratos ruins no quadro de missões para liberar espaço.",
    "Se cancelar um contrato antes dele ser aceito, você recebe 100% de reembolso."
)

// Carregador simples de bitmaps (seguro contra crash)
private fun loadAssetBitmap(context: Context, path: String): ImageBitmap? {
    return try {
        val inputStream = context.assets.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LoadingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val logoBmp = remember { loadAssetBitmap(context, "sprites/ambiente/logo_guildkeeper.png") }
    val campfireBmp = remember { loadAssetBitmap(context, "sprites/ambiente/campfire_1.png") }
    val currentTip = remember { loadingTips.random() }

    // Pulso animado para a chama/fogueira
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var screenAlpha by remember { mutableStateOf(1f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(durationMillis = 400),
        label = "fade"
    )

    // Efeito de delay com fade-out na saída
    LaunchedEffect(Unit) {
        delay(1400) // Duração ideal de percepção da marca
        screenAlpha = 0f
        delay(400) // Duração do fade-out
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(animatedAlpha)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF131B2B), Color(0xFF060709)),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // 🛡️ Logotipo centralizado
            if (logoBmp != null) {
                Image(
                    bitmap = logoBmp,
                    contentDescription = "Guildkeeper Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // 📜 Nome Serifado Dourado
            Text(
                text = "Guildkeeper",
                style = TextStyle(
                    color = Color(0xFFFFD54F), // GoldPrimary
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 🔥 Chama/Fogueira Pulsante
            if (campfireBmp != null) {
                Image(
                    bitmap = campfireBmp,
                    contentDescription = "Carregando...",
                    modifier = Modifier
                        .size(48.dp)
                        .scale(pulseScale)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 💡 Dica de RPG rotativa
            Text(
                text = "Dica: $currentTip",
                style = TextStyle(
                    color = Color(0xFFB0BEC5), // TextGray
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

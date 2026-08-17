package com.example.settlementrpg.ui.screens

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settlementrpg.data.model.*
import com.example.settlementrpg.theme.*
import kotlin.math.max

// --- DEFINIÇÕES DOS SPRITES EM PIXEL ART (FALLBACKS) ---

val warriorSprite = arrayOf(
    "..HHH...",
    "..HSH...",
    ".HHHHH..",
    "H.AAA.H.",
    "..AAA...",
    "..AAA...",
    ".L...L..",
    "L.....L."
)

val mageSprite = arrayOf(
    "...P....",
    "..PPP...",
    ".PPSP...",
    "P.PPP.P.",
    "..PPP...",
    "..PPP...",
    ".L...L..",
    "L.....L."
)

val archerSprite = arrayOf(
    "..GGG...",
    "..GSG...",
    ".GGGGG..",
    "G.CCC.G.",
    "..CCC...",
    "..CCC...",
    ".L...L..",
    "L.....L."
)

val clericSprite = arrayOf(
    "..WWW...",
    "..WSW...",
    ".WWWWW..",
    "W.BBB.W.",
    "..BBB...",
    "..BBB...",
    ".L...L..",
    "L.....L."
)

val slimeSprite = arrayOf(
    "....SS..",
    "..SSSSSS",
    ".SOSSOSS",
    "SSSSSSSS",
    "SSSSSSSS",
    "SSSSSSSS",
    ".SSSSSS.",
    "..SSSS.."
)

val wolfSprite = arrayOf(
    "W.....W.",
    "WW...WW.",
    "WWWEWWW.",
    "WWWWWWW.",
    ".WWWWW..",
    "..WWW...",
    "...W....",
    "........"
)

val goblinSprite = arrayOf(
    "G.....G.",
    "GG...GG.",
    "GGGEGGG.",
    "GGGGGGG.",
    ".GGGGG..",
    "..GGG...",
    "...G....",
    "........"
)

val graveSprite = arrayOf(
    "...GG...",
    "..GGGG..",
    ".GGGGGG.",
    "..GGGG..",
    "..GGGG..",
    "GGGGGGGG",
    "CCCCCCCC",
    "CCCCCCCC"
)

val treeSprite = arrayOf(
    "....TT....",
    "...TTTT...",
    "..TTTTTT..",
    ".TTTTTTTT.",
    "..TTTTTT..",
    "...BBBB...",
    "....BB....",
    "....BB...."
)

val rockSprite = arrayOf(
    "..RRR...",
    ".RRDRR..",
    "RRDDDRR.",
    "RRDDDDRR",
    ".RRRRRR."
)

val torchSprite = arrayOf(
    "...F...",
    "..FFF..",
    "..FFF..",
    "...S...",
    "...S...",
    "...S..."
)

// Dicionário global para guardar a largura do frame real após o trim
val spriteFrameWidths = mutableMapOf<ImageBitmap, Int>()

// Função utilitária de Trim Automático unificado para animações
fun trimSpriteSheet(src: Bitmap, frameWidth: Int, frameHeight: Int): Pair<Bitmap, Int> {
    val totalFrames = src.width / frameWidth
    var minX = frameWidth
    var minY = frameHeight
    var maxX = 0
    var maxY = 0
    
    // Alocar array de pixels para varredura rápida de RAM (evita getPixel em loop lento)
    val pixels = IntArray(src.width * src.height)
    src.getPixels(pixels, 0, src.width, 0, 0, src.width, src.height)
    
    // Achar os limites de pixels visíveis em todos os frames relativos
    for (f in 0 until totalFrames) {
        val frameOffset = f * frameWidth
        for (y in 0 until frameHeight) {
            for (x in 0 until frameWidth) {
                val pixelX = frameOffset + x
                val color = pixels[y * src.width + pixelX]
                val alpha = (color ushr 24) and 0xFF
                if (alpha > 5) { // Pixel visível
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
    }
    
    // Se não encontrou nenhum pixel visível (imagem vazia), retorna a original
    if (maxX < minX || maxY < minY) {
        return Pair(src, frameWidth)
    }
    
    // Calcular tamanho e offsets unificados
    val trimmedFrameW = maxX - minX + 1
    val trimmedFrameH = maxY - minY + 1
    
    // Criar um novo bitmap para o spritesheet recortado
    val destWidth = trimmedFrameW * totalFrames
    val destHeight = trimmedFrameH
    val destBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
    
    // Copiar cada frame recortado para o novo bitmap
    for (f in 0 until totalFrames) {
        val srcX = f * frameWidth + minX
        val srcY = minY
        val destX = f * trimmedFrameW
        val destY = 0
        
        // Copiar pixels do frame original para o destino
        for (y in 0 until trimmedFrameH) {
            for (x in 0 until trimmedFrameW) {
                val color = pixels[(srcY + y) * src.width + (srcX + x)]
                destBitmap.setPixel(destX + x, destY + y, color)
            }
        }
    }
    
    return Pair(destBitmap, trimmedFrameW)
}

// --- CARREGADOR DE BITMAPS DA PASTA ASSETS/ ---
fun loadImageFromAssets(context: Context, path: String): ImageBitmap? {
    return try {
        val inputStream = context.assets.open(path)
        val options = BitmapFactory.Options().apply {
            inScaled = false // Impede o Android de redimensionar a imagem com base na densidade da tela
        }
        val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null
        
        // Descobrir a largura de frame padrão original baseada no nome
        val defaultFrameW = when {
            path.contains("guerreiro", ignoreCase = true) -> 100
            path.contains("orc_", ignoreCase = true) -> 100
            else -> originalBitmap.width
        }
        val defaultFrameH = when {
            path.contains("guerreiro", ignoreCase = true) -> 100
            path.contains("orc_", ignoreCase = true) -> 100
            else -> originalBitmap.height
        }
        
        // Executar o trim automático unificado
        val (trimmedBitmap, finalFrameW) = trimSpriteSheet(originalBitmap, defaultFrameW, defaultFrameH)
        val imageBitmap = trimmedBitmap.asImageBitmap()
        
        // Registrar o frameWidth resultante no mapa para ser recuperado no desenho
        spriteFrameWidths[imageBitmap] = finalFrameW
        
        imageBitmap
    } catch (e: Exception) {
        null
    }
}

// --- AUXILIAR PARA DESENHAR PIXEL ART NO CANVAS ---
fun DrawScope.drawPixelSprite(
    sprite: Array<String>,
    colorMap: Map<Char, Color>,
    center: Offset,
    pixelSize: Float,
    flashActive: Boolean = false
) {
    val rows = sprite.size
    val cols = sprite[0].length
    val startX = center.x - (cols * pixelSize) / 2
    val startY = center.y - (rows * pixelSize) / 2
    
    val finalColorMap = if (flashActive) {
        colorMap.mapValues { Color(0xFFC62828) } // Flash vermelho sólido
    } else {
        colorMap
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val char = sprite[r][c]
            val color = finalColorMap[char]
            if (color != null) {
                drawRect(
                    color = color,
                    topLeft = Offset(startX + c * pixelSize, startY + r * pixelSize),
                    size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                )
            }
        }
    }
}

// Auxiliar de parsing de cores hex com segurança
fun parseColorHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.White
    }
}

// --- MATEMÁTICA DE PROJEÇÃO ISOMÉTRICA (2.5D) ---
fun toIsometric(x: Float, y: Float, centerX: Float, centerY: Float, scale: Float): Offset {
    val dx = x - 300f
    val dy = y - 300f
    val isoX = centerX + (dx - dy) * scale
    val isoY = centerY + (dx + dy) * 0.5f * scale
    return Offset(isoX, isoY)
}

// --- DESENHAR CAIXA ISOMÉTRICA PROCEDURAL (3D) ---
fun drawIsoBox(
    scope: DrawScope,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    leftColor: Color,
    rightColor: Color,
    topColor: Color
) {
    val leftPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy)
        lineTo(cx - w, cy - w * 0.5f)
        lineTo(cx - w, cy - h - w * 0.5f)
        lineTo(cx, cy - h)
        close()
    }
    val rightPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy)
        lineTo(cx + w, cy - w * 0.5f)
        lineTo(cx + w, cy - h - w * 0.5f)
        lineTo(cx, cy - h)
        close()
    }
    val topPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy - h)
        lineTo(cx - w, cy - h - w * 0.5f)
        lineTo(cx, cy - h - w)
        lineTo(cx + w, cy - h - w * 0.5f)
        close()
    }
    scope.drawPath(leftPath, leftColor)
    scope.drawPath(rightPath, rightColor)
    scope.drawPath(topPath, topColor)
}

// --- DESENHAR PIRÂMIDE/TELHADO ISOMÉTRICO (3D) ---
fun drawIsoPyramid(
    scope: DrawScope,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    leftColor: Color,
    rightColor: Color
) {
    val leftPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy - h)
        lineTo(cx - w, cy - w * 0.5f)
        lineTo(cx, cy)
        close()
    }
    val rightPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy - h)
        lineTo(cx + w, cy - w * 0.5f)
        lineTo(cx, cy)
        close()
    }
    scope.drawPath(leftPath, leftColor)
    scope.drawPath(rightPath, rightColor)
}

// --- DESENHAR GUILDA CENTRAL EVOLUTIVA ---
fun drawIsoCastle(scope: DrawScope, centerX: Float, centerY: Float, scale: Float, level: Int) {
    val stoneLeft = Color(0xFF4A515E)
    val stoneRight = Color(0xFF6C7787)
    val stoneTop = Color(0xFF8B98A8)
    val goldRoof = Color(0xFFFFD54F)
    val goldRoofLeft = Color(0xFFFFB300)
    
    val woodLeft = Color(0xFF5D4037)
    val woodRight = Color(0xFF8D6E63)
    val woodTop = Color(0xFFA1887F)
    val strawRoof = Color(0xFFC2B280)
    val strawRoofLeft = Color(0xFF9E8E63)

    if (level == 0) {
        // Nível 0: Fogueira (Campfire)
        val mainPos = toIsometric(300f, 300f, centerX, centerY, scale)
        
        // Círculo de pedras cinzas
        scope.drawOval(
            color = Color(0xFF757575),
            topLeft = Offset(mainPos.x - 10f * scale, mainPos.y - 5f * scale),
            size = androidx.compose.ui.geometry.Size(20f * scale, 10f * scale)
        )
        // Troncos de madeira marrom cruzados
        scope.drawLine(
            color = Color(0xFF5D4037),
            start = Offset(mainPos.x - 7f * scale, mainPos.y - 2f * scale),
            end = Offset(mainPos.x + 7f * scale, mainPos.y + 2f * scale),
            strokeWidth = 2.5f * scale
        )
        scope.drawLine(
            color = Color(0xFF4E342E),
            start = Offset(mainPos.x + 7f * scale, mainPos.y - 2f * scale),
            end = Offset(mainPos.x - 7f * scale, mainPos.y + 2f * scale),
            strokeWidth = 2.5f * scale
        )
        // Chamas de fogo vermelhas/laranjas
        scope.drawCircle(
            color = Color(0xFFFF3D00),
            radius = 4f * scale,
            center = Offset(mainPos.x, mainPos.y - 3f * scale)
        )
        scope.drawCircle(
            color = Color(0xFFFFC400),
            radius = 2.5f * scale,
            center = Offset(mainPos.x, mainPos.y - 5f * scale)
        )
    } else if (level == 1) {
        // Nível 1: Casebre rústico de madeira (Tenda)
        val mainPos = toIsometric(300f, 300f, centerX, centerY, scale)
        drawIsoBox(scope, mainPos.x, mainPos.y, 16f * scale, 22f * scale, woodLeft, woodRight, woodTop)
        drawIsoPyramid(scope, mainPos.x, mainPos.y - 22f * scale, 16f * scale, 12f * scale, strawRoofLeft, strawRoof)
    } else if (level == 2) {
        // Nível 2: Fortaleza básica de pedra
        val mainPos = toIsometric(300f, 300f, centerX, centerY, scale)
        drawIsoBox(scope, mainPos.x, mainPos.y, 20f * scale, 35f * scale, stoneLeft, stoneRight, stoneTop)
        
        val leftPos = toIsometric(275f, 325f, centerX, centerY, scale)
        drawIsoBox(scope, leftPos.x, leftPos.y, 14f * scale, 45f * scale, stoneLeft, stoneRight, stoneTop)
        drawIsoPyramid(scope, leftPos.x, leftPos.y - 45f * scale, 14f * scale, 15f * scale, goldRoofLeft, goldRoof)
    } else {
        // Nível 3+: Castelo de 3 torres completo
        val rearPos = toIsometric(275f, 275f, centerX, centerY, scale)
        drawIsoBox(scope, rearPos.x, rearPos.y, 14f * scale, 65f * scale, stoneLeft, stoneRight, stoneTop)
        drawIsoPyramid(scope, rearPos.x, rearPos.y - 65f * scale, 14f * scale, 20f * scale, goldRoofLeft, goldRoof)
        
        val rightPos = toIsometric(325f, 275f, centerX, centerY, scale)
        drawIsoBox(scope, rightPos.x, rightPos.y, 16f * scale, 55f * scale, stoneLeft, stoneRight, stoneTop)
        drawIsoPyramid(scope, rightPos.x, rightPos.y - 55f * scale, 16f * scale, 18f * scale, goldRoofLeft, goldRoof)

        val leftPos = toIsometric(275f, 325f, centerX, centerY, scale)
        drawIsoBox(scope, leftPos.x, leftPos.y, 16f * scale, 55f * scale, stoneLeft, stoneRight, stoneTop)
        drawIsoPyramid(scope, leftPos.x, leftPos.y - 55f * scale, 16f * scale, 18f * scale, goldRoofLeft, goldRoof)

        val mainPos = toIsometric(300f, 300f, centerX, centerY, scale)
        drawIsoBox(scope, mainPos.x, mainPos.y, 25f * scale, 45f * scale, stoneLeft, stoneRight, stoneTop)
        
        val doorPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(mainPos.x + 4f * scale, mainPos.y - 2f * scale)
            lineTo(mainPos.x + 12f * scale, mainPos.y - 6f * scale)
            lineTo(mainPos.x + 12f * scale, mainPos.y - 22f * scale)
            lineTo(mainPos.x + 4f * scale, mainPos.y - 18f * scale)
            close()
        }
        scope.drawPath(doorPath, Color(0xFF1E1E24))
    }
}

// --- DESENHAR FERRARIA PROCEDURAL ---
fun drawIsoBlacksmith(scope: DrawScope, centerX: Float, centerY: Float, scale: Float) {
    val stoneLeft = Color(0xFF3E2723) // Tijolos escuros/terra
    val stoneRight = Color(0xFF4E342E)
    val stoneTop = Color(0xFF5D4037)
    val roofLeft = Color(0xFF212121)
    val roofRight = Color(0xFF37474F)

    val pos = toIsometric(250f, 330f, centerX, centerY, scale)
    drawIsoBox(scope, pos.x, pos.y, 15f * scale, 24f * scale, stoneLeft, stoneRight, stoneTop)
    drawIsoPyramid(scope, pos.x, pos.y - 24f * scale, 15f * scale, 10f * scale, roofLeft, roofRight)
    
    // Pequena chaminé
    val chimPos = toIsometric(242f, 322f, centerX, centerY, scale)
    drawIsoBox(scope, chimPos.x, chimPos.y - 24f * scale, 4f * scale, 12f * scale, Color.Black, Color(0xFF212121), Color.Gray)
}

// --- DESENHAR TABERNA PROCEDURAL ---
fun drawIsoTavern(scope: DrawScope, centerX: Float, centerY: Float, scale: Float) {
    val pos = toIsometric(350f, 270f, centerX, centerY, scale)
    drawIsoBox(scope, pos.x, pos.y, 16f * scale, 22f * scale, Color(0xFF3E2723), Color(0xFF5D4037), Color(0xFF6D4C41))
    drawIsoPyramid(scope, pos.x, pos.y - 22f * scale, 16f * scale, 12f * scale, Color(0xFFB71C1C), Color(0xFFE53935))
}

// --- DESENHAR MERCADOR PROCEDURAL ---
fun drawIsoMerchant(scope: DrawScope, centerX: Float, centerY: Float, scale: Float, level: Int) {
    val woodLeft = Color(0xFF5D4037)
    val woodRight = Color(0xFF6D4C41)
    val woodTop = Color(0xFF8D6E63)
    val tentLeft = Color(0xFF0D47A1)
    val tentRight = Color(0xFF1976D2)
    
    val pos = toIsometric(350f, 330f, centerX, centerY, scale)
    
    // Caixa base (balcão de madeira)
    drawIsoBox(scope, pos.x, pos.y, 14f * scale, 16f * scale, woodLeft, woodRight, woodTop)
    
    // Toldo / cobertura da tenda (pirâmide azul acima do balcão)
    drawIsoPyramid(scope, pos.x, pos.y - 16f * scale, 15f * scale, 12f * scale, tentLeft, tentRight)
    
    // Moeda dourada flutuante
    val coinY = pos.y - 32f * scale
    scope.drawCircle(
        color = Color(0xFFFFD54F),
        radius = 2.5f * scale,
        center = Offset(pos.x, coinY)
    )
}

// --- DESENHAR BITMAP SLICE COM TRIMMING AUTOMÁTICO E ALTURA DE REFERÊNCIA ---
fun DrawScope.drawIsoSpriteBitmap(
    bitmap: ImageBitmap,
    center: Offset,
    scale: Float,
    refHeight: Float, // Altura de referência do personagem/objeto em tela (a zoom 1.0x)
    animIndex: Int = 0,
    flashActive: Boolean = false
): Boolean {
    return try {
        // Recuperar a largura real do frame pós-trimming (se cadastrado) ou assumir a do bitmap
        val fw = spriteFrameWidths[bitmap] ?: bitmap.width
        val fh = bitmap.height
        
        val totalFrames = max(1, bitmap.width / fw)
        val currentFrame = animIndex % totalFrames
        
        // Calcular largura proporcional à altura de referência desejada
        val destH = refHeight * scale
        val destW = destH * (fw.toFloat() / fh.toFloat())
        
        val dstX = center.x - destW / 2
        val dstY = center.y - destH / 2
        
        val colorFilter = if (flashActive) {
            androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFC62828), androidx.compose.ui.graphics.BlendMode.SrcAtop)
        } else {
            null
        }
        
        drawImage(
            image = bitmap,
            srcOffset = androidx.compose.ui.unit.IntOffset(currentFrame * fw, 0),
            srcSize = androidx.compose.ui.unit.IntSize(fw, fh),
            dstOffset = androidx.compose.ui.unit.IntOffset(dstX.toInt(), dstY.toInt()),
            dstSize = androidx.compose.ui.unit.IntSize(destW.toInt(), destH.toInt()),
            filterQuality = androidx.compose.ui.graphics.FilterQuality.None,
            colorFilter = colorFilter
        )
        true
    } catch (e: Exception) {
        false
    }
}

// --- RENDERIZADORES COM FALLBACK SE BITMAP NULO ---
fun DrawScope.drawHeroSprite(
    hero: Hero,
    center: Offset,
    scale: Float,
    animIndex: Int,
    warriorIdle: ImageBitmap?,
    warriorWalk: ImageBitmap?,
    warriorAttack: ImageBitmap?,
    warriorDeath: ImageBitmap?
) {
    if (hero.heroClass == HeroClass.WARRIOR) {
        val bitmap = when (hero.state) {
            HeroState.COMBAT -> warriorAttack
            HeroState.WALKING_TO_MONSTER, HeroState.WALKING_TO_GUILD -> warriorWalk
            else -> warriorIdle
        }
        if (bitmap != null) {
            val success = drawIsoSpriteBitmap(
                bitmap = bitmap,
                center = center,
                scale = scale,
                refHeight = 48f,
                animIndex = animIndex,
                flashActive = hero.flashTicks > 0
            )
            if (success) return
        }
    }
    
    val (sprite, colorMap) = when (hero.heroClass) {
        HeroClass.WARRIOR -> Pair(
            warriorSprite,
            mapOf('H' to Color(0xFFB0BEC5), 'S' to Color(0xFFFFD54F), 'A' to Color(0xFFE65100), 'L' to Color(0xFF4E342E))
        )
        HeroClass.MAGE -> Pair(
            mageSprite,
            mapOf('P' to Color(0xFF9C27B0), 'S' to Color(0xFFFFD54F), 'L' to Color(0xFF4E342E))
        )
        HeroClass.ARCHER -> Pair(
            archerSprite,
            mapOf('G' to Color(0xFF2E7D32), 'S' to Color(0xFFFFD54F), 'C' to Color(0xFF4CAF50), 'L' to Color(0xFF4E342E))
        )
        HeroClass.CLERIG -> Pair(
            clericSprite,
            mapOf('W' to Color(0xFFECEFF1), 'S' to Color(0xFFFFD54F), 'B' to Color(0xFF039BE5), 'L' to Color(0xFF4E342E))
        )
    }
    
    drawPixelSprite(
        sprite = sprite,
        colorMap = colorMap,
        center = center,
        pixelSize = 4.0f * scale,
        flashActive = hero.flashTicks > 0
    )
}

fun DrawScope.drawMonsterSprite(
    monster: Monster,
    center: Offset,
    scale: Float,
    animIndex: Int,
    orcIdle: ImageBitmap?,
    orcWalk: ImageBitmap?,
    orcAttack: ImageBitmap?,
    orcDeath: ImageBitmap?,
    orcIdleNew: ImageBitmap?,
    slimeIdle: ImageBitmap?,
    slimeDeath: ImageBitmap?,
    wolfIdle: ImageBitmap?,
    wolfDeath: ImageBitmap?,
    goblinIdle: ImageBitmap?,
    goblinDeath: ImageBitmap?,
    newTree: ImageBitmap?,
    newStone: ImageBitmap?
) {
    val name = monster.name
    val isDead = monster.isDead
    
    val bitmap: ImageBitmap? = when {
        name.startsWith("Orc") -> {
            val baseIdle = orcIdleNew ?: orcIdle
            when {
                isDead -> orcDeath
                monster.hp < monster.maxHp * 0.9f && animIndex % 2 == 0 -> orcAttack
                else -> baseIdle
            }
        }
        name.startsWith("Slime") -> {
            if (isDead) slimeDeath else slimeIdle
        }
        name.startsWith("Lobo") -> {
            if (isDead) wolfDeath else wolfIdle
        }
        name.startsWith("Goblin") -> {
            if (isDead) goblinDeath else goblinIdle
        }
        name.startsWith("Coleta de Madeira") -> {
            if (isDead) null else newTree
        }
        name.startsWith("Coleta de Pedra") -> {
            if (isDead) null else newStone
        }
        name.startsWith("Coleta de Ervas") -> {
            if (isDead) null else newTree
        }
        name.startsWith("Coleta de Ferro") -> {
            if (isDead) null else newStone
        }
        else -> null
    }

    if (bitmap != null) {
        val refH = when {
            name.startsWith("Orc") -> 52f
            name.startsWith("Slime") -> 28f
            name.startsWith("Lobo") -> 44f
            name.startsWith("Goblin") -> 40f
            name.startsWith("Coleta de Madeira") -> 70f
            name.startsWith("Coleta de Pedra") -> 50f
            name.startsWith("Coleta de Ervas") -> 38f
            name.startsWith("Coleta de Ferro") -> 52f
            else -> 48f
        }
        val success = drawIsoSpriteBitmap(
            bitmap = bitmap,
            center = center,
            scale = scale,
            refHeight = refH,
            animIndex = if (name.startsWith("Orc")) animIndex else 0,
            flashActive = monster.flashTicks > 0
        )
        if (success) return
    }

    if (!monster.isDead) {
        val (sprite, colorMap) = when {
            monster.name.startsWith("Slime") -> Pair(
                slimeSprite,
                mapOf('S' to Color(0xFF558B2F), 'O' to Color.Black)
            )
            monster.name.startsWith("Lobo") -> Pair(
                wolfSprite,
                mapOf('W' to Color(0xFF78909C), 'E' to Color.Red)
            )
            else -> Pair(
                goblinSprite,
                mapOf('G' to Color(0xFF689F38), 'E' to Color.Red)
            )
        }
        drawPixelSprite(
            sprite = sprite,
            colorMap = colorMap,
            center = center,
            pixelSize = 4f * scale,
            flashActive = monster.flashTicks > 0
        )
    } else {
        val graveColorMap = mapOf(
            'G' to Color(0xFF607D8B),
            'C' to Color(0xFF263238)
        )
        drawPixelSprite(
            sprite = graveSprite,
            colorMap = graveColorMap,
            center = center,
            pixelSize = 3f * scale
        )
    }
}

// --- ESTRUTURA PARA ORDENAMENTO POR PROFUNDIDADE (Z-INDEX) ---
sealed class IsoDrawable(val x: Float, val y: Float) {
    val depth: Float get() = x + y

    class GuildCastle : IsoDrawable(300f, 300f)
    
    class BlacksmithItem : IsoDrawable(250f, 330f)
    
    class TavernItem : IsoDrawable(350f, 270f)
    
    class MerchantItem : IsoDrawable(350f, 330f)

    class DecorItem(val decorX: Float, val decorY: Float, val type: String) : IsoDrawable(decorX, decorY)

    class HeroItem(val hero: Hero, visualX: Float, visualY: Float) : IsoDrawable(visualX, visualY)

    class MonsterItem(val monster: Monster) : IsoDrawable(monster.x, monster.y)
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun MapScreen(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    // --- ESTADOS MUTÁVEIS PARA ZOOM E PAN ---
    var zoom by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "guild_halo")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.7f,
        targetValue = 2.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    val frameFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame_index"
    )
    val frameIndex = frameFloat.toInt()

    // Elementos decorativos (apenas árvores e rochas - tochas são geradas dinamicamente!)
    val decors = remember {
        listOf(
            Triple(50f, 50f, "tree"), Triple(70f, 90f, "tree"), Triple(120f, 60f, "tree"),
            Triple(510f, 80f, "tree"), Triple(540f, 120f, "tree"), Triple(110f, 490f, "tree"),
            Triple(80f, 520f, "tree"), Triple(500f, 520f, "tree"), Triple(530f, 480f, "tree"),
            Triple(80f, 250f, "tree"), Triple(530f, 280f, "tree"), Triple(220f, 520f, "tree"),
            Triple(380f, 60f, "tree"),
            
            Triple(120f, 320f, "rock"), Triple(140f, 350f, "rock"), Triple(460f, 340f, "rock"),
            Triple(480f, 310f, "rock"), Triple(210f, 130f, "rock"), Triple(390f, 490f, "rock"),
            Triple(170f, 220f, "rock"), Triple(420f, 220f, "rock"), Triple(240f, 420f, "rock"),
            Triple(340f, 160f, "rock")
        )
    }

    // --- ENGENHA DE INTERPOLAÇÃO 60FPS DE MOVIMENTO ---
    val lastTickTime = gameState.lastTickTime
    val timeMillis by produceState(initialValue = System.currentTimeMillis(), keys = arrayOf(lastTickTime)) {
        while (true) {
            withFrameMillis {
                value = System.currentTimeMillis()
            }
        }
    }
    val elapsed = timeMillis - lastTickTime
    val fraction = (elapsed.toFloat() / 1000f).coerceIn(0f, 1f)

    val context = androidx.compose.ui.platform.LocalContext.current
    val warriorIdle = remember { loadImageFromAssets(context, "sprites/herois/guerreiro_idle.png") }
    val warriorWalk = remember { loadImageFromAssets(context, "sprites/herois/guerreiro_walk.png") }
    val warriorAttack = remember { loadImageFromAssets(context, "sprites/herois/guerreiro_attack.png") }
    val warriorDeath = remember { loadImageFromAssets(context, "sprites/herois/guerreiro_death.png") }
    
    val orcIdle = remember { loadImageFromAssets(context, "sprites/monstros/orc_idle.png") }
    val orcWalk = remember { loadImageFromAssets(context, "sprites/monstros/orc_walk.png") }
    val orcAttack = remember { loadImageFromAssets(context, "sprites/monstros/orc_attack.png") }
    val orcDeath = remember { loadImageFromAssets(context, "sprites/monstros/orc_death.png") }
    val orcIdleNew = remember { loadImageFromAssets(context, "sprites/monstros/orc_idle_new.png") }

    val slimeIdle = remember { loadImageFromAssets(context, "sprites/monstros/slime_idle.png") }
    val slimeDeath = remember { loadImageFromAssets(context, "sprites/monstros/slime_death.png") }
    val wolfIdle = remember { loadImageFromAssets(context, "sprites/monstros/wolf_idle.png") }
    val wolfDeath = remember { loadImageFromAssets(context, "sprites/monstros/wolf_death.png") }
    val goblinIdle = remember { loadImageFromAssets(context, "sprites/monstros/goblin_idle.png") }
    val goblinDeath = remember { loadImageFromAssets(context, "sprites/monstros/goblin_death.png") }
    
    val campfire1 = remember { loadImageFromAssets(context, "sprites/ambiente/campfire_1.png") }
    val campfire2 = remember { loadImageFromAssets(context, "sprites/ambiente/campfire_2.png") }
    
    val stone1 = remember { loadImageFromAssets(context, "sprites/ambiente/stone_1.png") }
    val stone2 = remember { loadImageFromAssets(context, "sprites/ambiente/stone_2.png") }
    val newStone = remember { loadImageFromAssets(context, "sprites/ambiente/stone_1_new.png") }
    val newTree = remember { loadImageFromAssets(context, "sprites/ambiente/tree_1.png") }
    val newMerchant = remember { loadImageFromAssets(context, "sprites/ambiente/merchant_new.png") }
    val newBlacksmith = remember { loadImageFromAssets(context, "sprites/ambiente/blacksmith_new.png") }

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GoldDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fronteira Isométrica",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Zoom: ${String.format("%.1fx", zoom)} | Caçada: ${gameState.heroes.count { it.state != HeroState.RESTING && it.state != HeroState.IDLE }}/${gameState.heroes.size}",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            // Box com suporte a Gesto de Pinça e Arrasto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                zoom = 1f
                                pan = Offset.Zero
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, panAmount, zoomAmount, _ ->
                            zoom = (zoom * zoomAmount).coerceIn(0.6f, 2.2f)
                            pan = pan + panAmount
                        }
                    }
            ) {
                val textMeasurer = rememberTextMeasurer()
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    val scale = canvasWidth / 740f
                    
                    val guildX = canvasWidth / 2
                    val guildY = canvasHeight / 2

                    // 1. Fundo com Gradiente Radial (Desenhado fora do transform para preencher sempre toda a tela)
                    val bgBrush = Brush.radialGradient(
                        colors = listOf(Color(0xFF131B2B), Color(0xFF060709)),
                        center = Offset(guildX, guildY),
                        radius = 450f * scale
                    )
                    drawRect(brush = bgBrush, size = size)

                    // Aplicar transformações globais no Canvas baseadas no Zoom e Pan
                    withTransform({
                        translate(pan.x, pan.y)
                        scale(zoom, zoom, pivot = Offset(guildX, guildY))
                    }) {

                        // 2. Terreno
                        for (gx in 0 until 10) {
                            for (gy in 0 until 10) {
                                val hash = (gx * 31 + gy * 17)
                                val tileColor = when (hash % 4) {
                                    0 -> Color(0xFF0D121D)
                                    1 -> Color(0xFF0F1524)
                                    2 -> Color(0xFF131B2A)
                                    else -> Color(0xFF111722)
                                }
                                val p1 = toIsometric(gx * 60f, gy * 60f, guildX, guildY, scale)
                                val p2 = toIsometric((gx + 1) * 60f, gy * 60f, guildX, guildY, scale)
                                val p3 = toIsometric((gx + 1) * 60f, (gy + 1) * 60f, guildX, guildY, scale)
                                val p4 = toIsometric(gx * 60f, (gy + 1) * 60f, guildX, guildY, scale)
                                
                                val tilePath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(p1.x, p1.y)
                                    lineTo(p2.x, p2.y)
                                    lineTo(p3.x, p3.y)
                                    lineTo(p4.x, p4.y)
                                    close()
                                }
                                drawPath(tilePath, tileColor)
                            }
                        }
                        
                        // 3. Trilhas de Terra Conectando Dinamicamente aos Spawns Aleatórios
                        val trailColor = Color(0xFF1E1712)
                        val trailWidth = 14f * scale
                        
                        fun drawTrailLine(startX: Float, startY: Float, endX: Float, endY: Float) {
                            val pStart = toIsometric(startX, startY, guildX, guildY, scale)
                            val pEnd = toIsometric(endX, endY, guildX, guildY, scale)
                            drawLine(
                                color = trailColor,
                                start = pStart,
                                end = pEnd,
                                strokeWidth = trailWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }

                        gameState.monsters.forEach { monster ->
                            drawTrailLine(300f, 300f, monster.spawnX, monster.spawnY)
                        }

                        // 4. Grade de Terreno
                        val gridSpacing = 60f
                        for (gx in 0..10) {
                            val xVal = gx * gridSpacing
                            val pStart = toIsometric(xVal, 0f, guildX, guildY, scale)
                            val pEnd = toIsometric(xVal, 600f, guildX, guildY, scale)
                            drawLine(color = Color(0x08FFFFFF), start = pStart, end = pEnd, strokeWidth = 1f)
                        }
                        for (gy in 0..10) {
                            val yVal = gy * gridSpacing
                            val pStart = toIsometric(0f, yVal, guildX, guildY, scale)
                            val pEnd = toIsometric(600f, yVal, guildX, guildY, scale)
                            drawLine(color = Color(0x08FFFFFF), start = pStart, end = pEnd, strokeWidth = 1f)
                        }

                        // 5. Halo de Luz Dourado da Guilda
                        val guildRadius = 35f * scale
                        drawOval(
                            color = GoldPrimary.copy(alpha = haloAlpha),
                            topLeft = Offset(guildX - guildRadius * haloScale, guildY - guildRadius * haloScale * 0.5f),
                            size = androidx.compose.ui.geometry.Size(guildRadius * haloScale * 2, guildRadius * haloScale)
                        )

                        // 6. Rotas de Movimento
                        gameState.heroes.forEach { hero ->
                            if (hero.state == HeroState.WALKING_TO_MONSTER || hero.state == HeroState.WALKING_TO_GUILD) {
                                val pathColor = if (hero.state == HeroState.WALKING_TO_MONSTER) Color(0x35C62828) else Color(0x353B789E)
                                val visualX = hero.prevX + (hero.x - hero.prevX) * fraction
                                val visualY = hero.prevY + (hero.y - hero.prevY) * fraction
                                val pStart = toIsometric(visualX, visualY, guildX, guildY, scale)
                                val pEnd = toIsometric(hero.targetX, hero.targetY, guildX, guildY, scale)
                                drawLine(
                                    color = pathColor,
                                    start = pStart,
                                    end = pEnd,
                                    strokeWidth = 2f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }
                        }

                        // 7. Coleta e Sorting (Z-Sorting) dos Elementos
                        val drawables = mutableListOf<IsoDrawable>()
                        
                        // Prédio da Guilda central
                        drawables.add(IsoDrawable.GuildCastle())
                        
                        // Prédios modulares se construídos
                        val guildLvl = gameState.buildings.find { it.id == "guild" }?.level ?: 1
                        val blacksmithLvl = gameState.buildings.find { it.id == "blacksmith" }?.level ?: 0
                        val tavernLvl = gameState.buildings.find { it.id == "tavern" }?.level ?: 0
                        val merchantLvl = gameState.buildings.find { it.id == "merchant" }?.level ?: 0
                        
                        if (blacksmithLvl > 0) drawables.add(IsoDrawable.BlacksmithItem())
                        if (tavernLvl > 0) drawables.add(IsoDrawable.TavernItem())
                        if (merchantLvl > 0) drawables.add(IsoDrawable.MerchantItem())
                        
                        // Elementos decorativos (árvores e rochas)
                        val activeDecors = decors.toMutableList()
                        
                        // Gerar e adicionar tochas flanking dinamicamente nas entradas (Guilda + Monstros ativos)
                        // Flanquear porta da guilda
                        activeDecors.add(Triple(315f, 325f, "torch"))
                        activeDecors.add(Triple(325f, 315f, "torch"))
                        
                        // Flanquear spawns dos monstros ativos
                        gameState.monsters.forEach { monster ->
                            if (!monster.isDead) {
                                val dx = monster.spawnX - 300f
                                val dy = monster.spawnY - 300f
                                val dist = Math.sqrt((dx * dx + dy * dy).toDouble())
                                if (dist > 0) {
                                    val spawnAngle = Math.atan2(dy.toDouble(), dx.toDouble())
                                    val perpAngle = spawnAngle + Math.PI / 2
                                    val offsetDist = 22f
                                    
                                    val t1x = monster.spawnX + offsetDist * kotlin.math.cos(perpAngle).toFloat()
                                    val t1y = monster.spawnY + offsetDist * kotlin.math.sin(perpAngle).toFloat()
                                    val t2x = monster.spawnX - offsetDist * kotlin.math.cos(perpAngle).toFloat()
                                    val t2y = monster.spawnY - offsetDist * kotlin.math.sin(perpAngle).toFloat()
                                    
                                    activeDecors.add(Triple(t1x, t1y, "torch"))
                                    activeDecors.add(Triple(t2x, t2y, "torch"))
                                }
                            }
                        }

                        activeDecors.forEach { drawables.add(IsoDrawable.DecorItem(it.first, it.second, it.third)) }
                        gameState.monsters.forEach { drawables.add(IsoDrawable.MonsterItem(it)) }
                        
                        // Heróis com Z-sorting offset no pátio da guilda
                        gameState.heroes.forEach { hero ->
                            val visualX = hero.prevX + (hero.x - hero.prevX) * fraction
                            val visualY = hero.prevY + (hero.y - hero.prevY) * fraction
                            
                            val (scatterX, scatterY) = if (hero.x == 300f && hero.y == 300f) {
                                val restingHeroes = gameState.heroes.filter { it.x == 300f && it.y == 300f }
                                val index = restingHeroes.indexOfFirst { it.id == hero.id }
                                when (index % 4) {
                                    0 -> Pair(-28f, -28f)
                                    1 -> Pair(28f, -28f)
                                    2 -> Pair(-28f, 28f)
                                    else -> Pair(28f, 28f)
                                }
                            } else {
                                Pair(0f, 0f)
                            }
                            
                            drawables.add(IsoDrawable.HeroItem(hero, visualX + scatterX, visualY + scatterY))
                        }
                        
                        drawables.sortBy { it.depth }

                        val occupiedLabels = mutableListOf<androidx.compose.ui.geometry.Rect>()
                        
                        fun adjustForCollisions(initialRect: androidx.compose.ui.geometry.Rect): androidx.compose.ui.geometry.Rect {
                            var current = initialRect
                            var collided = true
                            var attempts = 0
                            while (collided && attempts < 15) {
                                collided = false
                                for (occupied in occupiedLabels) {
                                    if (current.left < occupied.right && current.right > occupied.left &&
                                        current.top < occupied.bottom && current.bottom > occupied.top) {
                                        // Colisão detectada! Desloca verticalmente para cima
                                        val shiftY = occupied.top - current.bottom - 4f * scale
                                        current = current.translate(0f, shiftY)
                                        collided = true
                                        break
                                    }
                                }
                                attempts++
                            }
                            return current
                        }

                        // 8. Renderização na Ordem Z Correta
                        drawables.forEach { drawable ->
                            val isoPos = toIsometric(drawable.x, drawable.y, guildX, guildY, scale)
                            
                            when (drawable) {
                                is IsoDrawable.GuildCastle -> {
                                    drawIsoCastle(this, guildX, guildY, scale, guildLvl)
                                    
                                    val guildLayout = textMeasurer.measure(
                                        text = "Guilda Lvl $guildLvl",
                                        style = TextStyle(color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        softWrap = false
                                    )
                                    val guildWidth = guildLayout.size.width.toFloat()
                                    val guildHeight = guildLayout.size.height.toFloat()
                                    val textY = when (guildLvl) {
                                        1 -> guildY - 45f * scale
                                        2 -> guildY - 65f * scale
                                        else -> guildY - 85f * scale
                                    }
                                    
                                    val guildLabelWidth = guildWidth + 12f * scale
                                    val guildLabelHeight = guildHeight + 6f * scale
                                    val guildLabelX = guildX - guildWidth / 2 - 6f * scale
                                    val guildLabelY = textY - 3f * scale
                                    
                                    // Castelo é desenhado e seu label é registrado no occupiedLabels
                                    val guildRect = androidx.compose.ui.geometry.Rect(
                                        guildLabelX,
                                        guildLabelY,
                                        guildLabelX + guildLabelWidth,
                                        guildLabelY + guildLabelHeight
                                    )
                                    occupiedLabels.add(guildRect)
                                    
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        topLeft = Offset(guildLabelX, guildLabelY),
                                        size = androidx.compose.ui.geometry.Size(guildLabelWidth, guildLabelHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
                                    )
                                    
                                    drawText(
                                        textLayoutResult = guildLayout,
                                        topLeft = Offset(guildX - guildWidth / 2, textY)
                                    )
                                }
                                is IsoDrawable.BlacksmithItem -> {
                                    if (newBlacksmith != null) {
                                        drawIsoSpriteBitmap(
                                            bitmap = newBlacksmith,
                                            center = Offset(isoPos.x, isoPos.y - 10f * scale),
                                            scale = scale,
                                            refHeight = 75f,
                                            animIndex = 0
                                        )
                                    } else {
                                        drawIsoBlacksmith(this, guildX, guildY, scale)
                                    }
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = "Ferraria Lvl $blacksmithLvl",
                                        style = TextStyle(color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                        topLeft = Offset(isoPos.x - 22f * scale, isoPos.y + 4f * scale)
                                    )
                                }
                                is IsoDrawable.TavernItem -> {
                                    drawIsoTavern(this, guildX, guildY, scale)
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = "Taberna Lvl $tavernLvl",
                                        style = TextStyle(color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                        topLeft = Offset(isoPos.x - 22f * scale, isoPos.y + 4f * scale)
                                    )
                                }
                                is IsoDrawable.MerchantItem -> {
                                    if (newMerchant != null) {
                                        drawIsoSpriteBitmap(
                                            bitmap = newMerchant,
                                            center = Offset(isoPos.x, isoPos.y - 8f * scale),
                                            scale = scale,
                                            refHeight = 70f,
                                            animIndex = 0
                                        )
                                    } else {
                                        drawIsoMerchant(this, guildX, guildY, scale, merchantLvl)
                                    }
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = "Mercador Lvl $merchantLvl",
                                        style = TextStyle(color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                        topLeft = Offset(isoPos.x - 22f * scale, isoPos.y + 4f * scale)
                                    )
                                }
                                is IsoDrawable.DecorItem -> {
                                    when (drawable.type) {
                                        "tree" -> {
                                            drawOval(
                                                color = Color(0x28000000),
                                                topLeft = Offset(isoPos.x - 12f * scale, isoPos.y - 5f * scale),
                                                size = androidx.compose.ui.geometry.Size(24f * scale, 10f * scale)
                                            )
                                            if (newTree != null) {
                                                drawIsoSpriteBitmap(
                                                    bitmap = newTree,
                                                    center = Offset(isoPos.x, isoPos.y - 20f * scale),
                                                    scale = scale,
                                                    refHeight = 56f,
                                                    animIndex = 0
                                                )
                                            } else {
                                                val treeColors = mapOf('T' to Color(0xFF283626), 'B' to Color(0xFF4E3629))
                                                drawPixelSprite(
                                                    sprite = treeSprite,
                                                    colorMap = treeColors,
                                                    center = Offset(isoPos.x, isoPos.y - 18f * scale),
                                                    pixelSize = 3.5f * scale
                                                )
                                            }
                                        }
                                        "rock" -> {
                                            drawOval(
                                                color = Color(0x20000000),
                                                topLeft = Offset(isoPos.x - 14f * scale, isoPos.y - 6f * scale),
                                                size = androidx.compose.ui.geometry.Size(28f * scale, 12f * scale)
                                            )
                                            val rockBmp = if ((drawable.decorX.toInt() + drawable.decorY.toInt()) % 2 == 0) (newStone ?: stone1) else stone2
                                            if (rockBmp != null) {
                                                drawIsoSpriteBitmap(
                                                    bitmap = rockBmp,
                                                    center = Offset(isoPos.x, isoPos.y - 8f * scale),
                                                    scale = scale,
                                                    refHeight = 44f,
                                                    animIndex = 0
                                                )
                                            } else {
                                                val rockColors = mapOf('R' to Color(0xFF455A64), 'D' to Color(0xFF263238))
                                                drawPixelSprite(
                                                    sprite = rockSprite,
                                                    colorMap = rockColors,
                                                    center = Offset(isoPos.x, isoPos.y - 8f * scale),
                                                    pixelSize = 3f * scale
                                                )
                                            }
                                        }
                                        "torch" -> {
                                            drawCircle(
                                                color = Color(0xFFFF9100).copy(alpha = 0.12f),
                                                radius = 12f * scale,
                                                center = isoPos
                                            )
                                            // Substitui o asset grande de fogueira por tochas procedurais com micro-animação
                                            // Alterna as cores da chama (F) entre amarelo e laranja baseando-se no frameIndex para tremular
                                            val flameColor = if (frameIndex % 2 == 0) Color(0xFFFF9100) else Color(0xFFFFD54F)
                                            val torchColors = mapOf('F' to flameColor, 'S' to Color(0xFF5D4037))
                                            drawPixelSprite(
                                                sprite = torchSprite,
                                                colorMap = torchColors,
                                                center = Offset(isoPos.x, isoPos.y - 10f * scale),
                                                pixelSize = 2.2f * scale
                                            )
                                        }
                                    }
                                }
                                is IsoDrawable.HeroItem -> {
                                    val hero = drawable.hero
                                    drawOval(
                                        color = Color(0x35000000),
                                        topLeft = Offset(isoPos.x - 14f * scale, isoPos.y - 6f * scale),
                                        size = androidx.compose.ui.geometry.Size(28f * scale, 12f * scale)
                                    )
                                    
                                    if (hero.state == HeroState.COMBAT) {
                                        drawOval(
                                            color = HealthRed,
                                            topLeft = Offset(isoPos.x - 18f * scale, isoPos.y - 9f * scale),
                                            size = androidx.compose.ui.geometry.Size(36f * scale, 18f * scale),
                                            style = Stroke(width = 1.5f * scale)
                                        )
                                    }
                                    
                                    val spriteY = isoPos.y - 15f * scale
                                    
                                    drawHeroSprite(
                                        hero = hero,
                                        center = Offset(isoPos.x, spriteY),
                                        scale = scale,
                                        animIndex = frameIndex,
                                        warriorIdle = warriorIdle,
                                        warriorWalk = warriorWalk,
                                        warriorAttack = warriorAttack,
                                        warriorDeath = warriorDeath
                                    )

                                    // Barra HP
                                    val hpRatio = hero.hp / hero.maxHp
                                    val barWidth = 26f * scale
                                    val barHeight = 3f * scale
                                    val barX = isoPos.x - barWidth / 2
                                    val barY = spriteY + 16f * scale
                                    
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(barX - 1f, barY - 1f),
                                        size = androidx.compose.ui.geometry.Size(barWidth + 2f, barHeight + 2f)
                                    )
                                    drawRect(
                                        color = Color(0xFF2C2C2C),
                                        topLeft = Offset(barX, barY),
                                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                    )
                                    drawRect(
                                        color = if (hero.state == HeroState.RESTING) ExpGreen else Color(0xFFFFD54F),
                                        topLeft = Offset(barX, barY),
                                        size = androidx.compose.ui.geometry.Size(barWidth * hpRatio, barHeight)
                                    )

                                    val nameLayout = textMeasurer.measure(
                                        text = hero.name,
                                        style = TextStyle(color = TextWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        softWrap = false
                                    )
                                    val nameWidth = nameLayout.size.width.toFloat()
                                    val nameHeight = nameLayout.size.height.toFloat()
                                    val rectX = isoPos.x - nameWidth / 2 - 4f * scale
                                    val rectY = spriteY - 26f * scale
                                    
                                    val heroRectWidth = nameWidth + 8f * scale
                                    val heroRectHeight = nameHeight + 4f * scale
                                    val rawHeroRect = androidx.compose.ui.geometry.Rect(
                                        rectX,
                                        rectY - 2f * scale,
                                        rectX + heroRectWidth,
                                        rectY - 2f * scale + heroRectHeight
                                    )
                                    val adjustedHeroRect = adjustForCollisions(rawHeroRect)
                                    occupiedLabels.add(adjustedHeroRect)
                                    
                                    val finalRectX = adjustedHeroRect.left
                                    val finalRectY = adjustedHeroRect.top + 2f * scale
                                    
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        topLeft = Offset(finalRectX, finalRectY - 2f * scale),
                                        size = androidx.compose.ui.geometry.Size(heroRectWidth, heroRectHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
                                    )
                                    
                                    drawText(
                                        textLayoutResult = nameLayout,
                                        topLeft = Offset(finalRectX + 4f * scale, finalRectY)
                                    )
                                }
                                is IsoDrawable.MonsterItem -> {
                                    val monster = drawable.monster
                                    drawOval(
                                        color = Color(0x35000000),
                                        topLeft = Offset(isoPos.x - 16f * scale, isoPos.y - 7f * scale),
                                        size = androidx.compose.ui.geometry.Size(32f * scale, 14f * scale)
                                    )
                                    
                                    val spriteY = isoPos.y - 14f * scale
                                    
                                    drawMonsterSprite(
                                        monster = monster,
                                        center = Offset(isoPos.x, spriteY),
                                        scale = scale,
                                        animIndex = frameIndex,
                                        orcIdle = orcIdle,
                                        orcWalk = orcWalk,
                                        orcAttack = orcAttack,
                                        orcDeath = orcDeath,
                                        orcIdleNew = orcIdleNew,
                                        slimeIdle = slimeIdle,
                                        slimeDeath = slimeDeath,
                                        wolfIdle = wolfIdle,
                                        wolfDeath = wolfDeath,
                                        goblinIdle = goblinIdle,
                                        goblinDeath = goblinDeath,
                                        newTree = newTree,
                                        newStone = newStone
                                    )

                                    if (!monster.isDead) {
                                        // Barra HP
                                        val hpRatio = monster.hp / monster.maxHp
                                        val barWidth = 32f * scale
                                        val barHeight = 4f * scale
                                        val barX = isoPos.x - barWidth / 2
                                        val barY = spriteY - 15f * scale

                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(barX - 1f, barY - 1f),
                                            size = androidx.compose.ui.geometry.Size(barWidth + 2f, barHeight + 2f)
                                        )
                                        drawRect(
                                            color = Color(0xFF2C2C2C),
                                            topLeft = Offset(barX, barY),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                        )
                                        val hpColor = if (monster.name.startsWith("Coleta")) Color(0xFF29B6F6) else HealthRed
                                        drawRect(
                                            color = hpColor,
                                            topLeft = Offset(barX, barY),
                                            size = androidx.compose.ui.geometry.Size(barWidth * hpRatio, barHeight)
                                        )

                                        val displayName = if (monster.name.startsWith("Coleta")) {
                                            monster.name.substringAfter("Coleta de ")
                                        } else {
                                            monster.name.substringBefore(" ")
                                        }

                                        val nameLayout = textMeasurer.measure(
                                            text = displayName,
                                            style = TextStyle(color = TextWhite, fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                                            softWrap = false
                                        )
                                        val nameWidth = nameLayout.size.width.toFloat()
                                        val nameHeight = nameLayout.size.height.toFloat()
                                        val rectX = isoPos.x - nameWidth / 2 - 4f * scale
                                        val rectY = barY - 12f * scale
                                        
                                        val monsterRectWidth = nameWidth + 8f * scale
                                        val monsterRectHeight = nameHeight + 4f * scale
                                        val rawMonsterRect = androidx.compose.ui.geometry.Rect(
                                            rectX,
                                            rectY - 2f * scale,
                                            rectX + monsterRectWidth,
                                            rectY - 2f * scale + monsterRectHeight
                                        )
                                        val adjustedMonsterRect = adjustForCollisions(rawMonsterRect)
                                        occupiedLabels.add(adjustedMonsterRect)
                                        
                                        val finalMonsterRectX = adjustedMonsterRect.left
                                        val finalMonsterRectY = adjustedMonsterRect.top + 2f * scale
                                        
                                        drawRoundRect(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            topLeft = Offset(finalMonsterRectX, finalMonsterRectY - 2f * scale),
                                            size = androidx.compose.ui.geometry.Size(monsterRectWidth, monsterRectHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
                                        )
                                        
                                        drawText(
                                            textLayoutResult = nameLayout,
                                            topLeft = Offset(finalMonsterRectX + 4f * scale, finalMonsterRectY)
                                        )
                                    }
                                }
                            }
                        }

                        // 9. Textos Flutuantes
                        gameState.floatingTexts.forEach { ft ->
                            val baseIso = toIsometric(ft.x, ft.y, guildX, guildY, scale)
                            val offsetUp = ft.age * 20f * scale
                            val ftX = baseIso.x
                            val ftY = baseIso.y - offsetUp

                            drawText(
                                textMeasurer = textMeasurer,
                                text = ft.text,
                                style = TextStyle(color = parseColorHex(ft.colorHex), fontSize = 13.sp, fontWeight = FontWeight.Black),
                                topLeft = Offset(ftX - 10f, ftY)
                            )
                        }
                    }
                }
            }

            // Legenda Inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("Guerreiro (Arthur)", Color(0xFFE65100))
                LegendItem("Maga (Valeria)", Color(0xFF9C27B0))
                LegendItem("Construções 3D", Color(0xFF6C7787))
                LegendItem("Monstro", HealthRed)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextWhite, fontSize = 11.sp)
    }
}

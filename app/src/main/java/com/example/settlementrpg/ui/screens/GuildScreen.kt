package com.example.settlementrpg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settlementrpg.data.model.*
import com.example.settlementrpg.theme.*

@Composable
fun GuildScreen(
    gameState: GameState,
    onUpgradeBuilding: (String) -> Unit,
    onPublishMission: (String) -> Unit,
    onSellMaterial: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Seção: Quadro de Recursos da Guilda
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GoldDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cofre & Armazém",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ouro: ${gameState.gold} 🪙", color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(text = "Reputação: ${gameState.reputation} ★", color = GoldLight, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Materiais Guardados:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    
                    val matsList = listOf(
                        Triple("slime_gel", "Gel de Slime", 3),
                        Triple("wolf_fur", "Pele de Lobo", 5),
                        Triple("goblin_ear", "Orelha de Goblin", 8),
                        Triple("iron_ore", "Minério de Ferro", 12),
                        Triple("orc_tooth", "Dente de Orc", 15)
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matsList.forEach { (id, name, value) ->
                            val count = gameState.materials[id] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$name: $count",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Venda: $value 🪙 cada",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }
                                Button(
                                    onClick = { onSellMaterial(id) },
                                    enabled = count > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = Color.Black,
                                        disabledContainerColor = DarkSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = "Vender 1",
                                        color = if (count > 0) Color.Black else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seção: Quadro de Contratos (Missões)
        item {
            Text(
                text = "Quadro de Contratos",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        val activeMissions = gameState.missions.filter { !it.isCompleted }
        if (activeMissions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhum contrato disponível no momento.", color = TextGray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(activeMissions) { mission ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, Color(0xFF2C2C35))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mission.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            
                            // Estrelas de Dificuldade
                            Row {
                                repeat(mission.difficulty) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        
                        Text(text = mission.description, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Alvo: Lvl ${mission.monsterLevel} ${mission.targetMonsterName}", color = HealthRed, fontSize = 12.sp)
                            Text(text = "Recompensa: +${mission.goldReward} 🪙", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (!mission.isPublished) {
                            val canAfford = gameState.gold >= mission.goldReward
                            Button(
                                onClick = { onPublishMission(mission.id) },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black,
                                    disabledContainerColor = DarkSurfaceVariant
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (canAfford) "Publicar Contrato (Liberar Caçada)" else "Ouro da Guilda Insuficiente",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (canAfford) Color.Black else TextGray
                                )
                            }
                        } else {
                            if (mission.assignedHeroId != null) {
                                val heroName = gameState.heroes.find { it.id == mission.assignedHeroId }?.name ?: "Alguém"
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E3440))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Aceito por: $heroName", color = GoldLight, fontSize = 11.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E3A52))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Publicado (Aguardando Herói)", color = TextWhite, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seção: Lista de Heróis
        item {
            Text(
                text = "Heróis na Guilda",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        items(gameState.heroes) { hero ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color(0xFF2C2C35))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar/Classe Cor
                    val classColor = when (hero.heroClass) {
                        HeroClass.WARRIOR -> Color(0xFFF9A825)
                        HeroClass.MAGE -> Color(0xFFAB47BC)
                        HeroClass.ARCHER -> Color(0xFF4CAF50)
                        HeroClass.CLERIG -> Color(0xFF29B6F6)
                    }
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(classColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hero.heroClass.name.take(3),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${hero.name} (Nível ${hero.level})", color = TextWhite, fontWeight = FontWeight.Bold)
                            
                            val stateLabel = when(hero.state) {
                                HeroState.IDLE -> "Ocioso"
                                HeroState.WALKING_TO_MONSTER -> "Caminhando"
                                HeroState.COMBAT -> "💥 EM BATALHA"
                                HeroState.WALKING_TO_GUILD -> "Retornando"
                                HeroState.RESTING -> "💤 Descansando"
                            }
                            Text(
                                text = stateLabel,
                                color = if (hero.state == HeroState.COMBAT) HealthRed else GoldLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "ATK: ${hero.attack.toInt()}", color = TextGray, fontSize = 11.sp)
                            Text(text = "DEF: ${hero.defense.toInt()}", color = TextGray, fontSize = 11.sp)
                            Text(text = "Ouro: ${hero.gold} 🪙", color = TextGray, fontSize = 11.sp)
                        }

                        // Equipamentos
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "⚔ ${hero.weaponName}", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "🛡 ${hero.armorName}", color = MysticalBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Barras de Vida e XP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "HP: ${hero.hp.toInt()}/${hero.maxHp.toInt()}", color = TextWhite, fontSize = 10.sp)
                                LinearProgressIndicator(
                                    progress = { hero.hp / hero.maxHp },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = HealthRed,
                                    trackColor = Color.DarkGray
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "XP: ${hero.xp}/${hero.maxXp}", color = TextWhite, fontSize = 10.sp)
                                LinearProgressIndicator(
                                    progress = { hero.xp.toFloat() / hero.maxXp },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = ExpGreen,
                                    trackColor = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Seção: Construções / Upgrades
        item {
            Text(
                text = "Melhorias do Assentamento",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        items(gameState.buildings) { building ->
            val isUnlocked = building.isUnlocked || building.id == "guild"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) DarkSurface else Color(0xFF13131A)
                ),
                border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF2C2C35) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = building.name + if (building.isBuilt) " (Lvl ${building.level})" else " (Não construído)",
                            color = if (isUnlocked) TextWhite else TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        
                        if (!isUnlocked) {
                            Text(text = "Bloqueado", color = HealthRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(text = building.description, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))

                    if (isUnlocked) {
                        val (goldCost, matCost) = building.getUpgradeCost()
                        
                        Text(text = "Custo de Evolução:", color = TextGray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Ouro: $goldCost", color = if (gameState.gold >= goldCost) TextWhite else HealthRed, fontSize = 12.sp)
                            matCost.forEach { (mat, amt) ->
                                val owned = gameState.materials[mat] ?: 0
                                val displayName = when(mat) {
                                    "slime_gel" -> "Gel"
                                    "wolf_fur" -> "Pele"
                                    "goblin_ear" -> "Orelha"
                                    "iron_ore" -> "Ferro"
                                    else -> mat
                                }
                                Text(
                                    text = "$displayName: $owned/$amt",
                                    color = if (owned >= amt) TextWhite else HealthRed,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onUpgradeBuilding(building.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (building.isBuilt) "Melhorar Prédio" else "Construir Prédio",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

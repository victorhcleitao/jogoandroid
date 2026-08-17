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
    onCraftEquipment: (String) -> Unit,
    onEquipHero: (String, String) -> Unit,
    onDiscardMission: (String) -> Unit,
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
                    
                    fun getMaterialName(matId: String): String = when(matId) {
                        "slime_gel" -> "Gel de Slime"
                        "wolf_fur" -> "Pele de Lobo"
                        "goblin_ear" -> "Orelha de Goblin"
                        "iron_ore" -> "Minério de Ferro"
                        "orc_tooth" -> "Dente de Orc"
                        "rusty_sword" -> "Espada Enferrujada"
                        "broken_shield" -> "Escudo Quebrado"
                        "old_ring" -> "Anel Antigo"
                        "gold_nugget" -> "Pepita de Ouro"
                        else -> matId.replace("_", " ").replaceFirstChar { it.uppercase() }
                    }

                    fun getMaterialValue(matId: String): Int = when(matId) {
                        "slime_gel" -> 3
                        "wolf_fur" -> 5
                        "goblin_ear" -> 8
                        "iron_ore" -> 12
                        "orc_tooth" -> 15
                        "rusty_sword" -> 18
                        "broken_shield" -> 15
                        "old_ring" -> 25
                        "gold_nugget" -> 30
                        else -> 2
                    }

                    val activeMats = gameState.materials.filter { it.key != "gold_loot" && it.value > 0 }
                    
                    if (activeMats.isEmpty()) {
                        Text(
                            text = "Nenhum material no armazém. Envie aventureiros para caçar!",
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeMats.forEach { (id, count) ->
                                val name = getMaterialName(id)
                                val value = getMaterialValue(id)
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
                        
                        val activeEquips = gameState.equipments.filter { it.value > 0 }
                        if (activeEquips.isNotEmpty()) {
                            Divider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                            Text(text = "Equipamentos Fabricados:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                activeEquips.forEach { (id, count) ->
                                    val name = when (id) {
                                        "slime_sword" -> "Espada de Slime"
                                        "wolf_armor" -> "Armadura de Pele de Lobo"
                                        "power_ring" -> "Anel do Poder"
                                        else -> id
                                    }
                                    val value = when (id) {
                                        "slime_sword" -> 45
                                        "wolf_armor" -> 65
                                        "power_ring" -> 120
                                        else -> 10
                                    }
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
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GoldPrimary,
                                                contentColor = Color.Black
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(text = "Vender 1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seção: Oficina de Crafting (Ferraria)
        val blacksmithLvl = gameState.buildings.find { it.id == "blacksmith" }?.level ?: 0
        item {
            Text(
                text = "Oficina de Crafting",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        
        if (blacksmithLvl == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, Color(0xFF2C2C35))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Oficina bloqueada. Construa a Ferraria para liberar a fabricação de itens.",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            val recipes = listOf(
                Triple("slime_sword", "Espada de Slime", mapOf("slime_gel" to 5, "iron_ore" to 1) to 10),
                Triple("wolf_armor", "Armadura de Pele de Lobo", mapOf("wolf_fur" to 4, "goblin_ear" to 2) to 15),
                Triple("power_ring", "Anel do Poder", mapOf("old_ring" to 1, "gold_nugget" to 1) to 25)
            )
            
            items(recipes) { (id, name, costs) ->
                val (mats, goldCost) = costs
                val canCraftGold = gameState.gold >= goldCost
                val canCraftMats = mats.all { (mat, amt) -> (gameState.materials[mat] ?: 0) >= amt }
                val canCraft = canCraftGold && canCraftMats
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (canCraft) GoldPrimary.copy(alpha = 0.5f) else Color(0xFF2C2C35))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        val bonusText = when (id) {
                            "slime_sword" -> "+8 de Ataque (+12 para Guerreiro, +8 para Arqueiro)"
                            "wolf_armor" -> "+6 de Defesa (+12 para Guerreiro/Arqueiro)"
                            else -> "+8 ATK / +20 HP (+15 ATK / +40 HP para Mago/Clérigo)"
                        }
                        Text(text = "Efeito: $bonusText", color = ExpGreen, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Custo: $goldCost 🪙", color = if (canCraftGold) TextWhite else HealthRed, fontSize = 11.sp)
                            mats.forEach { (mat, amt) ->
                                val owned = gameState.materials[mat] ?: 0
                                val displayName = when(mat) {
                                    "slime_gel" -> "Gel"
                                    "wolf_fur" -> "Pele"
                                    "goblin_ear" -> "Orelha"
                                    "iron_ore" -> "Ferro"
                                    "old_ring" -> "Anel Antigo"
                                    "gold_nugget" -> "Pepita"
                                    else -> mat
                                }
                                Text(
                                    text = "$displayName: $owned/$amt",
                                    color = if (owned >= amt) TextWhite else HealthRed,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Button(
                            onClick = { onCraftEquipment(id) },
                            enabled = canCraft,
                            modifier = Modifier.fillMaxWidth().height(32.dp).padding(top = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black,
                                disabledContainerColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "Fabricar Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPublishMission(mission.id) },
                                    enabled = canAfford,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = Color.Black,
                                        disabledContainerColor = DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "Publicar" else "Sem Ouro",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (canAfford) Color.Black else TextGray
                                    )
                                }
                                Button(
                                    onClick = { onDiscardMission(mission.id) },
                                    modifier = Modifier.width(90.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC62828),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(text = "Descartar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        } else {
                            if (mission.assignedHeroId != null) {
                                val heroName = gameState.heroes.find { it.id == mission.assignedHeroId }?.name ?: "Alguém"
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E3440))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "Aceito por: $heroName", color = GoldLight, fontSize = 11.sp)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E3A52))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Publicado (Aguardando Herói)", color = TextWhite, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { onDiscardMission(mission.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White)
                                    ) {
                                        Text(text = "Cancelar (Reembolsar)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
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
                            val progressText = when (hero.rank) {
                                "F" -> "${hero.missionsCompleted}/5 contr."
                                "E" -> "${hero.missionsCompleted}/15 contr."
                                "D" -> "${hero.missionsCompleted}/30 contr."
                                "C" -> "${hero.missionsCompleted}/50 contr."
                                "B" -> "${hero.missionsCompleted}/75 contr."
                                "A" -> "${hero.missionsCompleted}/100 contr."
                                else -> "${hero.missionsCompleted} contr."
                            }
                            Text(text = "${hero.name} (Nível ${hero.level}) - [Rank ${hero.rank} | $progressText]", color = TextWhite, fontWeight = FontWeight.Bold)
                            
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

                        // Equipar itens da Oficina (Se o herói estiver em repouso/ocioso)
                        val isBaseState = hero.state == HeroState.IDLE || hero.state == HeroState.RESTING
                        val hasEquips = gameState.equipments.any { it.value > 0 }
                        if (isBaseState && hasEquips) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val ownedSwords = gameState.equipments["slime_sword"] ?: 0
                                if (ownedSwords > 0 && hero.weaponName != "Espada de Slime") {
                                    Button(
                                        onClick = { onEquipHero(hero.id, "slime_sword") },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                    ) {
                                        Text(text = "+ ⚔ Slime", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                val ownedRings = gameState.equipments["power_ring"] ?: 0
                                if (ownedRings > 0 && hero.weaponName != "Anel do Poder") {
                                    Button(
                                        onClick = { onEquipHero(hero.id, "power_ring") },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                    ) {
                                        Text(text = "+ 💍 Anel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                val ownedArmors = gameState.equipments["wolf_armor"] ?: 0
                                if (ownedArmors > 0 && hero.armorName != "Armadura de Pele de Lobo") {
                                    Button(
                                        onClick = { onEquipHero(hero.id, "wolf_armor") },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MysticalBlue, contentColor = Color.Black)
                                    ) {
                                        Text(text = "+ 🛡 Lobo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
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
                                    "orc_tooth" -> "Dente"
                                    "gold_nugget" -> "Pepita"
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

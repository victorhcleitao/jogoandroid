package com.example.settlementrpg.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.settlementrpg.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("settlement_rpg_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    
    // Contagem regressiva para respawn de monstros: monsterId -> ticks restantes
    private val monsterRespawns = mutableMapOf<String, Int>()

    private val obstacles = listOf(
        Pair(50f, 50f), Pair(70f, 90f), Pair(120f, 60f), Pair(510f, 80f), Pair(540f, 120f),
        Pair(110f, 490f), Pair(80f, 520f), Pair(500f, 520f), Pair(530f, 480f), Pair(80f, 250f),
        Pair(530f, 280f), Pair(220f, 520f), Pair(380f, 60f),
        Pair(120f, 320f), Pair(140f, 350f), Pair(460f, 340f), Pair(480f, 310f), Pair(210f, 130f),
        Pair(390f, 490f), Pair(170f, 220f), Pair(420f, 220f), Pair(240f, 420f), Pair(340f, 160f)
    )

    private fun getRandomSpawnPosition(): Pair<Float, Float> {
        val minDistance = 150f
        val maxDistance = 400f
        val guildaX = 300f
        val guildaY = 300f
        
        var attempts = 0
        var spawnX = 300f
        var spawnY = 300f
        
        while (attempts < 20) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val distance = minDistance + Random.nextFloat() * (maxDistance - minDistance)
            val tx = guildaX + distance * kotlin.math.cos(angle)
            val ty = guildaY + distance * kotlin.math.sin(angle)
            
            val posX = tx.coerceIn(50f, 550f)
            val posY = ty.coerceIn(50f, 550f)
            
            val collides = obstacles.any { obs ->
                val dx = obs.first - posX
                val dy = obs.second - posY
                sqrt(dx * dx + dy * dy) < 30f
            } || sqrt((guildaX - posX) * (guildaX - posX) + (guildaY - posY) * (guildaY - posY)) < 60f
            
            if (!collides) {
                spawnX = posX
                spawnY = posY
                break
            }
            attempts++
        }
        
        if (spawnX == 300f && spawnY == 300f) {
            spawnX = 150f + Random.nextFloat() * 300f
            spawnY = 150f + Random.nextFloat() * 300f
        }
        return Pair(spawnX, spawnY)
    }

    init {
        loadGame()
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)
                tick()
            }
        }
    }

    @Synchronized
    private fun tick() {
        val currentState = _gameState.value
        val newFloatingTexts = mutableListOf<FloatingText>()
        val currentMaterialsMap = currentState.materials.toMutableMap()
        val logsToAdd = mutableListOf<LogMessage>()
        
        val updatedHeroes = currentState.heroes.map { hero ->
            var updatedHero = hero.copy(prevX = hero.x, prevY = hero.y)
            
            // 1. Regeneração enquanto descansa
            if (hero.state == HeroState.RESTING) {
                val hasTavern = currentState.buildings.find { it.id == "tavern" }?.isBuilt == true
                val healAmount = if (hasTavern) 20f else 10f
                val newHp = min(hero.maxHp, hero.hp + healAmount)
                if (newHp > hero.hp) {
                    newFloatingTexts.add(FloatingText(
                        id = UUID.randomUUID().toString(),
                        text = "+${(newHp - hero.hp).toInt()}",
                        x = hero.x,
                        y = hero.y - 15f,
                        colorHex = "#FF81C784" // Verde
                    ))
                }
                updatedHero = hero.copy(
                    hp = newHp,
                    state = if (newHp >= hero.maxHp) HeroState.IDLE else HeroState.RESTING
                )
                if (updatedHero.state == HeroState.IDLE) {
                    addLog("Herói ${hero.name} está totalmente recuperado e pronto para missões!", LogType.GUILD)
                }
            }

            // 2. Movimentação
            if (updatedHero.state == HeroState.WALKING_TO_MONSTER || updatedHero.state == HeroState.WALKING_TO_GUILD) {
                val dx = updatedHero.targetX - updatedHero.x
                val dy = updatedHero.targetY - updatedHero.y
                val distance = sqrt(dx * dx + dy * dy)
                val speed = 25f // pixels por segundo
                
                if (distance <= speed) {
                    // Chegou ao destino
                    val nextState = if (updatedHero.state == HeroState.WALKING_TO_MONSTER) {
                        HeroState.COMBAT
                    } else {
                        // Chegou na Guilda - entregar materiais e descansar
                        HeroState.RESTING
                    }
                    updatedHero = updatedHero.copy(
                        x = updatedHero.targetX,
                        y = updatedHero.targetY,
                        state = nextState
                    )
                    
                    if (nextState == HeroState.RESTING) {
                        updatedHero = deliverLoot(updatedHero, currentMaterialsMap, logsToAdd)

                        // Tenta comprar novos equipamentos baseando-se no ferreiro desbloqueado
                        val blacksmithLvl = currentState.buildings.find { it.id == "blacksmith" }?.level ?: 0
                        updatedHero = handleHeroEquipPurchase(updatedHero, blacksmithLvl)
                    }
                } else {
                    // Mover em direção ao alvo
                    val angleX = dx / distance
                    val angleY = dy / distance
                    updatedHero = updatedHero.copy(
                        x = updatedHero.x + angleX * speed,
                        y = updatedHero.y + angleY * speed
                    )
                }
            }

            updatedHero
        }.toMutableList()

        // 3. Processamento de Combate
        val monstersList = currentState.monsters.toMutableList()
        val completedMissions = mutableListOf<String>()
        val hasBlacksmith = currentState.buildings.find { it.id == "blacksmith" }?.isBuilt == true

        for (i in updatedHeroes.indices) {
            val hero = updatedHeroes[i]
            if (hero.state == HeroState.COMBAT && hero.targetMonsterId != null) {
                val monsterIndex = monstersList.indexOfFirst { it.id == hero.targetMonsterId }
                if (monsterIndex != -1) {
                    val monster = monstersList[monsterIndex]
                    if (!monster.isDead) {
                        // Bônus de ataque da Ferraria
                        val heroAtk = if (hasBlacksmith) hero.attack * 1.15f else hero.attack
                        
                        // Herói ataca monstro
                        val damageToMonster = max(1f, heroAtk - monster.defense)
                        val newMonsterHp = max(0f, monster.hp - damageToMonster)
                        
                        // Gerar texto flutuante de dano ao monstro
                        newFloatingTexts.add(FloatingText(
                            id = UUID.randomUUID().toString(),
                            text = "-${damageToMonster.toInt()}",
                            x = monster.x,
                            y = monster.y - 10f,
                            colorHex = "#FFFF8A80" // Vermelho claro
                        ))

                        var updatedMonster = monster.copy(hp = newMonsterHp, flashTicks = 1)
                        monstersList[monsterIndex] = updatedMonster

                        logsToAdd.add(LogMessage(
                            id = UUID.randomUUID().toString(),
                            text = "${hero.name} causou ${damageToMonster.toInt()} de dano em ${monster.name}.",
                            timestamp = System.currentTimeMillis(),
                            type = LogType.COMBAT
                        ))

                        if (newMonsterHp <= 0f) {
                            // Monstro derrotado!
                            logsToAdd.add(LogMessage(
                                id = UUID.randomUUID().toString(),
                                text = "${hero.name} derrotou ${monster.name}!",
                                timestamp = System.currentTimeMillis(),
                                type = LogType.COMBAT
                            ))

                            // Drops de Loot
                            val collected = hero.collectedMaterials.toMutableMap()
                            monster.lootTable.forEach { drop ->
                                if (Random.nextFloat() <= drop.chance) {
                                    val amount = Random.nextInt(drop.minAmount, drop.maxAmount + 1)
                                    collected[drop.materialId] = (collected[drop.materialId] ?: 0) + amount
                                    logsToAdd.add(LogMessage(
                                        id = UUID.randomUUID().toString(),
                                        text = "Item obtido: +$amount ${getMaterialDisplayName(drop.materialId)}",
                                        timestamp = System.currentTimeMillis(),
                                        type = LogType.COMBAT
                                    ))
                                }
                            }

                            // Recompensas: Apenas XP e Loot (materiais), sem ouro direto!
                            val xpGained = monster.xpReward
                            var newXp = hero.xp + xpGained
                            var levelUpHero = hero.copy(
                                xp = newXp,
                                collectedMaterials = collected,
                                state = HeroState.WALKING_TO_GUILD,
                                targetX = 300f,
                                targetY = 300f,
                                targetMonsterId = null
                            )

                            if (levelUpHero.xp >= levelUpHero.maxXp) {
                                levelUpHero = levelUpHero.levelUp()
                                logsToAdd.add(LogMessage(
                                    id = UUID.randomUUID().toString(),
                                    text = "★ ${levelUpHero.name} subiu para o Nível ${levelUpHero.level}! ★",
                                    timestamp = System.currentTimeMillis(),
                                    type = LogType.GUILD
                                ))
                            }

                            updatedHeroes[i] = levelUpHero

                            // Iniciar respawn do monstro
                            monsterRespawns[monster.id] = 8 // 8 ticks para respawn

                            // Marcar missão como completada
                            if (hero.currentMissionId != null) {
                                completedMissions.add(hero.currentMissionId)
                            }

                        } else {
                            // Monstro contra-ataca
                            val damageToHero = max(1f, monster.attack - hero.defense)
                            val newHeroHp = max(0f, hero.hp - damageToHero)
                            
                            // Gerar texto flutuante de dano ao herói
                            newFloatingTexts.add(FloatingText(
                                id = UUID.randomUUID().toString(),
                                text = "-${damageToHero.toInt()}",
                                x = hero.x,
                                y = hero.y - 10f,
                                colorHex = "#FFE53935" // Vermelho mais forte
                            ))
                            
                            logsToAdd.add(LogMessage(
                                id = UUID.randomUUID().toString(),
                                text = "${monster.name} atacou ${hero.name} causando ${damageToHero.toInt()} de dano.",
                                timestamp = System.currentTimeMillis(),
                                type = LogType.COMBAT
                            ))

                            if (newHeroHp <= 0f) {
                                // Herói desmaiou!
                                logsToAdd.add(LogMessage(
                                    id = UUID.randomUUID().toString(),
                                    text = "☠ ${hero.name} foi derrotado por ${monster.name} e resgatado de volta à guilda!",
                                    timestamp = System.currentTimeMillis(),
                                    type = LogType.GUILD
                                ))
                                
                                // Teleportado de volta à guilda, sem loot
                                updatedHeroes[i] = hero.copy(
                                    hp = 1f,
                                    state = HeroState.RESTING,
                                    x = 300f,
                                    y = 300f,
                                    targetX = 300f,
                                    targetY = 300f,
                                    prevX = 300f,
                                    prevY = 300f,
                                    targetMonsterId = null,
                                    currentMissionId = null,
                                    collectedMaterials = emptyMap()
                                )

                                // Liberar missão se o herói falhou
                                if (hero.currentMissionId != null) {
                                    releaseMission(hero.currentMissionId)
                                }
                            } else {
                                updatedHeroes[i] = hero.copy(hp = newHeroHp, flashTicks = 1)
                            }
                        }
                    }
                } else {
                    // Monstro sumiu/inválido
                    updatedHeroes[i] = hero.copy(state = HeroState.IDLE, targetMonsterId = null)
                }
            }
        }

        // 4. Respawn de Monstros
        val keysToRespawn = mutableListOf<String>()
        monsterRespawns.forEach { (id, ticks) ->
            val remaining = ticks - 1
            if (remaining <= 0) {
                keysToRespawn.add(id)
            } else {
                monsterRespawns[id] = remaining
            }
        }
        keysToRespawn.forEach { id ->
            monsterRespawns.remove(id)
            val index = monstersList.indexOfFirst { it.id == id }
            if (index != -1) {
                val m = monstersList[index]
                val (newX, newY) = getRandomSpawnPosition()
                monstersList[index] = m.copy(
                    hp = m.maxHp,
                    x = newX,
                    y = newY,
                    spawnX = newX,
                    spawnY = newY
                )
                logsToAdd.add(LogMessage(
                    id = UUID.randomUUID().toString(),
                    text = "${m.name} ressurgiu em nova posição nos arredores do assentamento.",
                    timestamp = System.currentTimeMillis(),
                    type = LogType.SYSTEM
                ))
            }
        }

        // 5. Completar Missões ativas
        var repGained = 0
        val updatedMissions = currentState.missions.map { mission ->
            if (completedMissions.contains(mission.id)) {
                repGained += mission.reputationReward
                logsToAdd.add(LogMessage(
                    id = UUID.randomUUID().toString(),
                    text = "✓ Contrato Concluído: ${mission.title}! Recompensa reservada de ${mission.goldReward} 🪙 garantida ao herói, +${mission.reputationReward} reputação para a Guilda.",
                    timestamp = System.currentTimeMillis(),
                    type = LogType.GUILD
                ))
                mission.copy(isCompleted = true, assignedHeroId = null)
            } else {
                mission
            }
        }.toMutableList()

        // 6. Atribuição de Missões automáticas para heróis IDLE
        for (i in updatedHeroes.indices) {
            val hero = updatedHeroes[i]
            if (hero.state == HeroState.IDLE) {
                val availableMission = updatedMissions.find { it.isPublished && it.assignedHeroId == null && !it.isCompleted }
                if (availableMission != null) {
                    // Encontrar monstro alvo correspondente no mapa
                    val monster = monstersList.find { it.name.startsWith(availableMission.targetMonsterName) && !it.isDead }
                    if (monster != null) {
                        // Atribuir missão ao herói
                        val missionIndex = updatedMissions.indexOfFirst { it.id == availableMission.id }
                        updatedMissions[missionIndex] = availableMission.copy(assignedHeroId = hero.id)

                        updatedHeroes[i] = hero.copy(
                            state = HeroState.WALKING_TO_MONSTER,
                            targetX = monster.x,
                            targetY = monster.y,
                            targetMonsterId = monster.id,
                            currentMissionId = availableMission.id
                        )

                        logsToAdd.add(LogMessage(
                            id = UUID.randomUUID().toString(),
                            text = "${hero.name} aceitou o contrato: ${availableMission.title} e está caçando ${monster.name}.",
                            timestamp = System.currentTimeMillis(),
                            type = LogType.GUILD
                        ))
                    }
                }
            }
        }

        // Gerar novas missões aleatórias periodicamente se houver menos de 4 missões ativas
        val activeMissionsCount = updatedMissions.count { !it.isCompleted }
        if (activeMissionsCount < 4 && Random.nextFloat() < 0.25f) {
            val newMission = generateRandomMission(currentState.buildings.find { it.id == "guild" }?.level ?: 1)
            updatedMissions.add(newMission)
        }

        // Limpar logs antigos para economizar memória (manter últimos 100)
        val combinedLogs = (logsToAdd + currentState.logs).take(100)

        // Decrementar flashTicks de monstros e heróis e atualizar textos flutuantes
        val updatedMonsters = monstersList.map {
            it.copy(flashTicks = max(0, it.flashTicks - 1))
        }

        val updatedFloatingTexts = (currentState.floatingTexts.map { it.copy(age = it.age + 1) }.filter { it.age < 2 } + newFloatingTexts)

        // Atualizar o estado global
        _gameState.value = currentState.copy(
            heroes = updatedHeroes.map { it.copy(flashTicks = max(0, it.flashTicks - 1)) },
            monsters = updatedMonsters,
            missions = updatedMissions,
            logs = combinedLogs,
            gold = currentState.gold,
            materials = currentMaterialsMap,
            reputation = currentState.reputation + repGained,
            floatingTexts = updatedFloatingTexts,
            lastTickTime = System.currentTimeMillis()
        )

        // Salvar progresso de tempos em tempos
        if (Random.nextFloat() < 0.1f) {
            saveGame()
        }
    }

    private fun addLog(text: String, type: LogType) {
        val newLog = LogMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        _gameState.value = _gameState.value.copy(
            logs = (listOf(newLog) + _gameState.value.logs).take(100)
        )
    }

    private fun addGuildGold(amount: Int) {
        _gameState.value = _gameState.value.copy(
            gold = _gameState.value.gold + amount
        )
    }

    private fun releaseMission(missionId: String) {
        val currentMissions = _gameState.value.missions
        val updated = currentMissions.map {
            if (it.id == missionId) it.copy(assignedHeroId = null) else it
        }
        _gameState.value = _gameState.value.copy(missions = updated)
    }

    private fun deliverLoot(
        hero: Hero,
        currentMats: MutableMap<String, Int>,
        logsToAdd: MutableList<LogMessage>
    ): Hero {
        // 1. Receber os materiais trazidos pelo herói e armazenar no armazém da guilda
        hero.collectedMaterials.forEach { (mat, amt) ->
            currentMats[mat] = (currentMats[mat] ?: 0) + amt
        }

        // 2. Obter a recompensa contratada da missão correspondente
        val mission = _gameState.value.missions.find { it.id == hero.currentMissionId }
        val rewardGold = mission?.goldReward ?: 0
        
        if (rewardGold > 0) {
            logsToAdd.add(LogMessage(
                id = UUID.randomUUID().toString(),
                text = "Entrega: ${hero.name} entregou os espólios e recebeu a recompensa contratada de $rewardGold 🪙.",
                timestamp = System.currentTimeMillis(),
                type = LogType.GUILD
            ))
        } else {
            logsToAdd.add(LogMessage(
                id = UUID.randomUUID().toString(),
                text = "Entrega: ${hero.name} guardou materiais no Armazém da guilda.",
                timestamp = System.currentTimeMillis(),
                type = LogType.GUILD
            ))
        }

        return hero.copy(
            gold = hero.gold + rewardGold,
            currentMissionId = null,
            collectedMaterials = emptyMap()
        )
    }

    // Melhora estruturas
    fun upgradeBuilding(buildingId: String) {
        val currentState = _gameState.value
        val buildingIndex = currentState.buildings.indexOfFirst { it.id == buildingId }
        if (buildingIndex == -1) return

        val building = currentState.buildings[buildingIndex]
        val (goldCost, matCost) = building.getUpgradeCost()

        // Verificar recursos
        if (currentState.gold < goldCost) {
            addLog("Ouro insuficiente para evoluir ${building.name} (Necessário: $goldCost).", LogType.SYSTEM)
            return
        }

        for ((mat, amt) in matCost) {
            val owned = currentState.materials[mat] ?: 0
            if (owned < amt) {
                addLog("Recurso insuficiente para evoluir ${building.name} (Necessário: $amt ${getMaterialDisplayName(mat)}).", LogType.SYSTEM)
                return
            }
        }

        // Deduzir recursos e evoluir
        val updatedMats = currentState.materials.toMutableMap()
        matCost.forEach { (mat, amt) ->
            updatedMats[mat] = (updatedMats[mat] ?: 0) - amt
        }

        val updatedBuilding = building.copy(
            level = building.level + 1,
            isUnlocked = true
        )
        val updatedBuildingsList = currentState.buildings.toMutableList()
        updatedBuildingsList[buildingIndex] = updatedBuilding

        var updatedHeroesList = currentState.heroes.toMutableList()
        var logsToAdd = mutableListOf<LogMessage>()

        // Se evoluir a Guilda, libera novos heróis errantes e desbloqueia outras construções
        if (buildingId == "guild") {
            val guildLvl = updatedBuilding.level
            addLog("Guilda evoluiu para o Nível $guildLvl!", LogType.UPGRADE)

            if (guildLvl == 2) {
                // Desbloqueia Ferraria e atrai Arqueiro Robin
                val robin = Hero(
                    id = UUID.randomUUID().toString(),
                    name = "Robin",
                    heroClass = HeroClass.ARCHER,
                    level = 2,
                    hp = 110f,
                    maxHp = 110f,
                    attack = 18f,
                    defense = 6f,
                    x = 300f,
                    y = 300f
                )
                updatedHeroesList.add(robin)
                
                // Desbloquear Ferraria na lista
                val blacksmithIndex = updatedBuildingsList.indexOfFirst { it.id == "blacksmith" }
                if (blacksmithIndex != -1) {
                    updatedBuildingsList[blacksmithIndex] = updatedBuildingsList[blacksmithIndex].copy(isUnlocked = true)
                }
                
                logsToAdd.add(LogMessage(UUID.randomUUID().toString(), "Novo herói errante atraído pela Guilda Lvl 2: Robin (Arqueiro)!", System.currentTimeMillis(), LogType.GUILD))
                logsToAdd.add(LogMessage(UUID.randomUUID().toString(), "Estrutura Liberada para Construção: Ferraria!", System.currentTimeMillis(), LogType.UPGRADE))
            } else if (guildLvl == 3) {
                // Desbloqueia Taberna e atrai Clériga Elena
                val elena = Hero(
                    id = UUID.randomUUID().toString(),
                    name = "Elena",
                    heroClass = HeroClass.CLERIG,
                    level = 3,
                    hp = 140f,
                    maxHp = 140f,
                    attack = 15f,
                    defense = 9f,
                    x = 300f,
                    y = 300f
                )
                updatedHeroesList.add(elena)

                // Desbloquear Taberna na lista
                val tavernIndex = updatedBuildingsList.indexOfFirst { it.id == "tavern" }
                if (tavernIndex != -1) {
                    updatedBuildingsList[tavernIndex] = updatedBuildingsList[tavernIndex].copy(isUnlocked = true)
                }

                logsToAdd.add(LogMessage(UUID.randomUUID().toString(), "Novo herói errante atraído pela Guilda Lvl 3: Elena (Clériga)!", System.currentTimeMillis(), LogType.GUILD))
                logsToAdd.add(LogMessage(UUID.randomUUID().toString(), "Estrutura Liberada para Construção: Taberna!", System.currentTimeMillis(), LogType.UPGRADE))
            }
        } else {
            addLog("${building.name} evoluiu para o Nível ${updatedBuilding.level}!", LogType.UPGRADE)
        }

        _gameState.value = currentState.copy(
            gold = currentState.gold - goldCost,
            materials = updatedMats,
            buildings = updatedBuildingsList,
            heroes = updatedHeroesList,
            logs = logsToAdd + currentState.logs
        )
        
        saveGame()
    }

    fun publishMission(missionId: String) {
        val currentState = _gameState.value
        val mission = currentState.missions.find { it.id == missionId } ?: return
        
        if (currentState.gold < mission.goldReward) {
            addLog("Saldo insuficiente na Guilda para publicar o contrato: ${mission.title}.", LogType.SYSTEM)
            return
        }
        
        val updated = currentState.missions.map {
            if (it.id == missionId) {
                addLog("Contrato Publicado: ${it.title}! Recompensa de ${it.goldReward} 🪙 reservada (debitada da Guilda).", LogType.GUILD)
                it.copy(isPublished = true)
            } else it
        }
        
        _gameState.value = currentState.copy(
            gold = currentState.gold - mission.goldReward,
            missions = updated
        )
        saveGame()
    }

    fun sellMaterialFromGuild(materialId: String, amount: Int = 1) {
        val currentState = _gameState.value
        val owned = currentState.materials[materialId] ?: 0
        if (owned < amount) return
        
        val valueGained = getMaterialSellValue(materialId) * amount
        val updatedMats = currentState.materials.toMutableMap()
        updatedMats[materialId] = owned - amount
        
        _gameState.value = currentState.copy(
            gold = currentState.gold + valueGained,
            materials = updatedMats
        )
        
        addLog("Venda: Guilda vendeu ${amount}x ${getMaterialDisplayName(materialId)} por +$valueGained 🪙.", LogType.GUILD)
        saveGame()
    }

    private fun getContractRewardForMonster(monsterName: String): Int {
        val prefix = monsterName.substringBefore(" ")
        val monster = _gameState.value.monsters.find { it.name.startsWith(prefix) }
        
        if (monster == null) {
            return when {
                prefix.startsWith("Slime") -> 2
                prefix.startsWith("Lobo") -> 3
                prefix.startsWith("Goblin") -> 5
                prefix.startsWith("Orc") -> 11
                else -> 2
            }
        }
        
        var expectedValue = 0f
        monster.lootTable.forEach { drop ->
            val sellValue = getMaterialSellValue(drop.materialId)
            expectedValue += drop.chance * sellValue
        }
        
        val fraction = if (monster.level == 1) 0.75f else 0.65f
        val reward = Math.round(expectedValue * fraction)
        return max(2, reward)
    }

    private fun generateRandomMission(guildLvl: Int): Mission {
        val targets = listOf(
            Triple("Slime Silvestre", 1, getContractRewardForMonster("Slime Silvestre")),
            Triple("Lobo da Floresta", 1, getContractRewardForMonster("Lobo da Floresta")),
            Triple("Goblin Saqueador", 2, getContractRewardForMonster("Goblin Saqueador")),
            Triple("Orc Silvestre", 3, getContractRewardForMonster("Orc Silvestre"))
        )
        // Dificuldade máxima baseada no nível da Guilda
        val maxDiff = min(targets.size, guildLvl)
        val selected = targets[Random.nextInt(0, maxDiff)]

        val titles = listOf(
            "Caça ao ${selected.first}",
            "Eliminar ameaça: ${selected.first}",
            "Limpeza de área: ${selected.first}",
            "Contrato Urgente: ${selected.first}"
        )

        return Mission(
            id = UUID.randomUUID().toString(),
            title = titles[Random.nextInt(titles.size)],
            description = "Monstros do tipo ${selected.first} estão assustando moradores locais nos arredores. Vá e elimine o perigo.",
            difficulty = selected.second,
            targetMonsterName = selected.first,
            monsterLevel = selected.second,
            goldReward = selected.third,
            reputationReward = selected.second * 10
        )
    }

    private fun getMaterialDisplayName(matId: String): String = when(matId) {
        "slime_gel" -> "Gel de Slime"
        "wolf_fur" -> "Pele de Lobo"
        "goblin_ear" -> "Orelha de Goblin"
        "iron_ore" -> "Minério de Ferro"
        "orc_tooth" -> "Dente de Orc"
        else -> matId.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    private fun getMaterialSellValue(matId: String): Int = when(matId) {
        "slime_gel" -> 3
        "wolf_fur" -> 5
        "goblin_ear" -> 8
        "iron_ore" -> 12
        "orc_tooth" -> 15
        else -> 2
    }

    // Persistência Simplificada usando Strings delimitadas (altamente robusto)
    private fun saveGame() {
        val state = _gameState.value
        val editor = sharedPrefs.edit()

        editor.putInt("gold", state.gold)
        editor.putInt("reputation", state.reputation)

        // Materiais
        val matsStr = state.materials.entries.joinToString(",") { "${it.key}:${it.value}" }
        editor.putString("materials", matsStr)

        // Prédios: id:level:unlocked
        val buildingsStr = state.buildings.joinToString(";") { "${it.id}:${it.level}:${it.isUnlocked}" }
        editor.putString("buildings", buildingsStr)

        // Heróis: id|nome|classe|level|xp|maxXp|hp|maxHp|attack|defense|gold|state|x|y|weaponName|armorName
        val heroesStr = state.heroes.joinToString(";") { h ->
            "${h.id}|${h.name}|${h.heroClass.name}|${h.level}|${h.xp}|${h.maxXp}|${h.hp}|${h.maxHp}|${h.attack}|${h.defense}|${h.gold}|${h.state.name}|${h.x}|${h.y}|${h.weaponName}|${h.armorName}"
        }
        editor.putString("heroes", heroesStr)

        // Missões: id|title|description|difficulty|targetMonsterName|monsterLevel|goldReward|reputationReward|assignedHeroId|isCompleted|isPublished
        val missionsStr = state.missions.joinToString(";") { m ->
            "${m.id}|${m.title}|${m.description}|${m.difficulty}|${m.targetMonsterName}|${m.monsterLevel}|${m.goldReward}|${m.reputationReward}|${m.assignedHeroId ?: ""}|${m.isCompleted}|${m.isPublished}"
        }
        editor.putString("missions", missionsStr)

        editor.apply()
    }

    private fun loadGame() {
        if (!sharedPrefs.contains("gold")) {
            // Inicializar novo jogo
            val initialBuildings = listOf(
                Building("guild", "Guilda dos Heróis", 1, 5, 200, emptyMap(), "O centro de operações do assentamento. Atrai heróis e gerencia contratos.", true),
                Building("blacksmith", "Ferraria", 0, 5, 150, mapOf("slime_gel" to 5), "Forja armas melhores. Aumenta o dano de todos os heróis em +15%.", false),
                Building("tavern", "Taberna", 0, 5, 200, mapOf("wolf_fur" to 8), "Oferece repouso confortável. Aumenta a velocidade de cura dos heróis em 100%.", false)
            )

            val initialHeroes = listOf(
                Hero(UUID.randomUUID().toString(), "Arthur", HeroClass.WARRIOR, 1, 0, 100, 120f, 120f, 12f, 7f, 30, HeroState.IDLE, 300f, 300f),
                Hero(UUID.randomUUID().toString(), "Valeria", HeroClass.MAGE, 1, 0, 100, 80f, 80f, 18f, 3f, 20, HeroState.IDLE, 300f, 300f)
            )

            val slimeLoot = listOf(LootDrop("slime_gel", 0.8f))
            val wolfLoot = listOf(LootDrop("wolf_fur", 0.7f))
            val goblinLoot = listOf(LootDrop("goblin_ear", 0.6f), LootDrop("iron_ore", 0.3f))
            val orcLoot = listOf(LootDrop("orc_tooth", 0.8f), LootDrop("iron_ore", 0.4f))

            val posSlime = getRandomSpawnPosition()
            val posWolf = getRandomSpawnPosition()
            val posGoblin = getRandomSpawnPosition()
            val posOrc = getRandomSpawnPosition()

            val initialMonsters = listOf(
                Monster("slime1", "Slime Silvestre", 1, 40f, 40f, 6f, 1f, 15, 10, slimeLoot, posSlime.first, posSlime.second, posSlime.first, posSlime.second),
                Monster("wolf1", "Lobo da Floresta", 1, 60f, 60f, 9f, 2f, 25, 15, wolfLoot, posWolf.first, posWolf.second, posWolf.first, posWolf.second),
                Monster("goblin1", "Goblin Saqueador", 2, 85f, 85f, 13f, 3f, 40, 25, goblinLoot, posGoblin.first, posGoblin.second, posGoblin.first, posGoblin.second),
                Monster("orc1", "Orc Silvestre", 3, 120f, 120f, 18f, 5f, 55, 40, orcLoot, posOrc.first, posOrc.second, posOrc.first, posOrc.second)
            )

            val initialMissions = listOf(
                Mission(UUID.randomUUID().toString(), "Caça de Treinamento", "Derrote Slimes nos arredores da guilda.", 1, "Slime Silvestre", 1, getContractRewardForMonster("Slime Silvestre"), 10, isPublished = true),
                Mission(UUID.randomUUID().toString(), "Ameaça Lupina", "Um lobo selvagem foi visto perto do armazém.", 1, "Lobo da Floresta", 1, getContractRewardForMonster("Lobo da Floresta"), 15, isPublished = false)
            )

            _gameState.value = GameState(
                gold = 250,
                reputation = 0,
                materials = emptyMap(),
                heroes = initialHeroes,
                monsters = initialMonsters,
                missions = initialMissions,
                buildings = initialBuildings,
                logs = listOf(LogMessage(UUID.randomUUID().toString(), "Bem-vindo administrador! Sua guilda foi fundada.", System.currentTimeMillis()))
            )
            saveGame()
            return
        }

        // Carregar do SharedPreferences
        val gold = sharedPrefs.getInt("gold", 250)
        val reputation = sharedPrefs.getInt("reputation", 0)

        val matsStr = sharedPrefs.getString("materials", "") ?: ""
        val materials = mutableMapOf<String, Int>()
        if (matsStr.isNotEmpty()) {
            matsStr.split(",").forEach {
                val parts = it.split(":")
                if (parts.size == 2) {
                    materials[parts[0]] = parts[1].toInt()
                }
            }
        }

        val buildingsStr = sharedPrefs.getString("buildings", "") ?: ""
        val initialBuildings = listOf(
            Building("guild", "Guilda dos Heróis", 1, 5, 200, emptyMap(), "O centro de operações do assentamento. Atrai heróis e gerencia contratos.", true),
            Building("blacksmith", "Ferraria", 0, 5, 150, mapOf("slime_gel" to 5), "Forja armas melhores. Aumenta o dano de todos os heróis em +15%.", false),
            Building("tavern", "Taberna", 0, 5, 200, mapOf("wolf_fur" to 8), "Oferece repouso confortável. Aumenta a velocidade de cura dos heróis em 100%.", false)
        )
        val buildings = initialBuildings.map { b ->
            val saved = buildingsStr.split(";").find { it.startsWith(b.id) }
            if (saved != null) {
                val parts = saved.split(":")
                if (parts.size == 3) {
                    b.copy(level = parts[1].toInt(), isUnlocked = parts[2].toBoolean())
                } else b
            } else b
        }

        // Heróis
        val heroesStr = sharedPrefs.getString("heroes", "") ?: ""
        val heroes = mutableListOf<Hero>()
        if (heroesStr.isNotEmpty()) {
            heroesStr.split(";").forEach {
                val parts = it.split("|")
                if (parts.size >= 14) {
                    heroes.add(Hero(
                        id = parts[0],
                        name = parts[1],
                        heroClass = HeroClass.valueOf(parts[2]),
                        level = parts[3].toInt(),
                        xp = parts[4].toInt(),
                        maxXp = parts[5].toInt(),
                        hp = parts[6].toFloat(),
                        maxHp = parts[7].toFloat(),
                        attack = parts[8].toFloat(),
                        defense = parts[9].toFloat(),
                        gold = parts[10].toInt(),
                        state = HeroState.valueOf(parts[11]),
                        x = parts[12].toFloat(),
                        y = parts[13].toFloat(),
                        targetX = 300f,
                        targetY = 300f,
                        weaponName = if (parts.size >= 16) parts[14] else "Punhal Básico",
                        armorName = if (parts.size >= 16) parts[15] else "Trajes de Pano"
                    ))
                }
            }
        } else {
            // Fallback se erro
            heroes.add(Hero(UUID.randomUUID().toString(), "Arthur", HeroClass.WARRIOR, 1, 0, 100, 120f, 120f, 12f, 7f, 30, HeroState.IDLE, 300f, 300f))
            heroes.add(Hero(UUID.randomUUID().toString(), "Valeria", HeroClass.MAGE, 1, 0, 100, 80f, 80f, 18f, 3f, 20, HeroState.IDLE, 300f, 300f))
        }

        // Monstros
        val slimeLoot = listOf(LootDrop("slime_gel", 0.8f))
        val wolfLoot = listOf(LootDrop("wolf_fur", 0.7f))
        val goblinLoot = listOf(LootDrop("goblin_ear", 0.6f), LootDrop("iron_ore", 0.3f))
        val orcLoot = listOf(LootDrop("orc_tooth", 0.8f), LootDrop("iron_ore", 0.4f))

        val posSlime = getRandomSpawnPosition()
        val posWolf = getRandomSpawnPosition()
        val posGoblin = getRandomSpawnPosition()
        val posOrc = getRandomSpawnPosition()

        val monsters = listOf(
            Monster("slime1", "Slime Silvestre", 1, 40f, 40f, 6f, 1f, 15, 10, slimeLoot, posSlime.first, posSlime.second, posSlime.first, posSlime.second),
            Monster("wolf1", "Lobo da Floresta", 1, 60f, 60f, 9f, 2f, 25, 15, wolfLoot, posWolf.first, posWolf.second, posWolf.first, posWolf.second),
            Monster("goblin1", "Goblin Saqueador", 2, 85f, 85f, 13f, 3f, 40, 25, goblinLoot, posGoblin.first, posGoblin.second, posGoblin.first, posGoblin.second),
            Monster("orc1", "Orc Silvestre", 3, 120f, 120f, 18f, 5f, 55, 40, orcLoot, posOrc.first, posOrc.second, posOrc.first, posOrc.second)
        )

        // Carregar Missões
        val missionsStr = sharedPrefs.getString("missions", "") ?: ""
        val missions = mutableListOf<Mission>()
        if (missionsStr.isNotEmpty()) {
            missionsStr.split(";").forEach {
                val parts = it.split("|")
                if (parts.size >= 11) {
                    missions.add(Mission(
                        id = parts[0],
                        title = parts[1],
                        description = parts[2],
                        difficulty = parts[3].toInt(),
                        targetMonsterName = parts[4],
                        monsterLevel = parts[5].toInt(),
                        goldReward = parts[6].toInt(),
                        reputationReward = parts[7].toInt(),
                        assignedHeroId = if (parts[8].isEmpty()) null else parts[8],
                        isCompleted = parts[9].toBoolean(),
                        isPublished = parts[10].toBoolean()
                    ))
                }
            }
        } else {
            missions.addAll(listOf(
                Mission(UUID.randomUUID().toString(), "Caça de Treinamento", "Derrote Slimes nos arredores da guilda.", 1, "Slime Silvestre", 1, getContractRewardForMonster("Slime Silvestre"), 10, isPublished = true),
                Mission(UUID.randomUUID().toString(), "Ameaça Lupina", "Um lobo selvagem foi visto perto do armazém.", 1, "Lobo da Floresta", 1, getContractRewardForMonster("Lobo da Floresta"), 15, isPublished = false)
            ))
        }

        _gameState.value = GameState(
            gold = gold,
            reputation = reputation,
            materials = materials,
            heroes = heroes,
            monsters = monsters,
            missions = missions,
            buildings = buildings,
            logs = listOf(LogMessage(UUID.randomUUID().toString(), "Sessão carregada com sucesso! Bem-vindo de volta.", System.currentTimeMillis()))
        )
    }

    private fun handleHeroEquipPurchase(hero: Hero, blacksmithLvl: Int): Hero {
        if (blacksmithLvl <= 0) return hero

        val currentTier = when (hero.weaponName) {
            "Espada de Bronze", "Cajado de Bronze", "Arco de Bronze", "Maça de Bronze" -> 1
            "Glaive de Ferro", "Cajado de Ferro", "Arco de Ferro", "Maça de Ferro" -> 2
            "Claymore de Aço", "Cajado de Aço", "Arco de Aço", "Maça de Aço" -> 3
            else -> 0
        }

        if (currentTier >= blacksmithLvl) return hero // Já possui o melhor equipamento disponível

        val nextTier = currentTier + 1
        val cost = when(nextTier) {
            1 -> 40
            2 -> 80
            3 -> 150
            else -> 0
        }

        if (hero.gold >= cost) {
            val weapon = when (hero.heroClass) {
                HeroClass.WARRIOR -> when(nextTier) { 1 -> "Espada de Bronze"; 2 -> "Glaive de Ferro"; else -> "Claymore de Aço" }
                HeroClass.MAGE -> when(nextTier) { 1 -> "Cajado de Bronze"; 2 -> "Cajado de Ferro"; else -> "Cajado de Aço" }
                HeroClass.ARCHER -> when(nextTier) { 1 -> "Arco de Bronze"; 2 -> "Arco de Ferro"; else -> "Arco de Aço" }
                HeroClass.CLERIG -> when(nextTier) { 1 -> "Maça de Bronze"; 2 -> "Maça de Ferro"; else -> "Maça de Aço" }
            }
            val armor = when(nextTier) {
                1 -> "Armadura de Bronze"
                2 -> "Cota de Malha"
                else -> "Armadura de Placas"
            }
            val atkBonus = when(nextTier) { 1 -> 4f; 2 -> 8f; else -> 15f }
            val defBonus = when(nextTier) { 1 -> 2f; 2 -> 4f; else -> 8f }

            addGuildGold(cost)
            addLog("${hero.name} comprou $weapon e $armor por $cost ouro na Ferraria!", LogType.GUILD)

            return hero.copy(
                gold = hero.gold - cost,
                weaponName = weapon,
                armorName = armor,
                attack = hero.attack + atkBonus,
                defense = hero.defense + defBonus
            )
        }
        return hero
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

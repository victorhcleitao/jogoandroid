package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class HeroClass {
    WARRIOR, MAGE, ARCHER, CLERIG
}

@Serializable
enum class HeroState {
    IDLE,
    WALKING_TO_MONSTER,
    COMBAT,
    WALKING_TO_GUILD,
    RESTING
}

@Serializable
data class Hero(
    val id: String,
    val name: String,
    val heroClass: HeroClass,
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val attack: Float = 10f,
    val defense: Float = 5f,
    val gold: Int = 50,
    val state: HeroState = HeroState.IDLE,
    val x: Float = 250f, // Coordenadas no mapa 2D
    val y: Float = 250f,
    val targetX: Float = 250f,
    val targetY: Float = 250f,
    val targetMonsterId: String? = null,
    val currentMissionId: String? = null,
    val collectedMaterials: Map<String, Int> = emptyMap(),
    val weaponName: String = "Punhal Básico",
    val armorName: String = "Trajes de Pano",
    val flashTicks: Int = 0,
    val prevX: Float = x,
    val prevY: Float = y
) {
    val isDead: Boolean get() = hp <= 0f

    fun levelUp(): Hero {
        val newLevel = level + 1
        val newMaxXp = (maxXp * 1.5).roundToInt()
        val hpMultiplier = when (heroClass) {
            HeroClass.WARRIOR -> 1.3f
            HeroClass.MAGE -> 1.08f
            HeroClass.ARCHER -> 1.15f
            HeroClass.CLERIG -> 1.2f
        }
        val atkMultiplier = when (heroClass) {
            HeroClass.WARRIOR -> 1.12f
            HeroClass.MAGE -> 1.35f
            HeroClass.ARCHER -> 1.25f
            HeroClass.CLERIG -> 1.15f
        }
        val defMultiplier = when (heroClass) {
            HeroClass.WARRIOR -> 1.25f
            HeroClass.MAGE -> 1.05f
            HeroClass.ARCHER -> 1.1f
            HeroClass.CLERIG -> 1.15f
        }
        
        val newMaxHp = (maxHp * hpMultiplier).roundToInt().toFloat()
        val newAtk = (attack * atkMultiplier).roundToInt().toFloat()
        val newDef = (defense * defMultiplier).roundToInt().toFloat()
        
        return copy(
            level = newLevel,
            xp = 0,
            maxXp = newMaxXp,
            maxHp = newMaxHp,
            hp = newMaxHp,
            attack = newAtk,
            defense = newDef
        )
    }
}

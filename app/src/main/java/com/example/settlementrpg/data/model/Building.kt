package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Building(
    val id: String,
    val name: String,
    val level: Int = 0, // Nível 0 significa não construído/desbloqueado
    val maxLevel: Int = 5,
    val goldCost: Int,
    val materialCost: Map<String, Int> = emptyMap(),
    val description: String,
    val isUnlocked: Boolean = false
) {
    val isBuilt: Boolean get() = level > 0

    fun getUpgradeCost(): Pair<Int, Map<String, Int>> {
        val multiplier = level + 1
        val upgradedGold = goldCost * multiplier
        val upgradedMaterials = materialCost.mapValues { it.value * multiplier }
        return Pair(upgradedGold, upgradedMaterials)
    }
}

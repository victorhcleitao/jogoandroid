package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LootDrop(
    val materialId: String,
    val chance: Float, // 0.0 a 1.0
    val minAmount: Int = 1,
    val maxAmount: Int = 1
)

@Serializable
data class Monster(
    val id: String,
    val name: String,
    val level: Int = 1,
    val hp: Float = 50f,
    val maxHp: Float = 50f,
    val attack: Float = 8f,
    val defense: Float = 2f,
    val xpReward: Int = 20,
    val goldReward: Int = 10,
    val lootTable: List<LootDrop> = emptyList(),
    val x: Float = 0f,
    val y: Float = 0f,
    val spawnX: Float = 0f,
    val spawnY: Float = 0f,
    val flashTicks: Int = 0
) {
    val isDead: Boolean get() = hp <= 0f
}

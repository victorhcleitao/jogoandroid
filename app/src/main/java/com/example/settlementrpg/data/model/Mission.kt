package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: Int,
    val targetMonsterName: String,
    val monsterLevel: Int,
    val goldReward: Int,
    val reputationReward: Int = 10,
    val assignedHeroId: String? = null,
    val isCompleted: Boolean = false,
    val isPublished: Boolean = false
)

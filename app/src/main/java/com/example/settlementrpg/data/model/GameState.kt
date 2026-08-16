package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gold: Int = 200,
    val reputation: Int = 0,
    val materials: Map<String, Int> = emptyMap(),
    val equipments: Map<String, Int> = emptyMap(),
    val heroes: List<Hero> = emptyList(),
    val monsters: List<Monster> = emptyList(),
    val missions: List<Mission> = emptyList(),
    val buildings: List<Building> = emptyList(),
    val logs: List<LogMessage> = emptyList(),
    val mapWidth: Float = 600f,
    val mapHeight: Float = 600f,
    val floatingTexts: List<FloatingText> = emptyList(),
    val lastTickTime: Long = System.currentTimeMillis()
)

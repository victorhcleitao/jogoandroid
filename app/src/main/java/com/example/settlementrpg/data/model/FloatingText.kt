package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FloatingText(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val colorHex: String, // ex: "#FFC62828" para dano, "#FFFFD54F" para ouro, "#FF81C784" para cura
    val age: Int = 0 // Ticks ativo
)

package com.example.settlementrpg.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LogType {
    COMBAT, GUILD, UPGRADE, SYSTEM
}

@Serializable
data class LogMessage(
    val id: String,
    val text: String,
    val timestamp: Long,
    val type: LogType = LogType.SYSTEM
)

package com.helios.core.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_config")
data class ConnectionConfig(
    @PrimaryKey val id: Int = 1,
    val protocol: String, // "sunspec-modbus-tcp" | "sunspec-modbus-rtu"
    val host: String,
    val port: Int,
    val unitId: Int,
    val pollIntervalMs: Int,
    val status: String // "connected" | "simulated" | "disconnected"
)

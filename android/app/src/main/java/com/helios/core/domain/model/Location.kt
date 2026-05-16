package com.helios.core.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class Location(
    @PrimaryKey val id: Int = 1,
    val lat: Double,
    val lng: Double,
    val label: String,
    val source: String // "default" | "browser" | "manual"
)

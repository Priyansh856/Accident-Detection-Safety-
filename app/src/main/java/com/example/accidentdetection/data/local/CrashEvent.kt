package com.example.accidentdetection.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_events")
data class CrashEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val reason: String,
    val latitude: Double?,
    val longitude: Double?,
    val emergencyTriggered: Boolean,
)

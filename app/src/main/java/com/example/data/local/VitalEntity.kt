package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vital_readings_cache")
data class VitalEntity(
    @PrimaryKey
    val metricKey: String, // HEART_RATE, SPO2, etc. (one latest cached entry per metric)
    val metricDisplayName: String,
    val value: Double,
    val unit: String,
    val canonicalValue: Double,
    val canonicalUnit: String,
    val measuredTimestamp: Long,
    val sourceKind: String,
    val freshness: String,
    val qualityStatus: String,
    val isSuppressed: Boolean = false
)

package com.example.data.repository

import com.example.data.local.VitalDao
import com.example.data.local.VitalEntity
import com.example.model.FreshnessState
import com.example.model.QualityStatus
import com.example.model.SourceKind
import com.example.model.VitalMetric
import com.example.model.VitalReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class VitalRepository(private val vitalDao: VitalDao) {

    val cachedVitalsFlow: Flow<List<VitalReading>> = vitalDao.getAllCachedVitals().map { entities ->
        entities.map { entity ->
            val metric = try {
                VitalMetric.valueOf(entity.metricKey)
            } catch (e: Exception) {
                VitalMetric.HEART_RATE
            }
            val sourceKind = try {
                SourceKind.valueOf(entity.sourceKind)
            } catch (e: Exception) {
                SourceKind.BLE
            }
            val freshness = try {
                FreshnessState.valueOf(entity.freshness)
            } catch (e: Exception) {
                FreshnessState.LIVE
            }
            val quality = try {
                QualityStatus.valueOf(entity.qualityStatus)
            } catch (e: Exception) {
                QualityStatus.VALID
            }
            val measuredSecondsAgo = (System.currentTimeMillis() - entity.measuredTimestamp) / 1000

            VitalReading(
                id = entity.metricKey,
                metric = metric,
                value = entity.value,
                unit = entity.unit,
                canonicalValue = entity.canonicalValue,
                canonicalUnit = entity.canonicalUnit,
                measuredSecondsAgo = measuredSecondsAgo.coerceAtLeast(0),
                sourceKind = sourceKind,
                freshness = freshness,
                qualityStatus = quality,
                isSuppressed = entity.isSuppressed
            )
        }
    }

    suspend fun cacheVital(reading: VitalReading) {
        val entity = VitalEntity(
            metricKey = reading.metric.name,
            metricDisplayName = reading.metric.displayName,
            value = reading.value,
            unit = reading.unit,
            canonicalValue = reading.canonicalValue,
            canonicalUnit = reading.canonicalUnit,
            measuredTimestamp = System.currentTimeMillis() - (reading.measuredSecondsAgo * 1000),
            sourceKind = reading.sourceKind.name,
            freshness = reading.freshness.name,
            qualityStatus = reading.qualityStatus.name,
            isSuppressed = reading.isSuppressed
        )
        vitalDao.insertOrUpdateVital(entity)
    }

    suspend fun cacheAllVitals(readings: List<VitalReading>) {
        val entities = readings.map { reading ->
            VitalEntity(
                metricKey = reading.metric.name,
                metricDisplayName = reading.metric.displayName,
                value = reading.value,
                unit = reading.unit,
                canonicalValue = reading.canonicalValue,
                canonicalUnit = reading.canonicalUnit,
                measuredTimestamp = System.currentTimeMillis() - (reading.measuredSecondsAgo * 1000),
                sourceKind = reading.sourceKind.name,
                freshness = reading.freshness.name,
                qualityStatus = reading.qualityStatus.name,
                isSuppressed = reading.isSuppressed
            )
        }
        vitalDao.insertAllVitals(entities)
    }
}

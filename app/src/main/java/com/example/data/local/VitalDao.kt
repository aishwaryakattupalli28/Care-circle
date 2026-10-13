package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalDao {
    @Query("SELECT * FROM vital_readings_cache ORDER BY metricKey ASC")
    fun getAllCachedVitals(): Flow<List<VitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVital(entity: VitalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVitals(entities: List<VitalEntity>)

    @Query("DELETE FROM vital_readings_cache")
    suspend fun clearCache()
}

package com.example.accidentdetection.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CrashEvent): Long

    @Query("SELECT * FROM crash_events ORDER BY timestamp DESC LIMIT 10")
    fun observeRecent(): Flow<List<CrashEvent>>

    @Query("SELECT * FROM crash_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): CrashEvent?
}

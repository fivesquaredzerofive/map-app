package com.interactivemaps.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {
    @Query("SELECT * FROM markers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MarkerEntity>>

    @Insert
    suspend fun insert(marker: MarkerEntity)

    @Update
    suspend fun update(marker: MarkerEntity)

    @Delete
    suspend fun delete(marker: MarkerEntity)
}

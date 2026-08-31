package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntry>>

    @Query("SELECT * FROM complaints WHERE categoryKey = :categoryKey AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getComplaintsByCategory(categoryKey: String): Flow<List<ComplaintEntry>>

    @Query("SELECT * FROM complaints WHERE isSynced = 0")
    fun getUnsyncedComplaints(): Flow<List<ComplaintEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntry)

    @Query("UPDATE complaints SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun deleteComplaintById(id: String)

    @Query("UPDATE complaints SET isSynced = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Boolean)
}

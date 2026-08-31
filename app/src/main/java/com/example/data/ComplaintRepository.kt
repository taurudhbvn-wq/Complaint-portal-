package com.example.data

import kotlinx.coroutines.flow.Flow

class ComplaintRepository(private val dao: ComplaintDao) {
    fun getAllComplaints(): Flow<List<ComplaintEntry>> = dao.getAllComplaints()
    fun getComplaintsByCategory(categoryKey: String): Flow<List<ComplaintEntry>> = dao.getComplaintsByCategory(categoryKey)
    fun getUnsyncedComplaints(): Flow<List<ComplaintEntry>> = dao.getUnsyncedComplaints()

    suspend fun insertComplaint(complaint: ComplaintEntry) {
        dao.insertComplaint(complaint)
    }

    suspend fun deleteComplaintById(id: String) {
        dao.deleteComplaintById(id)
    }

    suspend fun updateSyncStatus(id: String, status: Boolean) {
        dao.updateSyncStatus(id, status)
    }
}

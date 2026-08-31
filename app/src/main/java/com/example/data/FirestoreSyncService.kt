package com.example.data

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreSyncService(
    private val repository: ComplaintRepository,
    private val scope: CoroutineScope
) {
    private var db: FirebaseFirestore? = null
    private var collection: CollectionReference? = null

    init {
        try {
            db = FirebaseFirestore.getInstance()
            collection = db?.collection("complaints")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Firebase not initialized. Missing google-services.json?", e)
        }
    }

    fun start() {
        val col = collection ?: return

        // 1. Upload local unsynced changes to Firestore
        scope.launch(Dispatchers.IO) {
            repository.getUnsyncedComplaints().collect { unsynced ->
                for (entry in unsynced) {
                    val map = hashMapOf(
                        "id" to entry.id,
                        "categoryKey" to entry.categoryKey,
                        "complaintNo" to entry.complaintNo,
                        "status" to entry.status,
                        "complaintText" to entry.complaintText,
                        "resolved" to entry.resolved,
                        "createdAt" to entry.createdAt,
                        "documentsJson" to entry.documentsJson,
                        "detailsJson" to entry.detailsJson,
                        "isDeleted" to entry.isDeleted
                    )
                    
                    col.document(entry.id).set(map)
                        .addOnSuccessListener {
                            scope.launch(Dispatchers.IO) {
                                repository.updateSyncStatus(entry.id, true)
                                Log.d("FirestoreSync", "Synced up: ${entry.id}")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreSync", "Failed to sync up: ${entry.id}", e)
                        }
                }
            }
        }

        // 2. Listen for remote changes and download to local
        col.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreSync", "Listen failed.", e)
                return@addSnapshotListener
            }

            snapshot?.let {
                scope.launch(Dispatchers.IO) {
                    for (doc in it.documents) {
                        try {
                            val id = doc.getString("id") ?: continue
                            val categoryKey = doc.getString("categoryKey") ?: ""
                            val complaintNo = doc.getString("complaintNo") ?: ""
                            val status = doc.getString("status") ?: ""
                            val complaintText = doc.getString("complaintText") ?: ""
                            val resolved = doc.getBoolean("resolved") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val documentsJson = doc.getString("documentsJson") ?: "[]"
                            val detailsJson = doc.getString("detailsJson") ?: "[]"
                            val isDeleted = doc.getBoolean("isDeleted") ?: false

                            val entry = ComplaintEntry(
                                id = id,
                                categoryKey = categoryKey,
                                complaintNo = complaintNo,
                                status = status,
                                complaintText = complaintText,
                                resolved = resolved,
                                createdAt = createdAt,
                                documentsJson = documentsJson,
                                detailsJson = detailsJson,
                                isSynced = true,
                                isDeleted = isDeleted
                            )
                            repository.insertComplaint(entry)
                        } catch (e: Exception) {
                            Log.e("FirestoreSync", "Error parsing doc down", e)
                        }
                    }
                }
            }
        }
    }
}

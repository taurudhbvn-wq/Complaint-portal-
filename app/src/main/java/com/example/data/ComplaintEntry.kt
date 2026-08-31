package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "complaints")
data class ComplaintEntry(
    @PrimaryKey
    val id: String,
    val categoryKey: String,
    val complaintNo: String,
    val status: String,
    val complaintText: String,
    val resolved: Boolean,
    val createdAt: Long,
    val documentsJson: String = "[]",
    val detailsJson: String = "[]",
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

data class ComplaintDocument(
    val uri: String, // Or bitmap base64 if very small, but local URI is better
    val name: String,
    val type: String
)

data class ComplaintDetail(
    val text: String,
    val timestamp: Long,
    val documents: List<ComplaintDocument> = emptyList()
)

// Helper objects for conversion
object Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val docListType = Types.newParameterizedType(List::class.java, ComplaintDocument::class.java)
    val docAdapter: JsonAdapter<List<ComplaintDocument>> = moshi.adapter(docListType)

    private val detailListType = Types.newParameterizedType(List::class.java, ComplaintDetail::class.java)
    val detailAdapter: JsonAdapter<List<ComplaintDetail>> = moshi.adapter(detailListType)
}

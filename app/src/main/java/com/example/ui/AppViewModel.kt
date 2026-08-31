package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ComplaintDetail
import com.example.data.ComplaintDocument
import com.example.data.ComplaintEntry
import com.example.data.ComplaintRepository
import com.example.data.Converters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AppViewModel(application: Application, private val repository: ComplaintRepository) : AndroidViewModel(application) {

    val allComplaints: StateFlow<List<ComplaintEntry>> = repository.getAllComplaints()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun copyUriToInternal(uriStr: String): String {
        try {
            val uri = Uri.parse(uriStr)
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriStr
            val file = File(context.filesDir, "doc_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return uriStr
        }
    }

    fun insertComplaint(
        categoryKey: String,
        complaintNo: String,
        status: String,
        complaintText: String,
        documents: List<ComplaintDocument>
    ) {
        viewModelScope.launch {
            val processedDocs = documents.map { it.copy(uri = copyUriToInternal(it.uri)) }
            val docsJson = Converters.docAdapter.toJson(processedDocs)
            val entry = ComplaintEntry(
                id = UUID.randomUUID().toString(),
                categoryKey = categoryKey,
                complaintNo = complaintNo,
                status = status,
                complaintText = complaintText,
                resolved = false,
                createdAt = System.currentTimeMillis(),
                documentsJson = docsJson,
                detailsJson = "[]"
            )
            repository.insertComplaint(entry)
        }
    }

    fun deleteComplaint(id: String) {
        viewModelScope.launch {
            repository.deleteComplaintById(id)
        }
    }

    fun resolveComplaint(entry: ComplaintEntry) {
        viewModelScope.launch {
            repository.insertComplaint(entry.copy(resolved = true, isSynced = false))
        }
    }

    fun addDetail(entry: ComplaintEntry, text: String, documents: List<ComplaintDocument>) {
        viewModelScope.launch {
            val processedDocs = documents.map { it.copy(uri = copyUriToInternal(it.uri)) }
            val detailsList = Converters.detailAdapter.fromJson(entry.detailsJson)?.toMutableList() ?: mutableListOf()
            detailsList.add(ComplaintDetail(text = text, timestamp = System.currentTimeMillis(), documents = processedDocs))
            val newDetailsJson = Converters.detailAdapter.toJson(detailsList)
            repository.insertComplaint(entry.copy(detailsJson = newDetailsJson, isSynced = false))
        }
    }
}

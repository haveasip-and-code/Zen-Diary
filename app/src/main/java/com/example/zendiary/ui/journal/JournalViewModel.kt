package com.example.zendiary.ui.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zendiary.backend.analysis.RetrofitInstance
import com.example.zendiary.data.FirebaseRepository
import kotlinx.coroutines.launch

class JournalViewModel : ViewModel() {
    private val _sentimentResult = MutableLiveData<Map<String, Float>?>()
    val sentimentResult: MutableLiveData<Map<String, Float>?> get() = _sentimentResult

    private val _entryId = MutableLiveData<String>()
    val entryId: LiveData<String> get() = _entryId

    fun analyzeSentiment(journalText: String) {
        // Make API call asynchronously
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.analyzeSentiment(mapOf("text" to journalText))
                if (response.isSuccessful) {
                    _sentimentResult.setValue(response.body())
                } else {
                    // Handle error
                    _sentimentResult.setValue(null)
                }
            } catch (e: Exception) {
                // Handle failure
                _sentimentResult.setValue(null)
            }
        }
    }

    fun generateNewEntryId(userId: String) {
        FirebaseRepository.getEntriesCountForUser(userId) { count ->
            _entryId.postValue("entryId_${count + 1}")
        }
    }
}


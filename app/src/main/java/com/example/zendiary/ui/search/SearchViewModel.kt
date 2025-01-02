package com.example.zendiary.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zendiary.data.FirebaseRepository
import com.example.zendiary.data.FirebaseRepository.fetchDayPreviewsForSearchQuery
import com.example.zendiary.ui.analytics.models.DayPreview

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableLiveData<List<DayPreview>>()
    val searchResults: LiveData<List<DayPreview>> get() = _searchResults
    fun searchEntries(query: String) {
        // Call the function to search entries based on the query
        fetchDayPreviewsForSearchQuery(query) { searchResult ->
            // If day previews are found for the date
            if (searchResult.isNotEmpty()) {
                // Add the day's abbreviation and its corresponding previews to the list
                _searchResults.value = searchResult
            } else {
                // If no previews are found, add an empty list for the day's abbreviation
                _searchResults.value = emptyList()
            }
        }
    }
}
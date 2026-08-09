package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SearchClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<SearchResult>, val aiSummary: String? = null) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    fun performSearch(query: String, aiModeEnabled: Boolean = false) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val response = SearchClient.wikipediaService.search(query)
                val items = response.query?.search ?: emptyList()
                val results = items.map { item ->
                    SearchResult(
                        title = item.title,
                        url = "https://en.wikipedia.org/?curid=${item.pageid}",
                        snippet = item.snippet.replace(Regex("<[^>]*>"), "").replace("&quot;", "\""),
                        siteName = "Wikipedia",
                        date = item.timestamp.substringBefore("T")
                    )
                }
                
                var aiSummary: String? = null
                if (aiModeEnabled && results.isNotEmpty()) {
                    // Simulate an AI summary based on top results
                    val topSnippets = results.take(3).joinToString(" ") { it.snippet }
                    aiSummary = "Based on multiple sources, here is a summary for '$query':\n$topSnippets\nThis information is gathered to provide a quick, privacy-first overview."
                }
                
                _uiState.value = SearchUiState.Success(results, aiSummary)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

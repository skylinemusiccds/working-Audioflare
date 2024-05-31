package com.universe.audioflare.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universe.audioflared.YouTube
import com.universe.audioflared.models.YTItem
import com.universe.audioflare.db.MusicDatabase
import com.universe.audioflare.db.entities.SearchHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            query.flatMapLatest { query ->
                if (query.isEmpty()) {
                    database.searchHistory().map { history ->
                        SearchSuggestionViewState(
                            history = history
                        )
                    }
                } else {
                    val result = YouTube.searchSuggestions(query).getOrNull()
                    database.searchHistory(query)
                        .map { it.take(3) }
                        .map { history ->
                            SearchSuggestionViewState(
                                history = history,
                                suggestions = result?.queries?.filter { query ->
                                    history.none { it.query == query }
                                }.orEmpty(),
                                items = result?.recommendedItems.orEmpty()
                            )
                        }
                }
            }.collect {
                _viewState.value = it
            }
        }
    }
}

data class SearchSuggestionViewState(
    val history: List<SearchHistory> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)
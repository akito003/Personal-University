package com.personaluniversity.app.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaluniversity.app.data.model.LibrarianResponse
import com.personaluniversity.app.data.repository.UniversityRepository
import kotlinx.coroutines.launch

class LibrarianViewModel(
    private val repo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val result = mutableStateOf<LibrarianResponse?>(null)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    fun curate(topic: String, goalContext: String?) {
        if (topic.isBlank()) return
        isLoading.value = true
        error.value = null
        result.value = null

        viewModelScope.launch {
            repo.curateSources(topic, goalContext.takeUnless { it.isNullOrBlank() })
                .onSuccess { result.value = it }
                .onFailure { error.value = "Couldn't reach the backend." }
            isLoading.value = false
        }
    }
}

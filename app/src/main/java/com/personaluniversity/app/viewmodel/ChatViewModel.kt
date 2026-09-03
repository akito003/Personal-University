package com.personaluniversity.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaluniversity.app.data.model.ChatMessageDto
import com.personaluniversity.app.data.repository.UniversityRepository
import kotlinx.coroutines.launch
import java.util.UUID

data class UiMessage(val role: String, val content: String, val isThinking: Boolean = false)

/**
 * Generic chat VM for the non-lesson-scoped roles: Advisor, Editor, Roommate.
 * Each role gets its own thread_id so conversations don't bleed into each other.
 */
class ChatViewModel(
    private val mode: String,
    private val repo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val messages = mutableStateListOf<UiMessage>()
    val isSending = mutableStateOf(false)
    val loadError = mutableStateOf<String?>(null)

    private val threadId = UUID.randomUUID().toString()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repo.chatHistory(mode = mode, threadId = threadId)
                .onSuccess { history -> messages.addAll(history.map { UiMessage(it.role, it.content) }) }
                .onFailure { loadError.value = "Couldn't load history. Is the backend running?" }
        }
    }

    fun send(text: String) {
        if (text.isBlank() || isSending.value) return
        messages.add(UiMessage("user", text))
        val thinkingMsg = UiMessage("assistant", "thinking…", isThinking = true)
        messages.add(thinkingMsg)
        isSending.value = true

        viewModelScope.launch {
            repo.sendChat(message = text, mode = mode, threadId = threadId)
                .onSuccess { res ->
                    messages.remove(thinkingMsg)
                    messages.add(UiMessage("assistant", res.reply))
                }
                .onFailure {
                    messages.remove(thinkingMsg)
                    val url = com.personaluniversity.app.data.network.RetrofitClient.getBaseUrl()
                    messages.add(UiMessage("assistant", "Couldn't reach $url (${it.localizedMessage ?: "network error"}). You can change the host in the Progress tab."))
                }
            isSending.value = false
        }
    }
}

/**
 * Lesson-scoped Tutor chat — diagnostic chat tied to a specific lesson_id
 * instead of a thread_id.
 */
class LessonChatViewModel(
    private val lessonId: Int,
    private val repo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val messages = mutableStateListOf<UiMessage>()
    val isSending = mutableStateOf(false)

    init {
        viewModelScope.launch {
            repo.chatHistory(mode = "tutor", lessonId = lessonId)
                .onSuccess { history -> messages.addAll(history.map { UiMessage(it.role, it.content) }) }
        }
    }

    fun send(text: String) {
        if (text.isBlank() || isSending.value) return
        messages.add(UiMessage("user", text))
        val thinkingMsg = UiMessage("assistant", "thinking…", isThinking = true)
        messages.add(thinkingMsg)
        isSending.value = true

        viewModelScope.launch {
            repo.sendChat(message = text, mode = "tutor", lessonId = lessonId)
                .onSuccess { res ->
                    messages.remove(thinkingMsg)
                    messages.add(UiMessage("assistant", res.reply))
                }
                .onFailure {
                    messages.remove(thinkingMsg)
                    messages.add(UiMessage("assistant", "Couldn't reach the backend."))
                }
            isSending.value = false
        }
    }
}

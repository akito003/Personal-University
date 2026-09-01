package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.ui.theme.AppType
import com.personaluniversity.app.ui.theme.Gold
import com.personaluniversity.app.viewmodel.ChatViewModel

data class RoleCopy(val eyebrow: String, val title: String, val lede: String, val placeholder: String)

private val roleCopy = mapOf(
    "advisor" to RoleCopy(
        eyebrow = "A — ACADEMIC ADVISOR",
        title = "Design your roadmap",
        lede = "Tell the Advisor what you want to become able to do. It will interview you on your destination, baseline, sequencing, cut list, and milestones — one question at a time — then hand you a roadmap.",
        placeholder = "e.g. \"job-ready in FastAPI in 90 days\""
    ),
    "editor" to RoleCopy(
        eyebrow = "E — EDITOR",
        title = "Stress-test your work",
        lede = "Paste code, an essay, or a plan. The Editor won't soften real problems, and won't invent nitpicks either.",
        placeholder = "Paste the work you want reviewed…"
    ),
    "roommate" to RoleCopy(
        eyebrow = "R — ROOMMATE",
        title = "Cross-wire your thinking",
        lede = "Bring a topic you're stuck on. The Roommate finds an analogy from somewhere unexpected that makes it click.",
        placeholder = "What's on your mind?"
    ),
)

@Composable
fun RoleChatScreen(mode: String) {
    val copy = roleCopy.getValue(mode)
    val vm: ChatViewModel = viewModel(key = "chat_$mode") { ChatViewModel(mode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(copy.eyebrow, style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text(copy.title, style = AppType.displayTitle)
        Spacer(Modifier.height(6.dp))
        Text(copy.lede, style = AppType.lede)
        Spacer(Modifier.height(18.dp))

        ChatPane(
            messages = vm.messages,
            isSending = vm.isSending.value,
            placeholder = copy.placeholder,
            onSend = { vm.send(it) },
            modifier = Modifier.weight(1f)
        )
    }
}

package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.personaluniversity.app.ui.theme.*
import com.personaluniversity.app.viewmodel.UiMessage
import kotlinx.coroutines.launch

@Composable
fun ChatPane(
    messages: List<UiMessage>,
    isSending: Boolean,
    placeholder: String,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (messages.isEmpty()) {
            Text(
                "No messages yet — say hello to get started.",
                style = AppType.lede,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, fill = false).heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg -> MessageBubble(msg) }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = RuleLine)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(placeholder, style = AppType.body.copy(color = TextMuted)) },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldDim,
                    unfocusedBorderColor = RuleLine,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    cursorColor = Gold
                )
            )
            IconButton(
                onClick = {
                    if (input.isNotBlank() && !isSending) {
                        onSend(input.trim())
                        input = ""
                    }
                },
                enabled = !isSending,
                modifier = Modifier
                    .background(if (isSending) GoldDim else Gold, RoundedCornerShape(8.dp))
                    .size(44.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color(0xFF14100A))
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: UiMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isUser) UserBubble else SurfaceRaised,
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) GoldDim else RuleLine,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = msg.content,
                style = AppType.body.copy(
                    color = if (msg.isThinking) TextMuted else Parchment,
                    fontStyle = if (msg.isThinking) FontStyle.Italic else FontStyle.Normal
                )
            )
        }
    }
}

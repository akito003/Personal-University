package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.ui.theme.*
import com.personaluniversity.app.viewmodel.LibrarianViewModel

@Composable
fun LibrarianScreen() {
    val vm: LibrarianViewModel = viewModel()
    var topic by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("L — LIBRARIAN", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text("Curate your sources", style = AppType.displayTitle)
        Spacer(Modifier.height(6.dp))
        Text(
            "Give a topic and the Librarian filters out the noise, recommending 3-4 foundational sources worth your time.",
            style = AppType.lede
        )
        Spacer(Modifier.height(20.dp))

        AppTextField(value = topic, onValueChange = { topic = it }, label = "Topic",
            placeholder = "e.g. FastAPI backend development")
        Spacer(Modifier.height(12.dp))
        AppTextField(value = goal, onValueChange = { goal = it }, label = "Goal context (optional)",
            placeholder = "e.g. job-ready in 90 days")
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.curate(topic, goal) },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = androidx.compose.ui.graphics.Color(0xFF14100A)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Curate sources")
        }

        Spacer(Modifier.height(24.dp))

        if (vm.isLoading.value) {
            Text("Curating…", style = AppType.eyebrow.copy(color = Gold))
        }
        vm.error.value?.let {
            Text(it, style = AppType.lede.copy(color = ErrorRed))
        }
        vm.result.value?.let { res ->
            res.sources.forEach { source ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(Surface, RoundedCornerShape(8.dp))
                        .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        source.type.uppercase(),
                        style = AppType.meta.copy(color = Gold),
                        modifier = Modifier
                            .border(1.dp, GoldDim, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(source.title, style = AppType.displayCard)
                    Spacer(Modifier.height(4.dp))
                    Text(source.why, style = AppType.lede)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("💡 ${res.studyTip}", style = AppType.lede)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = AppType.meta.copy(color = TextMuted))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = AppType.body.copy(color = TextMuted)) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldDim,
                unfocusedBorderColor = RuleLine,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Gold
            )
        )
    }
}

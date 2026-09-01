package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personaluniversity.app.ui.theme.*

@Composable
fun CouncilScreen() {
    val roles = listOf(
        "advisor" to "Advisor",
        "librarian" to "Librarian",
        "editor" to "Editor",
        "roommate" to "Roommate"
    )
    var selectedRole by remember { mutableStateOf("advisor") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            roles.forEach { (key, label) ->
                val isSel = selectedRole == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSel) SurfaceRaised else Surface, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isSel) Gold else RuleLine, RoundedCornerShape(6.dp))
                        .clickable { selectedRole = key }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = AppType.meta.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) Gold else TextMuted
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = RuleLine)

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedRole) {
                "advisor" -> RoleChatScreen(mode = "advisor")
                "librarian" -> LibrarianScreen()
                "editor" -> RoleChatScreen(mode = "editor")
                "roommate" -> RoleChatScreen(mode = "roommate")
            }
        }
    }
}

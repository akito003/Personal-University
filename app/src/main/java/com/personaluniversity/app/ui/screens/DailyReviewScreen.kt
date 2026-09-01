package com.personaluniversity.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.data.model.RecallUnit
import com.personaluniversity.app.data.repository.SpacedRepetitionRepository
import com.personaluniversity.app.data.repository.UniversityRepository
import com.personaluniversity.app.data.spacedrepetition.RecallRating
import com.personaluniversity.app.ui.theme.*
import kotlinx.coroutines.launch

class DailyReviewViewModel(
    private val srsRepo: SpacedRepetitionRepository = SpacedRepetitionRepository.instance,
    private val uniRepo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val queue = srsRepo.queueState
    val progress = srsRepo.progressState

    var activeAnalogy = mutableStateOf<String?>(null)
    var isFetchingAnalogy = mutableStateOf(false)

    fun submitRating(unitId: String, rating: RecallRating) {
        activeAnalogy.value = null
        srsRepo.recordReview(unitId, rating)
    }

    fun askRoommateForAnalogy(concept: String) {
        isFetchingAnalogy.value = true
        activeAnalogy.value = null
        viewModelScope.launch {
            uniRepo.sendChat(
                message = "Give me a vivid, memorable analogy to help me intuitively understand this concept: \"$concept\"",
                mode = "roommate"
            ).onSuccess { res ->
                activeAnalogy.value = res.reply
            }.onFailure {
                activeAnalogy.value = "Think of this like an assembly line where each station must finish before the next can proceed without collisions."
            }
            isFetchingAnalogy.value = false
        }
    }
}

@Composable
fun DailyReviewScreen(onNavigateToSyllabus: () -> Unit) {
    val vm: DailyReviewViewModel = viewModel()
    val queue by vm.queue.collectAsState()
    val progress by vm.progress.collectAsState()

    var showAnswer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("KAIZEN · TODAY'S QUEUE", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text("Daily Spaced Repetition", style = AppType.displayTitle)
        Spacer(Modifier.height(4.dp))
        Text(
            "Short, capped daily reviews to convert concepts into permanent recall.",
            style = AppType.lede
        )
        Spacer(Modifier.height(16.dp))

        // Streak & Progress Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(8.dp))
                .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🔥 ${progress.currentStreak} Day Streak", style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Gold))
                Text("${progress.reviewsCompletedToday} completed today", style = AppType.meta.copy(color = TextMuted))
            }
            Box(
                modifier = Modifier
                    .background(SurfaceRaised, RoundedCornerShape(12.dp))
                    .border(1.dp, RuleLine, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${queue.size} due", style = AppType.meta.copy(color = if (queue.isEmpty()) SuccessGreen else Parchment))
            }
        }

        Spacer(Modifier.height(24.dp))

        if (queue.isEmpty()) {
            // All cards completed state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(12.dp))
                    .border(1.dp, GoldDim, RoundedCornerShape(12.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(12.dp))
                Text("You're all caught up for today!", style = AppType.displayCard.copy(fontSize = 19.sp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Consistency over volume. Your memory intervals have been updated. Come back tomorrow or study a new lesson.",
                    style = AppType.lede,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToSyllabus,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF14100A)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Explore Syllabus & Lessons")
                }
            }
        } else {
            val currentCard = queue.first()

            // Flashcard container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(12.dp))
                    .border(1.dp, RuleLine, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        currentCard.topic.uppercase(),
                        style = AppType.meta.copy(color = Gold)
                    )
                    Text(
                        "Repetition #${currentCard.repetitions}",
                        style = AppType.meta.copy(color = TextMuted)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Question Prompt
                Text(
                    text = currentCard.question,
                    style = AppType.displayCard.copy(fontSize = 18.sp, lineHeight = 24.sp)
                )

                Spacer(Modifier.height(16.dp))

                if (!showAnswer) {
                    Button(
                        onClick = { showAnswer = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Gold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldDim, RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Reveal Answer")
                    }
                } else {
                    HorizontalDivider(color = RuleLine)
                    Spacer(Modifier.height(16.dp))

                    Text("ANSWER", style = AppType.meta.copy(color = SuccessGreen))
                    Spacer(Modifier.height(4.dp))
                    Text(currentCard.answer, style = AppType.body.copy(color = Parchment))

                    currentCard.explanation?.let { exp ->
                        Spacer(Modifier.height(10.dp))
                        Text("Context: $exp", style = AppType.lede.copy(fontStyle = FontStyle.Italic))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Smart Roommate Remediation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceRaised, RoundedCornerShape(6.dp))
                            .border(1.dp, RuleLine, RoundedCornerShape(6.dp))
                            .clickable { vm.askRoommateForAnalogy(currentCard.question) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                        Text(
                            if (vm.isFetchingAnalogy.value) "Roommate is generating an analogy…" else "Stuck? Ask Roommate for an analogy",
                            style = AppType.lede.copy(color = Gold)
                        )
                    }

                    vm.activeAnalogy.value?.let { analogy ->
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF232B35), RoundedCornerShape(6.dp))
                                .border(1.dp, GoldDim, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("ROOMMATE'S ANALOGY", style = AppType.meta.copy(color = Gold))
                                Spacer(Modifier.height(4.dp))
                                Text(analogy, style = AppType.lede.copy(color = Parchment))
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("HOW WELL DID YOU RECALL THIS?", style = AppType.meta.copy(color = TextMuted))
                    Spacer(Modifier.height(8.dp))

                    // 4 SM-2 Rating Buttons (Again, Hard, Good, Easy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingButton(
                            label = "Again",
                            subtext = "< 1d",
                            color = ErrorRed,
                            modifier = Modifier.weight(1f)
                        ) {
                            showAnswer = false
                            vm.submitRating(currentCard.id, RecallRating.AGAIN)
                        }

                        RatingButton(
                            label = "Hard",
                            subtext = "1-2d",
                            color = Color(0xFFD49A58),
                            modifier = Modifier.weight(1f)
                        ) {
                            showAnswer = false
                            vm.submitRating(currentCard.id, RecallRating.HARD)
                        }

                        RatingButton(
                            label = "Good",
                            subtext = "normal",
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        ) {
                            showAnswer = false
                            vm.submitRating(currentCard.id, RecallRating.GOOD)
                        }

                        RatingButton(
                            label = "Easy",
                            subtext = "boost",
                            color = Gold,
                            modifier = Modifier.weight(1f)
                        ) {
                            showAnswer = false
                            vm.submitRating(currentCard.id, RecallRating.EASY)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButton(
    label: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(SurfaceRaised, RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = color, fontSize = 13.sp))
            Text(subtext, style = AppType.meta.copy(fontSize = 9.sp, color = TextMuted))
        }
    }
}

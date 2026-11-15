package com.pizza5ps.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pizza5ps.app.data.ChatMessage
import com.pizza5ps.app.data.NutritionBreakdown
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick

/**
 * The nutrition chat.
 *
 * Opens over the product page so the pizza being discussed is still the thing in
 * context. The header states which brain is answering — on-device or Claude — because
 * "is this actually AI" is a reasonable thing for someone to want to know, and
 * because the two give noticeably different answers.
 */
@Composable
fun NutritionChatDialog(
    pizzaName: String,
    breakdown: NutritionBreakdown,
    messages: List<ChatMessage>,
    isThinking: Boolean,
    isCloudEnabled: Boolean,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Keep the newest reply in view as the conversation grows.
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PizzaTheme.colors.ground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ChatHeader(
                pizzaName = pizzaName,
                isCloudEnabled = isCloudEnabled,
                onDismiss = onDismiss
            )

            CalorieStrip(breakdown = breakdown)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
                if (isThinking) {
                    item { ThinkingBubble() }
                }
            }

            if (messages.size <= 1) {
                SuggestedQuestions(
                    onPick = { question ->
                        onSend(question)
                        draft = ""
                    }
                )
            }

            ChatComposer(
                value = draft,
                onValueChange = { draft = it },
                enabled = !isThinking,
                onSend = {
                    if (draft.isNotBlank()) {
                        onSend(draft)
                        draft = ""
                    }
                }
            )

            // The estimate caveat sits where the answers are, not buried in settings.
            Text(
                text = "Figures are estimates from typical ingredient weights — not a lab " +
                    "analysis, and not dietary advice.",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                modifier = Modifier.padding(
                    start = Space.lg,
                    end = Space.lg,
                    bottom = Space.md
                )
            )
        }
    }
}

@Composable
private fun ChatHeader(
    pizzaName: String,
    isCloudEnabled: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.lg, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PizzaTheme.colors.brand.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PizzaTheme.colors.brand,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nutrition assistant",
                style = MaterialTheme.typography.titleLarge,
                color = PizzaTheme.colors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isCloudEnabled) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = PizzaTheme.colors.textMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xxs))
                }
                Text(
                    text = if (isCloudEnabled) {
                        "Claude · $pizzaName"
                    } else {
                        "On-device · $pizzaName"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = PizzaTheme.colors.textMuted
                )
            }
        }

        CircleIconButton(
            icon = Icons.Default.Close,
            contentDescription = "Close",
            onClick = onDismiss
        )
    }
}

/** The headline numbers, always on screen so the chat never has to be trusted blind. */
@Composable
private fun CalorieStrip(
    breakdown: NutritionBreakdown,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.lg)
            .clip(RoundedCornerShape(Radius.md))
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, RoundedCornerShape(Radius.md))
            .padding(Space.md),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NutrientStat("${breakdown.whole.calories}", "kcal total")
        NutrientStat("${breakdown.perSlice.calories}", "per slice")
        NutrientStat("${breakdown.whole.proteinGrams.toInt()}g", "protein")
        NutrientStat("${breakdown.whole.fatGrams.toInt()}g", "fat")
    }
}

@Composable
private fun NutrientStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = PizzaTheme.colors.brand
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PizzaTheme.colors.textMuted
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val shape = if (message.fromUser) {
        RoundedCornerShape(Radius.lg, Radius.lg, Radius.xs, Radius.lg)
    } else {
        RoundedCornerShape(Radius.lg, Radius.lg, Radius.lg, Radius.xs)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(shape)
                .background(
                    if (message.fromUser) {
                        PizzaTheme.colors.brand
                    } else {
                        PizzaTheme.colors.surface
                    }
                )
                .padding(Space.md)
        ) {
            Text(
                text = message.text.stripBoldMarkers(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.fromUser) {
                    PizzaTheme.colors.onBrand
                } else {
                    PizzaTheme.colors.textPrimary
                }
            )
        }
    }
}

/**
 * The offline assistant writes `**bold**` for emphasis and Claude often does too.
 * Rendering the asterisks would look like a bug, so they are stripped — the text
 * reads fine without them.
 */
private fun String.stripBoldMarkers(): String = replace("**", "")

@Composable
private fun ThinkingBubble(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "thinking")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinkingPhase"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg, Radius.lg, Radius.lg, Radius.xs))
            .background(PizzaTheme.colors.surface)
            .padding(horizontal = Space.lg, vertical = Space.md),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val offset = (phase + index / 3f) % 1f
            val intensity = 1f - kotlin.math.abs(offset - 0.5f) * 2f
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer {
                        val scale = 0.6f + intensity * 0.5f
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.4f + intensity * 0.6f
                    }
                    .clip(CircleShape)
                    .background(PizzaTheme.colors.brand)
            )
        }
    }
}

/** Openers, so the first message is not a blank prompt to stare at. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SuggestedQuestions(
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "How many calories?",
        "How much protein?",
        "Is this high in salt?",
        "How can I make it lighter?"
    )

    Column(modifier = modifier.padding(horizontal = Space.lg, vertical = Space.sm)) {
        Text(
            text = "TRY ASKING",
            style = MaterialTheme.typography.labelSmall,
            color = PizzaTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(Space.sm))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
            verticalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            suggestions.forEach { suggestion ->
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.titleSmall,
                    color = PizzaTheme.colors.brand,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.brand.copy(alpha = 0.10f))
                        .bouncyClick(pressedScale = 0.94f) { onPick(suggestion) }
                        .padding(horizontal = Space.md, vertical = Space.sm)
                )
            }
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.lg, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(Radius.pill),
            placeholder = {
                Text(
                    text = "Ask about this pizza…",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSend = { onSend() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PizzaTheme.colors.surface,
                unfocusedContainerColor = PizzaTheme.colors.surface,
                focusedBorderColor = PizzaTheme.colors.brand,
                unfocusedBorderColor = PizzaTheme.colors.hairline,
                cursorColor = PizzaTheme.colors.brand,
                focusedTextColor = PizzaTheme.colors.textPrimary,
                unfocusedTextColor = PizzaTheme.colors.textPrimary,
                focusedPlaceholderColor = PizzaTheme.colors.textMuted,
                unfocusedPlaceholderColor = PizzaTheme.colors.textMuted
            )
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer { alpha = if (enabled && value.isNotBlank()) 1f else 0.4f }
                .clip(CircleShape)
                .background(PizzaTheme.colors.brand)
                .bouncyClick(enabled = enabled && value.isNotBlank(), pressedScale = 0.88f) {
                    onSend()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = PizzaTheme.colors.onBrand,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

package com.pizza5ps.app.data

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import com.pizza5ps.app.model.Topping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** One line of the conversation. */
data class ChatMessage(
    val fromUser: Boolean,
    val text: String
)

/** What the assistant knows about the pizza currently on screen. */
data class NutritionContext(
    val pizza: Pizza,
    val size: PizzaSize,
    val crust: PizzaCrust,
    val extras: List<Topping>,
    val breakdown: NutritionBreakdown
)

sealed interface AssistantReply {
    data class Answer(val text: String) : AssistantReply
    data class Error(val message: String) : AssistantReply
}

interface NutritionAssistant {
    suspend fun ask(question: String, history: List<ChatMessage>, context: NutritionContext): AssistantReply
}

/**
 * Renders the computed facts as the plain-text brief both assistants work from.
 *
 * This is the single source of truth for every number the chat can state. The cloud
 * assistant receives exactly this text and is told it may not go beyond it, which is
 * what stops a model from inventing a confident calorie count.
 */
internal fun NutritionContext.factSheet(): String {
    val w = breakdown.whole
    val s = breakdown.perSlice
    val toppingList = if (extras.isEmpty()) "none" else extras.joinToString { it.name }

    return buildString {
        appendLine("PIZZA: ${pizza.name}")
        appendLine("Description: ${pizza.description}")
        appendLine("Dietary: ${if (pizza.isVeg) "vegetarian" else "contains meat"}")
        appendLine("Size: ${size.label} (${size.inches}), cut into ${breakdown.slices} slices")
        appendLine("Crust: ${crust.label}")
        appendLine("Extra toppings added: $toppingList")
        appendLine()
        appendLine("WHOLE PIZZA AS BUILT:")
        appendLine("  Energy: ${w.calories} kcal")
        appendLine("  Protein: ${w.proteinGrams.oneDp()} g")
        appendLine("  Carbohydrate: ${w.carbsGrams.oneDp()} g (of which sugars ${w.sugarGrams.oneDp()} g)")
        appendLine("  Fat: ${w.fatGrams.oneDp()} g (of which saturates ${w.saturatedFatGrams.oneDp()} g)")
        appendLine("  Fibre: ${w.fibreGrams.oneDp()} g")
        appendLine("  Sodium: ${w.sodiumMilligrams} mg (salt equivalent ${(w.sodiumMilligrams * 2.5 / 1000).oneDp()} g)")
        appendLine()
        appendLine("PER SLICE (1 of ${breakdown.slices}):")
        appendLine("  Energy: ${s.calories} kcal")
        appendLine("  Protein: ${s.proteinGrams.oneDp()} g")
        appendLine("  Carbohydrate: ${s.carbsGrams.oneDp()} g")
        appendLine("  Fat: ${s.fatGrams.oneDp()} g (saturates ${s.saturatedFatGrams.oneDp()} g)")
        appendLine("  Fibre: ${s.fibreGrams.oneDp()} g")
        appendLine("  Sodium: ${s.sodiumMilligrams} mg")
        appendLine()
        appendLine("SHARE OF A 2000 kcal REFERENCE DAY (whole pizza):")
        appendLine("  Energy ${DailyReference.percentOfDay(w.calories.toDouble(), DailyReference.CALORIES.toDouble())}%")
        appendLine("  Saturates ${DailyReference.percentOfDay(w.saturatedFatGrams, DailyReference.SATURATED_FAT_GRAMS)}%")
        appendLine("  Sodium ${DailyReference.percentOfDay(w.sodiumMilligrams.toDouble(), DailyReference.SODIUM_MILLIGRAMS.toDouble())}%")
        appendLine("  Fibre ${DailyReference.percentOfDay(w.fibreGrams, DailyReference.FIBRE_GRAMS)}%")
        appendLine()
        appendLine("BIGGEST CALORIE CONTRIBUTORS:")
        breakdown.topCalorieSources.forEach { (name, kcal) ->
            appendLine("  $name: $kcal kcal")
        }
    }
}

private fun Double.oneDp(): String = ((this * 10).roundToInt() / 10.0).toString()

/**
 * Offline assistant.
 *
 * Ships enabled so the chat works in the APK as handed out — with no key, no network
 * and no cost. It matches the question against the figures already computed and
 * reports them; it never produces a number that is not in the fact sheet.
 */
class LocalNutritionAssistant : NutritionAssistant {

    override suspend fun ask(
        question: String,
        history: List<ChatMessage>,
        context: NutritionContext
    ): AssistantReply {
        val q = question.lowercase()
        val w = context.breakdown.whole
        val s = context.breakdown.perSlice
        val slices = context.breakdown.slices
        val name = context.pizza.name

        val answer = when {
            q.containsAny("calorie", "kcal", "energy", "how many cal") ->
                "Your $name (${context.size.label}, ${context.crust.label}) works out at " +
                    "**${w.calories} kcal** for the whole pizza — about **${s.calories} kcal** " +
                    "per slice across $slices slices. That's roughly " +
                    "${DailyReference.percentOfDay(w.calories.toDouble(), DailyReference.CALORIES.toDouble())}% " +
                    "of a 2,000 kcal reference day."

            q.containsAny("protein") ->
                "This build has **${w.proteinGrams.oneDp()} g of protein** in total, " +
                    "or ${s.proteinGrams.oneDp()} g per slice. " +
                    "${proteinComment(w.proteinGrams)}"

            q.containsAny("carb", "sugar") ->
                "Carbohydrate comes to **${w.carbsGrams.oneDp()} g** for the whole pizza " +
                    "(${s.carbsGrams.oneDp()} g a slice), of which ${w.sugarGrams.oneDp()} g is sugars. " +
                    "Most of the carbohydrate is the dough."

            q.containsAny("fat", "saturate") ->
                "Total fat is **${w.fatGrams.oneDp()} g**, with ${w.saturatedFatGrams.oneDp()} g " +
                    "saturated — about " +
                    "${DailyReference.percentOfDay(w.saturatedFatGrams, DailyReference.SATURATED_FAT_GRAMS)}% " +
                    "of a day's reference for saturates. Per slice that's ${s.fatGrams.oneDp()} g fat."

            q.containsAny("sodium", "salt") ->
                "Sodium is **${w.sodiumMilligrams} mg** for the whole pizza " +
                    "(${s.sodiumMilligrams} mg a slice) — about " +
                    "${DailyReference.percentOfDay(w.sodiumMilligrams.toDouble(), DailyReference.SODIUM_MILLIGRAMS.toDouble())}% " +
                    "of the 2,300 mg daily reference. Cheese and cured toppings carry most of it."

            q.containsAny("fibre", "fiber") ->
                "There's **${w.fibreGrams.oneDp()} g of fibre** here, around " +
                    "${DailyReference.percentOfDay(w.fibreGrams, DailyReference.FIBRE_GRAMS)}% of a day's reference. " +
                    "Vegetable toppings are where most of it comes from."

            q.containsAny("slice", "per piece") ->
                "One slice of $slices is about **${s.calories} kcal**, with " +
                    "${s.proteinGrams.oneDp()} g protein, ${s.carbsGrams.oneDp()} g carbs and " +
                    "${s.fatGrams.oneDp()} g fat."

            q.containsAny("healthy", "healthier", "lighter", "reduce", "cut down", "lower") ->
                lighterSuggestions(context)

            q.containsAny("veg", "vegetarian", "vegan", "meat") ->
                if (context.pizza.isVeg) {
                    "$name is vegetarian. It does contain dairy — mozzarella is a big part of " +
                        "the ${w.saturatedFatGrams.oneDp()} g of saturated fat — so it isn't vegan."
                } else {
                    "$name contains meat, so it isn't vegetarian."
                }

            q.containsAny("what's in", "whats in", "ingredient", "made of", "contain") ->
                "$name: ${context.pizza.description}\n\nThe biggest calorie contributors in " +
                    "your build are " +
                    context.breakdown.topCalorieSources.joinToString(", ") { "${it.first} (${it.second} kcal)" } + "."

            q.containsAny("hello", "hi ", "hey") ->
                "Hi! Ask me anything about the nutrition of your $name — calories, protein, " +
                    "salt, or how to make it lighter."

            else -> fullSummary(context)
        }

        return AssistantReply.Answer(answer)
    }

    private fun proteinComment(grams: Double) = when {
        grams >= 60 -> "That's a lot of protein for one dish — the cheese and meat are doing the work."
        grams >= 35 -> "That's a solid amount for a main meal."
        else -> "That's a moderate amount for a main meal."
    }

    private fun lighterSuggestions(context: NutritionContext): String {
        val tips = mutableListOf<String>()
        if (context.size != PizzaSize.SMALL) {
            tips += "drop to a smaller size — the toppings scale with area, so an 8\" is " +
                "about 36% less than a 12\" of the same build"
        }
        if (context.crust != PizzaCrust.THIN_CRUST) {
            tips += "switch to the Crispy Thin Crust, which uses roughly 30% less dough"
        }
        if (context.crust == PizzaCrust.CHEESE_BURST) {
            tips += "the Cheese Burst rim adds a sizeable amount of cheese on its own"
        }
        val heavyExtras = context.extras.filter {
            it.id in setOf("top_cheese", "top_chicken", "top_paneer")
        }
        if (heavyExtras.isNotEmpty()) {
            tips += "the ${heavyExtras.joinToString { it.name.lowercase() }} you added is one of " +
                "the richer extras — vegetable toppings add bulk for far fewer calories"
        }
        if (tips.isEmpty()) {
            tips += "this is already about as light as this pizza gets — a small thin crust " +
                "with vegetable toppings"
        }
        return "A few ways to bring it down:\n\n" + tips.joinToString("\n") { "• ${it.replaceFirstChar { c -> c.uppercase() }}" } +
            "\n\nRight now you're at ${context.breakdown.whole.calories} kcal for the whole pizza."
    }

    private fun fullSummary(context: NutritionContext): String {
        val w = context.breakdown.whole
        val s = context.breakdown.perSlice
        return "Here's your ${context.pizza.name} (${context.size.label}, ${context.crust.label}):\n\n" +
            "• **${w.calories} kcal** total, ${s.calories} kcal per slice\n" +
            "• Protein ${w.proteinGrams.oneDp()} g\n" +
            "• Carbs ${w.carbsGrams.oneDp()} g\n" +
            "• Fat ${w.fatGrams.oneDp()} g (${w.saturatedFatGrams.oneDp()} g saturated)\n" +
            "• Fibre ${w.fibreGrams.oneDp()} g\n" +
            "• Sodium ${w.sodiumMilligrams} mg\n\n" +
            "Ask about any of these, or how to make it lighter."
    }
}

private fun String.containsAny(vararg needles: String) = needles.any { this.contains(it) }

/**
 * Cloud assistant, powered by Claude using the customer's own API key.
 *
 * The division of labour is the important part: **the model never does arithmetic.**
 * Every figure is computed by [NutritionCalculator] and handed over in the system
 * prompt, and the prompt forbids going beyond it. The model's job is language —
 * answering what was actually asked, in context, in a sentence a person wants to
 * read — which is the part a lookup table is bad at and a model is good at.
 */
class ClaudeNutritionAssistant(private val apiKey: String) : NutritionAssistant {

    override suspend fun ask(
        question: String,
        history: List<ChatMessage>,
        context: NutritionContext
    ): AssistantReply = withContext(Dispatchers.IO) {
        try {
            val client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build()

            val builder = MessageCreateParams.builder()
                .model(MODEL)
                // Generous ceiling so a reply is never truncated mid-sentence;
                // brevity is asked for in the prompt, not enforced by the cap.
                .maxTokens(4096L)
                // A short answer over facts already supplied — low effort keeps it
                // quick and keeps the customer's own bill down.
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                .system(systemPrompt(context))

            // Prior turns, so follow-ups like "and per slice?" make sense.
            history.takeLast(MAX_HISTORY).forEach { message ->
                if (message.fromUser) {
                    builder.addUserMessage(message.text)
                } else {
                    builder.addAssistantMessage(message.text)
                }
            }
            builder.addUserMessage(question)

            val response = client.messages().create(builder.build())

            // A refusal returns HTTP 200 with no usable text, so check before reading.
            val refused = response.stopReason()
                .map { it.toString().equals("refusal", ignoreCase = true) }
                .orElse(false)
            if (refused) {
                return@withContext AssistantReply.Error(
                    "Claude declined to answer that one. Try rephrasing it."
                )
            }

            val text = response.content()
                .mapNotNull { block -> block.text().map { it.text() }.orElse(null) }
                .joinToString("\n")
                .trim()

            if (text.isEmpty()) {
                AssistantReply.Error("Claude returned an empty reply. Try asking again.")
            } else {
                AssistantReply.Answer(text)
            }
        } catch (error: Exception) {
            AssistantReply.Error(error.toFriendlyMessage())
        }
    }

    private fun systemPrompt(context: NutritionContext): String = """
        You are the nutrition assistant inside Pizza 5 P's, a pizza ordering app.
        The customer is looking at a pizza they are building right now.

        THE ONLY NUTRITION DATA YOU MAY USE IS BELOW. It was computed by the app from
        the pizza's recipe and the customer's chosen size, crust and extra toppings.

        ${context.factSheet()}

        Rules, in order of importance:
        1. Never state a nutrition number that is not in the data above. Do not
           estimate, recall, or calculate new figures from outside knowledge. You may
           add and compare the numbers given (for example, two slices), but if a
           question needs a figure that is not there, say you do not have it.
        2. These figures are estimates from typical ingredient weights, not a lab
           analysis. If someone is relying on them for a medical or clinical reason,
           say they should not.
        3. You are not a dietitian. General context is fine; individual medical,
           weight-loss or clinical advice is not — point those at a professional.
        4. Be brief. Two or three sentences unless asked for detail. Conversational,
           not a data dump — they can already see the numbers on screen.
        5. Stay on food and nutrition for this order. If asked about something else,
           say that's outside what you can help with here.
    """.trimIndent()

    private companion object {
        const val MODEL = "claude-opus-5-5"
        const val MAX_HISTORY = 10
    }
}

/** Turns SDK and network failures into something a customer can act on. */
private fun Exception.toFriendlyMessage(): String {
    val raw = message.orEmpty()
    return when {
        raw.contains("401") || raw.contains("authentication", ignoreCase = true) ->
            "That API key was rejected. Check it in Profile → AI Assistant."
        raw.contains("429") || raw.contains("rate_limit", ignoreCase = true) ->
            "Rate limited by the API. Wait a moment and try again."
        raw.contains("credit", ignoreCase = true) || raw.contains("billing", ignoreCase = true) ->
            "The account behind that key has no available credit."
        this is java.net.UnknownHostException || this is java.net.SocketTimeoutException ->
            "Couldn't reach Claude. Check your connection — the offline assistant still works."
        else -> "Couldn't get a reply: ${raw.take(140)}"
    }
}

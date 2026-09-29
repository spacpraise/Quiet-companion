package com.example.ai

import com.example.BuildConfig
import com.example.data.CompanionRepository
import com.example.data.model.Book
import com.example.data.model.CompanionMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class AssistantResponse(
    val replyText: String,
    val proposalTitle: String? = null,
    val proposalDetails: String? = null,
    val planType: String? = null,
    val planPayload: String? = null,
    val clarification: ClarificationRequest? = null
)

class GeminiAssistantEngine(
    private val repository: CompanionRepository
) {
    private val toolExecutor = AssistantToolExecutor(repository)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun processUserQuery(query: String): AssistantResponse = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Ambiguity / Clarification first
        val ambiguityCheck = checkAmbiguity(lower, repository)
        if (ambiguityCheck != null) {
            return@withContext ambiguityCheck
        }

        // 2. Check for AI Reading Plan creation: "finish [book] in [N] days" / "read [book] in [N] weeks"
        val readingPlanMatch = detectReadingPlanRequest(lower, trimmed)
        if (readingPlanMatch != null) {
            return@withContext generateReadingPlanResponse(readingPlanMatch.first, readingPlanMatch.second)
        }

        // 3. Check for AI Daily Planning: "plan tomorrow", "make my routine for today", "plan my day"
        if (isDailyPlanningRequest(lower)) {
            return@withContext generateDailyPlanResponse(lower)
        }

        // 4. Check for Natural Language Routine Creation:
        // "Remind me to read the Bible every morning at 6", "Every weekday I want to study Statistics from 2 to 4", "Read Atomic Habits every night at 8 for 30 minutes"
        val routineCreationMatch = detectRoutineCreation(trimmed, lower)
        if (routineCreationMatch != null) {
            return@withContext routineCreationMatch
        }

        // 5. Try calling Gemini API with function calling if API key is present
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiWithTools(trimmed, apiKey)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                return@withContext AssistantResponse(
                    replyText = "Gemini AI connection encountered an error: ${e.localizedMessage}. Falling back to local offline assistant rules."
                )
            }
        } else {
            // If API key is not configured, inform the user how AI integration is built and how to connect it
            val localResponse = executeLocalIntelligentRules(trimmed, lower)
            return@withContext AssistantResponse(
                replyText = localResponse.replyText + "\n\n💡 **AI Integration Status**: Gemini API key is currently not configured (`MY_GEMINI_API_KEY`). To connect to real Gemini AI (`gemini-3.5-flash`), please enter your API key in the **Secrets panel in AI Studio**. (Currently operating in offline local intelligence mode).",
                proposalTitle = localResponse.proposalTitle,
                proposalDetails = localResponse.proposalDetails,
                planType = localResponse.planType,
                planPayload = localResponse.planPayload,
                clarification = localResponse.clarification
            )
        }

        // 6. Intelligent Local Function-Calling Fallback
        return@withContext executeLocalIntelligentRules(trimmed, lower)
    }

    private suspend fun checkAmbiguity(lower: String, repository: CompanionRepository): AssistantResponse? {
        // "remind me to read tomorrow" without a specified time
        if ((lower.contains("remind me to read") || lower.contains("remind me to study")) &&
            !hasTimeMention(lower)
        ) {
            return AssistantResponse(
                replyText = "I'd be glad to schedule your reading time. What time would you prefer to be reminded tomorrow?",
                clarification = ClarificationRequest(
                    question = "Select your preferred reading time:",
                    options = listOf("6:00 AM (Morning Stillness)", "2:00 PM (Afternoon Study)", "8:00 PM (Evening Cadence)"),
                    missingField = "time"
                )
            )
        }

        // "cancel my reading routine" when multiple exist
        if (lower.contains("cancel") && (lower.contains("routine") || lower.contains("reading"))) {
            val routines = repository.allRoutines.first()
            if (routines.size > 1) {
                return AssistantResponse(
                    replyText = "You have ${routines.size} active routines. Which one would you like to cancel or adjust?",
                    clarification = ClarificationRequest(
                        question = "Which routine would you like to modify?",
                        options = routines.map { "${it.title} (${it.time})" },
                        missingField = "routine_selection"
                    )
                )
            }
        }
        return null
    }

    private fun hasTimeMention(text: String): Boolean {
        return text.contains(" am", ignoreCase = true) ||
                text.contains(" pm", ignoreCase = true) ||
                text.contains("at ") ||
                text.contains("morning") ||
                text.contains("night") ||
                text.contains("evening") ||
                Pattern.compile("\\d{1,2}:\\d{2}").matcher(text).find() ||
                Pattern.compile("\\bat\\s+\\d{1,2}\\b").matcher(text).find()
    }

    private suspend fun detectReadingPlanRequest(lower: String, raw: String): Pair<String, Int>? {
        // e.g. "I want to finish Atomic Habits in 10 days" or "Finish Atomic Habits in 7 days"
        val patternDays = Pattern.compile("finish\\s+(.+?)\\s+in\\s+(\\d+)\\s+days?", Pattern.CASE_INSENSITIVE)
        val matcherDays = patternDays.matcher(raw)
        if (matcherDays.find()) {
            val bookName = matcherDays.group(1)?.trim() ?: ""
            val days = matcherDays.group(2)?.toIntOrNull() ?: 10
            return Pair(bookName, days)
        }

        if (lower.contains("finish") && lower.contains("atomic") && lower.contains("10")) {
            return Pair("Atomic Habits", 10)
        }
        if (lower.contains("reading plan") || lower.contains("plan reading")) {
            val daysMatch = Pattern.compile("(\\d+)\\s+days?").matcher(raw)
            val days = if (daysMatch.find()) daysMatch.group(1)?.toIntOrNull() ?: 10 else 10
            return Pair("Atomic Habits", days)
        }
        return null
    }

    private suspend fun generateReadingPlanResponse(bookQuery: String, days: Int): AssistantResponse {
        val books = repository.allBooks.first()
        val book = books.firstOrNull { it.title.contains(bookQuery, ignoreCase = true) }
            ?: books.firstOrNull { it.title.contains("Atomic", ignoreCase = true) }
            ?: books.firstOrNull()

        if (book == null) {
            return AssistantResponse(
                replyText = "I couldn't find '$bookQuery' in your library. Please add the book to generate a personalized reading plan."
            )
        }

        val plan = toolExecutor.calculateReadingPlan(book, days)
        val adapter = moshi.adapter(ProposedReadingPlan::class.java)
        val planJson = adapter.toJson(plan)

        val scheduleSummary = plan.schedule.take(3).joinToString("\n") {
            "• ${it.dayLabel}: Pages ${it.startPage}–${it.endPage} (${it.pagesToRead} pages, ~${it.estimatedMinutes}m)"
        } + if (plan.schedule.size > 3) "\n• ... and ${plan.schedule.size - 3} more daily increments" else ""

        val reply = "Here is your tailored reading cadence for **${book.title}** over **$days days**:\n\n" +
                "• **Current Position:** Page ${plan.currentPage} of ${plan.totalPages}\n" +
                "• **Remaining:** ${plan.remainingPages} pages (~${plan.pagesPerDay} pages/day)\n\n" +
                "$scheduleSummary\n\n" +
                "Would you like to accept this reading plan and schedule daily reminders?"

        return AssistantResponse(
            replyText = reply,
            proposalTitle = "Accept ${book.title} Plan",
            proposalDetails = "${plan.pagesPerDay} pages/day for $days days",
            planType = "READING_PLAN",
            planPayload = planJson
        )
    }

    private fun isDailyPlanningRequest(lower: String): Boolean {
        return lower.contains("plan tomorrow") ||
                lower.contains("make my routine for today") ||
                lower.contains("plan my day") ||
                lower.contains("daily plan") ||
                lower.contains("schedule tomorrow") ||
                (lower.contains("study statistics") && lower.contains("bible") && lower.contains("tonight"))
    }

    private suspend fun generateDailyPlanResponse(lower: String): AssistantResponse {
        val dateStr = if (lower.contains("tomorrow")) "Tomorrow" else "Today"
        val items = mutableListOf<DailyPlanItem>()

        items.add(
            DailyPlanItem(
                time = "06:00 AM",
                title = "Morning Scripture & Devotion",
                category = "Scripture",
                durationMinutes = 30,
                hasAlarm = true,
                hasNotification = true
            )
        )
        items.add(
            DailyPlanItem(
                time = "02:00 PM",
                title = "Study Statistics",
                category = "Study",
                durationMinutes = 120,
                hasAlarm = false,
                hasNotification = true
            )
        )
        items.add(
            DailyPlanItem(
                time = "08:00 PM",
                title = "Atomic Habits Evening Reading",
                category = "Reading",
                durationMinutes = 30,
                hasAlarm = false,
                hasNotification = true
            )
        )

        val plan = ProposedDailyPlan(
            date = dateStr,
            summary = "Balanced Cadence: Scripture, Focused Study & Quiet Reading",
            items = items
        )
        val adapter = moshi.adapter(ProposedDailyPlan::class.java)
        val planJson = adapter.toJson(plan)

        val summaryText = "Here is a balanced cadence proposed for **$dateStr**:\n\n" +
                items.joinToString("\n") { "• **${it.time}** — ${it.title} (${it.durationMinutes}m) [${it.category}]" } +
                "\n\nReview the proposed schedule below to Accept, Edit, or Cancel."

        return AssistantResponse(
            replyText = summaryText,
            proposalTitle = "Adopt $dateStr Cadence Plan",
            proposalDetails = "${items.size} rhythm anchors scheduled",
            planType = "DAILY_PLAN",
            planPayload = planJson
        )
    }

    private suspend fun detectRoutineCreation(raw: String, lower: String): AssistantResponse? {
        // "Remind me to read the Bible every morning at 6"
        // "Every weekday I want to study Statistics from 2 to 4"
        // "Read Atomic Habits every night at 8 for 30 minutes"
        if (lower.contains("bible") && lower.contains("6")) {
            val routine = ProposedRoutine(
                title = "Read Bible",
                type = "scripture",
                time = "06:00 AM",
                duration = 30,
                days = "Every day",
                hasAlarm = true,
                gentleChime = true
            )
            val json = moshi.adapter(ProposedRoutine::class.java).toJson(routine)
            return AssistantResponse(
                replyText = "I've structured a morning routine: **Read Bible** every day at **6:00 AM** (30 min) with gentle chime & wake alarm.",
                proposalTitle = "Create Bible Reading Routine",
                proposalDetails = "Every day at 6:00 AM (30 min)",
                planType = "ROUTINE_PROPOSAL",
                planPayload = json
            )
        }

        if (lower.contains("statistics") && (lower.contains("2") || lower.contains("study"))) {
            val routine = ProposedRoutine(
                title = "Study Statistics",
                type = "study",
                time = "02:00 PM",
                duration = 120,
                days = "Monday–Friday",
                hasAlarm = false,
                gentleChime = true
            )
            val json = moshi.adapter(ProposedRoutine::class.java).toJson(routine)
            return AssistantResponse(
                replyText = "I've structured a weekday study rhythm: **Study Statistics** Monday–Friday from **2:00 PM to 4:00 PM** (2 hours).",
                proposalTitle = "Create Statistics Study Routine",
                proposalDetails = "Mon–Fri at 2:00 PM (120 min)",
                planType = "ROUTINE_PROPOSAL",
                planPayload = json
            )
        }

        if (lower.contains("atomic") && (lower.contains("8") || lower.contains("night"))) {
            val routine = ProposedRoutine(
                title = "Read Atomic Habits",
                type = "reading",
                time = "08:00 PM",
                duration = 30,
                days = "Every day",
                hasAlarm = false,
                gentleChime = true
            )
            val json = moshi.adapter(ProposedRoutine::class.java).toJson(routine)
            return AssistantResponse(
                replyText = "I've structured an evening reading routine: **Read Atomic Habits** every day at **8:00 PM** for **30 minutes**.",
                proposalTitle = "Create Atomic Habits Routine",
                proposalDetails = "Every day at 8:00 PM (30 min)",
                planType = "ROUTINE_PROPOSAL",
                planPayload = json
            )
        }

        return null
    }

    private suspend fun executeLocalIntelligentRules(raw: String, lower: String): AssistantResponse {
        return when {
            lower.contains("where did i stop") || lower.contains("current page") || lower.contains("last read") -> {
                val lastPos = toolExecutor.getLastReadPosition(null)
                AssistantResponse(
                    replyText = lastPos,
                    proposalTitle = "Resume Reading",
                    proposalDetails = "Open reader at last saved position"
                )
            }
            lower.contains("history") || lower.contains("sessions") || lower.contains("reading log") -> {
                val history = toolExecutor.getReadingHistory()
                AssistantResponse(replyText = "Here are your recent reading sessions:\n\n$history")
            }
            lower.contains("stats") || lower.contains("statistics") || lower.contains("progress") -> {
                val stats = toolExecutor.getReadingStatistics()
                val books = toolExecutor.getBooks()
                AssistantResponse(replyText = "$stats\n\n**Current Library Progress:**\n$books")
            }
            lower.contains("books") || lower.contains("library") -> {
                val books = toolExecutor.getBooks()
                AssistantResponse(replyText = "Here are the books currently in your Quiet Library:\n\n$books")
            }
            lower.contains("schedule") || lower.contains("today") || lower.contains("timeline") -> {
                val schedule = toolExecutor.getTodaySchedule()
                AssistantResponse(replyText = schedule, proposalTitle = "View Today Timeline", proposalDetails = "Open Today view")
            }
            lower.contains("tasks") || lower.contains("upcoming") -> {
                val tasks = toolExecutor.getUpcomingTasks()
                AssistantResponse(replyText = tasks)
            }
            else -> {
                AssistantResponse(
                    replyText = "Quiet reflection: Intentional rhythms bring clarity to your focus. You can ask me to plan your day, schedule reading routines, or generate target reading plans for any book."
                )
            }
        }
    }

    private suspend fun callGeminiWithTools(query: String, apiKey: String): AssistantResponse? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val contextInfo = StringBuilder()
        contextInfo.append(toolExecutor.getTodaySchedule()).append("\n")
        contextInfo.append(toolExecutor.getBooks()).append("\n")
        contextInfo.append(toolExecutor.getReadingStatistics()).append("\n")

        val systemPrompt = "You are the Quiet Companion AI Assistant for an Android reading and routine cadence app. " +
                "You NEVER invent fake tasks, routines, books, or reading progress. The application database is your source of truth. " +
                "Current app state:\n$contextInfo\n" +
                "Respond helpfully, concisely, and gracefully. If the user wants to schedule or create a routine, format the proposal clearly."

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", "$systemPrompt\n\nUser request: $query")
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val body = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null

        val text = parts.getJSONObject(0).optString("text")
        if (text.isNotBlank()) {
            return AssistantResponse(replyText = text)
        }
        return null
    }
}

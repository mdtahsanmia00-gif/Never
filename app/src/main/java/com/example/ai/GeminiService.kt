package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskCategory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {

    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (_: Exception) {
            false
        }
    }

    suspend fun parseNaturalLanguageTask(userInput: String): ParsedTaskResult = withContext(Dispatchers.IO) {
        val offlineFallback = SmartReminderParser.parseOffline(userInput)
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext offlineFallback
        }

        val prompt = """
            You are the AI engine of NEVER FORGET AI reminder app.
            Analyze the user's reminder text (which could be in Bengali, English, or Banglish):
            "${userInput}"

            Current timestamp millis: ${System.currentTimeMillis()}
            Return STRICT JSON with keys:
            - title: clean concise task title (e.g. "পড়তে বসা" or "Study Session")
            - dueTimestamp: unix timestamp in milliseconds for when the task is scheduled
            - priority: "HIGH", "MEDIUM", or "LOW"
            - category: "WORK", "STUDY", "HEALTH", "GROCERIES", "MEETING", "PERSONAL", or "OTHER"
            - repeatInterval: "NONE", "HOURLY", "DAILY", "WEEKDAYS", "WEEKLY", or "MONTHLY"
            - locationName: location name if mentioned (e.g. "Supermarket", "Gym", "Home") or null
            - explanation: a short 1-sentence friendly confirmation in Bengali or English.
            Only output raw JSON without markdown formatting.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(apiKey, prompt) ?: return@withContext offlineFallback
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)
            val title = json.optString("title", offlineFallback.title)
            val dueTimestamp = json.optLong("dueTimestamp", offlineFallback.dueTimestamp)
            val priorityStr = json.optString("priority", offlineFallback.priority.name)
            val categoryStr = json.optString("category", offlineFallback.category.name)
            val repeatStr = json.optString("repeatInterval", offlineFallback.repeatInterval.name)
            val locationName = if (json.has("locationName") && !json.isNull("locationName")) {
                json.getString("locationName")
            } else {
                offlineFallback.locationName
            }
            val explanation = json.optString("explanation", offlineFallback.parsedExplanation)

            ParsedTaskResult(
                title = title.ifBlank { offlineFallback.title },
                dueTimestamp = if (dueTimestamp > 0) dueTimestamp else offlineFallback.dueTimestamp,
                priority = Priority.fromString(priorityStr),
                category = TaskCategory.fromString(categoryStr),
                repeatInterval = RepeatInterval.fromString(repeatStr),
                locationName = locationName,
                parsedExplanation = explanation
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini parsing, using offline fallback", e)
            offlineFallback
        }
    }

    suspend fun getAiAssistantAdvice(userQuery: String, contextInfo: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "🤖 [Offline AI Assistant]\n\n" +
                    "To unlock live cloud Gemini reasoning, add your GEMINI_API_KEY in the Secrets panel.\n\n" +
                    "💡 Quick Daily Tip:\n" +
                    "• Tackle your High Priority tasks first in the morning.\n" +
                    "• Never Forget AI keeps active tasks pinned in your notifications so you stay focused!\n" +
                    "• Check your Overdue list regularly to reschedule missed commitments."
        }

        val prompt = """
            You are NEVER FORGET AI, a helpful, encouraging personal assistant and productivity planner.
            The user is asking: "${userQuery}"
            
            Current user schedule context:
            ${contextInfo}
            
            Provide a helpful, motivating, concise response (2-4 bullet points or short paragraphs).
            Support both Bengali and English based on the user's language. Keep tone warm, futuristic, and actionable.
        """.trimIndent()

        try {
            val response = callGeminiApi(apiKey, prompt)
            response ?: "Could not get AI advice at this time. Please check your network connection."
        } catch (e: Exception) {
            "Error contacting Gemini AI: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    private fun callGeminiApi(apiKey: String, textPrompt: String): String? {
        val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            val contents = org.json.JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", textPrompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val bodyString = response.body?.string() ?: return null

        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API error ${response.code}: $bodyString")
            return null
        }

        val root = JSONObject(bodyString)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text")
            }
        }
        return null
    }
}

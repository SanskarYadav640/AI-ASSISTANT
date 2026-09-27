package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PrivaAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val isApiKeyConfigured: Boolean
        get() = !BuildConfig.GEMINI_API_KEY.isNullOrBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    suspend fun generateBriefingResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext Result.failure(Exception("Gemini API key is not configured in Secrets panel"))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val partObject = JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    }
                    put(partObject)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 800)
                })
            }

            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.failure(Exception("API Error ${response.code}: $responseBody"))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext Result.success(text.trim())
                }
            }

            Result.failure(Exception("Empty candidate response from Gemini API"))
        } catch (e: Exception) {
            Log.e("PrivaAiService", "Gemini API call failed", e)
            Result.failure(e)
        }
    }

    suspend fun summarizeUrgentMessage(sender: String, messageText: String): String {
        val prompt = """
            You are Priva, a private on-device AI assistant.
            Summarize the following urgent message in 1 crisp sentence, and state any immediate action required.
            Sender: $sender
            Message: "$messageText"
            Format your response strictly as:
            Summary: <1 sentence>
            Action: <immediate action required>
        """.trimIndent()

        val apiResult = generateBriefingResponse(prompt)
        if (apiResult.isSuccess) {
            return apiResult.getOrThrow()
        }

        // On-device Edge Intelligence Fallback
        return when {
            messageText.contains("asap", ignoreCase = true) || messageText.contains("urgent", ignoreCase = true) ->
                "Summary: High priority request from $sender requiring quick turnaround.\nAction: Review within 1 hour."
            messageText.contains("approved", ignoreCase = true) ->
                "Summary: Confirmation received from $sender regarding project approval.\nAction: File for documentation."
            messageText.contains("deadline", ignoreCase = true) ->
                "Summary: Upcoming deadline approaching from $sender.\nAction: Verify pending deliverables."
            else ->
                "Summary: Priority communication received from $sender.\nAction: Review when convenient."
        }
    }

    suspend fun auditAlarmAndTraffic(alarmTime: String, commuteMinutes: Int, delayMinutes: Int): String {
        val prompt = """
            You are Priva, personal morning assistant.
            The user has an alarm set for $alarmTime.
            Current commute to office normally takes $commuteMinutes min, but has a +$delayMinutes min traffic delay.
            Provide a 1-sentence assessment and recommend whether to boost alarm volume or wake up earlier.
        """.trimIndent()

        val apiResult = generateBriefingResponse(prompt)
        if (apiResult.isSuccess) {
            return apiResult.getOrThrow()
        }

        // On-device Edge fallback
        return if (delayMinutes > 15) {
            "Commute delay is +$delayMinutes min. Recommend boosting alarm volume to 90% and waking 15 mins earlier."
        } else {
            "Traffic is nominal (+${delayMinutes}m). Current alarm volume is sufficient."
        }
    }
}

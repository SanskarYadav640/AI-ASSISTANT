package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * British Butler AI Voice Engine (J.A.R.V.I.S.)
 *
 * Characteristics:
 * - Diction: Polished, fluent, natural RP British diction
 * - Name Normalization: Converts all variants of "J.A.R.V.I.S." / "JARVIS" / "J-A-R-V-I-S" to fluid "Jarvis"
 * - Pitch: 0.98f (natural gentlemanly harmonic warmth, zero rasp or pitch distortion)
 * - Pace: 1.0f (fluent, crisp, confident cadence)
 * - Time-Aware: Automatically adjusts greetings for morning, afternoon, evening, and night protocols
 * - Audio Focus: Transient audio focus with USAGE_ASSISTANT and STREAM_MUSIC routing for car and Bluetooth
 */
class PrivaVoiceSpeaker(private val context: Context) : TextToSpeech.OnInitListener {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _currentVoiceName = MutableStateFlow("British RP (Jarvis Prime)")
    val currentVoiceName: StateFlow<String> = _currentVoiceName.asStateFlow()

    // Butler Persona Speech Parameters: Natural, composed, articulate
    var speechRate: Float = 1.0f  // Natural, confident conversational pace
    var speechPitch: Float = 0.98f // Velvet, rich natural resonance without robotic distortion

    val characterDescription: String = "Accent: Refined British RP, Tone: Dignified, fluent, unhurried & composed"

    var preferredTimeZone: java.util.TimeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")

    // Queue for speech requested before TTS engine completes initialization
    private val pendingSpeechQueue = ConcurrentLinkedQueue<String>()

    fun updateVoiceParameters(rate: Float, pitch: Float) {
        speechRate = rate.coerceIn(0.7f, 1.3f)
        speechPitch = pitch.coerceIn(0.7f, 1.3f)
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)
    }

    /**
     * Sanitizes raw text for fluid, natural speech synthesis:
     * - Fixes J.A.R.V.I.S. / JARVIS / J-A-R-V-I-S spelling out letters
     * - Replaces Indian Rupee symbols with spoken words
     * - Removes Markdown asterisks, bullets, underscores
     * - Normalizes units for speech
     */
    fun sanitizeForSpeech(input: String): String {
        return input
            .replace(Regex("""(?i)\bJ(?:\s*[\.\-_]\s*|\s*)A(?:\s*[\.\-_]\s*|\s*)R(?:\s*[\.\-_]\s*|\s*)V(?:\s*[\.\-_]\s*|\s*)I(?:\s*[\.\-_]\s*|\s*)S(?:\.|\b)"""), "Jarvis")
            .replace(Regex("""(?i)\bJ\.?A\.?R\.?V\.?I\.?S\.?"""), "Jarvis")
            .replace(Regex("""(?i)\bJ-A-R-V-I-S\b"""), "Jarvis")
            .replace(Regex("""(?i)\bJARVIS\b"""), "Jarvis")
            .replace("₹", "Rupees ")
            .replace("INR", "Rupees")
            .replace("km/h", "kilometers per hour")
            .replace("km", "kilometers")
            .replace("min", "minutes")
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("_", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    fun getTimeBasedGreeting(): String {
        val cal = Calendar.getInstance(preferredTimeZone)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    fun isNightTime(): Boolean {
        val cal = Calendar.getInstance(preferredTimeZone)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= 21 || hour < 5
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureBritishButlerVoice()
            _isReady.value = true
            Log.i("PrivaVoiceSpeaker", "TTS initialized successfully. Processing queue (${pendingSpeechQueue.size} items)...")

            // Setup audio attributes for modern Android
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
            } catch (e: Exception) {
                Log.w("PrivaVoiceSpeaker", "Could not set audio attributes: ${e.message}")
            }

            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    abandonAudioFocus()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    abandonAudioFocus()
                }
            })

            // Immediately flush any speech queued prior to onInit
            if (!pendingSpeechQueue.isEmpty()) {
                val latestText = pendingSpeechQueue.poll()
                pendingSpeechQueue.clear()
                if (!latestText.isNullOrBlank()) {
                    speak(latestText)
                }
            }
        } else {
            Log.e("PrivaVoiceSpeaker", "TTS Initialization failed with status: $status")
        }
    }

    private fun configureBritishButlerVoice() {
        val ukLocale = Locale.UK
        var langResult = tts?.setLanguage(ukLocale)

        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("PrivaVoiceSpeaker", "UK locale not installed, using English default")
            langResult = tts?.setLanguage(Locale.ENGLISH)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
        }

        // Apply calibrated natural pitch and rate
        tts?.setPitch(speechPitch)
        tts?.setSpeechRate(speechRate)

        // Select the most natural British or English voice
        try {
            val availableVoices = tts?.voices ?: emptySet()
            val britishVoices = availableVoices.filter { voice ->
                val loc = voice.locale
                ((loc.language.equals("en", ignoreCase = true) && loc.country.equals("GB", ignoreCase = true)) ||
                        voice.name.contains("en-gb", ignoreCase = true)) &&
                        !voice.isNetworkConnectionRequired
            }

            // Prefer natural male voice without harshness
            val preferredVoice = britishVoices.find { voice ->
                val name = voice.name.lowercase()
                (name.contains("rjs") || name.contains("male") || name.contains("gbc") || name.contains("gbb")) &&
                        !name.contains("female")
            } ?: britishVoices.firstOrNull() ?: availableVoices.find {
                it.locale.language.equals("en", ignoreCase = true) && !it.isNetworkConnectionRequired
            }

            if (preferredVoice != null) {
                tts?.voice = preferredVoice
                _currentVoiceName.value = "British RP (${preferredVoice.name.substringAfterLast("-").take(12)})"
                Log.i("PrivaVoiceSpeaker", "Selected British Voice: ${preferredVoice.name}")
            } else {
                _currentVoiceName.value = "British RP (Natural Calibrated)"
            }
        } catch (e: Exception) {
            Log.w("PrivaVoiceSpeaker", "Could not query specific voices: ${e.message}")
            _currentVoiceName.value = "British RP (Standard)"
        }
    }

    private fun requestAudioFocus(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(audioAttributes)
                    .build()
                return audioManager?.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                return audioManager?.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            return true
        }
    }

    private fun abandonAudioFocus() {
        try {
            audioManager?.abandonAudioFocus(null)
        } catch (ignored: Exception) {}
    }

    /**
     * Speaks text with automatic pronunciation normalization and volume leveling
     */
    fun speak(text: String, rate: Float = speechRate, pitch: Float = speechPitch) {
        val sanitized = sanitizeForSpeech(text)
        if (sanitized.isBlank()) return

        if (!_isReady.value) {
            Log.i("PrivaVoiceSpeaker", "TTS not ready yet. Queuing speech: \"$sanitized\"")
            pendingSpeechQueue.offer(sanitized)
            return
        }

        requestAudioFocus()
        speechRate = rate
        speechPitch = pitch
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)
        _isSpeaking.value = true

        val utteranceId = "JARVIS_VOICE_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }

        val result = tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            Log.w("PrivaVoiceSpeaker", "TTS speak fallback with English locale...")
            tts?.setLanguage(Locale.ENGLISH)
            tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    /**
     * Speaks in formal Butler character with polished British framing
     */
    fun speakInCharacter(message: String) {
        val trimmed = message.trim()
        val formatted = if (!trimmed.startsWith("Good", ignoreCase = true) &&
            !trimmed.startsWith("I'm", ignoreCase = true) &&
            !trimmed.startsWith("Sir", ignoreCase = true) &&
            !trimmed.startsWith("Hello", ignoreCase = true) &&
            !trimmed.contains("Sir", ignoreCase = true)
        ) {
            "Sir, $trimmed"
        } else {
            trimmed
        }
        speak(formatted)
    }

    fun testPhrase(phraseIndex: Int) {
        val salutation = getTimeBasedGreeting()
        val phrase = when (phraseIndex) {
            1 -> "$salutation, Sir. J.A.R.V.I.S. is online, all tactical and automotive systems are nominal."
            2 -> "I'm afraid that's not possible, Sir."
            3 -> "Route from Arihant Aarohi to Seawoods Grand Central is calculated and ready, Sir."
            4 -> "Master synchronization complete, Sir. All telemetry is up to date."
            5 -> "Hello Sir! $salutation. Please tell me what is your destination?"
            else -> "At your service, Sir. Jarvis is fully operational."
        }
        speak(phrase)
    }

    fun stop() {
        pendingSpeechQueue.clear()
        tts?.stop()
        _isSpeaking.value = false
        abandonAudioFocus()
    }

    fun shutdown() {
        pendingSpeechQueue.clear()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * J.A.R.V.I.S. Voice Command Manager
 * Supports "Hey Jarvis!", "Hello Jarvis!", and natural language voice interactions.
 */
class JarvisVoiceCommandManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _commandStatus = MutableStateFlow("Tap to speak or say 'Hey Jarvis'")
    val commandStatus: StateFlow<String> = _commandStatus.asStateFlow()

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(onCommandRecognized: (String) -> Unit) {
        if (!hasRecordPermission()) {
            _commandStatus.value = "Microphone permission required for voice commands"
            return
        }

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                        _commandStatus.value = "Speech recognition unavailable on this device"
                        return@post
                    }
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _commandStatus.value = "Listening, Sir... (Say 'Hey Jarvis' or ask any question)"
                    }

                    override fun onBeginningOfSpeech() {
                        _commandStatus.value = "Processing audio..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _commandStatus.value = "Interpreting command..."
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to retry."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue during speech recognition."
                            else -> "Voice recognition paused."
                        }
                        _commandStatus.value = errorMsg
                        Log.w("JarvisVoiceCommand", "Speech recognition error: $error ($errorMsg)")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _spokenText.value = text
                            _commandStatus.value = "Recognized: \"$text\""
                            onCommandRecognized(cleanVoiceCommand(text))
                        } else {
                            _commandStatus.value = "No command detected."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            _spokenText.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to J.A.R.V.I.S.")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _commandStatus.value = "Error starting voice recognition: ${e.message}"
                Log.e("JarvisVoiceCommand", "startListening error", e)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
                _commandStatus.value = "Listening stopped."
            } catch (e: Exception) {
                Log.e("JarvisVoiceCommand", "stopListening error", e)
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                _isListening.value = false
            } catch (e: Exception) {
                Log.e("JarvisVoiceCommand", "destroy error", e)
            }
        }
    }

    private fun cleanVoiceCommand(raw: String): String {
        var clean = raw.trim()
        val wakeWords = listOf("hey jarvis", "hello jarvis", "hi jarvis", "ok jarvis", "jarvis")
        for (w in wakeWords) {
            if (clean.lowercase().startsWith(w)) {
                clean = clean.substring(w.length).trim().removePrefix(",").trim()
                break
            }
        }
        return if (clean.isBlank()) raw.trim() else clean
    }
}

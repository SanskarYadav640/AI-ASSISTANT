package com.example.service

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator

class JarvisCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return JarvisCarAppSession()
    }
}

class JarvisCarAppSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return JarvisCarAppScreen(carContext)
    }
}

class JarvisCarAppScreen(carContext: CarContext) : Screen(carContext) {
    private val voiceSpeaker = try { PrivaVoiceSpeaker(carContext.applicationContext) } catch (e: Exception) { null }

    init {
        try {
            val cal = java.util.Calendar.getInstance()
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val salutation = when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            val greeting = "Hello Sir! $salutation. Please tell me what is your destination?"
            voiceSpeaker?.speak(greeting)
        } catch (ignored: Exception) {}
    }

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val salutation = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        val carGreeting = "Hello Sir! $salutation. Please tell me what is your destination?"

        // Row 1: Greeting & Ask Destination
        val greetingRow = Row.Builder()
            .setTitle(carGreeting)
            .addText("J.A.R.V.I.S. Automotive Protocol Active • Tap to replay greeting")
            .setImage(CarIcon.APP_ICON)
            .setOnClickListener {
                voiceSpeaker?.speak(carGreeting)
            }
            .build()
        itemListBuilder.addItem(greetingRow)

        // Row 2: YouTube Music Action
        val musicRow = Row.Builder()
            .setTitle("🎵 Play YouTube Music")
            .addText("Executive Drive & Synthwave Playlist")
            .setOnClickListener {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://music.youtube.com/search?q=synthwave+cyberpunk+executive+drive")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    carContext.startActivity(intent)
                } catch (ignored: Exception) {}
            }
            .build()
        itemListBuilder.addItem(musicRow)

        // Saved destinations
        val destinations = listOf(
            "Seawoods Grand Central Mall" to "16.5 km • 32 min • Workplace",
            "Residence / Home (Palm Beach Rd)" to "5.2 km • 14 min • Residence",
            "Bandra-Kurla Complex (BKC)" to "24.1 km • 42 min • Financial Hub",
            "Airport (BOM Terminal 2)" to "28.4 km • 48 min • Transit",
            "Gold's Gym Nerul" to "3.8 km • 9 min • Fitness"
        )

        for ((dest, info) in destinations) {
            val row = Row.Builder()
                .setTitle("📍 $dest")
                .addText(info)
                .setOnClickListener {
                    try {
                        val navIntent = Intent(CarContext.ACTION_NAVIGATE, Uri.parse("geo:0,0?q=$dest"))
                        carContext.startCarApp(navIntent)
                    } catch (e: Exception) {
                        try {
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$dest")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            carContext.startActivity(mapIntent)
                        } catch (ignored: Exception) {}
                    }
                }
                .build()
            itemListBuilder.addItem(row)
        }

        return ListTemplate.Builder()
            .setTitle("J.A.R.V.I.S. • Auto Hub")
            .setSingleList(itemListBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

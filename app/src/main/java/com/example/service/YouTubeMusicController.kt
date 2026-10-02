package com.example.service

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class YouTubeMusicPlaylist(
    val id: String,
    val title: String,
    val description: String,
    val genre: String,
    val webUrl: String,
    val sampleTracks: List<String>
)

class YouTubeMusicController(private val context: Context) {

    val curatedPlaylists: List<YouTubeMusicPlaylist> = listOf(
        YouTubeMusicPlaylist(
            id = "ytm_exec",
            title = "Executive Drive & Synthwave",
            description = "High-tempo cyberpunk retrowave & driving synth beats curated for J.A.R.V.I.S.",
            genre = "Synthwave / Cyberpunk",
            webUrl = "https://music.youtube.com/search?q=synthwave+cyberpunk+executive+drive",
            sampleTracks = listOf(
                "Resonance • HOME",
                "Midnight City • M83",
                "Starboy • The Weeknd",
                "Turbo Killer • Carpenter Brut",
                "Nightcall • Kavinsky"
            )
        ),
        YouTubeMusicPlaylist(
            id = "ytm_bolly",
            title = "Bollywood Highway Beats",
            description = "High-energy Indian highway anthems and melodic road-trip favorites.",
            genre = "Bollywood / Indie Pop",
            webUrl = "https://music.youtube.com/search?q=bollywood+highway+roadtrip+drive+playlist",
            sampleTracks = listOf(
                "Ilahi • Yeh Jawaani Hai Deewani",
                "Safarnama • Lucky Ali (Tamasha)",
                "Humraah • Malang",
                "Patakha Guddi • Highway",
                "Yun Hi Chala Chal • Swades"
            )
        ),
        YouTubeMusicPlaylist(
            id = "ytm_focus",
            title = "Deep Focus & Electronic Commute",
            description = "Deep melodic techno and ambient electronic tracks to optimize mental bandwidth.",
            genre = "Deep House / Melodic Techno",
            webUrl = "https://music.youtube.com/search?q=deep+focus+electronic+commute+playlist",
            sampleTracks = listOf(
                "Opus • Eric Prydz",
                "Sun & Moon • Above & Beyond",
                "Breathing • Ben Böhmer",
                "Ghost Voices • Virtual Self",
                "Sirens of the Sea • OceanLab"
            )
        ),
        YouTubeMusicPlaylist(
            id = "ytm_rock",
            title = "Classic Rock Highway Anthems",
            description = "Timeless power chords and cruising anthems for long highway stretches.",
            genre = "Classic Rock",
            webUrl = "https://music.youtube.com/search?q=classic+rock+highway+anthems+playlist",
            sampleTracks = listOf(
                "Highway to Hell • AC/DC",
                "Hotel California • Eagles",
                "Born to Run • Bruce Springsteen",
                "Sweet Child O' Mine • Guns N' Roses",
                "Don't Stop Believin' • Journey"
            )
        ),
        YouTubeMusicPlaylist(
            id = "ytm_lofi",
            title = "Sunset Lo-Fi Chill Cruiser",
            description = "Mellow vinyl beats and soothing guitar riffs for peaceful evening traffic.",
            genre = "Lo-Fi Beats / Chillhop",
            webUrl = "https://music.youtube.com/search?q=lofi+chill+sunset+drive+playlist",
            sampleTracks = listOf(
                "Snowman • WYS",
                "Affection • Jinsang",
                "Controlla • Idealism",
                "Again • Kupla",
                "Daydreaming • lofi fruits"
            )
        )
    )

    private val _currentPlaylist = MutableStateFlow(curatedPlaylists[0])
    val currentPlaylist: StateFlow<YouTubeMusicPlaylist> = _currentPlaylist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _autoPlayOnCarConnect = MutableStateFlow(true)
    val autoPlayOnCarConnect: StateFlow<Boolean> = _autoPlayOnCarConnect.asStateFlow()

    fun toggleAutoPlayOnConnect() {
        _autoPlayOnCarConnect.value = !_autoPlayOnCarConnect.value
    }

    fun selectPlaylist(playlist: YouTubeMusicPlaylist, andPlay: Boolean = false) {
        _currentPlaylist.value = playlist
        _currentTrackIndex.value = 0
        if (andPlay) {
            play(context, playlist)
        }
    }

    fun play(appContext: Context, playlist: YouTubeMusicPlaylist = _currentPlaylist.value) {
        _currentPlaylist.value = playlist
        _isPlaying.value = true

        // Launch YouTube Music via Android Intent
        try {
            val ytmPackage = "com.google.android.apps.youtube.music"
            val uri = Uri.parse(playlist.webUrl)

            val ytmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(ytmPackage)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            // Check if YouTube Music app is installed
            val pm = appContext.packageManager
            if (ytmIntent.resolveActivity(pm) != null) {
                appContext.startActivity(ytmIntent)
                Log.i("YouTubeMusicController", "Launched native YouTube Music app for: ${playlist.title}")
            } else {
                // Fallback 1: Generic View Intent for YouTube Music web
                val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (webIntent.resolveActivity(pm) != null) {
                    appContext.startActivity(webIntent)
                } else {
                    // Fallback 2: Media search intent
                    val mediaIntent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                        putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/playlist")
                        putExtra(SearchManager.QUERY, playlist.title)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    appContext.startActivity(mediaIntent)
                }
                Log.i("YouTubeMusicController", "Launched web/media fallback for YouTube Music playlist")
            }
        } catch (e: Exception) {
            Log.e("YouTubeMusicController", "Could not launch YouTube Music: ${e.message}")
        }
    }

    fun pause() {
        _isPlaying.value = false
    }

    fun togglePlayPause(appContext: Context) {
        if (_isPlaying.value) {
            pause()
        } else {
            play(appContext, _currentPlaylist.value)
        }
    }

    fun nextTrack() {
        val tracks = _currentPlaylist.value.sampleTracks
        if (tracks.isNotEmpty()) {
            _currentTrackIndex.value = (_currentTrackIndex.value + 1) % tracks.size
            _isPlaying.value = true
        }
    }

    fun prevTrack() {
        val tracks = _currentPlaylist.value.sampleTracks
        if (tracks.isNotEmpty()) {
            _currentTrackIndex.value = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else tracks.size - 1
            _isPlaying.value = true
        }
    }

    fun getCurrentTrackTitle(): String {
        val tracks = _currentPlaylist.value.sampleTracks
        val idx = _currentTrackIndex.value
        return if (tracks.isNotEmpty() && idx < tracks.size) tracks[idx] else "Cruising Beats • YouTube Music"
    }
}

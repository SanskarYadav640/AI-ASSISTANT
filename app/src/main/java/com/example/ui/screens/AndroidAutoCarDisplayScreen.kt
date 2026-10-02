package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CarConnectionLogEntity
import com.example.data.local.entity.SavedDestinationEntity
import com.example.service.YouTubeMusicPlaylist
import com.example.ui.PrivaViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcReactorBlue
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AndroidAutoCarDisplayScreen(
    viewModel: PrivaViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isConnected by viewModel.isCarConnected.collectAsStateWithLifecycle()
    val carName by viewModel.connectedCarName.collectAsStateWithLifecycle()
    val connType by viewModel.carConnectionType.collectAsStateWithLifecycle()
    val greetingText by viewModel.carSpokenGreeting.collectAsStateWithLifecycle()

    val savedDestinations by viewModel.savedDestinations.collectAsStateWithLifecycle()
    val carLogs by viewModel.carConnectionLogs.collectAsStateWithLifecycle()

    val ytmPlaylist by viewModel.ytmCurrentPlaylist.collectAsStateWithLifecycle()
    val ytmIsPlaying by viewModel.ytmIsPlaying.collectAsStateWithLifecycle()
    val ytmAutoPlay by viewModel.ytmAutoPlayOnConnect.collectAsStateWithLifecycle()

    val commuteTraffic by viewModel.commuteTraffic.collectAsStateWithLifecycle()
    val isRefreshingTraffic by viewModel.isRefreshingTraffic.collectAsStateWithLifecycle()
    val autoRunVoice by viewModel.autoRunVoice.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }

    // Pulsing indicator for active connection
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B0E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Android Auto Automotive Top Status Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isConnected) Brush.horizontalGradient(listOf(ElectricCyan, SuccessGreen))
                        else Brush.horizontalGradient(listOf(DarkCardBorder, StarkGold.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) SuccessGreen else ElectricCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color(0xFF002B1D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ANDROID AUTO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E1400))
                                            .border(1.dp, StarkGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "J.A.R.V.I.S. HUD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarkGold
                                        )
                                    }
                                }
                                Text(
                                    text = if (isConnected) "● PROJECTION ACTIVE" else "○ WAITING FOR VEHICLE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isConnected) SuccessGreen.copy(alpha = pulseAlpha) else Color.Gray
                                )
                            }
                        }

                        // Connect / Disconnect Simulator Action Button
                        if (!isConnected) {
                            Button(
                                onClick = {
                                    viewModel.connectToCar(
                                        carName = "Audi MMI Pro (Wireless Android Auto)",
                                        type = "Android Auto (Projection)"
                                    )
                                    Toast.makeText(context, "Car Connected! Voice Greeting initiated.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("simulate_car_connect_btn")
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Connect", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    viewModel.disconnectFromCar()
                                    Toast.makeText(context, "Car Disconnected", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("simulate_car_disconnect_btn")
                            ) {
                                Text("Disconnect", color = AlertRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vehicle: ${if (isConnected) carName else "None (Ready for Bluetooth / Auto)"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mode: $connType",
                            fontSize = 11.sp,
                            color = ElectricTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2. Greeting & Destination Voice Command Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Brush.horizontalGradient(listOf(StarkGold.copy(alpha = 0.7f), ElectricCyan)), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(StarkGold, Color(0xFF8A5A00)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Greeting",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "J.A.R.V.I.S. AUTOMOTIVE GREETING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarkGold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Time-Aware Speech Engine (British Butler RP)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.speakCarGreeting()
                                Toast.makeText(context, "Speaking greeting...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("car_replay_greeting_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay Greeting",
                                tint = ElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // The Exact Greeting Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "SPOKEN BY J.A.R.V.I.S. ON CONNECT:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayGreeting = if (greetingText.isNotBlank()) greetingText else viewModel.carConnectionDetector.getCarGreeting()
                            Text(
                                text = "\"$displayGreeting\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.speakCarGreeting()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StarkGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tell Destination", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showLogsDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log History", color = ElectricCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2.5 Live Commute Traffic Telemetry Card (OSRM Real-time API)
        commuteTraffic?.let { commute ->
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Brush.horizontalGradient(listOf(ElectricTeal, ElectricCyan)), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ElectricTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LIVE COMMUTE TELEMETRY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricTeal,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${commute.homeAddress} ➔ ${commute.officeAddress}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (commute.delayMinutes > 0) Color(0xFF3B1519) else Color(0xFF0F291E))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (commute.delayMinutes > 0) "+${commute.delayMinutes}m DELAY" else "FLOW CLEAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (commute.delayMinutes > 0) AlertRed else SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${commute.currentMinutes}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = " min total",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 5.dp)
                                    )
                                }
                                Text(
                                    text = "${commute.distanceKm} km • ${commute.trafficCondition}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (commute.delayMinutes > 0) WarningAmber else SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.refreshTrafficWithApi { updated ->
                                        Toast.makeText(context, "OSRM: ${updated.currentMinutes} min (${updated.trafficCondition})", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isRefreshingTraffic) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF003548), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Live API", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = commute.incidentAlert,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // 3. YouTube Music Auto-Play Controller Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Brush.horizontalGradient(listOf(Color(0xFFFF0033), NeonIndigo)), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF0033)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "YouTube Music",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "YOUTUBE MUSIC CAR PLAYLIST",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF4D6D),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = ytmPlaylist.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Auto-play switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto-Play", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = ytmAutoPlay,
                                onCheckedChange = { viewModel.toggleAutoPlayOnCarConnect() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF0033)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Track Title & Genre Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = viewModel.youtubeMusicController.getCurrentTrackTitle(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${ytmPlaylist.genre} • Auto-Streaming",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Open in YouTube Music native button
                            Button(
                                onClick = {
                                    viewModel.playYouTubeMusicPlaylist(ytmPlaylist, context)
                                    Toast.makeText(context, "Opening YouTube Music...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0033)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Automotive Playback Controls (Large Touch Targets)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.prevYouTubeMusicTrack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Track", tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF0033))
                                .clickable {
                                    viewModel.toggleYouTubeMusicPlayPause(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (ytmIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (ytmIsPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextYouTubeMusicTrack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Track", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Playlist Chips
                    Text(
                        text = "SELECT CAR PLAYLIST:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(viewModel.youtubeMusicController.curatedPlaylists) { pl ->
                            val isSel = pl.id == ytmPlaylist.id
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    viewModel.playYouTubeMusicPlaylist(pl, context)
                                    Toast.makeText(context, "Switched to ${pl.title}", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(pl.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF0033),
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Saved Destinations Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAVED DESTINATIONS",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "One-touch Turn-by-Turn Navigation for Android Auto",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_destination_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // 5. Destination Items
        items(savedDestinations) { destination ->
            DestinationCard(
                destination = destination,
                onNavigate = {
                    viewModel.selectSavedDestination(destination, context) {
                        Toast.makeText(context, "Navigating to ${destination.title}...", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = {
                    viewModel.deleteSavedDestination(destination.id)
                    Toast.makeText(context, "Destination removed", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Add Destination Dialog
    if (showAddDialog) {
        AddDestinationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, address, category, minutes, km ->
                viewModel.addSavedDestination(title, address, category, minutes, km)
                showAddDialog = false
                Toast.makeText(context, "Destination saved: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Car Connection Logs Dialog
    if (showLogsDialog) {
        CarLogsDialog(
            logs = carLogs,
            onDismiss = { showLogsDialog = false },
            onClear = {
                viewModel.clearCarConnectionLogs()
                Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DestinationCard(
    destination: SavedDestinationEntity,
    onNavigate: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryIcon = when (destination.category.lowercase()) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "transit", "airport" -> Icons.Default.Flight
        "fitness", "gym" -> Icons.Default.FitnessCenter
        else -> Icons.Default.LocationOn
    }

    val categoryColor = when (destination.category.lowercase()) {
        "home" -> SuccessGreen
        "work" -> ElectricCyan
        "transit", "airport" -> StarkGold
        "fitness", "gym" -> NeonIndigo
        else -> ElectricTeal
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f))
                            .border(1.dp, categoryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = destination.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = destination.address,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // ETA & Distance Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${destination.estimatedMinutes} min",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = categoryColor
                    )
                    Text(
                        text = "${destination.distanceKm} km",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate",
                        tint = Color(0xFF002B1D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Navigate",
                        color = Color(0xFF002B1D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddDestinationDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, address: String, category: String, minutes: Int, km: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var minutesText by remember { mutableStateOf("25") }
    var kmText by remember { mutableStateOf("12.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Saved Destination", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Destination Name (e.g. Office, Gym)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full Address / Landmark") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it },
                        label = { Text("ETA (Mins)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = kmText,
                        onValueChange = { kmText = it },
                        label = { Text("Distance (Km)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Work", "Home", "Transit", "Fitness", "Leisure").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = Color(0xFF003548),
                                containerColor = DarkSurfaceVariant,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val min = minutesText.toIntOrNull() ?: 20
                        val km = kmText.toDoubleOrNull() ?: 10.0
                        onAdd(title, address.ifBlank { title }, category, min, km)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Save", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun CarLogsDialog(
    logs: List<CarConnectionLogEntity>,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.ENGLISH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Car Connection History", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(onClick = onClear) {
                    Text("Clear All", color = AlertRed, fontSize = 12.sp)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = "No car connection logs recorded yet.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
                items(logs) { log ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.carName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = timeFormat.format(Date(log.connectedTimestamp)),
                                    fontSize = 10.sp,
                                    color = ElectricTeal
                                )
                            }
                            Text(
                                text = "Type: ${log.connectionType}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (log.greetingSpoken.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${log.greetingSpoken}\"",
                                    fontSize = 11.sp,
                                    color = StarkGold,
                                    maxLines = 2
                                )
                            }
                            if (log.musicPlaylistStarted != null) {
                                Text(
                                    text = "🎵 ${log.musicPlaylistStarted}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF4D6D)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Close", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
            }
        }
    )
}

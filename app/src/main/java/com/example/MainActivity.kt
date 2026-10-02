package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import com.example.ui.theme.SeaBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.PitchBlack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.PhoneUnlockReceiver
import com.example.ui.PrivaViewModel
import com.example.ui.screens.AlertsAndAlarmsScreen
import com.example.ui.screens.AndroidAutoCarDisplayScreen
import com.example.ui.screens.PrivacySettingsScreen
import com.example.ui.screens.RadarAndNewsScreen
import com.example.ui.screens.TodayBriefingScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen

class MainActivity : ComponentActivity() {
    private val unlockReceiver = PhoneUnlockReceiver()
    private val targetTabState = mutableStateOf<String?>(null)
    private val autoConnectState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val filter = android.content.IntentFilter(android.content.Intent.ACTION_USER_PRESENT)
            registerReceiver(unlockReceiver, filter)
        } catch (ignored: Exception) {}

        targetTabState.value = intent.getStringExtra("EXTRA_TARGET_TAB")
        autoConnectState.value = intent.getBooleanExtra("EXTRA_AUTO_CONNECT", false)

        setContent {
            MyApplicationTheme {
                PrivaMainApp(
                    initialTabKey = targetTabState.value,
                    autoConnectOnStart = autoConnectState.value
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetTabState.value = intent.getStringExtra("EXTRA_TARGET_TAB")
        autoConnectState.value = intent.getBooleanExtra("EXTRA_AUTO_CONNECT", false)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(unlockReceiver)
        } catch (ignored: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivaMainApp(
    initialTabKey: String? = null,
    autoConnectOnStart: Boolean = false,
    viewModel: PrivaViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember {
        mutableIntStateOf(
            when (initialTabKey) {
                "alerts" -> 1
                "radar" -> 2
                "settings" -> 3
                else -> 0
            }
        )
    }
    var targetAlertsSubTab by remember { mutableIntStateOf(0) }
    var targetRadarTab by remember { mutableIntStateOf(0) }
    var showCarHudScreen by remember { mutableStateOf(initialTabKey == "car_hud" || autoConnectOnStart) }

    val isCarConnected by viewModel.isCarConnected.collectAsStateWithLifecycle()
    val autoRunVoice by viewModel.autoRunVoice.collectAsStateWithLifecycle()
    val isMasterSyncing by viewModel.isMasterSyncing.collectAsStateWithLifecycle()
    val isVoiceListening by viewModel.voiceCommandManager.isListening.collectAsStateWithLifecycle()

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.voiceCommandManager.startListening { command ->
                viewModel.handleVoiceCommand(command)
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(initialTabKey, autoConnectOnStart) {
        if (initialTabKey == "car_hud" || autoConnectOnStart) {
            showCarHudScreen = true
            viewModel.connectToCar()
        }
    }

    // Permission launcher for Android 13+ Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val missedCalls by viewModel.missedCalls.collectAsStateWithLifecycle()
    val urgentMessages by viewModel.urgentMessages.collectAsStateWithLifecycle()

    val pendingAlertCount = missedCalls.count { !it.isResolved } + urgentMessages.count { !it.isRead }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PitchBlack
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SeaBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PitchBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = PureWhite,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1B2B))
                                .border(1.dp, SeaBlue.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CORE ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeaBlue
                            )
                        }
                    }
                },
                actions = {
                    // Voice Command Mic button ("Hey Jarvis!")
                    IconButton(
                        onClick = {
                            if (isVoiceListening) {
                                viewModel.voiceCommandManager.stopListening()
                            } else {
                                if (viewModel.voiceCommandManager.hasRecordPermission()) {
                                    viewModel.voiceCommandManager.startListening { command ->
                                        viewModel.handleVoiceCommand(command)
                                    }
                                } else {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier.testTag("top_voice_command_btn")
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Command (Hey Jarvis!)",
                            tint = if (isVoiceListening) AlertRed else SeaBlue
                        )
                    }

                    // Master Sync Everything button
                    IconButton(
                        onClick = {
                            viewModel.syncEverything {
                                Toast.makeText(context, "All Systems Synchronized with Arihant Aarohi!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("top_master_sync_btn")
                    ) {
                        if (isMasterSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SeaBlue, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Master Synchronize All",
                                tint = SeaBlue
                            )
                        }
                    }

                    // Android Auto Car HUD button
                    IconButton(
                        onClick = { showCarHudScreen = true },
                        modifier = Modifier.testTag("top_car_hud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Android Auto Car HUD",
                            tint = if (isCarConnected) SuccessGreen else PureWhite
                        )
                    }

                    // Voice Auto-Run button
                    IconButton(
                        onClick = { viewModel.toggleAutoRunVoice() },
                        modifier = Modifier.testTag("top_auto_run_voice_button")
                    ) {
                        Icon(
                            imageVector = if (autoRunVoice) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (autoRunVoice) "Voice Auto-Run: Active" else "Voice Auto-Run: Muted",
                            tint = if (autoRunVoice) SeaBlue else Color.Gray
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier.border(1.dp, DarkCardBorder)
            ) {
                // Tab 0: Today
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Today Hub"
                        )
                    },
                    label = { Text("Today") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF003548),
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = ElectricCyan,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_today")
                )

                // Tab 1: Alerts & Alarms
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingAlertCount > 0) {
                                    Badge(containerColor = AlertRed) {
                                        Text("$pendingAlertCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alerts & Alarms"
                            )
                        }
                    },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF003548),
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = ElectricCyan,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_alerts")
                )

                // Tab 2: Radar & News
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Radar & News"
                        )
                    },
                    label = { Text("Radar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF003548),
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = ElectricTeal,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_radar")
                )

                // Tab 3: Privacy & Settings
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Vault & Settings"
                        )
                    },
                    label = { Text("Vault") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF003548),
                        indicatorColor = NeonIndigo,
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = NeonIndigo,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_vault")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showCarHudScreen) {
                androidx.activity.compose.BackHandler {
                    showCarHudScreen = false
                }
                AndroidAutoCarDisplayScreen(
                    viewModel = viewModel,
                    onBack = { showCarHudScreen = false }
                )
            } else {
                when (selectedTab) {
                    0 -> TodayBriefingScreen(
                        viewModel = viewModel,
                        onNavigateToAlerts = { subTab ->
                            targetAlertsSubTab = subTab
                            selectedTab = 1
                        },
                        onNavigateToRadar = { radarTab ->
                            targetRadarTab = radarTab
                            selectedTab = 2
                        },
                        onOpenCarHud = {
                            showCarHudScreen = true
                        }
                    )
                    1 -> AlertsAndAlarmsScreen(
                        viewModel = viewModel,
                        initialSubTab = targetAlertsSubTab
                    )
                    2 -> RadarAndNewsScreen(
                        viewModel = viewModel,
                        initialTab = targetRadarTab
                    )
                    3 -> PrivacySettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

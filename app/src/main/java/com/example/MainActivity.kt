package com.example

import android.Manifest
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PrivaViewModel
import com.example.ui.screens.AlertsAndAlarmsScreen
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
import com.example.ui.theme.SuccessGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTargetTab = intent.getStringExtra("EXTRA_TARGET_TAB")

        setContent {
            MyApplicationTheme {
                PrivaMainApp(initialTabKey = initialTargetTab)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivaMainApp(
    initialTabKey: String? = null,
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
                    containerColor = DarkSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF003548),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVA AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0B2B1B))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ON-DEVICE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendTestNotificationTalk() },
                        modifier = Modifier.testTag("top_ping_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Notification Talk",
                            tint = ElectricCyan
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
                            imageVector = Icons.Default.TrendingUp,
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
            when (selectedTab) {
                0 -> TodayBriefingScreen(
                    viewModel = viewModel,
                    onNavigateToAlerts = { selectedTab = 1 },
                    onNavigateToRadar = { selectedTab = 2 }
                )
                1 -> AlertsAndAlarmsScreen(
                    viewModel = viewModel
                )
                2 -> RadarAndNewsScreen(
                    viewModel = viewModel
                )
                3 -> PrivacySettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

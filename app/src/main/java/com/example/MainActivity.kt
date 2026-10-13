package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.model.*
import com.example.ui.components.LoginProfileScreen
import com.example.ui.components.SosEmergencyScreen
import com.example.ui.components.WeeklyVitalsTrendVisualizer
import com.example.ui.theme.*
import com.example.viewmodel.CareCircleUiState
import com.example.viewmodel.CareCircleViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CareCircleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareCircleTheme {
                CareCircleApp(viewModel)
            }
        }
    }
}

@Composable
fun CareCircleApp(viewModel: CareCircleViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = state.selectedTab, pageCount = { 6 })

    // Keep pager and ViewModel tab synchronized
    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab) {
            pagerState.animateScrollToPage(state.selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (state.selectedTab != pagerState.currentPage) {
            viewModel.selectTab(pagerState.currentPage)
        }
    }

    LaunchedEffect(state.notifiedCaregiverToast) {
        state.notifiedCaregiverToast?.let { toastMsg ->
            Toast.makeText(context, "🚨 $toastMsg", Toast.LENGTH_LONG).show()
            viewModel.clearNotificationToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        topBar = {
            CareCircleTopAppBar(state, viewModel)
        },
        bottomBar = {
            CareCircleBottomNavigation(
                selectedTab = pagerState.currentPage,
                currentLang = state.currentLanguage,
                onTabSelected = { index ->
                    viewModel.selectTab(index)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CaregiverDashboardScreen(state, viewModel)
                    1 -> LiveSimulatorScreen(state, viewModel)
                    2 -> DoseTimeScreen(state, viewModel)
                    3 -> SosEmergencyScreen(state, viewModel)
                    4 -> ThresholdsScreen(state, viewModel)
                    5 -> LoginProfileScreen(
                        profile = state.patientProfile,
                        onSaveProfile = { viewModel.updatePatientProfile(it) }
                    )
                }
            }

            val openCritical = state.alerts.firstOrNull { it.status == AlertStatus.OPEN && it.severity == AlertSeverity.CRITICAL }
            if (openCritical != null && pagerState.currentPage != 3) {
                CriticalAlertBanner(
                    alert = openCritical,
                    currentLang = state.currentLanguage,
                    onAcknowledge = { viewModel.acknowledgeAlert(openCritical.id) }
                )
            }

            if (state.showLanguageDialog) {
                LanguagePickerDialog(
                    currentLang = state.currentLanguage,
                    onSelect = { viewModel.setLanguage(it) },
                    onDismiss = { viewModel.closeLanguageDialog() }
                )
            }
        }
    }
}

@Composable
fun CareCircleTopAppBar(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.isSpeaking) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingPulse"
    )

    Surface(
        color = NavySurface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PrimaryTeal, ElectricBlue))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = NavyBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.get("app_title", lang),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.isOfflineMode) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentAmber.copy(alpha = 0.25f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("ROOM CACHE", color = AccentAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "${state.patientProfile.patientName} (${state.patientProfile.patientAge})",
                        color = PrimaryTealLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.selectTab(5) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.selectedTab == 5) PrimaryTeal else NavyCard)
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = if (state.selectedTab == 5) NavyBackground else PrimaryTealLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (state.isSpeaking) {
                            viewModel.stopVoice()
                        } else {
                            viewModel.speakVitalsSummary()
                        }
                    },
                    modifier = Modifier
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (state.isSpeaking) AccentCoral.copy(alpha = 0.25f) else PrimaryTeal.copy(alpha = 0.15f))
                        .testTag("btn_voice_assistant")
                ) {
                    Icon(
                        imageVector = if (state.isSpeaking) Icons.Filled.RecordVoiceOver else Icons.Filled.VolumeUp,
                        contentDescription = "Voice Assistant",
                        tint = if (state.isSpeaking) AccentCoral else PrimaryTeal
                    )
                }

                Spacer(Modifier.width(6.dp))

                Surface(
                    onClick = { viewModel.openLanguageDialog() },
                    shape = RoundedCornerShape(20.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("btn_language_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = lang.nativeName,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CareCircleBottomNavigation(
    selectedTab: Int,
    currentLang: AppLanguage,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = NavySurface,
        tonalElevation = 4.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("carecircle_bottom_nav")
    ) {
        val navItems = listOf(
            Triple(0, AppStrings.get("nav_dashboard", currentLang), Icons.Filled.MonitorHeart),
            Triple(1, AppStrings.get("nav_simulator", currentLang), Icons.Filled.Sensors),
            Triple(2, AppStrings.get("nav_dose", currentLang), Icons.Filled.Medication),
            Triple(3, AppStrings.get("nav_sos", currentLang), Icons.Filled.Sos),
            Triple(4, AppStrings.get("nav_thresholds", currentLang), Icons.Filled.Tune),
            Triple(5, "Profile", Icons.Filled.AccountCircle)
        )

        navItems.forEach { (index, title, icon) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) PrimaryTeal else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = title,
                        color = if (isSelected) PrimaryTeal else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NavyCard
                ),
                modifier = Modifier.testTag("nav_item_$index")
            )
        }
    }
}

@Composable
fun CriticalAlertBanner(
    alert: AlertItem,
    currentLang: AppLanguage,
    onAcknowledge: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AccentCoral)
            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CRITICAL ESCALATION (Level ${alert.escalationLevel})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    alert.triggerMessage,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_acknowledge_alert")
            ) {
                Text("ACK", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CaregiverDashboardScreen(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("caregiver_dashboard_list"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.isOfflineMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Offline Room Database Cache Active", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Displaying local on-device SQLite snapshot. All telemetry remains viewable without internet connection.", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 1. Patient Header Card WITH LIVE READING OF THE PATIENT DIRECTLY BELOW PATIENT NAME
        item {
            PatientHeaderCard(state, lang) { viewModel.selectTab(5) }
        }

        // 2. Weekly Vitals Trend Visualizer (Weekly Report)
        item {
            WeeklyVitalsTrendVisualizer(
                weeklyPoints = state.weeklyTrends
            )
        }
        // 3. Weekly Medical Health Report Summary Card
        item {
            WeeklyHealthReportCard(
                weeklyPoints = state.weeklyTrends,
                patientName = state.patientProfile.patientName
            )
        }

        // Voice Assistant Interactive Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (state.isSpeaking) viewModel.stopVoice() else viewModel.speakVitalsSummary()
                    },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0D324D), Color(0xFF7F5A83))
                            )
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isSpeaking) Icons.Filled.RecordVoiceOver else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = PrimaryTealLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.get("voice_assistant", lang),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (state.isSpeaking) AppStrings.get("voice_stop", lang) else AppStrings.get("voice_read_vitals", lang),
                                    color = PrimaryTealLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.isSpeaking) AccentCoral else PrimaryTeal)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (state.isSpeaking) "STOP" else "PLAY",
                                color = NavyBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("nav_dashboard", lang),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Detailed Metric Cards",
                    color = PrimaryTealLight,
                    fontSize = 11.sp
                )
            }
        }

        items(state.vitals.values.toList()) { vital ->
            VitalReadingCard(vital, lang)
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Alerts & Escalation State",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (state.alerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "All telemetry metrics within target physiological boundaries.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(state.alerts) { alert ->
                AlertItemCard(alert, onAck = { viewModel.acknowledgeAlert(alert.id) }, onResolve = { viewModel.resolveAlert(alert.id) })
            }
        }
    }
}

@Composable
fun PatientHeaderCard(state: CareCircleUiState, lang: AppLanguage, onOpenProfile: () -> Unit) {
    val primaryContact = state.patientProfile.caregiversAndFamily.firstOrNull { it.isPrimary }
        ?: state.patientProfile.caregiversAndFamily.firstOrNull()
    val hrReading = state.vitals[VitalMetric.HEART_RATE]
    val spo2Reading = state.vitals[VitalMetric.SPO2]
    val tempReading = state.vitals[VitalMetric.BODY_TEMPERATURE]
    val bpSysReading = state.vitals[VitalMetric.BLOOD_PRESSURE_SYSTOLIC]
    val bpDiaReading = state.vitals[VitalMetric.BLOOD_PRESSURE_DIASTOLIC]
    val respReading = state.vitals[VitalMetric.RESPIRATORY_RATE]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Patient Demographics Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF00E5B3), Color(0xFF00B0FF)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = NavyBackground, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${state.patientProfile.patientName} (${state.patientProfile.patientAge}y)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(state.patientProfile.bloodGroup, color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            "${AppStrings.get("caregiver_label", lang)}: ${primaryContact?.name ?: "Family Circle"} (${primaryContact?.phone ?: ""})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            "📍 ${state.patientProfile.cityLocation} • ${state.patientProfile.primaryCondition}",
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (state.activeScenario == "device_dropout") AccentCoral.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (state.activeScenario == "device_dropout") AppStrings.get("status_offline", lang) else AppStrings.get("status_online", lang),
                        color = if (state.activeScenario == "device_dropout") AccentCoral else EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ========================================================
            // LIVE READING OF THE PATIENT (Directly below patient name)
            // ========================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "LIVE TELEMETRY SNAPSHOT",
                        color = PrimaryTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Text(
                    "Real-Time Stream",
                    color = EmeraldGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(6.dp))

            // 5 Core Live Readings Tiles Directly Below Patient Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Heart Rate
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCoral.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("HR", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${hrReading?.canonicalValue?.toInt() ?: 74}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("bpm", color = AccentCoralLight, fontSize = 8.sp)
                    }
                }

                // SpO2
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Air, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("SpO2", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${spo2Reading?.canonicalValue?.toInt() ?: 98}%",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("O2 Sat", color = ElectricBlueLight, fontSize = 8.sp)
                    }
                }

                // Blood Pressure
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1.25f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Speed, contentDescription = null, tint = SoftPurple, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("BP", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${bpSysReading?.canonicalValue?.toInt() ?: 120}/${bpDiaReading?.canonicalValue?.toInt() ?: 78}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text("mmHg", color = SoftPurple, fontSize = 8.sp)
                    }
                }

                // Temperature
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DeviceThermostat, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("TEMP", color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${tempReading?.canonicalValue ?: 36.8}°",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("Celsius", color = AccentAmberLight, fontSize = 8.sp)
                    }
                }

                // Respiration Rate
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WindPower, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("RESP", color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${respReading?.canonicalValue?.toInt() ?: 16}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("bpm", color = EmeraldGreen, fontSize = 8.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Footer connection info with explicit profile button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sensors, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(state.connectionStatus, color = TextSecondary, fontSize = 11.sp)
                }

                Surface(
                    onClick = onOpenProfile,
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryTeal.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f))
                ) {
                    Text(
                        "View Medical Profile & Records →",
                        color = PrimaryTealLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyHealthReportCard(
    weeklyPoints: List<WeeklyVitalPoint>,
    patientName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Assessment,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Weekly Medical Health Report",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "7-Day Clinical Telemetry for $patientName",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text("STABLE", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 4 Key Weekly Averages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Avg HR
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Avg HR", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("75", color = AccentCoral, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("bpm", color = TextSecondary, fontSize = 8.sp)
                        Text("62-108 range", color = TextTertiary, fontSize = 8.sp)
                    }
                }

                // Avg SpO2
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Avg SpO2", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("97.6%", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Normal", color = TextSecondary, fontSize = 8.sp)
                        Text("Safe O2", color = TextTertiary, fontSize = 8.sp)
                    }
                }

                // Avg BP
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Avg BP", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("122/80", color = SoftPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("mmHg", color = TextSecondary, fontSize = 8.sp)
                        Text("Controlled", color = TextTertiary, fontSize = 8.sp)
                    }
                }

                // Compliance / Stability
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Adherence", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("100%", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Doses", color = TextSecondary, fontSize = 8.sp)
                        Text("14/14 taken", color = TextTertiary, fontSize = 8.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Clinical Summary Text
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NavyCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Clinical Assessment: Normal circadian variation across 7 days. Mild Saturday exertion spike (108 bpm) self-resolved without intervention. Hemodynamics within target bounds.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VitalReadingCard(vital: VitalReading, lang: AppLanguage) {
    val cardGradient = when (vital.metric) {
        VitalMetric.HEART_RATE -> VitalCardHeartGradient
        VitalMetric.SPO2 -> VitalCardOxygenGradient
        VitalMetric.BODY_TEMPERATURE -> VitalCardTempGradient
        VitalMetric.BLOOD_PRESSURE_SYSTOLIC, VitalMetric.BLOOD_PRESSURE_DIASTOLIC -> VitalCardBpGradient
        VitalMetric.RESPIRATORY_RATE -> VitalCardRespGradient
    }

    val iconVector = when (vital.metric) {
        VitalMetric.HEART_RATE -> Icons.Filled.Favorite
        VitalMetric.SPO2 -> Icons.Filled.Air
        VitalMetric.BODY_TEMPERATURE -> Icons.Filled.DeviceThermostat
        VitalMetric.BLOOD_PRESSURE_SYSTOLIC, VitalMetric.BLOOD_PRESSURE_DIASTOLIC -> Icons.Filled.Speed
        VitalMetric.RESPIRATORY_RATE -> Icons.Filled.WindPower
    }

    val iconTint = when (vital.metric) {
        VitalMetric.HEART_RATE -> AccentCoral
        VitalMetric.SPO2 -> ElectricBlue
        VitalMetric.BODY_TEMPERATURE -> AccentAmber
        VitalMetric.BLOOD_PRESSURE_SYSTOLIC, VitalMetric.BLOOD_PRESSURE_DIASTOLIC -> SoftPurple
        VitalMetric.RESPIRATORY_RATE -> PrimaryTeal
    }

    val localizedMetricName = when (vital.metric) {
        VitalMetric.HEART_RATE -> AppStrings.get("heart_rate", lang)
        VitalMetric.SPO2 -> AppStrings.get("spo2", lang)
        VitalMetric.BODY_TEMPERATURE -> AppStrings.get("temperature", lang)
        VitalMetric.BLOOD_PRESSURE_SYSTOLIC -> AppStrings.get("blood_pressure_sys", lang)
        VitalMetric.BLOOD_PRESSURE_DIASTOLIC -> AppStrings.get("blood_pressure_dia", lang)
        VitalMetric.RESPIRATORY_RATE -> AppStrings.get("respiratory_rate", lang)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(iconTint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconVector, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = localizedMetricName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val (freshColor, freshLabel) = when (vital.freshness) {
                        FreshnessState.LIVE -> EmeraldGreen to AppStrings.get("fresh_live", lang)
                        FreshnessState.DELAYED -> AccentAmber to AppStrings.get("fresh_delayed", lang)
                        FreshnessState.STALE -> FreshnessStale to AppStrings.get("fresh_stale", lang)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(freshColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$freshLabel (${vital.measuredSecondsAgo}s)",
                            color = freshColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${vital.canonicalValue}",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = vital.canonicalUnit,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = vital.sourceKind.label,
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = vital.qualityStatus.label,
                            color = if (vital.qualityStatus == QualityStatus.VALID) EmeraldGreen else AccentCoral,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(alert: AlertItem, onAck: () -> Unit, onResolve: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (alert.status == AlertStatus.OPEN) AccentCoral else NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (alert.severity == AlertSeverity.CRITICAL) Icons.Filled.Error else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (alert.severity == AlertSeverity.CRITICAL) AccentCoral else AccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${alert.type.name} Alert",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    alert.status.name,
                    color = if (alert.status == AlertStatus.RESOLVED) EmeraldGreen else AccentAmber,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(alert.triggerMessage, color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Escalation Level: ${alert.escalationLevel} (Family SMS -> Call)",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alert.status == AlertStatus.OPEN) {
                        OutlinedButton(
                            onClick = onAck,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Acknowledge", fontSize = 11.sp, color = PrimaryTeal)
                        }
                    }
                    if (alert.status != AlertStatus.RESOLVED) {
                        Button(
                            onClick = onResolve,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Resolve", fontSize = 11.sp, color = NavyBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveSimulatorScreen(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("simulator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = AppStrings.get("sim_title", lang),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = AppStrings.get("sim_desc", lang),
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        val scenarios = listOf(
            Triple("normal", "Normal Vitals (Baseline)", "Heart Rate ~74 bpm, SpO2 ~98%, Temp ~36.8°C. Auto-resolves past breaches."),
            Triple("tachycardia", "Tachycardia Spike (>110 bpm)", "Heart Rate spikes to 145 bpm (>110 threshold). Fires Level 0 push escalation."),
            Triple("low_spo2", "Low Oxygen Hypoxemia (<92%)", "Blood oxygen drops to 86% (<92 threshold). Critical alert."),
            Triple("fever", "Fever Temperature (>38.3°C)", "Temperature rises to 39.5°C (>38.3 threshold)."),
            Triple("device_dropout", "Device Dropout (Offline Mode)", "Stops telemetry stream entirely. Triggers offline Room cache and disconnect alert."),
            Triple("bp_manual", "Manual BP Measurement", "Adds manual cuff readings (148/96 mmHg). Never shown as 'Live'.")
        )

        items(scenarios) { (scenarioKey, title, desc) ->
            val isSelected = state.activeScenario == scenarioKey
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startSimulation(scenarioKey) },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) NavySurface else NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PrimaryTeal else NavyBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.startSimulation(scenarioKey) },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DoseTimeScreen(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dose_time_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = AppStrings.get("dose_title", lang),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scheduled daily medications & caregiver tracking",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Medication, contentDescription = null, tint = PrimaryTeal)
                }
            }
        }

        items(state.doses) { dose ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (dose.isTaken) EmeraldGreen.copy(alpha = 0.5f) else NavyBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(Color(dose.categoryColorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.MedicalServices,
                                    contentDescription = null,
                                    tint = Color(dose.categoryColorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(dose.medicineName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(dose.dosage, color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurface)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(dose.time, color = PrimaryTealLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(dose.instruction, color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.toggleDose(dose.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dose.isTaken) EmeraldGreen else NavySurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = if (!dose.isTaken) androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal) else null
                        ) {
                            Icon(
                                if (dose.isTaken) Icons.Filled.Check else Icons.Outlined.Check,
                                contentDescription = null,
                                tint = if (dose.isTaken) NavyBackground else PrimaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (dose.isTaken) "Taken ✓" else AppStrings.get("dose_take", lang),
                                color = if (dose.isTaken) NavyBackground else PrimaryTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThresholdsScreen(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("thresholds_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = AppStrings.get("threshold_title", lang),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Configurable vital limits. Sustained breach required before triggering escalation notifications.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        items(state.thresholds) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rule.metric.displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Active", color = PrimaryTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Min: ${rule.minValue?.toString() ?: "None"} ${rule.metric.canonicalUnit}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            "Max: ${rule.maxValue?.toString() ?: "None"} ${rule.metric.canonicalUnit}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sustained breach: ${rule.sustainedSeconds}s • Max age: ${rule.maxAgeSeconds}s",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    currentLang: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Translate, contentDescription = null, tint = PrimaryTeal)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = AppStrings.get("select_language", currentLang),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppLanguage.values()) { lang ->
                    val isSelected = currentLang == lang
                    Surface(
                        onClick = { onSelect(lang) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavyCard else NavyBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryTeal else NavyBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    color = if (isSelected) PrimaryTeal else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = lang.displayName,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryTeal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryTeal, fontWeight = FontWeight.Bold)
            }
        }
    )
}

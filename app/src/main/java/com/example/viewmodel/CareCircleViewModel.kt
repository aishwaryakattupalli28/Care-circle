package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistant.VoiceAssistantManager
import com.example.data.local.AppDatabase
import com.example.data.repository.VitalRepository
import com.example.hospital.HospitalDataProvider
import com.example.hospital.NearbyHospital
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class DoseItem(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val time: String,
    val instruction: String,
    var isTaken: Boolean = false,
    val categoryColorHex: Long = 0xFF00E5B3
)

data class CareCircleUiState(
    val selectedTab: Int = 0, // 0: Vitals Dashboard, 1: Simulator, 2: Dose Time, 3: SOS Emergency, 4: Thresholds, 5: Patient Profile/Login
    val activeScenario: String = "normal", // normal, tachycardia, low_spo2, fever, device_dropout, bp_manual
    val isSimulating: Boolean = true,
    val isOfflineMode: Boolean = false,
    val patientProfile: PatientProfile = PatientProfile(),
    val connectionStatus: String = "Connected via BLE & Realtime WS",
    val vitals: Map<VitalMetric, VitalReading> = emptyMap(),
    val weeklyTrends: List<WeeklyVitalPoint> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val thresholds: List<ThresholdConfig> = emptyList(),
    val doses: List<DoseItem> = emptyList(),
    val nearbyHospitals: List<NearbyHospital> = emptyList(),
    val sosCountdownSeconds: Int = 10,
    val isSosActive: Boolean = false,
    val notifiedCaregiverToast: String? = null,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isSpeaking: Boolean = false,
    val showLanguageDialog: Boolean = false
)

class CareCircleViewModel(application: Application) : AndroidViewModel(application) {
    private val voiceManager = VoiceAssistantManager(application)
    private val appDatabase = AppDatabase.getInstance(application)
    private val vitalRepository = VitalRepository(appDatabase.vitalDao())

    private val _uiState = MutableStateFlow(CareCircleUiState())
    val uiState: StateFlow<CareCircleUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private var sosJob: Job? = null
    private var tickerSeconds: Long = 0

    init {
        initializeThresholds()
        initializeDoses()
        initializeWeeklyTrends()
        loadNearbyHospitals()
        observeLocalRoomCache()
        generateInitialVitals()
        startSimulation("normal")

        // Track voice speaking state
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    private fun initializeWeeklyTrends() {
        val days = listOf(
            WeeklyVitalPoint("Mon", 72f, 64f, 82f, 98f, 36.6f, 120f, 78f),
            WeeklyVitalPoint("Tue", 75f, 68f, 88f, 97f, 36.7f, 122f, 80f),
            WeeklyVitalPoint("Wed", 71f, 62f, 79f, 99f, 36.6f, 118f, 76f),
            WeeklyVitalPoint("Thu", 78f, 70f, 95f, 98f, 36.8f, 126f, 82f),
            WeeklyVitalPoint("Fri", 74f, 65f, 84f, 97f, 36.7f, 121f, 79f),
            WeeklyVitalPoint("Sat", 82f, 72f, 108f, 96f, 37.1f, 130f, 85f),
            WeeklyVitalPoint("Sun", 74f, 66f, 86f, 98f, 36.8f, 120f, 78f)
        )
        _uiState.update { it.copy(weeklyTrends = days) }
    }

    private fun loadNearbyHospitals() {
        val hospitals = HospitalDataProvider.getSampleNearbyHospitals()
        _uiState.update { it.copy(nearbyHospitals = hospitals) }
    }

    private fun observeLocalRoomCache() {
        viewModelScope.launch {
            vitalRepository.cachedVitalsFlow.collect { cachedList ->
                if (cachedList.isNotEmpty() && _uiState.value.vitals.isEmpty()) {
                    val map = cachedList.associateBy { it.metric }
                    _uiState.update { it.copy(vitals = map, isOfflineMode = true) }
                }
            }
        }
    }

    private fun initializeDoses() {
        val initialDoses = listOf(
            DoseItem(
                id = UUID.randomUUID().toString(),
                medicineName = "Metoprolol Tartrate",
                dosage = "25 mg • Tablet",
                time = "08:00 AM",
                instruction = "Take with morning breakfast & water",
                isTaken = true,
                categoryColorHex = 0xFF00E5B3
            ),
            DoseItem(
                id = UUID.randomUUID().toString(),
                medicineName = "Atorvastatin Calcium",
                dosage = "20 mg • Capsule",
                time = "01:30 PM",
                instruction = "Post-lunch with a glass of water",
                isTaken = false,
                categoryColorHex = 0xFF00B0FF
            ),
            DoseItem(
                id = UUID.randomUUID().toString(),
                medicineName = "Lisinopril Ace Inhibitor",
                dosage = "10 mg • Tablet",
                time = "08:30 PM",
                instruction = "Before bedtime. Check blood pressure first",
                isTaken = false,
                categoryColorHex = 0xFFFFB300
            )
        )
        _uiState.update { it.copy(doses = initialDoses) }
    }

    private fun initializeThresholds() {
        val defaultThresholds = listOf(
            ThresholdConfig(VitalMetric.HEART_RATE, 50.0, 110.0, sustainedSeconds = 20),
            ThresholdConfig(VitalMetric.SPO2, 92.0, null, sustainedSeconds = 15),
            ThresholdConfig(VitalMetric.BODY_TEMPERATURE, 35.5, 38.3, sustainedSeconds = 30),
            ThresholdConfig(VitalMetric.BLOOD_PRESSURE_SYSTOLIC, 90.0, 140.0, sustainedSeconds = 30),
            ThresholdConfig(VitalMetric.BLOOD_PRESSURE_DIASTOLIC, 60.0, 90.0, sustainedSeconds = 30),
            ThresholdConfig(VitalMetric.RESPIRATORY_RATE, 10.0, 24.0, sustainedSeconds = 25)
        )
        _uiState.update { it.copy(thresholds = defaultThresholds) }
    }

    private fun generateInitialVitals() {
        val initialMap = mapOf(
            VitalMetric.HEART_RATE to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.HEART_RATE,
                value = 74.0,
                unit = "bpm",
                canonicalValue = 74.0,
                canonicalUnit = "bpm",
                measuredSecondsAgo = 2,
                sourceKind = SourceKind.BLE,
                freshness = FreshnessState.LIVE,
                qualityStatus = QualityStatus.VALID
            ),
            VitalMetric.SPO2 to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.SPO2,
                value = 98.0,
                unit = "%",
                canonicalValue = 98.0,
                canonicalUnit = "%",
                measuredSecondsAgo = 3,
                sourceKind = SourceKind.BLE,
                freshness = FreshnessState.LIVE,
                qualityStatus = QualityStatus.VALID
            ),
            VitalMetric.BODY_TEMPERATURE to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.BODY_TEMPERATURE,
                value = 36.8,
                unit = "°C",
                canonicalValue = 36.8,
                canonicalUnit = "°C",
                measuredSecondsAgo = 14,
                sourceKind = SourceKind.SIMULATOR,
                freshness = FreshnessState.LIVE,
                qualityStatus = QualityStatus.VALID
            ),
            VitalMetric.BLOOD_PRESSURE_SYSTOLIC to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.BLOOD_PRESSURE_SYSTOLIC,
                value = 120.0,
                unit = "mmHg",
                canonicalValue = 120.0,
                canonicalUnit = "mmHg",
                measuredSecondsAgo = 120,
                sourceKind = SourceKind.MANUAL,
                freshness = FreshnessState.DELAYED,
                qualityStatus = QualityStatus.VALID
            ),
            VitalMetric.BLOOD_PRESSURE_DIASTOLIC to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.BLOOD_PRESSURE_DIASTOLIC,
                value = 78.0,
                unit = "mmHg",
                canonicalValue = 78.0,
                canonicalUnit = "mmHg",
                measuredSecondsAgo = 120,
                sourceKind = SourceKind.MANUAL,
                freshness = FreshnessState.DELAYED,
                qualityStatus = QualityStatus.VALID
            ),
            VitalMetric.RESPIRATORY_RATE to VitalReading(
                id = UUID.randomUUID().toString(),
                metric = VitalMetric.RESPIRATORY_RATE,
                value = 16.0,
                unit = "rpm",
                canonicalValue = 16.0,
                canonicalUnit = "rpm",
                measuredSecondsAgo = 4,
                sourceKind = SourceKind.BLE,
                freshness = FreshnessState.LIVE,
                qualityStatus = QualityStatus.VALID
            )
        )
        _uiState.update { it.copy(vitals = initialMap) }

        viewModelScope.launch {
            vitalRepository.cacheAllVitals(initialMap.values.toList())
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updatePatientProfile(updated: PatientProfile) {
        _uiState.update { it.copy(patientProfile = updated) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(currentLanguage = lang, showLanguageDialog = false) }
        voiceManager.setLanguage(lang)

        val welcomeMsg = when (lang) {
            AppLanguage.HINDI -> "केयरसर्कल में आपका स्वागत है। भाषा हिन्दी में बदल दी गई है।"
            AppLanguage.TELUGU -> "కేర్ సర్కిల్‌కి స్వాగతం. భాష తెలుగులోకి మార్చబడింది."
            AppLanguage.TAMIL -> "கேர்சர்க்கிளுக்கு நல்வரவு. மொழி தமிழாக மாற்றப்பட்டது."
            AppLanguage.BENGALI -> "কেয়ারসার্কেলে স্বাগতম। ভাষা বাংলায় পরিবর্তিত হয়েছে।"
            AppLanguage.MARATHI -> "केअरसर्कलमध्ये आपले स्वागत आहे. भाषा मराठीत बदलली आहे."
            AppLanguage.GUJARATI -> "કેરસર્કલ માં તમારું સ્વાગત છે. ભાષા ગુજરાતી માં બદલાઈ ગઈ છે."
            AppLanguage.KANNADA -> "ಕೇರ್‌ಸರ್ಕಲ್‌ಗೆ ಸುಸ್ವಾಗತ. ಭಾಷೆಯನ್ನು ಕನ್ನಡಕ್ಕೆ ಬದಲಾಯಿಸಲಾಗಿದೆ."
            AppLanguage.MALAYALAM -> "കെയർസർക്കിളിലേക്ക് സ്വാഗതം. ഭാഷ മലയാളത്തിലേക്ക് മാറ്റി."
            AppLanguage.ENGLISH -> "Welcome to CareCircle. Language set to English."
        }
        voiceManager.speak(welcomeMsg, lang)
    }

    fun openLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun closeLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun speakVitalsSummary() {
        val s = _uiState.value
        val hr = s.vitals[VitalMetric.HEART_RATE]?.canonicalValue ?: 74.0
        val spo2 = s.vitals[VitalMetric.SPO2]?.canonicalValue ?: 98.0
        val temp = s.vitals[VitalMetric.BODY_TEMPERATURE]?.canonicalValue ?: 36.8
        val bpSys = s.vitals[VitalMetric.BLOOD_PRESSURE_SYSTOLIC]?.canonicalValue ?: 120.0
        val bpDia = s.vitals[VitalMetric.BLOOD_PRESSURE_DIASTOLIC]?.canonicalValue ?: 80.0

        val speechText = AppStrings.getSpokenVitalsSummary(
            lang = s.currentLanguage,
            patientName = s.patientProfile.patientName,
            heartRate = hr,
            spo2 = spo2,
            temp = temp,
            bpSys = bpSys,
            bpDia = bpDia
        )
        voiceManager.speak(speechText, s.currentLanguage)
    }

    fun stopVoice() {
        voiceManager.stop()
    }

    fun toggleDose(doseId: String) {
        _uiState.update { state ->
            val updated = state.doses.map { d ->
                if (d.id == doseId) d.copy(isTaken = !d.isTaken) else d
            }
            state.copy(doses = updated)
        }
    }

    fun startSimulation(scenario: String) {
        simulationJob?.cancel()
        _uiState.update {
            it.copy(
                activeScenario = scenario,
                isSimulating = true,
                connectionStatus = if (scenario == "device_dropout") "Device Disconnected (Offline Room Cache Active)" else "Live Telemetry Streaming (Persisting to Room Cache)"
            )
        }

        simulationJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                tickerSeconds += 2

                if (scenario == "device_dropout") {
                    _uiState.update { state ->
                        val updated = state.vitals.mapValues { (_, v) ->
                            val newAge = v.measuredSecondsAgo + 2
                            val newFreshness = if (newAge > 30) FreshnessState.STALE else FreshnessState.DELAYED
                            v.copy(measuredSecondsAgo = newAge, freshness = newFreshness)
                        }

                        val existingDisconn = state.alerts.any { it.type == AlertType.DEVICE_DISCONNECTED && it.status == AlertStatus.OPEN }
                        val newAlerts = state.alerts.toMutableList()
                        if (!existingDisconn && tickerSeconds >= 10) {
                            newAlerts.add(
                                0,
                                AlertItem(
                                    id = UUID.randomUUID().toString(),
                                    type = AlertType.DEVICE_DISCONNECTED,
                                    metric = null,
                                    severity = AlertSeverity.WARNING,
                                    status = AlertStatus.OPEN,
                                    triggerMessage = "Can't monitor live: no telemetry received from wearable. Showing offline Room cache.",
                                    openedSecondsAgo = 2,
                                    escalationLevel = 0
                                )
                            )
                        }
                        state.copy(
                            vitals = updated,
                            alerts = newAlerts,
                            isOfflineMode = true,
                            connectionStatus = "Device Offline - Serving Local Room Cache"
                        )
                    }
                    continue
                }

                val hrVal = when (scenario) {
                    "tachycardia" -> Random.nextDouble(138.0, 154.0)
                    else -> Random.nextDouble(71.0, 78.0)
                }
                val spo2Val = when (scenario) {
                    "low_spo2" -> Random.nextDouble(85.0, 89.0)
                    else -> Random.nextDouble(97.0, 99.0)
                }
                val tempVal = when (scenario) {
                    "fever" -> Random.nextDouble(39.1, 40.2)
                    else -> Random.nextDouble(36.6, 37.1)
                }
                val respVal = Random.nextDouble(14.0, 18.0)

                _uiState.update { state ->
                    val updatedVitals = state.vitals.toMutableMap()

                    updatedVitals[VitalMetric.HEART_RATE] = VitalReading(
                        id = UUID.randomUUID().toString(),
                        metric = VitalMetric.HEART_RATE,
                        value = Math.round(hrVal * 10.0) / 10.0,
                        unit = "bpm",
                        canonicalValue = Math.round(hrVal * 10.0) / 10.0,
                        canonicalUnit = "bpm",
                        measuredSecondsAgo = 1,
                        sourceKind = SourceKind.BLE,
                        freshness = FreshnessState.LIVE,
                        qualityStatus = QualityStatus.VALID
                    )

                    updatedVitals[VitalMetric.SPO2] = VitalReading(
                        id = UUID.randomUUID().toString(),
                        metric = VitalMetric.SPO2,
                        value = Math.round(spo2Val * 10.0) / 10.0,
                        unit = "%",
                        canonicalValue = Math.round(spo2Val * 10.0) / 10.0,
                        canonicalUnit = "%",
                        measuredSecondsAgo = 1,
                        sourceKind = SourceKind.BLE,
                        freshness = FreshnessState.LIVE,
                        qualityStatus = QualityStatus.VALID
                    )

                    updatedVitals[VitalMetric.BODY_TEMPERATURE] = VitalReading(
                        id = UUID.randomUUID().toString(),
                        metric = VitalMetric.BODY_TEMPERATURE,
                        value = Math.round(tempVal * 10.0) / 10.0,
                        unit = "°C",
                        canonicalValue = Math.round(tempVal * 10.0) / 10.0,
                        canonicalUnit = "°C",
                        measuredSecondsAgo = 2,
                        sourceKind = SourceKind.SIMULATOR,
                        freshness = FreshnessState.LIVE,
                        qualityStatus = QualityStatus.VALID
                    )

                    updatedVitals[VitalMetric.RESPIRATORY_RATE] = VitalReading(
                        id = UUID.randomUUID().toString(),
                        metric = VitalMetric.RESPIRATORY_RATE,
                        value = Math.round(respVal * 10.0) / 10.0,
                        unit = "rpm",
                        canonicalValue = Math.round(respVal * 10.0) / 10.0,
                        canonicalUnit = "rpm",
                        measuredSecondsAgo = 2,
                        sourceKind = SourceKind.SIMULATOR,
                        freshness = FreshnessState.LIVE,
                        qualityStatus = QualityStatus.VALID
                    )

                    if (scenario == "bp_manual") {
                        updatedVitals[VitalMetric.BLOOD_PRESSURE_SYSTOLIC] = VitalReading(
                            id = UUID.randomUUID().toString(),
                            metric = VitalMetric.BLOOD_PRESSURE_SYSTOLIC,
                            value = 148.0,
                            unit = "mmHg",
                            canonicalValue = 148.0,
                            canonicalUnit = "mmHg",
                            measuredSecondsAgo = 5,
                            sourceKind = SourceKind.MANUAL,
                            freshness = FreshnessState.DELAYED,
                            qualityStatus = QualityStatus.VALID
                        )
                    }

                    val currentAlerts = state.alerts.toMutableList()

                    if (scenario == "tachycardia") {
                        val hasHrAlert = currentAlerts.any { it.metric == VitalMetric.HEART_RATE && it.status in listOf(AlertStatus.OPEN, AlertStatus.ESCALATED) }
                        if (!hasHrAlert) {
                            currentAlerts.add(
                                0,
                                AlertItem(
                                    id = UUID.randomUUID().toString(),
                                    type = AlertType.THRESHOLD,
                                    metric = VitalMetric.HEART_RATE,
                                    severity = AlertSeverity.CRITICAL,
                                    status = AlertStatus.OPEN,
                                    triggerMessage = "Sustained Tachycardia: Heart rate ${Math.round(hrVal)} bpm exceeds safe limit (110 bpm)",
                                    openedSecondsAgo = 2,
                                    escalationLevel = 0
                                )
                            )
                        }
                    } else if (scenario == "low_spo2") {
                        val hasSpo2Alert = currentAlerts.any { it.metric == VitalMetric.SPO2 && it.status in listOf(AlertStatus.OPEN, AlertStatus.ESCALATED) }
                        if (!hasSpo2Alert) {
                            currentAlerts.add(
                                0,
                                AlertItem(
                                    id = UUID.randomUUID().toString(),
                                    type = AlertType.THRESHOLD,
                                    metric = VitalMetric.SPO2,
                                    severity = AlertSeverity.CRITICAL,
                                    status = AlertStatus.OPEN,
                                    triggerMessage = "Hypoxemia alert: SpO2 level ${Math.round(spo2Val)}% is below critical threshold (92%)",
                                    openedSecondsAgo = 2,
                                    escalationLevel = 0
                                )
                            )
                        }
                    } else if (scenario == "fever") {
                        val hasTempAlert = currentAlerts.any { it.metric == VitalMetric.BODY_TEMPERATURE && it.status in listOf(AlertStatus.OPEN, AlertStatus.ESCALATED) }
                        if (!hasTempAlert) {
                            currentAlerts.add(
                                0,
                                AlertItem(
                                    id = UUID.randomUUID().toString(),
                                    type = AlertType.THRESHOLD,
                                    metric = VitalMetric.BODY_TEMPERATURE,
                                    severity = AlertSeverity.WARNING,
                                    status = AlertStatus.OPEN,
                                    triggerMessage = "Elevated Body Temperature: ${Math.round(tempVal * 10) / 10.0} °C",
                                    openedSecondsAgo = 2,
                                    escalationLevel = 0
                                )
                            )
                        }
                    } else if (scenario == "normal") {
                        currentAlerts.forEach { a ->
                            if (a.type == AlertType.THRESHOLD && a.status in listOf(AlertStatus.OPEN, AlertStatus.ESCALATED)) {
                                a.status = AlertStatus.RESOLVED
                            }
                        }
                    }

                    viewModelScope.launch {
                        vitalRepository.cacheAllVitals(updatedVitals.values.toList())
                    }

                    state.copy(
                        vitals = updatedVitals,
                        alerts = currentAlerts,
                        isOfflineMode = false,
                        connectionStatus = "Telemetry Live (BLE + Room Cached)"
                    )
                }
            }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        _uiState.update { state ->
            val updated = state.alerts.map { a ->
                if (a.id == alertId) a.copy(status = AlertStatus.ACKNOWLEDGED) else a
            }
            state.copy(alerts = updated)
        }
    }

    fun resolveAlert(alertId: String) {
        _uiState.update { state ->
            val updated = state.alerts.map { a ->
                if (a.id == alertId) a.copy(status = AlertStatus.RESOLVED) else a
            }
            state.copy(alerts = updated)
        }
    }

    fun triggerManualSos() {
        sosJob?.cancel()
        _uiState.update { it.copy(isSosActive = true, sosCountdownSeconds = 10) }

        sosJob = viewModelScope.launch {
            for (sec in 10 downTo 1) {
                _uiState.update { it.copy(sosCountdownSeconds = sec) }
                delay(1000)
            }

            // Countdown finished: Notify all registered family and caregiver contacts!
            val profile = _uiState.value.patientProfile
            val contactsToNotify = profile.caregiversAndFamily.filter { it.notifyOnSos }
            val notifiedSummary = contactsToNotify.joinToString { "${it.name} (${it.phone})" }

            _uiState.update { state ->
                val newAlerts = state.alerts.toMutableList()
                newAlerts.add(
                    0,
                    AlertItem(
                        id = UUID.randomUUID().toString(),
                        type = AlertType.MANUAL_SOS,
                        metric = null,
                        severity = AlertSeverity.CRITICAL,
                        status = AlertStatus.OPEN,
                        triggerMessage = "EMERGENCY SOS: Notified Caregivers: $notifiedSummary. Location: ${profile.fullAddress}",
                        openedSecondsAgo = 1,
                        escalationLevel = 0,
                        patientName = profile.patientName
                    )
                )
                state.copy(
                    isSosActive = false,
                    alerts = newAlerts,
                    notifiedCaregiverToast = "Emergency SOS Alert dispatched to: $notifiedSummary",
                    selectedTab = 0
                )
            }
        }
    }

    fun clearNotificationToast() {
        _uiState.update { it.copy(notifiedCaregiverToast = null) }
    }

    fun cancelSos() {
        sosJob?.cancel()
        _uiState.update { it.copy(isSosActive = false, sosCountdownSeconds = 10) }
    }

    fun updateThreshold(metric: VitalMetric, minVal: Double?, maxVal: Double?) {
        _uiState.update { state ->
            val updated = state.thresholds.map { t ->
                if (t.metric == metric) t.copy(minValue = minVal, maxValue = maxVal) else t
            }
            state.copy(thresholds = updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
        simulationJob?.cancel()
        sosJob?.cancel()
    }
}

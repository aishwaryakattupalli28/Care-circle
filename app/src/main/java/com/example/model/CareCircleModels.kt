package com.example.model

enum class VitalMetric(val displayName: String, val canonicalUnit: String) {
    HEART_RATE("Heart Rate", "bpm"),
    SPO2("Blood Oxygen (SpO2)", "%"),
    BODY_TEMPERATURE("Body Temperature", "°C"),
    BLOOD_PRESSURE_SYSTOLIC("Blood Pressure (Sys)", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("Blood Pressure (Dia)", "mmHg"),
    RESPIRATORY_RATE("Respiratory Rate", "rpm")
}

enum class FreshnessState(val label: String) {
    LIVE("Live (Realtime)"),
    DELAYED("Delayed (Cached)"),
    STALE("Stale (Outdated)")
}

enum class QualityStatus(val label: String) {
    VALID("Physiologically Valid"),
    SUSPECT("Suspect / Artefact"),
    INVALID("Physiologically Impossible")
}

enum class SourceKind(val label: String) {
    SIMULATOR("Simulator Engine"),
    BLE("Bluetooth Low Energy"),
    HEALTH_CONNECT("Health Connect"),
    HEALTHKIT("Apple HealthKit"),
    MANUAL("Manual Log")
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class AlertStatus {
    OPEN,
    ACKNOWLEDGED,
    ESCALATED,
    RESOLVED
}

enum class AlertType {
    THRESHOLD,
    STALE_DATA,
    DEVICE_DISCONNECTED,
    MANUAL_SOS
}

data class VitalReading(
    val id: String,
    val metric: VitalMetric,
    val value: Double,
    val unit: String,
    val canonicalValue: Double,
    val canonicalUnit: String,
    val measuredSecondsAgo: Long,
    val sourceKind: SourceKind,
    val freshness: FreshnessState,
    val qualityStatus: QualityStatus,
    val isSuppressed: Boolean = false
)

data class AlertItem(
    val id: String,
    val type: AlertType,
    val metric: VitalMetric?,
    val severity: AlertSeverity,
    var status: AlertStatus,
    val triggerMessage: String,
    val openedSecondsAgo: Long,
    var escalationLevel: Int,
    val patientName: String = "Eleanor Vance"
)

data class ThresholdConfig(
    val metric: VitalMetric,
    var minValue: Double?,
    var maxValue: Double?,
    var sustainedSeconds: Int = 20,
    var maxAgeSeconds: Int = 120,
    var enabled: Boolean = true
)

package com.example.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val relationship: String, // e.g. "Primary Caregiver", "Daughter", "Spouse", "Son"
    val phone: String,
    val isPrimary: Boolean = false,
    val notifyOnSos: Boolean = true
)

data class MedicalReportItem(
    val id: String,
    val title: String,
    val date: String,
    val doctorOrClinic: String,
    val summary: String,
    val category: String = "Cardiology"
)

data class PatientProfile(
    val patientName: String = "Eleanor Vance",
    val patientAge: Int = 74,
    val bloodGroup: String = "O+ Positive",
    val primaryCondition: String = "Hypertension & Mild Arrhythmia",
    val cityLocation: String = "New Delhi, South Ext. II",
    val fullAddress: String = "B-42, Gulmohar Enclave, South Ext. II, New Delhi - 110049",
    val caregiversAndFamily: List<EmergencyContact> = listOf(
        EmergencyContact("c1", "Sarah Vance", "Daughter (Primary Caregiver)", "+91 98101 23456", isPrimary = true),
        EmergencyContact("c2", "Dr. Arvind Vance", "Son", "+91 98202 34567"),
        EmergencyContact("c3", "Robert Vance", "Spouse", "+91 98303 45678")
    ),
    val previousMedicalReports: List<MedicalReportItem> = listOf(
        MedicalReportItem("r1", "Annual Echocardiogram & Doppler", "15 Sep 2026", "Fortis Escorts Heart Inst.", "Normal LV systolic function, EF 58%, mild diastolic relaxation impairment."),
        MedicalReportItem("r2", "Comprehensive Metabolic & Lipid Panel", "02 Aug 2026", "Apollo Diagnostics", "Total Cholesterol 185 mg/dL, HbA1c 5.7% (Pre-diabetic target controlled)."),
        MedicalReportItem("r3", "24-Hour Ambulatory Holter ECG", "20 May 2026", "Max Healthcare Saket", "Isolated nocturnal premature ventricular contractions, baseline sinus rhythm preserved.")
    ),
    val isLoggedIn: Boolean = true
)

data class WeeklyVitalPoint(
    val dayLabel: String, // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val heartRateAvg: Float,
    val heartRateMin: Float,
    val heartRateMax: Float,
    val spo2Avg: Float,
    val tempAvg: Float,
    val bpSysAvg: Float,
    val bpDiaAvg: Float
)

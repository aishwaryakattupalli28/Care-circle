package com.example.hospital

data class NearbyHospital(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val emergencyPhone: String,
    val address: String,
    val emergencyType: String, // "Trauma Level 1", "Cardiac Care Unit", "General Emergency"
    val isAvailable24x7: Boolean = true,
    val estimatedArrivalMinutes: Int,
    val rating: Double = 4.8,
    val bedsAvailable: Int = 18
)

object HospitalDataProvider {
    fun getSampleNearbyHospitals(userLatitude: Double = 28.6139, userLongitude: Double = 77.2090): List<NearbyHospital> {
        return listOf(
            NearbyHospital(
                id = "hosp_1",
                name = "Apollo Multispeciality & Emergency Center",
                distanceKm = 1.2,
                emergencyPhone = "+91 11 2692 5858",
                address = "Sarita Vihar, Main Mathura Road, New Delhi",
                emergencyType = "Cardiac Care & ICU 24/7",
                isAvailable24x7 = true,
                estimatedArrivalMinutes = 4,
                rating = 4.9,
                bedsAvailable = 24
            ),
            NearbyHospital(
                id = "hosp_2",
                name = "Max Super Speciality Hospital",
                distanceKm = 2.4,
                emergencyPhone = "+91 11 2651 5050",
                address = "1, 2, Press Enclave Marg, Saket, New Delhi",
                emergencyType = "Trauma Center & Stroke Unit 24/7",
                isAvailable24x7 = true,
                estimatedArrivalMinutes = 7,
                rating = 4.8,
                bedsAvailable = 19
            ),
            NearbyHospital(
                id = "hosp_3",
                name = "Fortis Escorts Heart Institute",
                distanceKm = 3.8,
                emergencyPhone = "+91 11 4713 5000",
                address = "Okhla Road, Sukhdev Vihar, New Delhi",
                emergencyType = "Advanced Cardiology & Resuscitation",
                isAvailable24x7 = true,
                estimatedArrivalMinutes = 11,
                rating = 4.9,
                bedsAvailable = 15
            ),
            NearbyHospital(
                id = "hosp_4",
                name = "AIIMS Trauma & Emergency Hospital",
                distanceKm = 5.1,
                emergencyPhone = "102",
                address = "Sri Aurobindo Marg, Ansari Nagar, New Delhi",
                emergencyType = "National Emergency & Critical Care",
                isAvailable24x7 = true,
                estimatedArrivalMinutes = 14,
                rating = 4.7,
                bedsAvailable = 32
            ),
            NearbyHospital(
                id = "hosp_5",
                name = "Moolchand Medcity Emergency",
                distanceKm = 6.0,
                emergencyPhone = "+91 11 4200 0000",
                address = "Lala Lajpat Rai Marg, Defence Colony, New Delhi",
                emergencyType = "Multispeciality Trauma & Stroke Emergency",
                isAvailable24x7 = true,
                estimatedArrivalMinutes = 16,
                rating = 4.8,
                bedsAvailable = 21
            )
        )
    }
}

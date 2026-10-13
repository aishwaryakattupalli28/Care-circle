package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyContact
import com.example.model.MedicalReportItem
import com.example.model.PatientProfile
import com.example.ui.theme.*

@Composable
fun LoginProfileScreen(
    profile: PatientProfile,
    onSaveProfile: (PatientProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(!profile.isLoggedIn) }

    var patientName by remember(profile) { mutableStateOf(profile.patientName) }
    var patientAge by remember(profile) { mutableStateOf(profile.patientAge.toString()) }
    var bloodGroup by remember(profile) { mutableStateOf(profile.bloodGroup) }
    var location by remember(profile) { mutableStateOf(profile.cityLocation) }
    var fullAddress by remember(profile) { mutableStateOf(profile.fullAddress) }
    var primaryCondition by remember(profile) { mutableStateOf(profile.primaryCondition) }

    var contacts by remember(profile) { mutableStateOf(profile.caregiversAndFamily) }
    var reports by remember(profile) { mutableStateOf(profile.previousMedicalReports) }

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showAddReportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("login_profile_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF0F2B3E), Color(0xFF1D263B)))
                        )
                        .padding(12.dp)
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
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(PrimaryTeal, ElectricBlue))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = NavyBackground,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = patientName.ifEmpty { "Patient Profile" },
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "${patientAge.ifEmpty { "74" }} Yrs • $bloodGroup",
                                        color = PrimaryTealLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isEditing) {
                                        val ageInt = patientAge.toIntOrNull() ?: 74
                                        val updated = profile.copy(
                                            patientName = patientName,
                                            patientAge = ageInt,
                                            bloodGroup = bloodGroup,
                                            cityLocation = location,
                                            fullAddress = fullAddress,
                                            primaryCondition = primaryCondition,
                                            caregiversAndFamily = contacts,
                                            previousMedicalReports = reports,
                                            isLoggedIn = true
                                        )
                                        onSaveProfile(updated)
                                        isEditing = false
                                    } else {
                                        isEditing = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEditing) EmeraldGreen else NavyCard
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (!isEditing) androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal) else null
                            ) {
                                Icon(
                                    if (isEditing) Icons.Filled.Check else Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = if (isEditing) NavyBackground else PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (isEditing) "Save Profile" else "Edit",
                                    color = if (isEditing) NavyBackground else PrimaryTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(location.ifEmpty { "New Delhi, South Ext. II" }, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Patient Demographics & Health Profile
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Patient Details & Medical Registration",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = PrimaryTeal) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = NavyBorder,
                                focusedLabelColor = PrimaryTeal
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = patientAge,
                                onValueChange = { patientAge = it },
                                label = { Text("Age (Years)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryTeal,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it },
                                label = { Text("Blood Group") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryTeal,
                                    unfocusedBorderColor = NavyBorder
                                )
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = primaryCondition,
                            onValueChange = { primaryCondition = it },
                            label = { Text("Primary Diagnosis / Condition") },
                            leadingIcon = { Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = SoftPurple) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = NavyBorder
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("City & Neighborhood") },
                            leadingIcon = { Icon(Icons.Filled.PinDrop, contentDescription = null, tint = AccentCoral) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = NavyBorder
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = fullAddress,
                            onValueChange = { fullAddress = it },
                            label = { Text("Detailed Residence Address (For SOS Responders)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = NavyBorder
                            )
                        )
                    } else {
                        ProfileInfoRow("Primary Diagnosis", primaryCondition, Icons.Filled.MonitorHeart, SoftPurple)
                        Spacer(Modifier.height(8.dp))
                        ProfileInfoRow("Residence Address", fullAddress, Icons.Filled.Home, AccentCoral)
                        Spacer(Modifier.height(8.dp))
                        ProfileInfoRow("Blood Group", bloodGroup, Icons.Filled.Bloodtype, AccentCoral)
                    }
                }
            }
        }

        // Section: Caregivers & Family Emergency Circle (Connected to SOS Button)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Caregivers & Family Contacts",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Directly notified when patient presses SOS",
                        color = PrimaryTealLight,
                        fontSize = 12.sp
                    )
                }

                if (isEditing) {
                    IconButton(
                        onClick = { showAddContactDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add Contact", tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Contact Cards List
        items(contacts) { contact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (contact.isPrimary) PrimaryTeal else NavyBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (contact.isPrimary) PrimaryTeal.copy(alpha = 0.2f) else NavySurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = null,
                                tint = if (contact.isPrimary) PrimaryTeal else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (contact.isPrimary) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryTeal.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PRIMARY", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("${contact.relationship} • ${contact.phone}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("SOS SYNC", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Section: Previous Medical Reports & Clinical History
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Previous Medical Reports",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Clinical diagnostics & hospital health history",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (isEditing) {
                    IconButton(
                        onClick = { showAddReportDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Filled.NoteAdd, contentDescription = "Add Report", tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Reports Cards List
        items(reports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(report.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${report.doctorOrClinic} • ${report.date}", color = PrimaryTealLight, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavySurface)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(report.category, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(report.summary, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }

    // Modal Dialog: Add Family / Caregiver Contact
    if (showAddContactDialog) {
        var newName by remember { mutableStateOf("") }
        var newRel by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            containerColor = NavySurface,
            title = { Text("Add Caregiver / Family Contact", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRel,
                        onValueChange = { newRel = it },
                        label = { Text("Relationship (e.g. Caregiver, Son)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Emergency Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val newC = EmergencyContact(
                                id = "c_${System.currentTimeMillis()}",
                                name = newName,
                                relationship = newRel.ifBlank { "Family Member" },
                                phone = newPhone
                            )
                            contacts = contacts + newC
                            showAddContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Add Contact", color = NavyBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Modal Dialog: Add Medical Report
    if (showAddReportDialog) {
        var repTitle by remember { mutableStateOf("") }
        var repClinic by remember { mutableStateOf("") }
        var repSummary by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddReportDialog = false },
            containerColor = NavySurface,
            title = { Text("Add Medical History Report", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = repTitle,
                        onValueChange = { repTitle = it },
                        label = { Text("Report Name / Test Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = repClinic,
                        onValueChange = { repClinic = it },
                        label = { Text("Hospital / Diagnostic Lab") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = repSummary,
                        onValueChange = { repSummary = it },
                        label = { Text("Clinical Findings / Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repTitle.isNotBlank()) {
                            val newR = MedicalReportItem(
                                id = "r_${System.currentTimeMillis()}",
                                title = repTitle,
                                date = "Oct 2026",
                                doctorOrClinic = repClinic.ifBlank { "CareCircle Medical" },
                                summary = repSummary.ifBlank { "Routine review normal." }
                            )
                            reports = reports + newR
                            showAddReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Save Report", color = NavyBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = TextTertiary, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

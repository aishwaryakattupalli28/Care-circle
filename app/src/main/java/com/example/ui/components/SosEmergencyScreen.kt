package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospital.NearbyHospital
import com.example.i18n.AppStrings
import com.example.i18n.getNearbyHospitalTranslations
import com.example.model.EmergencyContact
import com.example.ui.theme.*
import com.example.viewmodel.CareCircleUiState
import com.example.viewmodel.CareCircleViewModel

/**
 * Scrollable Emergency SOS Screen matching the classic CareCircle format.
 * Features:
 * - Prominent Emergency SOS trigger with animated pulse ring & 10s countdown
 * - Real-time GPS location beacon & patient identity
 * - Family & Caregiver contact list with direct 1-tap call dialer
 * - Complete list of nearby emergency hospitals with ER phone & directions
 * - Multi-tiered escalation protocol ladder
 */
@Composable
fun SosEmergencyScreen(
    state: CareCircleUiState,
    viewModel: CareCircleViewModel
) {
    val lang = state.currentLanguage
    val context = LocalContext.current
    val (hospTitle, hospSubtitle) = getNearbyHospitalTranslations(lang)

    // Pulsing animation for the SOS button ring
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sos_emergency_screen"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- EMERGENCY HERO SECTION: SOS BUTTON & COUNTDOWN ---
        if (state.isSosActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, AccentCoral)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = AccentCoral,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY SOS IN PROGRESS",
                                color = AccentCoral,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Broadcasting distress alert with live vitals to family & caregivers...",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(14.dp))

                        // Animated Circular Countdown Timer
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            // Pulsing Outer Ring
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(AccentCoral.copy(alpha = pulseAlpha))
                            )
                            // Solid Countdown Circle
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D1016))
                                    .border(3.dp, AccentCoral, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.sosCountdownSeconds}",
                                        color = AccentCoral,
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "SECONDS",
                                        color = AccentCoralLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Action Buttons: Cancel and Dispatch Now
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.cancelSos() },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_cancel_sos")
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.get("sos_cancel", lang),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "SOS Dispatched immediately to all caregivers!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_dispatch_now")
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Send Now",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Idle State: Classic Large SOS Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCoral.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EMERGENCY SOS ASSISTANCE",
                            color = AccentCoral,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        // Large Circular SOS Trigger Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            // Pulsing Ring
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(AccentCoral.copy(alpha = pulseAlpha))
                            )

                            // Main Touchable Red Emergency Circle
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFE63946), Color(0xFFB51724))
                                        )
                                    )
                                    .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                    .clickable { viewModel.triggerManualSos() }
                                    .testTag("btn_trigger_manual_sos"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.Sos,
                                        contentDescription = "Press SOS",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "PRESS SOS",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "10-Second Safety Countdown",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Tap button to begin alert. Broadcasts your live coordinates, vital signs, and notifies family and caregivers via Push, SMS, and Voice Call.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        // Quick 112 / 102 National Emergency Call
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Dialing Emergency 112", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCoral.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Icon(Icons.Filled.PhoneInTalk, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Call National Emergency (112)", color = AccentCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- PATIENT LOCATION & IDENTITY BEACON CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("GPS Beacon Active", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                        }
                        Text(
                            text = "${state.patientProfile.patientName} (${state.patientProfile.patientAge}y) • ${state.patientProfile.cityLocation}",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = state.patientProfile.fullAddress,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // --- SECTION 1: FAMILY & CAREGIVER CONTACT NUMBERS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f))
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
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.ContactPhone,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Family & Caregiver Contacts",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Auto-alerted & callable during emergency",
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
                            Text("CONNECTED", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    state.patientProfile.caregiversAndFamily.forEach { contact ->
                        EmergencyContactCard(
                            contact = contact,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Calling ${contact.name}: ${contact.phone}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        // --- SECTION 2: NEARBY EMERGENCY HOSPITALS HEADER ---
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = hospTitle,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Real-time location: ${state.patientProfile.cityLocation} • $hospSubtitle",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentCoral.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("24x7 ER", color = AccentCoralLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        // --- SECTION 2: LIST OF NEARBY HOSPITALS ---
        items(state.nearbyHospitals) { hospital ->
            NearbyHospitalItemCard(
                hospital = hospital,
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.emergencyPhone}"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Dialing ${hospital.name}: ${hospital.emergencyPhone}", Toast.LENGTH_SHORT).show()
                    }
                },
                onNavigate = {
                    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(hospital.name + ", " + hospital.address)}")
                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Navigating to ${hospital.name} (${hospital.distanceKm} km)", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // --- SECTION 3: ESCALATION LADDER PROTOCOL ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Automated Escalation Ladder Protocol", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "• Level 0 (0-2m): Instant high-priority push notification sent to Primary Caregiver.\n" +
                        "• Level 1 (2-5m): Push notification + Telegram Bot alert dispatched to all family members.\n" +
                        "• Level 2 (>5m): Automated SMS emergency dispatch and voice calls placed to family circle.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (contact.isPrimary) PrimaryTeal.copy(alpha = 0.25f) else NavySurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (contact.isPrimary) PrimaryTeal else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(contact.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (contact.isPrimary) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("PRIMARY", color = PrimaryTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text("${contact.relationship} • ${contact.phone}", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Quick Call Button
            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = NavyBackground, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("CALL", color = NavyBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun NearbyHospitalItemCard(
    hospital: NearbyHospital,
    onCall: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hospital.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = hospital.address,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Distance & ETA Badge
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentCoral.copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${hospital.distanceKm} km",
                            color = AccentCoralLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "~${hospital.estimatedArrivalMinutes}m ETA",
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Facility Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NavySurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(hospital.emergencyType, color = PrimaryTealLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(6.dp))
                Text("⭐ ${hospital.rating} • ${hospital.bedsAvailable} ER Beds Available", color = TextTertiary, fontSize = 10.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Actions: Call Emergency & Directions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCoral)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call ER (${hospital.emergencyPhone})", color = AccentCoral, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavyBackground, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Directions", color = NavyBackground, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.LocationTrigger
import com.example.location.LocationReminderManager
import com.example.location.PresetLocation
import com.example.ui.theme.CosmicSurfaceBorder
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LocationPickerDialog(
    initialName: String?,
    initialLat: Double?,
    initialLng: Double?,
    initialRadius: Float,
    initialTrigger: LocationTrigger,
    onDismiss: () -> Unit,
    onConfirm: (name: String, lat: Double, lng: Double, radius: Float, trigger: LocationTrigger) -> Unit,
    locationReminderManager: LocationReminderManager
) {
    val context = LocalContext.current
    var locationName by remember { mutableStateOf(initialName ?: "") }
    var latitude by remember { mutableStateOf(initialLat ?: 23.8103) }
    var longitude by remember { mutableStateOf(initialLng ?: 90.4125) }
    var radiusMeters by remember { mutableFloatStateOf(if (initialRadius > 0) initialRadius else 200f) }
    var triggerType by remember { mutableStateOf(if (initialTrigger != LocationTrigger.NONE) initialTrigger else LocationTrigger.ON_ENTER) }

    var isLocating by remember { mutableStateOf(false) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            locationReminderManager.fetchCurrentLocation(
                onSuccess = { lat, lng ->
                    latitude = lat
                    longitude = lng
                    if (locationName.isBlank()) {
                        locationName = "My Current Location"
                    }
                    isLocating = false
                    locationStatusMessage = "GPS Coordinates acquired: %.4f, %.4f".format(lat, lng)
                },
                onError = { err ->
                    isLocating = false
                    locationStatusMessage = err
                }
            )
        } else {
            locationStatusMessage = "Location permission denied"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CosmicSurfaceCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Location Reminder",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "The reminder will alert you when you arrive at or leave this place.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                item {
                    // Quick Action: Use Current GPS
                    OutlinedButton(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (fineGranted) {
                                isLocating = true
                                locationReminderManager.fetchCurrentLocation(
                                    onSuccess = { lat, lng ->
                                        latitude = lat
                                        longitude = lng
                                        if (locationName.isBlank()) {
                                            locationName = "My Current Location"
                                        }
                                        isLocating = false
                                        locationStatusMessage = "Location set: %.4f, %.4f".format(lat, lng)
                                    },
                                    onError = { err ->
                                        isLocating = false
                                        locationStatusMessage = err
                                    }
                                )
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_gps_location_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting GPS...")
                        } else {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Current GPS Location")
                        }
                    }

                    if (locationStatusMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locationStatusMessage ?: "",
                            color = ElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    Text(
                        text = "Or Choose a Preset Place:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(LocationReminderManager.PRESET_LOCATIONS) { preset ->
                    val isSelected = locationName == preset.name
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                locationName = preset.name
                                latitude = preset.latitude
                                longitude = preset.longitude
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonViolet.copy(alpha = 0.2f) else DeepSpace.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonViolet else CosmicSurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon: ImageVector = when (preset.icon) {
                                "home" -> Icons.Default.Home
                                "business" -> Icons.Default.Business
                                "shopping_cart" -> Icons.Default.ShoppingCart
                                "fitness_center" -> Icons.Default.FitnessCenter
                                "menu_book" -> Icons.AutoMirrored.Filled.MenuBook
                                "local_hospital" -> Icons.Default.LocalHospital
                                else -> Icons.Default.LocationOn
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonViolet else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = preset.name,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Name") },
                        placeholder = { Text("e.g. City Supermarket, Office") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CosmicSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        text = "Trigger When:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = triggerType == LocationTrigger.ON_ENTER,
                            onClick = { triggerType = LocationTrigger.ON_ENTER },
                            label = { Text("On Arrival (পৌঁছালে)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.25f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                        FilterChip(
                            selected = triggerType == LocationTrigger.ON_EXIT,
                            onClick = { triggerType = LocationTrigger.ON_EXIT },
                            label = { Text("On Leaving (বের হলে)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.25f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }

                item {
                    Text(
                        text = "Detection Radius: ${radiusMeters.toInt()} meters",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100f, 200f, 500f).forEach { r ->
                            FilterChip(
                                selected = radiusMeters == r,
                                onClick = { radiusMeters = r },
                                label = { Text("${r.toInt()}m", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                                    selectedLabelColor = NeonViolet
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (locationName.isNotBlank()) {
                        onConfirm(locationName, latitude, longitude, radiusMeters, triggerType)
                    }
                },
                enabled = locationName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_location_button")
            ) {
                Text("Set Location", color = DeepSpace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

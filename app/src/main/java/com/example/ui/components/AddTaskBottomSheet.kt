package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationTrigger
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskCategory
import com.example.location.LocationReminderManager
import com.example.ui.theme.CosmicSurfaceBorder
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DeepSpaceLighter
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskBottomSheet(
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        description: String,
        dueTimestamp: Long,
        priority: Priority,
        category: TaskCategory,
        repeatInterval: RepeatInterval,
        isPermanentNotification: Boolean,
        locationName: String?,
        latitude: Double?,
        longitude: Double?,
        locationRadiusMeters: Float,
        locationTrigger: LocationTrigger
    ) -> Unit,
    onParseAi: (input: String, onParsed: (com.example.ai.ParsedTaskResult) -> Unit) -> Unit,
    isAiParsing: Boolean,
    locationReminderManager: LocationReminderManager
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var naturalLanguageInput by remember { mutableStateOf("") }
    var aiParsedMessage by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueTimestamp by remember { mutableLongStateOf(System.currentTimeMillis() + (2 * 3600 * 1000L)) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var category by remember { mutableStateOf(TaskCategory.WORK) }
    var repeatInterval by remember { mutableStateOf(RepeatInterval.NONE) }
    var isPermanentNotification by remember { mutableStateOf(true) }

    // Location state
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationName by remember { mutableStateOf<String?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationRadiusMeters by remember { mutableFloatStateOf(200f) }
    var locationTrigger by remember { mutableStateOf(LocationTrigger.NONE) }

    val calendar = remember(dueTimestamp) {
        Calendar.getInstance().apply { timeInMillis = dueTimestamp }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CosmicSurfaceCard,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Smart Reminder",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            // AI Natural Language Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DeepSpaceLighter,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🤖 AI Smart Input (বাংলা / English)",
                            color = NeonViolet,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = naturalLanguageInput,
                        onValueChange = { naturalLanguageInput = it },
                        placeholder = {
                            Text(
                                "e.g. 'আজ রাত ৯টায় পড়তে বসব' or 'Doctor appointment tomorrow at 10 AM (Urgent)'",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_natural_language_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CosmicSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Suggestion Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "আজ রাত ৯টায় পড়তে বসব",
                            "Tomorrow 9:30 AM Team Sync",
                            "Grocery at Supermarket (High)",
                            "প্রতিদিন সকাল ৮টায় ব্যায়াম"
                        ).forEach { sample ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CosmicSurfaceBorder.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { naturalLanguageInput = sample }
                            ) {
                                Text(
                                    text = sample,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (naturalLanguageInput.isNotBlank()) {
                                onParseAi(naturalLanguageInput) { parsed ->
                                    title = parsed.title
                                    dueTimestamp = parsed.dueTimestamp
                                    priority = parsed.priority
                                    category = parsed.category
                                    repeatInterval = parsed.repeatInterval
                                    isPermanentNotification = parsed.isPermanentNotification
                                    if (parsed.locationName != null) {
                                        locationName = parsed.locationName
                                        locationTrigger = parsed.locationTrigger
                                        // Set reasonable default coords based on place
                                        val match = LocationReminderManager.PRESET_LOCATIONS.firstOrNull {
                                            it.name.contains(parsed.locationName, ignoreCase = true)
                                        }
                                        if (match != null) {
                                            latitude = match.latitude
                                            longitude = match.longitude
                                        } else {
                                            latitude = 23.8103
                                            longitude = 90.4125
                                        }
                                    }
                                    aiParsedMessage = parsed.parsedExplanation
                                }
                            }
                        },
                        enabled = naturalLanguageInput.isNotBlank() && !isAiParsing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_parse_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        if (isAiParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = DeepSpace,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is parsing schedule...", color = DeepSpace, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DeepSpace)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Magic AI Parse", color = DeepSpace, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (aiParsedMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiParsedMessage ?: "",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                placeholder = { Text("What needs to be done?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CosmicSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Task Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes (Optional)") },
                placeholder = { Text("Additional context, links, or instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CosmicSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Priority Selection (High, Medium, Low)
            Column {
                Text(
                    text = "Priority Level",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { p ->
                        val isSelected = priority == p
                        val color = when (p) {
                            Priority.HIGH -> PriorityHigh
                            Priority.MEDIUM -> PriorityMedium
                            Priority.LOW -> PriorityLow
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                                .testTag("priority_chip_${p.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else DeepSpaceLighter,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) color else CosmicSurfaceBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = p.title,
                                    color = if (isSelected) color else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Category Chips
            Column {
                Text(
                    text = "Category",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                                selectedLabelColor = NeonViolet
                            )
                        )
                    }
                }
            }

            // Due Date & Time Picker Row
            Column {
                Text(
                    text = "Due Date & Time",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Button
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = dueTimestamp }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    c.set(Calendar.YEAR, y)
                                    c.set(Calendar.MONTH, m)
                                    c.set(Calendar.DAY_OF_MONTH, d)
                                    dueTimestamp = c.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_due_date_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        Text(sdf.format(Date(dueTimestamp)), color = TextPrimary, fontSize = 12.sp)
                    }

                    // Time Button
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = dueTimestamp }
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    c.set(Calendar.HOUR_OF_DAY, h)
                                    c.set(Calendar.MINUTE, m)
                                    dueTimestamp = c.timeInMillis
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_due_time_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        Text(sdf.format(Date(dueTimestamp)), color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }

            // Repeat Options
            Column {
                Text(
                    text = "Automatic Repeat (পুনরাবৃত্তি)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RepeatInterval.entries.forEach { r ->
                        FilterChip(
                            selected = repeatInterval == r,
                            onClick = { repeatInterval = r },
                            label = { Text(r.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.25f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }
            }

            // Permanent Notification Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DeepSpaceLighter,
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Permanent Notification (স্থায়ী নোটিফিকেশন)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keep pinned in notification drawer until marked done",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isPermanentNotification,
                        onCheckedChange = { isPermanentNotification = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepSpace,
                            checkedTrackColor = NeonViolet
                        ),
                        modifier = Modifier.testTag("permanent_notification_switch")
                    )
                }
            }

            // Location-Based Reminder Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DeepSpaceLighter,
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Location Reminder (লোকেশন ভিত্তিক)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showLocationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_location_dialog_button")
                        ) {
                            Text(
                                text = if (locationName != null) "Edit Location" else "+ Add Place",
                                color = ElectricCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (locationName != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "📍 $locationName",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Trigger: ${locationTrigger.displayName} • Radius: ${locationRadiusMeters.toInt()}m",
                                    color = ElectricCyan,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = {
                                locationName = null
                                latitude = null
                                longitude = null
                                locationTrigger = LocationTrigger.NONE
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove location", tint = TextMuted)
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSaveTask(
                            title,
                            description,
                            dueTimestamp,
                            priority,
                            category,
                            repeatInterval,
                            isPermanentNotification,
                            locationName,
                            latitude,
                            longitude,
                            locationRadiusMeters,
                            locationTrigger
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
            ) {
                Text(
                    text = "Save Task (কাজ সংরক্ষণ করুন)",
                    color = DeepSpace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            initialName = locationName,
            initialLat = latitude,
            initialLng = longitude,
            initialRadius = locationRadiusMeters,
            initialTrigger = locationTrigger,
            onDismiss = { showLocationDialog = false },
            onConfirm = { name, lat, lng, radius, trigger ->
                locationName = name
                latitude = lat
                longitude = lng
                locationRadiusMeters = radius
                locationTrigger = trigger
                showLocationDialog = false
            },
            locationReminderManager = locationReminderManager
        )
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.ui.components.TaskCard
import com.example.ui.theme.CosmicSurfaceBorder
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DeepSpaceLighter
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HologramPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletLight
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ProductivityStats
import com.example.ui.viewmodel.TaskFilterTab
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onOpenAddTask: () -> Unit,
    onNavigateToTasks: (TaskFilterTab) -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val stats by viewModel.productivityStats.collectAsStateWithLifecycle()

    val pendingTasks = allTasks.filter { !it.isCompleted }
    val urgentTasks = pendingTasks.filter { it.priority == Priority.HIGH }
    val overdueTasks = pendingTasks.filter { it.isOverdue }

    val dateFormatted = SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top App Bar / User Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonViolet, ElectricCyan)
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(CosmicSurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NF",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Good day, Master",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "NEVER FORGET AI",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Add Task Button
                    IconButton(
                        onClick = onOpenAddTask,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceCard)
                            .border(1.dp, CosmicSurfaceBorder, CircleShape)
                            .testTag("home_add_task_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications Status Bell
                    IconButton(
                        onClick = { viewModel.testPermanentNotification() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceCard)
                            .border(1.dp, CosmicSurfaceBorder, CircleShape)
                            .testTag("home_notification_bell")
                    ) {
                        Box {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notification Bell", tint = NeonViolet)
                            if (stats.inProgressCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's AI Analysis Hero Banner (matches user's reference mockup with 3D robot)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_analysis_hero_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CosmicSurfaceElevated,
                                    CosmicSurfaceCard
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = 0.8f), ElectricCyan.copy(alpha = 0.4f))),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Date and AI-Report pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DeepSpace.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "📅 $dateFormatted",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonViolet.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable { onNavigateToAiAssistant() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = NeonVioletLight,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "AI-Report",
                                            color = NeonVioletLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Today's AI Analysis",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (urgentTasks.isNotEmpty()) {
                                    "You Have ${urgentTasks.size} Tasks Urgent\nFor Today."
                                } else if (overdueTasks.isNotEmpty()) {
                                    "You Have ${overdueTasks.size} Overdue Tasks\nNeeding Attention."
                                } else {
                                    "All Urgent Tasks Clear!\n${pendingTasks.size} In Progress."
                                },
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                        }

                        // Generated 3D AI Robot Companion Graphic
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(DeepSpace.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ai_bot_companion),
                                contentDescription = "AI Companion",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // Overdue Alert Banner if tasks are missed
        if (overdueTasks.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTasks(TaskFilterTab.OVERDUE) }
                        .testTag("overdue_alert_banner"),
                    shape = RoundedCornerShape(16.dp),
                    color = OverdueRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OverdueRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = OverdueRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Missed Task Alert (${overdueTasks.size} Overdue)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap to review and reschedule or mark complete",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = OverdueRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Stats Row: "Priority Task" mini-card, "In Progress", "Completed" ring
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Priority Tasks mini-card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTasks(TaskFilterTab.URGENT) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Priority Task",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        urgentTasks.take(3).forEach { t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PriorityHigh)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = t.title,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (urgentTasks.isEmpty()) {
                            Text(
                                text = "No urgent tasks",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // In Progress & Completed mini-column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // In Progress Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTasks(TaskFilterTab.PENDING) },
                        shape = RoundedCornerShape(16.dp),
                        color = CosmicSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("In Progress", color = TextSecondary, fontSize = 11.sp)
                                Text("${stats.inProgressCount} tasks", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Completed Card with Circular percentage
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTasks(TaskFilterTab.COMPLETED) },
                        shape = RoundedCornerShape(16.dp),
                        color = CosmicSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Completed", color = TextSecondary, fontSize = 11.sp)
                                Text("${stats.completionPercentage}%", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { stats.completionPercentage / 100f },
                                    modifier = Modifier.size(32.dp),
                                    color = NeonViolet,
                                    trackColor = DeepSpace,
                                    strokeWidth = 3.dp
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonViolet,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Permanent Notification Status Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DeepSpaceLighter,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.3f))
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Permanent Notification Active",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Important tasks stay pinned in your notification shade",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.testPermanentNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sync", color = NeonVioletLight, fontSize = 11.sp)
                    }
                }
            }
        }

        // "All Tasks" section header with View All action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Today's Tasks",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "See All (${pendingTasks.size}) →",
                    color = NeonVioletLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onNavigateToTasks(TaskFilterTab.ALL) }
                        .testTag("see_all_tasks_link")
                )
            }
        }

        // Tasks list preview
        if (pendingTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CosmicSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No pending tasks!",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap + to add a reminder or ask AI to plan your day.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(pendingTasks.take(6), key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onSnooze = { viewModel.snoozeTask(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }
}

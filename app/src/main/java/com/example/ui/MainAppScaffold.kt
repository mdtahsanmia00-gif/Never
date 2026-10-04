package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTaskBottomSheet
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WelcomeHeroScreen
import com.example.ui.theme.CosmicSurfaceBorder
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DeepSpaceLighter
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaskFilterTab
import com.example.ui.viewmodel.TaskViewModel

enum class NavDestination(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    AI_ASSISTANT("Personal AI", Icons.Default.AutoAwesome),
    TASKS("Planner", Icons.Default.Checklist),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainAppScaffold(
    viewModel: TaskViewModel
) {
    var showWelcomeScreen by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    val isAiParsing by viewModel.isAiParsing.collectAsStateWithLifecycle()

    BackHandler(enabled = currentDestination != NavDestination.HOME) {
        currentDestination = NavDestination.HOME
    }

    if (showWelcomeScreen) {
        WelcomeHeroScreen(onGetStarted = { showWelcomeScreen = false })
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepSpace,
        bottomBar = {
            // Futuristic glowing bottom navigation bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = DeepSpaceLighter,
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination
                        val isCenterAi = destination == NavDestination.AI_ASSISTANT

                        if (isCenterAi) {
                            // Center Glowing Sparkle AI Button
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeonViolet else CosmicSurfaceCard)
                                    .border(
                                        2.dp,
                                        if (isSelected) ElectricCyan else CosmicSurfaceBorder,
                                        CircleShape
                                    )
                                    .clickable { currentDestination = destination }
                                    .testTag("nav_item_${destination.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = destination.title,
                                    tint = if (isSelected) DeepSpace else NeonViolet,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) NeonViolet.copy(alpha = 0.2f) else DeepSpaceLighter,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { currentDestination = destination }
                                    .testTag("nav_item_${destination.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        tint = if (isSelected) NeonViolet else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = destination.title,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenAddTask = { showAddTaskSheet = true },
                    onNavigateToTasks = { tab ->
                        viewModel.setSelectedTab(tab)
                        currentDestination = NavDestination.TASKS
                    },
                    onNavigateToAiAssistant = {
                        currentDestination = NavDestination.AI_ASSISTANT
                    }
                )

                NavDestination.CALENDAR -> CalendarScreen(
                    viewModel = viewModel
                )

                NavDestination.AI_ASSISTANT -> AiAssistantScreen(
                    viewModel = viewModel
                )

                NavDestination.TASKS -> TasksScreen(
                    viewModel = viewModel,
                    onOpenAddTask = { showAddTaskSheet = true }
                )

                NavDestination.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }

        if (showAddTaskSheet) {
            AddTaskBottomSheet(
                onDismiss = { showAddTaskSheet = false },
                onSaveTask = { title, desc, due, priority, cat, repeat, permanent, locName, lat, lng, radius, trigger ->
                    viewModel.createTask(
                        title = title,
                        description = desc,
                        dueTimestamp = due,
                        priority = priority,
                        category = cat,
                        repeatInterval = repeat,
                        isPermanentNotification = permanent,
                        locationName = locName,
                        latitude = lat,
                        longitude = lng,
                        locationRadiusMeters = radius,
                        locationTrigger = trigger
                    )
                },
                onParseAi = { input, onParsed ->
                    viewModel.parseSmartNaturalLanguage(input, onParsed)
                },
                isAiParsing = isAiParsing,
                locationReminderManager = viewModel.locationReminderManager
            )
        }
    }
}

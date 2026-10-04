package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NeverForgetApp
import com.example.ai.GeminiService
import com.example.ai.ParsedTaskResult
import com.example.data.model.LocationTrigger
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.location.LocationReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilterTab(val title: String) {
    ALL("All Tasks"),
    TODAY("Today (আজ)"),
    URGENT("Top Priority (জরুরি)"),
    PENDING("In Progress"),
    OVERDUE("Overdue (মিসড)"),
    LOCATION("Location (লোকেশন)"),
    COMPLETED("Completed (সম্পন্ন)")
}

enum class SortOption(val title: String) {
    PRIORITY_DESC("Priority (High to Low)"),
    DUE_DATE_ASC("Due Date (Earliest)"),
    TITLE_ASC("Title (A-Z)"),
    CREATED_DESC("Recently Added")
}

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProductivityStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val overdueCount: Int = 0,
    val urgentTodayCount: Int = 0,
    val completionPercentage: Int = 0
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NeverForgetApp).repository
    val locationReminderManager = LocationReminderManager(application)
    private val notificationHelper = (application as NeverForgetApp).notificationHelper

    val allTasks = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTab = MutableStateFlow(TaskFilterTab.ALL)
    val selectedTab: StateFlow<TaskFilterTab> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.PRIORITY_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedCategory: StateFlow<TaskCategory?> = _selectedCategory.asStateFlow()

    private val _isAiParsing = MutableStateFlow(false)
    val isAiParsing: StateFlow<Boolean> = _isAiParsing.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                isUser = false,
                text = "Hello! I am NEVER FORGET AI 🤖. Write to me in Bengali or English (e.g. 'আজ রাত ৯টায় পড়তে বসব' or 'Doctor appointment tomorrow at 10 AM') and I'll schedule it, pin it to your notifications, and ensure you never miss it!"
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    val productivityStats: StateFlow<ProductivityStats> = allTasks.combine(MutableStateFlow(0)) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val inProgress = tasks.count { !it.isCompleted }
        val overdue = tasks.count { it.isOverdue }
        val now = Calendar.getInstance()
        val urgentToday = tasks.count { task ->
            if (task.isCompleted || task.priority != Priority.HIGH) return@count false
            val cal = Calendar.getInstance().apply { timeInMillis = task.dueTimestamp }
            cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        }
        val pct = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0
        ProductivityStats(
            totalCount = total,
            completedCount = completed,
            inProgressCount = inProgress,
            overdueCount = overdue,
            urgentTodayCount = urgentToday,
            completionPercentage = pct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityStats())

    // Filtered and sorted tasks
    val filteredTasks = combine(
        allTasks,
        _selectedTab,
        _sortOption,
        _searchQuery,
        _selectedCategory
    ) { tasks, tab, sort, query, category ->
        var list = tasks

        // Search query filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.locationName?.lowercase()?.contains(q) == true
            }
        }

        // Category filter
        if (category != null) {
            list = list.filter { it.category == category }
        }

        // Tab filter
        val now = Calendar.getInstance()
        list = when (tab) {
            TaskFilterTab.ALL -> list
            TaskFilterTab.TODAY -> list.filter {
                if (it.dueTimestamp <= 0) false else {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.dueTimestamp }
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
            }
            TaskFilterTab.URGENT -> list.filter { it.priority == Priority.HIGH && !it.isCompleted }
            TaskFilterTab.PENDING -> list.filter { !it.isCompleted }
            TaskFilterTab.OVERDUE -> list.filter { it.isOverdue }
            TaskFilterTab.LOCATION -> list.filter { it.hasLocation && !it.isCompleted }
            TaskFilterTab.COMPLETED -> list.filter { it.isCompleted }
        }

        // Sorting
        when (sort) {
            SortOption.PRIORITY_DESC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.priority.level }
                    .thenBy { it.dueTimestamp }
            )
            SortOption.DUE_DATE_ASC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { if (it.dueTimestamp == 0L) Long.MAX_VALUE else it.dueTimestamp }
            )
            SortOption.TITLE_ASC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.title.lowercase() }
            )
            SortOption.CREATED_DESC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.createdAt }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: TaskFilterTab) {
        _selectedTab.value = tab
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: TaskCategory?) {
        _selectedCategory.value = category
    }

    fun createTask(
        title: String,
        description: String = "",
        dueTimestamp: Long,
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK,
        repeatInterval: RepeatInterval = RepeatInterval.NONE,
        isPermanentNotification: Boolean = true,
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        locationRadiusMeters: Float = 200f,
        locationTrigger: LocationTrigger = LocationTrigger.NONE
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                dueTimestamp = dueTimestamp,
                priority = priority,
                category = category,
                repeatInterval = repeatInterval,
                isPermanentNotification = isPermanentNotification,
                locationName = locationName?.trim()?.ifBlank { null },
                latitude = latitude,
                longitude = longitude,
                locationRadiusMeters = locationRadiusMeters,
                locationTrigger = locationTrigger
            )
            repository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun snoozeTask(task: TaskEntity, minutes: Int = 10) {
        viewModelScope.launch {
            repository.snoozeTask(task.id, minutes)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun parseSmartNaturalLanguage(input: String, onResult: (ParsedTaskResult) -> Unit) {
        viewModelScope.launch {
            _isAiParsing.value = true
            val parsed = GeminiService.parseNaturalLanguageTask(input)
            _isAiParsing.value = false
            onResult(parsed)
        }
    }

    fun sendAiAssistantQuery(query: String) {
        val userMsg = AiChatMessage(isUser = true, text = query)
        _aiMessages.value = _aiMessages.value + userMsg

        viewModelScope.launch {
            _isAiThinking.value = true
            val pendingList = repository.getPendingTasksSync()
            val overdueCount = pendingList.count { it.isOverdue }
            val highPriorityCount = pendingList.count { it.priority == Priority.HIGH }

            val contextInfo = buildString {
                append("Total Pending Tasks: ${pendingList.size}. ")
                append("High Priority: $highPriorityCount. ")
                append("Overdue: $overdueCount. ")
                append("Top Tasks: ")
                pendingList.take(5).forEach {
                    append("[${it.priority.name}] ${it.title}; ")
                }
            }

            val advice = GeminiService.getAiAssistantAdvice(query, contextInfo)
            _isAiThinking.value = false

            val botMsg = AiChatMessage(isUser = false, text = advice)
            _aiMessages.value = _aiMessages.value + botMsg
        }
    }

    fun checkUserLocation(lat: Double, lng: Double) {
        locationReminderManager.evaluateLocationTasks(lat, lng)
    }

    fun testPermanentNotification() {
        viewModelScope.launch {
            repository.refreshPermanentNotification()
        }
    }

    fun triggerTestAlarmNotification() {
        val current = allTasks.value.firstOrNull { !it.isCompleted } ?: TaskEntity(
            title = "Sample Urgent Test Task",
            description = "Never Forget AI Alarm Trigger Test",
            dueTimestamp = System.currentTimeMillis(),
            priority = Priority.HIGH
        )
        notificationHelper.showTaskAlarmNotification(current, isOverdue = false)
    }
}

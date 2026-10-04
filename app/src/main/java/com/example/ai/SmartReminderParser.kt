package com.example.ai

import com.example.data.model.LocationTrigger
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskCategory
import java.util.Calendar
import java.util.regex.Pattern

data class ParsedTaskResult(
    val title: String,
    val description: String = "",
    val dueTimestamp: Long,
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val locationName: String? = null,
    val locationTrigger: LocationTrigger = LocationTrigger.NONE,
    val isPermanentNotification: Boolean = true,
    val parsedExplanation: String = ""
)

object SmartReminderParser {

    /**
     * Resilient offline NLP parser for Bengali and English natural language inputs.
     * Supports inputs like:
     * - "আজ রাত ৯টায় পড়তে বসব"
     * - "কাল সকাল ৮টায় ডাক্তারের কাছে যেতে হবে High priority"
     * - "Tomorrow at 7:30 PM buy groceries at Supermarket"
     * - "Every day 8am take vitamins"
     * - "আজ বিকাল ৫টায় মিটিং"
     */
    fun parseOffline(input: String): ParsedTaskResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Determine Priority
        val priority = when {
            lower.contains("জরুরি") || lower.contains("high") || lower.contains("urgent") || lower.contains("top priority") || lower.contains("গুরুত্বপূর্ণ") -> Priority.HIGH
            lower.contains("low") || lower.contains("কম") || lower.contains("স্বাভাবিক") || lower.contains("simple") -> Priority.LOW
            else -> Priority.MEDIUM
        }

        // 2. Determine Repeat
        val repeat = when {
            lower.contains("প্রতিদিন") || lower.contains("every day") || lower.contains("daily") || lower.contains("রোজ") -> RepeatInterval.DAILY
            lower.contains("প্রতি সপ্তাহে") || lower.contains("every week") || lower.contains("weekly") || lower.contains("সাপ্তাহিক") -> RepeatInterval.WEEKLY
            lower.contains("প্রতি ঘণ্টা") || lower.contains("hourly") || lower.contains("every hour") -> RepeatInterval.HOURLY
            lower.contains("কর্মদিবস") || lower.contains("weekdays") -> RepeatInterval.WEEKDAYS
            lower.contains("প্রতি মাসে") || lower.contains("monthly") -> RepeatInterval.MONTHLY
            else -> RepeatInterval.NONE
        }

        // 3. Determine Category
        val category = TaskCategory.fromString(trimmed)

        // 4. Determine Location & Trigger
        var locationName: String? = null
        var locationTrigger = LocationTrigger.NONE
        when {
            lower.contains("supermarket") || lower.contains("বাজার") || lower.contains("মুদি") -> {
                locationName = "City Supermarket"
                locationTrigger = LocationTrigger.ON_ENTER
            }
            lower.contains("gym") || lower.contains("জিম") || lower.contains("fitness") -> {
                locationName = "Fitness Gym Center"
                locationTrigger = LocationTrigger.ON_ENTER
            }
            lower.contains("office") || lower.contains("অফিস") -> {
                locationName = "Office / Workplace"
                locationTrigger = LocationTrigger.ON_ENTER
            }
            lower.contains("home") || lower.contains("বাসা") || lower.contains("বাড়ি") -> {
                locationName = "Home"
                locationTrigger = if (lower.contains("leave") || lower.contains("বের হলে")) LocationTrigger.ON_EXIT else LocationTrigger.ON_ENTER
            }
            lower.contains("hospital") || lower.contains("হাসপাতাল") || lower.contains("clinic") -> {
                locationName = "Hospital / Clinic"
                locationTrigger = LocationTrigger.ON_ENTER
            }
        }

        // 5. Parse Date and Time
        val calendar = Calendar.getInstance()

        // Days offset
        if (lower.contains("কাল") || lower.contains("আগামীকাল") || lower.contains("tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        } else if (lower.contains("পরশু") || lower.contains("day after tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 2)
        }

        // Time detection
        var hour = 20 // Default to 8 PM if evening or night mentioned
        var minute = 0
        var timeFound = false

        // Bengali number mapping helper
        fun bengaliToEnglishDigits(str: String): String {
            return str.replace('০', '0')
                .replace('১', '1')
                .replace('২', '2')
                .replace('৩', '3')
                .replace('৪', '4')
                .replace('৫', '5')
                .replace('৬', '6')
                .replace('৭', '7')
                .replace('৮', '8')
                .replace('৯', '9')
        }

        val normalized = bengaliToEnglishDigits(trimmed)

        // Detect Bengali specific time words:
        // রাত ৯টায় / রাত 9 টায় / সকাল ৮টায় / বিকাল ৫টায় / দুপুর ১টায়
        val bnTimePattern = Pattern.compile("(সকাল|দুপুর|বিকাল|সন্ধ্যা|রাত)\\s*(\\d{1,2})\\s*(?:টায়|টা|:\\s*(\\d{1,2}))?")
        val bnMatcher = bnTimePattern.matcher(normalized)

        if (bnMatcher.find()) {
            val period = bnMatcher.group(1) ?: ""
            val h = bnMatcher.group(2)?.toIntOrNull() ?: 9
            val m = bnMatcher.group(3)?.toIntOrNull() ?: 0
            minute = m

            hour = when (period) {
                "সকাল" -> if (h == 12) 0 else h // e.g. সকাল ৮টা -> 8 AM
                "দুপুর" -> if (h < 12) h + 12 else 12 // e.g. দুপুর ১টা -> 13 (1 PM)
                "বিকাল", "সন্ধ্যা", "রাত" -> if (h < 12) h + 12 else h // e.g. রাত ৯টায় -> 21 (9 PM)
                else -> h
            }
            timeFound = true
        } else {
            // English time pattern: e.g. 9pm, 9:30 am, at 8:00, 15:00
            val enTimePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?", Pattern.CASE_INSENSITIVE)
            val enMatcher = enTimePattern.matcher(normalized)
            if (enMatcher.find()) {
                val h = enMatcher.group(1)?.toIntOrNull() ?: 9
                val m = enMatcher.group(2)?.toIntOrNull() ?: 0
                val ampm = enMatcher.group(3)?.lowercase()
                minute = m
                hour = when {
                    ampm?.contains("pm") == true -> if (h < 12) h + 12 else 12
                    ampm?.contains("am") == true -> if (h == 12) 0 else h
                    lower.contains("রাত") || lower.contains("night") || lower.contains("evening") || lower.contains("বিকাল") -> if (h < 12) h + 12 else h
                    else -> h
                }
                timeFound = true
            }
        }

        if (!timeFound) {
            // Default 2 hours from now
            calendar.add(Calendar.HOUR_OF_DAY, 2)
            calendar.set(Calendar.MINUTE, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // If the time is already past today and no specific day was set, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis() &&
                !lower.contains("কাল") && !lower.contains("আগামীকাল") && !lower.contains("tomorrow")
            ) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Clean title
        var cleanTitle = trimmed
            .replace(Regex("(আজ|কাল|আগামীকাল|পরশু|সকাল|দুপুর|বিকাল|সন্ধ্যা|রাত|টায়|টা|হবে|বসব|করব)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(today|tomorrow|at|pm|am|every day|daily|weekly|high priority|urgent|low priority)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\d{1,2}:?\\d{0,2}"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanTitle.length < 3) {
            cleanTitle = trimmed
        }

        val explanation = buildString {
            append("AI Parsed: '${cleanTitle}' for ")
            append(if (calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) "Today" else "Tomorrow")
            append(" at ${String.format("%02d:%02d", hour, minute)}")
            if (repeat != RepeatInterval.NONE) append(" • Repeat: ${repeat.displayName}")
            if (priority != Priority.MEDIUM) append(" • Priority: ${priority.title}")
            if (locationName != null) append(" • Location: $locationName")
        }

        return ParsedTaskResult(
            title = cleanTitle.replaceFirstChar { it.uppercase() },
            description = "Created via Never Forget AI Smart Parser",
            dueTimestamp = calendar.timeInMillis,
            priority = priority,
            category = category,
            repeatInterval = repeat,
            locationName = locationName,
            locationTrigger = locationTrigger,
            isPermanentNotification = true,
            parsedExplanation = explanation
        )
    }
}

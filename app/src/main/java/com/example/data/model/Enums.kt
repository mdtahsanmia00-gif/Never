package com.example.data.model

enum class Priority(val title: String, val level: Int) {
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1);

    companion object {
        fun fromString(value: String?): Priority {
            return when (value?.uppercase()?.trim()) {
                "HIGH", "জরুরি", "URGENT", "TOP" -> HIGH
                "LOW", "স্বাভাবিক", "কম" -> LOW
                else -> MEDIUM
            }
        }
    }
}

enum class RepeatInterval(val displayName: String) {
    NONE("No Repeat"),
    HOURLY("Every Hour"),
    DAILY("Daily (প্রতিদিন)"),
    WEEKDAYS("Weekdays (সোম-শুক্র)"),
    WEEKLY("Weekly (সাপ্তাহিক)"),
    MONTHLY("Monthly (মাসিক)");

    companion object {
        fun fromString(value: String?): RepeatInterval {
            return when (value?.uppercase()?.trim()) {
                "HOURLY", "EVERY HOUR", "প্রতি ঘণ্টা" -> HOURLY
                "DAILY", "EVERY DAY", "প্রতিদিন", "রোজ" -> DAILY
                "WEEKDAYS", "কর্মদিবস" -> WEEKDAYS
                "WEEKLY", "EVERY WEEK", "সাপ্তাহিক" -> WEEKLY
                "MONTHLY", "প্রতি মাসে" -> MONTHLY
                else -> NONE
            }
        }
    }
}

enum class TaskCategory(val displayName: String, val iconName: String) {
    WORK("Work", "work"),
    STUDY("Study", "school"),
    HEALTH("Health", "favorite"),
    GROCERIES("Groceries", "shopping_cart"),
    MEETING("Meeting", "video_camera_front"),
    PERSONAL("Personal", "person"),
    OTHER("Other", "more_horiz");

    companion object {
        fun fromString(value: String?): TaskCategory {
            val v = value?.lowercase()?.trim() ?: return OTHER
            return when {
                v.contains("work") || v.contains("কাজ") || v.contains("অফিস") || v.contains("office") -> WORK
                v.contains("study") || v.contains("পড়া") || v.contains("পড়") || v.contains("read") || v.contains("exam") -> STUDY
                v.contains("health") || v.contains("ডাক্তার") || v.contains("doctor") || v.contains("medicine") || v.contains("ঔষধ") -> HEALTH
                v.contains("grocer") || v.contains("বাজার") || v.contains("shop") || v.contains("buy") || v.contains("কেনাকাটা") -> GROCERIES
                v.contains("meet") || v.contains("মিটিং") || v.contains("zoom") || v.contains("call") -> MEETING
                v.contains("person") || v.contains("ব্যক্তিগত") || v.contains("family") -> PERSONAL
                else -> OTHER
            }
        }
    }
}

enum class LocationTrigger(val displayName: String) {
    NONE("No Location"),
    ON_ENTER("When Entering (পৌঁছালে)"),
    ON_EXIT("When Leaving (প্রস্থানকালে)");

    companion object {
        fun fromString(value: String?): LocationTrigger {
            return when (value?.uppercase()?.trim()) {
                "ON_ENTER", "ENTER", "ARRIVAL", "পৌঁছালে" -> ON_ENTER
                "ON_EXIT", "EXIT", "DEPARTURE", "বের হলে", "প্রস্থানকালে" -> ON_EXIT
                else -> NONE
            }
        }
    }
}

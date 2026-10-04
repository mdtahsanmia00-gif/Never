package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.LocationTrigger
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskCategory

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = runCatching { Priority.valueOf(value) }.getOrDefault(Priority.MEDIUM)

    @TypeConverter
    fun fromRepeatInterval(interval: RepeatInterval): String = interval.name

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval = runCatching { RepeatInterval.valueOf(value) }.getOrDefault(RepeatInterval.NONE)

    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = runCatching { TaskCategory.valueOf(value) }.getOrDefault(TaskCategory.WORK)

    @TypeConverter
    fun fromLocationTrigger(trigger: LocationTrigger): String = trigger.name

    @TypeConverter
    fun toLocationTrigger(value: String): LocationTrigger = runCatching { LocationTrigger.valueOf(value) }.getOrDefault(LocationTrigger.NONE)
}

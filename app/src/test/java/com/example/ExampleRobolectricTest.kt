package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.SmartReminderParser
import com.example.data.model.Priority
import com.example.data.model.RepeatInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NEVER FORGET AI", appName)
    }

    @Test
    fun `test offline smart reminder parser with bengali input`() {
        val input = "আজ রাত ৯টায় পড়তে বসব"
        val parsed = SmartReminderParser.parseOffline(input)
        assertNotNull(parsed)
        assertNotNull(parsed.title)
    }

    @Test
    fun `test offline smart reminder parser priority and repeat`() {
        val input = "Tomorrow 8 AM team meeting High Priority every day"
        val parsed = SmartReminderParser.parseOffline(input)
        assertEquals(Priority.HIGH, parsed.priority)
        assertEquals(RepeatInterval.DAILY, parsed.repeatInterval)
    }
}

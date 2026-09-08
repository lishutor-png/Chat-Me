package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("chatme", appName)
  }

  @Test
  fun `default companion config is set for intimate curhat and girlfriend`() {
    val config = com.example.data.model.CompanionConfig()
    assertEquals(com.example.data.model.CompanionPersonality.SWEET_GIRLFRIEND, config.personality)
    assertEquals(com.example.data.model.ChatMode.STANDARD, config.mode)
    assertEquals("Aria", config.botName)
    assertEquals("Kamu", config.userName)
  }

  @Test
  fun `custom api key and config can be persisted and loaded`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.local.ConfigPreferences(context)
    val customConfig = com.example.data.model.CompanionConfig(
        botName = "Mimi",
        userName = "Mas Lutfi",
        customApiKey = "AIzaSyTestApiKey123"
    )
    prefs.saveConfig(customConfig)
    val loaded = prefs.loadConfig()
    assertEquals("Mimi", loaded.botName)
    assertEquals("Mas Lutfi", loaded.userName)
    assertEquals("AIzaSyTestApiKey123", loaded.customApiKey)
  }
}

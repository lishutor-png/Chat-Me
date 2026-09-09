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
    assertEquals(com.example.data.model.ContentFilterLevel.OFF, config.filterLevel)
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

  @Test
  fun `multiple api keys are persisted and rotated correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.local.ConfigPreferences(context)
    val multiKeyConfig = com.example.data.model.CompanionConfig(
        customApiKeys = listOf("AIzaKey1", "AIzaKey2", "AIzaKey3")
    )
    prefs.saveConfig(multiKeyConfig)
    val loaded = prefs.loadConfig()
    assertEquals(3, loaded.customApiKeys.size)
    assertEquals("AIzaKey1", loaded.customApiKeys[0])
    assertEquals("AIzaKey2", loaded.customApiKeys[1])
    assertEquals("AIzaKey3", loaded.customApiKeys[2])

    val client = com.example.data.remote.GeminiApiClient()
    val round1 = client.getCandidateApiKeys(loaded)
    val round2 = client.getCandidateApiKeys(loaded)
    val round3 = client.getCandidateApiKeys(loaded)

    // Verify round-robin starts at next key each round
    assertEquals(3, round1.size)
    assertEquals(3, round2.size)
    assertEquals(3, round3.size)
    // The starting keys should rotate sequentially
    val startKeys = listOf(round1.first(), round2.first(), round3.first())
    org.junit.Assert.assertTrue(startKeys.contains("AIzaKey1"))
    org.junit.Assert.assertTrue(startKeys.contains("AIzaKey2"))
    org.junit.Assert.assertTrue(startKeys.contains("AIzaKey3"))
  }
}

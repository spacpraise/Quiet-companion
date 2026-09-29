package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    assertEquals("Quiet Companion", appName)
  }

  @Test
  fun `verify all visual shell navigation routes exist and are navigable`() {
    val expectedScreens = listOf(
      Screen.ONBOARDING,
      Screen.TODAY,
      Screen.ROUTINE,
      Screen.TASK_CREATION,
      Screen.LIBRARY,
      Screen.BOOK_DETAIL,
      Screen.READER,
      Screen.COMPANION,
      Screen.ACTIVITY,
      Screen.INSIGHTS,
      Screen.SETTINGS,
      Screen.INTERCESSIONS,
      Screen.BOOK_COMPLETION
    )

    assertEquals(13, expectedScreens.size)

    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = CompanionViewModel(application)

    // Initial screen is TODAY
    assertEquals(Screen.TODAY, viewModel.currentScreen.value)

    // Verify navigating to every visual shell route works
    expectedScreens.forEach { targetScreen ->
      viewModel.navigateTo(targetScreen)
      assertEquals(targetScreen, viewModel.currentScreen.value)
    }

    // Verify back navigation returns safely
    assertTrue(viewModel.navigateBack())
    assertNotNull(viewModel.currentScreen.value)

    // Verify demo state toggles for empty, loading, completion
    viewModel.toggleDemoLoading()
    assertTrue(viewModel.isDemoLoading.value)
    viewModel.toggleDemoLoading()

    viewModel.toggleDemoEmpty()
    assertTrue(viewModel.isDemoEmpty.value)
    viewModel.toggleDemoEmpty()

    viewModel.triggerCompletionModal("CADENCE")
    assertEquals("CADENCE", viewModel.activeCompletionModal.value)
    viewModel.dismissCompletionModal()
    assertEquals(null, viewModel.activeCompletionModal.value)
  }
}

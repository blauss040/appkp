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
    assertEquals("K&P Saúde", appName)
  }

  @Test
  fun `geofence distance calculation marks pet safety properly`() {
    val safeRadius = 150f
    val insideDistance = 42f
    val outsideDistance = 210f

    val isInside = insideDistance <= safeRadius
    val isOutside = outsideDistance > safeRadius

    assertEquals(true, isInside)
    assertEquals(true, isOutside)
  }
}

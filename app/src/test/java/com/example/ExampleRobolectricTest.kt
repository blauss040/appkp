package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LocationSafetyStatus
import com.example.model.PetStatus
import com.example.viewmodel.PetHealthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
  fun `verify pet health viewmodel initialization`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = PetHealthViewModel(app)
    val state = viewModel.uiState.value

    assertEquals(3, state.pets.size)
    val thor = state.pets.find { it.id == "thor" }
    assertNotNull(thor)
    assertEquals(72, thor?.heartRateBpm)
    assertEquals(98, thor?.spo2Percent)
    assertEquals(38.2f, thor?.temperatureCelsius ?: 0f, 0.01f)
    assertEquals(LocationSafetyStatus.DENTRO_AREA_SEGURA, thor?.locationStatus)

    // Test simulate geofence exit
    viewModel.toggleSimulateFenceExit("thor")
    val updatedThor = viewModel.uiState.value.pets.find { it.id == "thor" }
    assertEquals(LocationSafetyStatus.FORA_PERIMETRO, updatedThor?.locationStatus)
    assertEquals(PetStatus.ALERTA, updatedThor?.currentStatus)
  }
}

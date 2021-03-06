package com.github.epfl.meili.util

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class LocationServiceTest {
    @Test
    fun listenToLocationChangesTest() {
        val testLocation = Location("testLocation")
        val mockLocationListener = Mockito.mock(LocationListener::class.java)
        Mockito.`when`(mockLocationListener.onLocationChanged(Mockito.any())).then {
            assertEquals(it.arguments[0], testLocation)
            return@then null
        }

        val mockLocationManager = Mockito.mock(LocationManager::class.java)
        Mockito.`when`(mockLocationManager.requestLocationUpdates(
                anyString(), anyLong(), anyFloat(), Mockito.any(LocationListener::class.java))
        ).then {
            val locListener = it.arguments[3] as LocationListener
            locListener.onLocationChanged(testLocation)
            return@then null
        }

        LocationService.getLocationManager = { mockLocationManager }
        LocationService.listenToLocationChanges(null, mockLocationListener)
    }
}
package com.github.epfl.meili.util

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class CustomMathTest {
    @Test
    fun distanceOnSphereTest(){
        assertEquals( 4196, CustomMath.distanceOnSphere(
            LatLng(41.063563, 1.083658),
            LatLng(41.075534, 1.131070)
        ).roundToInt()
        )
    }
}
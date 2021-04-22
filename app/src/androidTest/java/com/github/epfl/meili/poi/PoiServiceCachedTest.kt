package com.github.epfl.meili.poi

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.epfl.meili.MainApplication
import com.github.epfl.meili.models.PointOfInterest
import com.github.epfl.meili.util.InternetConnectionService
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class PoiServiceCachedTest {
    val service: PoiServiceCached = PoiServiceCached()
    var mockInternetConnectionService: InternetConnectionService = mock(InternetConnectionService::class.java)
    var mockSharedPreferences: SharedPreferences = mock(SharedPreferences::class.java)
    var mockSharedPreferencesEditor: SharedPreferences.Editor = mock(SharedPreferences.Editor::class.java)
    val mockPoiGoogleRetriever: PoiGoogleRetriever = mock(PoiGoogleRetriever::class.java)

    private val testPoiList = ArrayList<PointOfInterest>()
    private val poi1 = PointOfInterest(41.075000, 1.130870, "place1", "place1")
    private val poi2 = PointOfInterest(41.063563, 1.083658, "place2", "place2")
    private val testPosition = LatLng(43.0, 1.1)

    @Before
    fun initMocks() {
        testPoiList.add(poi1)
        testPoiList.add(poi2)

        `when`(mockSharedPreferencesEditor.putLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(null)
        `when`(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
    }

    private fun initEmptyPreferences() {
        `when`(mockSharedPreferences.getLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(0L)
        service.setSharedPreferences(mockSharedPreferences)
    }

    private fun initPreferencesWithData(timestamp: Long){
        `when`(mockSharedPreferences.getLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(timestamp)
        `when`(mockSharedPreferences.getString(Mockito.anyString(), Mockito.anyString())).then {
            val key = it.arguments[0] as String
            if(key == PoiServiceCached.POSITION_KEY){
                return@then Gson().toJson(testPosition, LatLng::class.java)
            }else{
                return@then Gson().toJson(testPoiList, testPoiList::class.java)
            }
        }

        service.setSharedPreferences(mockSharedPreferences)
    }


    @Test
    fun requestPoisWhenNoValidDataAndInternetConnection() {
        `when`(mockInternetConnectionService.isConnectedToInternet(MainApplication.applicationContext())).thenReturn(true)
        service.setInternetConnectionServicce(mockInternetConnectionService)
        initEmptyPreferences()

        `when`(mockPoiGoogleRetriever.requestPoisAPI(Mockito.any(), Mockito.any(), Mockito.any())).then {
            val onSuccess = it.arguments[1] as ((List<PointOfInterest>) -> Unit)
            onSuccess(testPoiList)
        }

        service.setPoiGoogleRetriever(mockPoiGoogleRetriever)

        val customOnSuccess: (List<PointOfInterest>) -> Unit = {
            assertEquals(testPoiList, it)
        }

        service.requestPois(LatLng(0.0, 0.0), customOnSuccess, { assert(false) })
    }

    @Test
    fun requestPoisWhenNoValidDataAndNoInternetConnectionCallsOnError() {
        `when`(mockInternetConnectionService.isConnectedToInternet(MainApplication.applicationContext())).thenReturn(false)
        service.setInternetConnectionServicce(mockInternetConnectionService)
        initEmptyPreferences()
        service.requestPois(LatLng(0.0, 0.0), { assert(false) }, { assert(true) })
    }

    @Test
    fun requestPoisWhenObjectDataIsValid(){
        service.lastPoiListResponse = testPoiList
        service.responseTimestamp = System.currentTimeMillis() / 1000

        val customOnSuccess: (List<PointOfInterest>) -> Unit = {
            assertEquals(testPoiList, it)
        }

        service.requestPois(LatLng(0.0,0.0), customOnSuccess, {assert(false)})
    }

    @Test
    fun requestPoisWhenCachedDataIsValid(){
        initPreferencesWithData(System.currentTimeMillis() / 1000)

        val customOnSuccess: (List<PointOfInterest>) -> Unit = {
            assertEquals(testPoiList, it)
        }

        service.requestPois(testPosition, customOnSuccess, {assert(false)})
    }

    @Test
    fun requestPoisWhenObjectDataIsOldButOnlyOption(){
        service.lastPoiListResponse = testPoiList
        service.responseTimestamp = 1L

        val customOnSuccess: (List<PointOfInterest>) -> Unit = {
            assertEquals(testPoiList, it)
        }

        service.requestPois(LatLng(0.0,0.0), customOnSuccess, {assert(false)})
    }

    @Test
    fun requestPoisWhenCachedDataIsOldButOnlyOption(){
        initPreferencesWithData(1L)

        val customOnSuccess: (List<PointOfInterest>) -> Unit = {
            assertEquals(testPoiList, it)
        }

        service.requestPois(testPosition, customOnSuccess, {assert(false)})
    }

}
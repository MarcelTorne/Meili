package com.github.epfl.meili.poi

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.epfl.meili.R
import com.github.epfl.meili.auth.Auth
import com.github.epfl.meili.database.FirestoreDatabase
import com.github.epfl.meili.map.MapActivity
import com.github.epfl.meili.poi.PointOfInterestStatus
import com.github.epfl.meili.models.PointOfInterest
import com.github.epfl.meili.util.MockAuthenticationService
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoResponse
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


@Suppress("UNCHECKED_CAST")
@LargeTest
@RunWith(AndroidJUnit4::class)
class PoiInfoActivityTest {

    companion object {
        private const val TEST_USERNAME = "AUTHOR"
        private const val TEST_TIMESTAMP = -1L
        private const val TEST_AUTHOR_ID = "author id"
        private const val TEST_UID = TEST_AUTHOR_ID + TEST_TIMESTAMP.toString()

    }

    private val fakePoi: PointOfInterest =
        PointOfInterest(10.0, 10.0, "art_brut", "ChIJAAAAAAAAAAARg4pb6XR5bo0")

    private val mockPlaces: PlacesClientService = mock(PlacesClientService::class.java)
    private val mockPlacesClient: PlacesClient = mock(PlacesClient::class.java)

    private val mockFirestore: FirebaseFirestore = mock(FirebaseFirestore::class.java)
    private val mockDocument: DocumentReference = mock(DocumentReference::class.java)
    private val mockAuthenticationService = MockAuthenticationService()
    private val mockSnapshot: QuerySnapshot = mock(QuerySnapshot::class.java)
    private val mockCollection: CollectionReference = mock(CollectionReference::class.java)
    private lateinit var database: FirestoreDatabase<PointOfInterest>


    @Before
    fun initIntents() {
        Intents.init()
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }

    init {
        setupMocks()
        mockAuthenticationService.signInIntent(null)
    }


    private fun setupMocks() {
        val placeBuilder = Place.builder()
        placeBuilder.address = "mockAddress"
        placeBuilder.phoneNumber = "mockPhone"
        placeBuilder.websiteUri = Uri.parse("http://epfl.ch")
        placeBuilder.utcOffsetMinutes = 30
        placeBuilder.openingHours = OpeningHours.builder().build()
        placeBuilder.photoMetadatas = listOf(PhotoMetadata.builder("a").build())
        val place = placeBuilder.build()
        val fpr: FetchPlaceResponse = FetchPlaceResponse.newInstance(place)
        val tcs: TaskCompletionSource<FetchPlaceResponse> = TaskCompletionSource()
        tcs.setResult(fpr)

        `when`(mockPlacesClient.fetchPlace(any())).thenReturn(tcs.task)


        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ALPHA_8)
        val fpr2: FetchPhotoResponse = FetchPhotoResponse.newInstance(bitmap)
        val tcs2: TaskCompletionSource<FetchPhotoResponse> = TaskCompletionSource()
        tcs2.setResult(fpr2)

        `when`(mockPlacesClient.fetchPhoto(any())).thenReturn(tcs2.task)

        `when`(
            mockPlaces.getPlacesClient(
                MockitoHelper.anyObject(),
                MockitoHelper.anyObject()
            )
        ).thenReturn(mockPlacesClient)

        `when`(mockFirestore.collection(any())).thenReturn(
            mockCollection
        )
        `when`(mockCollection.addSnapshotListener(any())).thenAnswer { invocation ->
            database = invocation.arguments[0] as FirestoreDatabase<PointOfInterest>
            mock(ListenerRegistration::class.java)
        }
        `when`(mockCollection.document(ArgumentMatchers.matches(fakePoi.uid))).thenReturn(
            mockDocument
        )

        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
        `when`(mockDocumentSnapshot.id).thenReturn(fakePoi.uid)
        `when`(mockDocumentSnapshot.toObject(PointOfInterest::class.java)).thenReturn(
            fakePoi
        )
        `when`(mockSnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))


        mockAuthenticationService.setMockUid(TEST_UID)
        mockAuthenticationService.setUsername(TEST_USERNAME)

        // Inject dependencies
        PoiInfoActivity.placesClientService = { mockPlaces }
        FirestoreDatabase.databaseProvider = { mockFirestore }
        Auth.authService = mockAuthenticationService
    }


    private val intent = Intent(
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
        PoiInfoActivity::class.java
    ).putExtra("POI_KEY", fakePoi)
        .putExtra(MapActivity.POI_STATUS_KEY, PointOfInterestStatus.VISITED)

    @get:Rule
    var mActivityTestRule: ActivityScenarioRule<PoiInfoActivity> = ActivityScenarioRule(intent)


    @Test
    fun poiActivityTest() {
        onView(withId(R.id.poi_name)).check(matches(withText(fakePoi.name)))
        onView(withId(R.id.openStatusTextView)).check(matches(withText("CLOSED")))
        onView(withId(R.id.call_poi_button)).check(matches(isDisplayed()))
        onView(withId(R.id.take_me_there_button)).check(matches(isDisplayed()))
    }

    @Test
    fun takeMeThereButtonTest() {
        onView(withId(R.id.take_me_there_button)).perform(click())
        Intents.intended(hasPackage("com.google.android.apps.maps"))
    }

    @Test
    fun callButtonTest() {
        onView(withId(R.id.call_poi_button)).perform(click())
        Intents.intended(toPackage("com.android.server.telecom"))
    }

    @Test
    fun favoriteButtonTestSignedIn() {
        database.onEvent(mockSnapshot, null)
        onView(withId(R.id.favorite_button)).check(matches(isDisplayed()))
        onView(withId(R.id.favorite_button)).check(matches(isChecked())).perform(click())
        onView(withId(R.id.favorite_button)).check(matches(isNotChecked())).perform(click())
    }

    object MockitoHelper {
        fun <T> anyObject(): T {
            Mockito.any<T>()
            return uninitialized()
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T = null as T
    }
}


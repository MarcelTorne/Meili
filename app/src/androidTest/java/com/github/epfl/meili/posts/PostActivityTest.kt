package com.github.epfl.meili.posts


import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.epfl.meili.R
import com.github.epfl.meili.auth.Auth
import com.github.epfl.meili.database.AtomicPostFirestoreDatabase
import com.github.epfl.meili.database.FirebaseStorageService
import com.github.epfl.meili.database.FirestoreDatabase
import com.github.epfl.meili.map.MapActivity
import com.github.epfl.meili.models.Comment
import com.github.epfl.meili.models.Post
import com.github.epfl.meili.models.User
import com.github.epfl.meili.profile.friends.UserInfoService
import com.github.epfl.meili.util.ListSorter
import com.github.epfl.meili.util.MockAuthenticationService
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
@RunWith(AndroidJUnit4::class)
class PostActivityTest {

    companion object {
        private const val TEST_POI_KEY = "POI_KEY"
        private const val TEST_POST_ID = "ID"
        private const val TEST_POST_AUTHOR_ID = "AUTHOR POST ID"
        private const val TEST_POI_NAME = "POI_NAME"
        private const val TEST_POST_AUTHOR_NAME = "AUTHOR NAME POST"
        private val TEST_POST = Post(TEST_POI_KEY, TEST_POI_NAME, TEST_POST_AUTHOR_ID, "TITLE", -1, "TEXT")
        private const val TEST_AUTHOR_NAME = "Author Name"
        private const val TEST_AUTHOR_ID = "Auhtor id"
        private val TEST_COMMENT = Comment(TEST_AUTHOR_ID, "TEXT_COMMENT")
    }

    private val mockFirestore: FirebaseFirestore = Mockito.mock(FirebaseFirestore::class.java)
    private val mockCollection: CollectionReference = Mockito.mock(CollectionReference::class.java)
    private val mockComments: CollectionReference = Mockito.mock(CollectionReference::class.java)
    private val mockDocument: DocumentReference = Mockito.mock(DocumentReference::class.java)

    private val mockSnapshotBeforeAddition: QuerySnapshot = Mockito.mock(QuerySnapshot::class.java)
    private val mockSnapshotAfterAddition: QuerySnapshot = Mockito.mock(QuerySnapshot::class.java)

    private val mockAuthenticationService = MockAuthenticationService()

    private lateinit var database: AtomicPostFirestoreDatabase
    private lateinit var commentsDatabase: FirestoreDatabase<Comment>

    private val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext, PostActivity::class.java)
            .putExtra(Post.TAG, TEST_POST)
            .putExtra(PostActivity.POST_ID, TEST_POST_ID)
            .putExtra(MapActivity.POI_KEY, TEST_POI_KEY)

    @get:Rule
    var testRule: ActivityScenarioRule<PostActivity> = ActivityScenarioRule(intent)

    @Before
    fun initIntents() = Intents.init()

    @After
    fun releaseIntents() = Intents.release()

    init {
        setupMocks()
        setupPostActivityMocks()
        setupUserServiceInfo()
    }

    private fun setupUserServiceInfo() {
        val testFriendMap = HashMap<String, User>()
        testFriendMap[TEST_AUTHOR_ID] = User(TEST_AUTHOR_ID, TEST_AUTHOR_NAME)
        testFriendMap[TEST_POST_AUTHOR_ID] = User(TEST_POST_AUTHOR_ID, TEST_POST_AUTHOR_NAME)

        val mockUserInfoService = Mockito.mock(UserInfoService::class.java)
        Mockito.`when`(mockUserInfoService.getUserInformation(Mockito.anyList(), Mockito.any())).then {
            val onSuccess = it.arguments[1] as ((Map<String, User>) -> Unit)

            onSuccess(testFriendMap)

            return@then null
        }

        ListSorter.serviceProvider = { mockUserInfoService }
    }

    private fun setupMocks() {
        Mockito.`when`(mockFirestore.collection("forum"))
                .thenReturn(mockCollection)
        Mockito.`when`(mockCollection.addSnapshotListener(any())).thenAnswer { invocation ->
            database = invocation.arguments[0] as AtomicPostFirestoreDatabase
            Mockito.mock(ListenerRegistration::class.java)
        }
        Mockito.`when`(mockCollection.document(ArgumentMatchers.contains(TEST_POST_ID)))
                .thenReturn(mockDocument)

        Mockito.`when`(mockFirestore.collection("forum/${TEST_POST_ID}/comments"))
                .thenReturn(mockComments)
        Mockito.`when`(mockComments.addSnapshotListener(any())).thenAnswer { invocation ->
            commentsDatabase = invocation.arguments[0] as FirestoreDatabase<Comment>
            Mockito.mock(ListenerRegistration::class.java)
        }
        Mockito.`when`(mockComments.document(ArgumentMatchers.contains(TEST_POST_ID)))
                .thenReturn(mockDocument)

        Mockito.`when`(mockSnapshotBeforeAddition.documents).thenReturn(ArrayList<DocumentSnapshot>())

        val mockDocumentSnapshot: DocumentSnapshot = Mockito.mock(DocumentSnapshot::class.java)
        Mockito.`when`(mockDocumentSnapshot.id).thenReturn(TEST_POST_ID)
        Mockito.`when`(mockDocumentSnapshot.toObject(Comment::class.java))
                .thenReturn(TEST_COMMENT)
        Mockito.`when`(mockSnapshotAfterAddition.documents).thenReturn(listOf(mockDocumentSnapshot))

        mockAuthenticationService.setMockUid(TEST_POST_ID)
        mockAuthenticationService.setUsername(TEST_POST_ID)

        // Inject dependencies
        FirestoreDatabase.databaseProvider = { mockFirestore }
        FirebaseStorageService.storageProvider = { Mockito.mock(FirebaseStorage::class.java) }
        Auth.authService = mockAuthenticationService
    }

    private fun setupPostActivityMocks() {
        val mockFirebase = Mockito.mock(FirebaseStorage::class.java)
        val mockReference = Mockito.mock(StorageReference::class.java)
        Mockito.`when`(mockFirebase.getReference(anyString())).thenReturn(mockReference)

        val mockUploadTask = Mockito.mock(UploadTask::class.java)
        Mockito.`when`(mockReference.putBytes(any())).thenReturn(mockUploadTask)

        val mockStorageTask = Mockito.mock(StorageTask::class.java)
        Mockito.`when`(mockUploadTask.addOnSuccessListener(any())).thenReturn(mockStorageTask as StorageTask<UploadTask.TaskSnapshot>?)

        val mockTask = Mockito.mock(Task::class.java)
        Mockito.`when`(mockReference.downloadUrl).thenReturn(mockTask as Task<Uri>?)
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

        FirebaseStorageService.storageProvider = { mockFirebase }
    }

    @Test
    fun checkPostShown() {
        commentsDatabase.onEvent(mockSnapshotBeforeAddition, null)

        onView(withId(R.id.userName)).check(matches(withText(containsString(TEST_POST_AUTHOR_NAME))))
        onView(withId(R.id.post_title)).check(matches(withText(containsString(TEST_POST.title))))
        onView(withId(R.id.post_text)).check(matches(withText(containsString(TEST_POST.text))))
        onView(withId(R.id.comments_recycler_view)).check(matches(isEnabled()))
    }

    @Test
    fun signedOutDisplayCheck() {
        mockAuthenticationService.signOut()
        commentsDatabase.onEvent(mockSnapshotBeforeAddition, null)

        onView(withId(R.id.userName)).check(matches(withText(containsString(TEST_POST_AUTHOR_NAME))))
        onView(withId(R.id.post_title)).check(matches(withText(containsString(TEST_POST.title))))
        onView(withId(R.id.post_text)).check(matches(withText(containsString(TEST_POST.text))))

        onView(withId(R.id.add_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.comment_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.comments_recycler_view)).check(matches(isEnabled()))
    }

    @Test
    fun signedInDisplayCheck() {
        mockAuthenticationService.signInIntent(null)
        commentsDatabase.onEvent(mockSnapshotBeforeAddition, null)

        onView(withId(R.id.userName)).check(matches(withText(containsString(TEST_POST_AUTHOR_NAME))))
        onView(withId(R.id.post_title)).check(matches(withText(containsString(TEST_POST.title))))
        onView(withId(R.id.post_text)).check(matches(withText(containsString(TEST_POST.text))))

        onView(withId(R.id.add_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.comment_button)).check(matches(isDisplayed()))
        onView(withId(R.id.comments_recycler_view)).check(matches(isEnabled()))
    }

    @Test
    fun addCommentDisplayCheck() {
        mockAuthenticationService.signInIntent(null)
        commentsDatabase.onEvent(mockSnapshotBeforeAddition, null)

        onView(withId(R.id.comment_button)).perform(click())

        onView(withId(R.id.add_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.comment_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.comments_recycler_view)).check(matches(isEnabled()))
    }

    @Test
    fun addCommentTest() {
        mockAuthenticationService.signInIntent(null)
        commentsDatabase.onEvent(mockSnapshotBeforeAddition, null)

        onView(withId(R.id.comment_button)).perform(click())
        onView(withId(R.id.edit_comment)).perform(typeText(TEST_COMMENT.text), closeSoftKeyboard())
        onView(withId(R.id.add_comment)).perform(click())

        // send comments map with added comment to the database
        commentsDatabase.onEvent(mockSnapshotAfterAddition, null)

        onView(withId(R.id.add_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_comment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.comment_button)).check(matches(isDisplayed()))

        onView(withId(R.id.comments_recycler_view))
                .check(matches(isDisplayed()))
                .perform(
                        RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(
                                TEST_COMMENT.text))))

        onView(textViewContainsText(TEST_COMMENT.text)).check(matches(isDisplayed()))
        onView(textViewContainsText(TEST_AUTHOR_NAME)).check(matches(isDisplayed()))

        // Check edit text clears after having posted comment
        onView(withId(R.id.comment_button)).perform(click())
        onView(withId(R.id.edit_comment)).check(matches(withText("")))
    }

    private fun textViewContainsText(content: String): Matcher<View?> {
        return object : TypeSafeMatcher<View?>() {
            override fun describeTo(description: Description) {
                description.appendText("A TextView with the text: $content")
            }

            override fun matchesSafely(item: View?): Boolean {
                when (item) {
                    is TextView -> return item.text == content
                }
                return false
            }
        }
    }
}

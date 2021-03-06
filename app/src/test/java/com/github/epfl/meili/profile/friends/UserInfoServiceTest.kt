package com.github.epfl.meili.profile.friends

import com.github.epfl.meili.database.FirestoreDocumentService
import com.github.epfl.meili.models.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
class UserInfoServiceTest {
    companion object {
        private const val TEST_UID = "TEST_ID"
        private const val TEST_NAME = "TEST_NAME"
    }

    @Test
    fun getUserInformationTest() {
        val testUser = User(TEST_UID, TEST_NAME)

        val mockDocument = Mockito.mock(DocumentSnapshot::class.java)
        Mockito.`when`(mockDocument.exists()).thenReturn(true)
        Mockito.`when`(mockDocument.toObject(User::class.java)).thenReturn(testUser)
        val mockTask = Mockito.mock(Task::class.java)
        Mockito.`when`(mockTask.addOnSuccessListener(Mockito.any())).then {
            val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(mockDocument)
            return@then null
        }

        val mockDocumentReference = Mockito.mock(DocumentReference::class.java)
        Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTask as Task<DocumentSnapshot>)
        val mockFirestore = Mockito.mock(FirebaseFirestore::class.java)
        Mockito.`when`(mockFirestore.document(Mockito.anyString()))
            .thenReturn(mockDocumentReference)

        FirestoreDocumentService.databaseProvider = { mockFirestore }

        val testList = ArrayList<String>()
        testList.add(TEST_UID)
        val expectedResult = HashMap<String, User>()
        expectedResult[TEST_UID] = testUser

        val onSuccess = { it: Map<String, User> -> assertEquals(it, expectedResult) }

        UserInfoService().getUserInformation(testList, onSuccess)
    }
}
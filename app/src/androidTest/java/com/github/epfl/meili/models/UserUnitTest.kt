package com.github.epfl.meili.models


import android.os.Parcel
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*


class UserUnitTest {

    private val TEST_UID: String = "test_uid"
    private val TEST_USERNAME: String = "moderator"



    @Test
    fun userConstructor() {

        var user = User(TEST_UID, TEST_USERNAME)
        assertThat(user.uid, `is`(TEST_UID))
        assertThat(user.component1(), `is`(TEST_UID))
        assertThat(user.username, `is`(TEST_USERNAME))
        assertThat(user.component2(), `is`(TEST_USERNAME))


        assertThat(user.describeContents(), `is`(0))
        assertThat(user.hashCode(), `is`(1110174274))
        assertThat(user.toString(), `is`("User(uid=test_uid, username=moderator)"))

        var otherSameUser = User(TEST_UID, TEST_USERNAME)
        assertThat(user.equals(otherSameUser), `is`(true))
        var otherUser = User("", TEST_USERNAME)
        assertThat(user.equals(otherUser), `is`(false))

        user.copy(TEST_UID, TEST_USERNAME)
        user.writeToParcel(Parcel.obtain(), 0)
    }


}
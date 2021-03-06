package com.github.epfl.meili.util

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.github.epfl.meili.auth.Auth
import com.github.epfl.meili.auth.AuthenticationService
import com.github.epfl.meili.models.User

class MockAuthenticationService : AuthenticationService {

    private var mockUid: String = "1234"
    private var username: String = "username"
    private var signedIn: Boolean = false

    fun setMockUid(uid: String) {
        mockUid = uid
    }

    fun setUsername(username: String) {
        this.username = username
    }

    override fun getCurrentUser(): User? {
        return if (signedIn) {
            User(mockUid, username, "", " ")
        } else {
            null
        }
    }

    override fun signInIntent(activity: AppCompatActivity?): Intent {
        signedIn = true
        Auth.isLoggedIn.postValue(true)
        return Intent()
    }

    override fun signOut() {
        signedIn = false
        Auth.isLoggedIn.postValue(false)
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, result: Int, data: Intent?, onComplete: () -> Unit) {
        onComplete()
    }
}
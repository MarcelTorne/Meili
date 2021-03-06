package com.github.epfl.meili.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.epfl.meili.MainApplication
import com.github.epfl.meili.R
import com.github.epfl.meili.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthenticationService : AuthenticationService {
    private var auth: FirebaseAuth
    private val googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
        var authProvider: () -> FirebaseAuth = { Firebase.auth }
    }

    init {
        val context = MainApplication.applicationContext()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)

        auth = authProvider()
    }

    override fun getCurrentUser(): User? {
        val user: FirebaseUser? = auth.currentUser

        return if (user == null) {
            null
        } else {
            return User(user.uid, user.displayName!!, user.email!!, " ")
        }
    }

    override fun signInIntent(activity: AppCompatActivity?): Intent {
        return googleSignInClient.signInIntent
    }

    override fun signOut() {
        auth.signOut()

        googleSignInClient.signOut()
    }

    private fun firebaseAuthWithGoogle(
            activity: Activity,
            idToken: String,
            onComplete: () -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { onComplete() }
    }

    override fun onActivityResult(
            activity: Activity,
            requestCode: Int,
            result: Int,
            data: Intent?,
            onComplete: () -> Unit
    ) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(activity, account.idToken!!, onComplete)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
}
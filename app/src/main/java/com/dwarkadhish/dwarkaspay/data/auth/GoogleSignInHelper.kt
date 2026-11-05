package com.dwarkadhish.dwarkaspay.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * Provides the Google Sign-In Intent that launches the native Google account picker.
 * The resulting intent should be launched via ActivityResultLauncher.
 */
object GoogleSignInHelper {

    // This web client ID comes from google-services.json → oauth_client type 3
    private const val WEB_CLIENT_ID = "386089693568-3pdmll81i5s2nr1i0cl7iifdqpr2k5c5.apps.googleusercontent.com"

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }
}

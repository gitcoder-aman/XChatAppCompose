package com.tech.xchatapp.googleSign

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.tech.xchatapp.ChatViewModel
import com.tech.xchatapp.R
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    val viewModel: ChatViewModel
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        }catch (e : Exception){
            e.printStackTrace()
            Log.d("@signIn", "signIn: $e")
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false).setServerClientId(R.string.default_web_client_id.toString()).build()
        ).setAutoSelectEnabled(true).build()
    }

    suspend fun signInWithIntent(intent : Intent) : SignInResult{
        viewModel.resetState()
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredential = GoogleAuthProvider.getCredential(googleIdToken,null)
        return try{
            val user = auth.signInWithCredential(googleCredential).await().user
            SignInResult(
                errorMessage = null,
                data = user?.run {
                    UserData(
                        email = email.toString(),
                        userId = uid,
                        username = displayName.toString(),
                        profilePictureUrl = photoUrl.toString().substring(0,photoUrl.toString().length-6)
                    )
                }
            )
        }catch (e : Exception){
            e.printStackTrace()
            Log.d("@signIn", "signInWithIntent: $e")
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

}
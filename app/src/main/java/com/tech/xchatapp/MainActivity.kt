package com.tech.xchatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import com.tech.xchatapp.googleSign.GoogleAuthUiClient
import com.tech.xchatapp.screens.SignInScreen
import com.tech.xchatapp.ui.theme.XChatAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            viewModel = viewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XChatAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = {result->
                            Log.d("@signIn", "onCreate: ${result.resultCode}")
                            if(result.resultCode == RESULT_OK){
                                lifecycleScope.launch {
                                    val signInResult = googleAuthUiClient.signInWithIntent(
                                        intent = result.data ?: return@launch
                                    )
                                    Log.d("@signIn", "onCreate: ${signInResult.data?.email}")
                                }
                            }
                        }
                    )
                    SignInScreen(
                        oneSignInClick = {
                            lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                    ).build()
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.tech.xchatapp.googleSign.GoogleAuthUiClient
import com.tech.xchatapp.navigation.ChatsRoutes
import com.tech.xchatapp.navigation.SignInRoutes
import com.tech.xchatapp.navigation.StartRoutes
import com.tech.xchatapp.screens.ChatScreen
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val state by viewModel.state.collectAsState()
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = StartRoutes) {
                            composable<SignInRoutes> {
                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                                    onResult = { result ->
                                        Log.d(
                                            "@signIn",
                                            "onCreate: result code ${result.resultCode}"
                                        )
                                        if (result.resultCode == RESULT_OK) {
                                            lifecycleScope.launch {
                                                val signInResult =
                                                    googleAuthUiClient.signInWithIntent(
                                                        intent = result.data ?: return@launch
                                                    )
                                                Log.d(
                                                    "@signIn",
                                                    "onCreate: Email ${signInResult.data?.email}"
                                                )
                                                viewModel.onSignInResult(signInResult)
                                            }
                                        }
                                    }
                                )
                                LaunchedEffect(key1 = state.isSignedIn) {
                                    if (state.isSignedIn) {
                                        state.userData.run {
                                            viewModel.addUserDataToFireStore(state.userData)
                                            viewModel.getUserData(state.userData?.userId)
                                            navController.navigate(ChatsRoutes)
                                        }
                                    }
                                }
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
                            composable<ChatsRoutes> {
                                ChatScreen(
                                    viewModel = viewModel,
                                    state = state
                                )
                            }
                            composable<StartRoutes> {
                                LaunchedEffect(Unit) {
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    Log.d("@signIn", "onCreate User Name: ${userData?.username}")
                                    if (userData != null) {
                                        viewModel.getUserData(userData.userId)
                                        navController.navigate(ChatsRoutes)
                                    } else {
                                        navController.navigate(SignInRoutes)
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

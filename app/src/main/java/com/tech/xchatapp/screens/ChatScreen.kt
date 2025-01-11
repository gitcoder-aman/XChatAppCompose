package com.tech.xchatapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.tech.xchatapp.AppState
import com.tech.xchatapp.ChatViewModel
import com.tech.xchatapp.R
import com.tech.xchatapp.dialogs.CustomDialogBox

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    state : AppState
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.showDialog()
            },
                shape = RoundedCornerShape(50.dp),
                containerColor = MaterialTheme.colorScheme.inversePrimary
            ){
                Icon(
                    imageVector = Icons.Filled.AddComment,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) {it
        Image(
            painter = painterResource(id = R.drawable.blck_blurry),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        AnimatedVisibility(visible = state.showDialog) {
            CustomDialogBox(
                state = state,
                setEmail = {
                    viewModel.setSrEmail(it)
                },
                hideDialog = {
                    viewModel.hideDialog()
                },
                addChat = {
                    viewModel.addChat(state.srEmail)
                    viewModel.hideDialog()
                    viewModel.setSrEmail("")
                }
            )
        }

    }
}
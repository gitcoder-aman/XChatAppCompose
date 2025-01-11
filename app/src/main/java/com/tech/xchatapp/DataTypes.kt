package com.tech.xchatapp

import com.google.firebase.Timestamp

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String? = "",
    val email: String = "",
    val bio : String = ""
)

data class AppState(
    val isSignedIn: Boolean = false,
    val userData: UserData? = null,
    val signInError: String? = null,
    val srEmail: String = "",
    val showDialog: Boolean = false
)

data class ChatData(
    val chatId: String = "",
    val lastMessage: Message? = null,
    val user1: ChatUserData? = null,
    val user2: ChatUserData? = null,
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val repliedMessage: Message? = null,
    val reaction: List<Reaction> = emptyList(),
    val imageUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val vidUrl: String = "",
    val progress: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val forwarded: Boolean = false
)

data class Reaction(
    val profilePictureUrl: String = "",
    val username: String = "",
    val userId: String = "",
    val reaction: String = ""
)

data class ChatUserData(
    val userId: String = "",
    val typing: Boolean = false,
    val bio: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val email: String = "",
    val status: Boolean = false,
    val unread: Int = 0
)

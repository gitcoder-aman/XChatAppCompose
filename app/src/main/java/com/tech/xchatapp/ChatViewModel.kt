package com.tech.xchatapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tech.xchatapp.constant.CHAT_COLLECTION
import com.tech.xchatapp.constant.USER_COLLECTION
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val userCollection = Firebase.firestore.collection(USER_COLLECTION)
    var userDataListener: ListenerRegistration? = null

    fun resetState() {

    }

    fun onSignInResult(signInResult: SignInResult) {
        _state.update {
            it.copy(
                isSignedIn = signInResult.data != null,
                signInError = signInResult.errorMessage,
                userData = signInResult.data
            )
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun addUserDataToFireStore(userData: UserData?) {
        val userDataMap = mapOf(
            "userId" to userData?.userId,
            "username" to userData?.username,
            "profilePictureUrl" to userData?.profilePictureUrl,
            "email" to userData?.email
        )
        Log.d(ContentValues.TAG, "addUserDataToFireStore Viewmodel: ${userData!!.username}")
        val userDocument = userCollection.document(userData.userId)
        userDocument.get().addOnSuccessListener {
            if (it.exists()) {
                userDocument.update(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User Data Update Successfully: $it")
                }.addOnFailureListener {
                    Log.d(ContentValues.TAG, "User Data Update Successfully: $it")
                }
            } else {
                userDocument.set(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User Data Added Successfully: $it")
                }.addOnFailureListener {
                    Log.d(ContentValues.TAG, "User Data added to Firebase failed: ${it.message}")
                }
            }
        }.addOnFailureListener {
            Log.d(ContentValues.TAG, "addUserDataToFireStore Viewmodel: ${it.message}")
        }
    }

    fun getUserData(userId: String?) {
        userDataListener = userCollection.document(userId!!).addSnapshotListener { value, error ->
            if (value != null) {
                _state.update {
                    it.copy(userData = value.toObject(UserData::class.java))
                }
            }
        }
    }

    fun hideDialog() {
        _state.update {
            it.copy(
                showDialog = false
            )
        }
    }

    fun showDialog() {
        _state.update {
            it.copy(
                showDialog = true
            )
        }
    }

    fun setSrEmail(email: String) {
        _state.update {
            it.copy(
                srEmail = email
            )
        }
    }

    fun addChat(email: String) {

        //here to not create duplicate chat
        Firebase.firestore.collection(CHAT_COLLECTION).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.email", email),
                    Filter.equalTo("user2.email", state.value.userData?.email)

                ),
                Filter.and(
                    Filter.equalTo("user1.email", state.value.userData?.email),
                    Filter.equalTo("user2.email", email)
                )
            )
        ).get().addOnSuccessListener {
            if(it.isEmpty){
                userCollection.whereEqualTo(
                    "email", email
                ).get().addOnSuccessListener {
                    if (it.isEmpty) {
                        Log.d("@ChatViewModel", "addChat: Email not found")
                    } else {
                        val chatPartner = it.toObjects(UserData::class.java).firstOrNull()
                        val chatId = Firebase.firestore.collection(CHAT_COLLECTION).document().id
                        val chat = ChatData(
                            chatId = chatId,
                            lastMessage = Message(
                                senderId = "",
                                content = "",
                                timestamp = null
                            ),
                            user1 = ChatUserData(
                                userId = state.value.userData?.userId.toString(),
                                username = state.value.userData?.username.toString(),
                                profilePictureUrl = state.value.userData?.profilePictureUrl.toString(),
                                email = state.value.userData?.email.toString(),
                                typing = false,
                                bio = state.value.userData?.bio.toString()
                            ),
                            user2 = ChatUserData(
                                userId = chatPartner?.userId.toString(),
                                username = chatPartner?.username.toString(),
                                profilePictureUrl = chatPartner?.profilePictureUrl.toString(),
                                email = chatPartner?.email.toString(),
                                typing = false,
                                bio = chatPartner?.bio.toString()
                            )
                        )
                        Firebase.firestore.collection(CHAT_COLLECTION).document().set(chat)
                    }
                }
            }else{
                Log.d("@ChatViewModel", "addChat: Chat already exists")
            }
        }
    }
}
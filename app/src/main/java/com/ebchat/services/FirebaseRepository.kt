package com.ebchat.services

import com.ebchat.data.ChatMessage
import com.ebchat.data.GroupProfile
import com.ebchat.data.StoryItem
import com.ebchat.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        setPresence(true)
    }

    suspend fun signup(name: String, email: String, password: String, dateOfBirth: String, gender: String, photoUrl: String): Result<UserProfile> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = requireNotNull(authResult.user?.uid)
        val profile = UserProfile(
            uid = uid,
            name = name,
            email = email,
            username = generateUsername(name),
            dateOfBirth = dateOfBirth,
            gender = com.ebchat.data.Gender.valueOf(gender),
            profilePictureUrl = photoUrl,
            online = true,
        )
        firestore.collection("users").document(uid).set(profile).await()
        setPresence(true)
        profile
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> = runCatching {
        realtimeDb.reference.child("chats").child(message.chatId).child("messages").child(message.id).setValue(message).await()
        realtimeDb.reference.child("recentChats").child(message.senderId).child(message.chatId).setValue(message).await()
        realtimeDb.reference.child("recentChats").child(message.receiverId).child(message.chatId).setValue(message).await()
    }

    suspend fun editMessage(chatId: String, messageId: String, newText: String): Result<Unit> = runCatching {
        realtimeDb.reference.child("chats/$chatId/messages/$messageId").updateChildren(
            mapOf("text" to newText, "edited" to true, "updatedAt" to System.currentTimeMillis())
        ).await()
    }

    suspend fun deleteForEveryone(chatId: String, messageId: String): Result<Unit> = runCatching {
        realtimeDb.reference.child("chats/$chatId/messages/$messageId").updateChildren(
            mapOf("deletedForEveryone" to true, "text" to "")
        ).await()
    }

    suspend fun setTyping(chatId: String, typing: Boolean): Result<Unit> = runCatching {
        val uid = requireNotNull(currentUid)
        realtimeDb.reference.child("typing/$chatId/$uid").setValue(typing).await()
    }

    suspend fun setPresence(online: Boolean): Result<Unit> = runCatching {
        val uid = requireNotNull(currentUid)
        realtimeDb.reference.child("presence/$uid").setValue(mapOf("online" to online, "lastSeen" to System.currentTimeMillis())).await()
        firestore.collection("users").document(uid).update(mapOf("online" to online, "lastSeen" to System.currentTimeMillis())).await()
    }

    suspend fun createGroup(group: GroupProfile): Result<Unit> = runCatching {
        firestore.collection("groups").document(group.id).set(group).await()
    }

    suspend fun uploadStory(story: StoryItem): Result<Unit> = runCatching {
        firestore.collection("stories").document(story.id).set(story).await()
    }

    suspend fun blockUser(uidToBlock: String): Result<Unit> = runCatching {
        val uid = requireNotNull(currentUid)
        firestore.collection("reports").document("block-$uid-$uidToBlock").set(mapOf("from" to uid, "to" to uidToBlock, "type" to "block")).await()
    }

    suspend fun reportUser(uidToReport: String, reason: String): Result<Unit> = runCatching {
        val uid = requireNotNull(currentUid)
        firestore.collection("reports").add(mapOf("from" to uid, "to" to uidToReport, "reason" to reason, "createdAt" to System.currentTimeMillis())).await()
    }

    private fun generateUsername(name: String): String {
        val slug = name.lowercase().filter { it.isLetterOrDigit() }.take(16).ifBlank { "ebuser" }
        return "@${slug}_${Random.nextInt(1000, 9999)}"
    }
}


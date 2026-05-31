package com.ebchat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender { Male, Female, Other }
enum class MessageStatus { Sending, Sent, Delivered, Seen, Failed }
enum class MediaKind { Image, Video, Audio, Story }

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.Other,
    val profilePictureUrl: String = "",
    val bio: String = "",
    val online: Boolean = false,
    val lastSeen: Long = 0,
    val lastSeenHidden: Boolean = false,
    val profilePicturePublic: Boolean = true,
    val blockedUsers: List<String> = emptyList(),
)

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val replyToMessageId: String? = null,
    val edited: Boolean = false,
    val deletedForEveryone: Boolean = false,
    val mediaUrl: String? = null,
    val mediaKind: MediaKind? = null,
    val reactions: Map<String, String> = emptyMap(),
    val status: MessageStatus = MessageStatus.Sending,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class GroupProfile(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val adminIds: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class StoryItem(
    val id: String = "",
    val ownerId: String = "",
    val mediaUrl: String = "",
    val mediaKind: MediaKind = MediaKind.Image,
    val viewerIds: List<String> = emptyList(),
    val likedBy: List<String> = emptyList(),
    val comments: Map<String, String> = emptyMap(),
    val expiresAt: Long = System.currentTimeMillis() + TWELVE_HOURS_MS,
) {
    companion object {
        const val TWELVE_HOURS_MS = 12 * 60 * 60 * 1000L
    }
}

@Entity(tableName = "cached_messages")
data class CachedMessage(
    @PrimaryKey val id: String,
    val chatId: String,
    val payload: String,
    val createdAt: Long,
    val pendingSync: Boolean,
)

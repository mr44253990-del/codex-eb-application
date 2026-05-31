package com.ebchat.services

object FeatureRoadmap {
    val enabledNow = listOf(
        "Firebase Auth email/password signup, login and reset",
        "Realtime DB chat, typing and presence paths",
        "Firestore users, groups, reports and stories metadata",
        "Cloudflare R2 media upload service",
        "Room local cache for retry-safe messages",
        "Firebase Cloud Messaging service with inline reply notification action",
    )

    val plannedSmartFeatures = listOf(
        "AI smart reply suggestions",
        "Message reactions",
        "Pinned and archived chats",
        "Multi-device login metadata",
        "Firebase or Google Drive chat backup",
        "Username search",
        "End-to-end encryption upgrade",
    )
}

package com.ebchat.security

object SecurityPlan {
    const val mediaAutoDeleteDays = 3
    const val storyAutoDeleteHours = 12
    const val endToEndEncryptionStatus = "Planned future upgrade: add per-device identity keys, signed prekeys, and message double-ratchet encryption."

    fun canShowMessagePreview(previewsEnabled: Boolean): Boolean = previewsEnabled
}

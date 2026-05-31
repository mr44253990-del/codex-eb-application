package com.ebchat.services

import com.ebchat.BuildConfig

data class R2Config(
    val accountId: String = BuildConfig.R2_ACCOUNT_ID,
    val bucket: String = BuildConfig.R2_BUCKET,
    val publicUrl: String = BuildConfig.R2_PUBLIC_URL,
    val endpoint: String = BuildConfig.R2_ENDPOINT,
    val accessKeyId: String = BuildConfig.R2_ACCESS_KEY_ID,
    val secretAccessKey: String = BuildConfig.R2_SECRET_ACCESS_KEY,
) {
    val isReady: Boolean
        get() = bucket.isNotBlank() && endpoint.isNotBlank() && accessKeyId.isNotBlank() && secretAccessKey.isNotBlank()
}

package com.ebchat.data

data class UploadProgress(
    val uploadedBytes: Long = 0,
    val totalBytes: Long = 0,
) {
    val remainingBytes: Long = (totalBytes - uploadedBytes).coerceAtLeast(0)
    val percent: Int = if (totalBytes <= 0) 0 else ((uploadedBytes * 100) / totalBytes).toInt()
    val uploadedMb: String = "%.2f MB".format(uploadedBytes / 1024.0 / 1024.0)
    val remainingMb: String = "%.2f MB".format(remainingBytes / 1024.0 / 1024.0)
}

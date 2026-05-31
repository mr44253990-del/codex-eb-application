package com.ebchat.util

inline fun <T> safeCall(block: () -> T): Result<T> = runCatching(block)


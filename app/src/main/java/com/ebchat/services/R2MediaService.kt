package com.ebchat.services

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.ebchat.data.MediaKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class R2MediaService(private val config: R2Config = R2Config()) {
    suspend fun uploadMedia(file: File, kind: MediaKind, onProgress: (uploaded: Long, total: Long) -> Unit): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(config.isReady) { "Cloudflare R2 secrets are missing. Configure local.properties or GitHub Secrets." }
                val key = "${kind.name.lowercase()}/${System.currentTimeMillis()}-${UUID.randomUUID()}-${file.name}"
                onProgress(0, file.length())
                val metadata = ObjectMetadata().apply {
                    contentLength = file.length()
                }
                client().putObject(PutObjectRequest(config.bucket, key, file).withMetadata(metadata))
                onProgress(file.length(), file.length())
                "${config.publicUrl.trimEnd('/')}/$key"
            }
        }

    private fun client(): AmazonS3Client {
        val credentials = BasicAWSCredentials(config.accessKeyId, config.secretAccessKey)
        return AmazonS3Client(credentials, Region.getRegion(Regions.US_EAST_1), ClientConfiguration()).apply {
            setEndpoint(config.endpoint)
            setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build())
        }
    }
}


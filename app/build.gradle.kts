import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun secret(name: String): String =
    (project.findProperty(name) as String?)
        ?: localProperties.getProperty(name)
        ?: System.getenv(name)
        ?: ""

val releaseKeystorePath = secret("ANDROID_KEYSTORE_PATH")
val hasReleaseKeystore = releaseKeystorePath.isNotBlank() && file(releaseKeystorePath).exists()

android {
    namespace = "com.ebchat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ebchat"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "R2_ACCOUNT_ID", "\"${secret("R2_ACCOUNT_ID")}\"")
        buildConfigField("String", "R2_BUCKET", "\"${secret("R2_BUCKET")}\"")
        buildConfigField("String", "R2_PUBLIC_URL", "\"${secret("R2_PUBLIC_URL")}\"")
        buildConfigField("String", "R2_ENDPOINT", "\"${secret("R2_ENDPOINT")}\"")
        buildConfigField("String", "R2_ACCESS_KEY_ID", "\"${secret("R2_ACCESS_KEY_ID")}\"")
        buildConfigField("String", "R2_SECRET_ACCESS_KEY", "\"${secret("R2_SECRET_ACCESS_KEY")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = file(releaseKeystorePath)
                storePassword = secret("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = secret("ANDROID_KEY_ALIAS")
                keyPassword = secret("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.amazonaws:aws-android-sdk-s3:2.76.0")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.76.0")
}

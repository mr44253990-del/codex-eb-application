# EB Chat

EB Chat is an Android/Kotlin chat application scaffold with Firebase authentication, realtime chat paths, Firestore metadata, Firebase Cloud Messaging notifications, Room local cache, and Cloudflare R2 media upload wiring.

## Package

`com.ebchat`

## Local setup

1. Copy `local.properties.example` to `local.properties` and fill secret values locally.
2. Copy `app/google-services.example.json` to `app/google-services.json` and set the Firebase API key, or download the file from Firebase Console.
3. Build debug APK:

```bash
./gradlew assembleDebug
```

## GitHub Actions secrets

Add these repository secrets so CI can build without committing secrets:

- `GOOGLE_SERVICES_JSON`: full `google-services.json` content.
- `R2_ACCOUNT_ID`
- `R2_BUCKET`
- `R2_PUBLIC_URL`
- `R2_ENDPOINT`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`

Optional signed release secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Architecture map

- Auth: Firebase Auth email/password signup, login, and reset.
- Realtime: Firebase Realtime Database for messages, typing, presence, and recent chats.
- Structured data: Firestore users, groups, stories, reports, privacy metadata.
- Media: Cloudflare R2 S3-compatible upload service.
- Notification: Firebase Cloud Messaging service with vibration, sound-ready channel, preview, and inline reply action.
- Local cache: Room database for offline retry-safe cached messages.
- 

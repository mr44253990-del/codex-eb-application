package com.ebchat

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.ebchat.data.ChatMessage
import com.ebchat.services.FirebaseRepository
import com.ebchat.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val repository = FirebaseRepository()
    private var splashPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSplash()
    }

    override fun onStart() {
        super.onStart()
        scope.launch { repository.setPresence(true) }
    }

    override fun onStop() {
        scope.launch { repository.setPresence(false) }
        super.onStop()
    }

    private fun root(title: String): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
            setBackgroundColor(0xFFF8FAFC.toInt())
        }
        container.addView(TextView(this).apply {
            text = title
            textSize = 28f
            setTextColor(0xFF0B5F61.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 24)
        })
        return container
    }

    private fun setScreen(content: LinearLayout) {
        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun showSplash() {
        val pages = listOf(
            "EB Chat কী?\n\nবাংলা ব্যবহারকারীর জন্য দ্রুত, সহজ ও নিরাপদ চ্যাট অ্যাপ।",
            "ফিচার\n\nচ্যাট, ভয়েস মেসেজ, ভিডিও/অডিও মিডিয়া, গ্রুপ, স্টোরি ও রিয়েল-টাইম সিংক।",
            "Privacy & Security\n\nলাস্ট সিন নিয়ন্ত্রণ, ব্লক/রিপোর্ট, লোকাল ক্যাশ, রিট্রাই সিস্টেম এবং future end-to-end encryption plan।",
        )
        val layout = root("EB Chat")
        layout.addView(TextView(this).apply {
            text = pages[splashPage]
            textSize = 18f
            setPadding(0, 32, 0, 32)
        })
        layout.addView(primaryButton(if (splashPage == pages.lastIndex) "Permissions দিন" else "পরবর্তী") {
            if (splashPage == pages.lastIndex) requestAppPermissions() else {
                splashPage += 1
                showSplash()
            }
        })
        setScreen(layout)
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
            permissions += Manifest.permission.READ_MEDIA_IMAGES
            permissions += Manifest.permission.READ_MEDIA_VIDEO
            permissions += Manifest.permission.READ_MEDIA_AUDIO
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissions(permissions.toTypedArray(), 100)
        showLogin()
    }

    private fun showLogin() {
        val layout = root("Login")
        val email = input("Email")
        val password = input("Password")
        layout.addView(email)
        layout.addView(password)
        layout.addView(primaryButton("Login") {
            runAction("Logging in...") { repository.login(email.text.toString(), password.text.toString()) }
        })
        layout.addView(linkButton("Signup") { showSignup() })
        layout.addView(linkButton("Forgot Password") {
            scope.launch {
                repository.resetPassword(email.text.toString()).toast("Password reset email sent")
            }
        })
        setScreen(layout)
    }

    private fun showSignup() {
        val layout = root("Signup")
        val name = input("Name")
        val photo = input("Profile picture URL (R2 upload URL)")
        val email = input("Email")
        val password = input("Password")
        val dob = input("Date of Birth (YYYY-MM-DD)")
        val gender = input("Gender: Male/Female/Other")
        listOf(name, photo, email, password, dob, gender).forEach(layout::addView)
        layout.addView(primaryButton("Create Account") {
            scope.launch {
                repository.signup(
                    name.text.toString(),
                    email.text.toString(),
                    password.text.toString(),
                    dob.text.toString(),
                    gender.text.toString().ifBlank { "Other" },
                    photo.text.toString(),
                ).onSuccess {
                    toast("Welcome ${it.username}")
                    showHome()
                }.onFailure { toast(it.message ?: "Signup failed") }
            }
        })
        layout.addView(linkButton("Back to login") { showLogin() })
        setScreen(layout)
    }

    private fun showHome() {
        val layout = root("Home")
        layout.addView(input("Search users or @username"))
        layout.addView(section("Recent chats", "Unread badge, pin chat and archive chat metadata are ready for Firebase-backed lists."))
        layout.addView(section("All users", "Realtime search will read Firestore users and open 1-to-1 chat."))
        layout.addView(primaryButton("Open Demo Chat") { showChat("demo-chat") })
        layout.addView(linkButton("Stories") { showStories() })
        layout.addView(linkButton("Settings") { showSettings() })
        setScreen(layout)
    }

    private fun showChat(chatId: String) {
        val layout = root("Chat")
        val message = input("Type message")
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { visibility = View.GONE }
        layout.addView(section("Status", "Typing…, delivered/seen, reply, edit, delete and reactions are represented in the message model."))
        layout.addView(message)
        layout.addView(progress)
        layout.addView(primaryButton("Send") {
            val uid = repository.currentUid ?: "anonymous"
            val chatMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = uid,
                receiverId = "demo-peer",
                text = message.text.toString(),
            )
            runAction("Sending...") { repository.sendMessage(chatMessage) }
        })
        layout.addView(linkButton("Voice hold/lock recorder placeholder") { toast("Voice recorder UI hook ready") })
        layout.addView(linkButton("Back Home") { showHome() })
        setScreen(layout)
    }

    private fun showStories() {
        val layout = root("Stories")
        layout.addView(section("Story", "Image/video stories expire after 12 hours, keep viewers, likes and comments in Firestore."))
        layout.addView(primaryButton("Upload Story") { toast("Connect media picker to R2MediaService.uploadMedia") })
        layout.addView(linkButton("Back Home") { showHome() })
        setScreen(layout)
    }

    private fun showSettings() {
        val layout = root("Settings")
        layout.addView(section("Theme", "Dark/light mode and theme color switches can be persisted locally."))
        layout.addView(section("Privacy", "Hide last seen and control profile-picture visibility."))
        layout.addView(section("Notifications", "FCM preview, vibration, sound and inline reply channel configured."))
        layout.addView(linkButton("Back Home") { showHome() })
        setScreen(layout)
    }

    private fun input(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        textSize = 16f
        setSingleLine(false)
    }

    private fun primaryButton(label: String, click: () -> Unit): Button = Button(this).apply {
        text = label
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundColor(0xFF0F8B8D.toInt())
        setOnClickListener { click() }
    }

    private fun linkButton(label: String, click: () -> Unit): Button = Button(this).apply {
        text = label
        setOnClickListener { click() }
    }

    private fun section(heading: String, body: String): TextView = TextView(this).apply {
        text = "$heading\n$body"
        textSize = 16f
        setPadding(0, 18, 0, 18)
    }

    private fun <T> runAction(workingText: String, action: suspend () -> Result<T>) {
        if (!NetworkMonitor.isOnline(this)) {
            toast("Offline: local cache/retry path will be used")
        }
        toast(workingText)
        scope.launch {
            action().toast("Done") { showHome() }
        }
    }

    private fun <T> Result<T>.toast(success: String, afterSuccess: () -> Unit = {}) {
        onSuccess {
            toast(success)
            afterSuccess()
        }.onFailure { toast(it.message ?: "Something went wrong") }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

package com.ebchat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
        window.decorView.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
            insets
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (allGranted) toast("All permissions granted") else toast("Some permissions were denied")
        }
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
        fadeIn(container, 400)
        return container
    }

    private fun setScreen(content: LinearLayout) {
        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun fadeIn(view: View, duration: Long = 600) {
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(duration).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun slideUp(view: View, duration: Long = 500) {
        view.translationY = 120f
        view.alpha = 0f
        view.animate().translationY(0f).alpha(1f).setDuration(duration)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun pulse(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = 300
            start()
        }
    }

    private fun animateScreenChange(newContent: LinearLayout) {
        val scroll = ScrollView(this).apply { addView(newContent) }
        scroll.alpha = 0f
        scroll.translationY = 60f
        setContentView(scroll)
        scroll.animate().alpha(1f).translationY(0f).setDuration(350)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun showSplash() {
        val pages = listOf(
            "EB Chat কী?\n\nবাংলা ব্যবহারকারীর জন্য দ্রুত, সহজ ও নিরাপদ চ্যাট অ্যাপ।",
            "ফিচার\n\nচ্যাট, ভয়েস মেসেজ, ভিডিও/অডিও মিডিয়া, গ্রুপ, স্টোরি ও রিয়েল-টাইম সিংক।",
            "Privacy & Security\n\nলাস্ট সিন নিয়ন্ত্রণ, ব্লক/রিপোর্ট, লোকাল ক্যাশ, রিট্রাই সিস্টেম এবং future end-to-end encryption plan।",
        )
        val layout = root("EB Chat")

        val subtitle = TextView(this).apply {
            text = pages[splashPage]
            textSize = 18f
            setPadding(0, 32, 0, 32)
        }
        layout.addView(subtitle)

        val btn = primaryButton(if (splashPage == pages.lastIndex) "Permissions দিন" else "পরবর্তী") {
            if (splashPage == pages.lastIndex) requestAppPermissions() else {
                splashPage += 1
                showSplash()
            }
        }
        layout.addView(btn)

        val indicator = pageIndicator(pages.size, splashPage)
        layout.addView(indicator)

        setScreen(layout)
        slideUp(subtitle, 500)
        pulse(btn)
    }

    private fun pageIndicator(total: Int, current: Int): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 24, 0, 0)
        }
        for (i in 0 until total) {
            val dot = View(this).apply {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setSize(32, 32)
                    setColor(if (i == current) 0xFF0F8B8D.toInt() else 0xFFCBD5E0.toInt())
                }
                background = bg
            }
            val size = if (i == current) 32 else 24
            val lp = LinearLayout.LayoutParams(size, size).apply { setMargins(6, 0, 6, 0) }
            dot.layoutParams = lp
            row.addView(dot)
        }
        return row
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
        animateScreenChange(layout)
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
        animateScreenChange(layout)
    }

    private fun showHome() {
        val layout = root("Home")
        layout.addView(input("Search users or @username"))
        layout.addView(section("Recent chats", "Unread badge, pin chat and archive chat metadata are ready for Firebase-backed lists."))
        layout.addView(section("All users", "Realtime search will read Firestore users and open 1-to-1 chat."))
        layout.addView(primaryButton("Open Demo Chat") { showChat("demo-chat") })
        layout.addView(linkButton("Stories") { showStories() })
        layout.addView(linkButton("Settings") { showSettings() })
        animateScreenChange(layout)
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
        animateScreenChange(layout)
    }

    private fun showStories() {
        val layout = root("Stories")
        layout.addView(section("Story", "Image/video stories expire after 12 hours, keep viewers, likes and comments in Firestore."))
        layout.addView(primaryButton("Upload Story") { toast("Connect media picker to R2MediaService.uploadMedia") })
        layout.addView(linkButton("Back Home") { showHome() })
        animateScreenChange(layout)
    }

    private fun showSettings() {
        val layout = root("Settings")
        layout.addView(section("Theme", "Dark/light mode and theme color switches can be persisted locally."))
        layout.addView(section("Privacy", "Hide last seen and control profile-picture visibility."))
        layout.addView(section("Notifications", "FCM preview, vibration, sound and inline reply channel configured."))
        layout.addView(linkButton("Back Home") { showHome() })
        animateScreenChange(layout)
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
        setOnClickListener {
            val anim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(this@apply, "scaleX", 1f, 0.95f, 1f),
                    ObjectAnimator.ofFloat(this@apply, "scaleY", 1f, 0.95f, 1f),
                )
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
            }
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(a: Animator) {}
                override fun onAnimationEnd(a: Animator) { click() }
                override fun onAnimationCancel(a: Animator) { click() }
                override fun onAnimationRepeat(a: Animator) {}
            })
            anim.start()
        }
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

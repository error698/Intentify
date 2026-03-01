package com.distractblock.ui

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.distractblock.R
import com.distractblock.data.AppRepository
import com.distractblock.service.BlockerAccessibilityService
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SplashBlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        private const val COUNTDOWN_MS = 5000L
        private const val TICK_MS = 500L

        /** Pair = (question, button text) */
        private val PROMPTS = listOf(
            "Do you really want\nto open this?" to "No, I'll stay focused",
            "Is this really\nworth your time?" to "You're right, skip it",
            "Think about it…\ndo you need this?" to "Nope, I'm good",
            "Are you sure\nyou want to scroll?" to "I'd rather not",
            "Will this help you\nright now?" to "No, let me focus",
            "You were doing great.\nWhy stop now?" to "I'll keep going",
            "Is this the best use\nof your time?" to "I'll do something better",
            "Remember why\nyou blocked this." to "Yes, I'll stay strong",
            "One minute turns\ninto an hour…" to "Not today",
            "Your future self\nwill thank you." to "I'll make them proud",
            "You don't need this\nright now." to "You're right, I don't",
            "Take a deep breath.\nDo you still want to?" to "I'll pass, thanks"
        )

        fun randomPrompt(): Pair<String, String> = PROMPTS.random()
    }

    private lateinit var repository: AppRepository
    private var timer: CountDownTimer? = null
    private var blockedPackage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_block)
        repository = AppRepository(applicationContext)
        blockedPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                timer?.cancel()
                goHome()
            }
        })

        setup()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        blockedPackage = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: blockedPackage
        timer?.cancel()
        setup()
    }

    private fun setup() {
        // Populate icon + name
        val pm = packageManager
        try {
            val info = pm.getApplicationInfo(blockedPackage, 0)
            findViewById<TextView>(R.id.tv_app_name).text =
                pm.getApplicationLabel(info).toString().uppercase()
            findViewById<ImageView>(R.id.iv_app_icon)
                .setImageDrawable(pm.getApplicationIcon(info))
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tv_app_name).text = blockedPackage.uppercase()
        }

        // Pick a random question + button text
        val (question, buttonText) = randomPrompt()
        findViewById<TextView>(R.id.tv_question).text = question

        val tvHourglass = findViewById<TextView>(R.id.tv_hourglass)
        val btnDontOpen = findViewById<MaterialButton>(R.id.btn_dont_open)
        btnDontOpen.text = buttonText

        // Reset button glow
        btnDontOpen.setStrokeColorResource(android.R.color.white)

        // Hourglass flip animation
        var isFlipped = false
        timer = object : CountDownTimer(COUNTDOWN_MS, TICK_MS) {
            override fun onTick(ms: Long) {
                // Flip hourglass emoji each tick
                isFlipped = !isFlipped
                tvHourglass.text = if (isFlipped) "⌛" else "⏳"

                // Rotate animation
                val rotation = if (isFlipped) 180f else 0f
                tvHourglass.animate()
                    .rotation(rotation)
                    .setDuration(400)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

                // Progressive button glow: stroke becomes more red as time runs out
                val progress = 1f - (ms.toFloat() / COUNTDOWN_MS)
                val glowColor = ArgbEvaluator().evaluate(
                    progress,
                    Color.parseColor("#4DE8E8E8"),  // start: subtle white
                    Color.parseColor("#CCFF6B6B")   // end: bright red (but not fully opaque)
                ) as Int
                btnDontOpen.setStrokeColor(android.content.res.ColorStateList.valueOf(glowColor))
                btnDontOpen.setTextColor(
                    ArgbEvaluator().evaluate(
                        progress,
                        Color.parseColor("#E8E8E8"),  // start: light gray
                        Color.parseColor("#FFFFFF")    // end: bright white
                    ) as Int
                )
            }
            override fun onFinish() {
                // Grant pass-through so the service won't re-block
                BlockerAccessibilityService.allowPassThrough(blockedPackage)
                // Record the open then let the app through
                lifecycleScope.launch {
                    repository.recordOpen(blockedPackage)
                }
                finish()
            }
        }.start()

        btnDontOpen.setOnClickListener {
            timer?.cancel()
            goHome()
        }
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        finish()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}

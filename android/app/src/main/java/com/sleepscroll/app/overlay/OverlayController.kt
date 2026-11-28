package com.sleepscroll.app.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.sleepscroll.app.core.IntensityMode
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.core.WindDownContent
import com.sleepscroll.app.core.WindDownStyle
import com.sleepscroll.app.service.SleepController
import java.time.LocalDate

/**
 * Draws the three escalating screens on top of whatever app is in front:
 * phase 1 a small banner, phase 2 a dim card with snooze, phase 3 the full wind-down.
 * Plain Views (no Compose) because overlay windows have no lifecycle owner to hang Compose on.
 */
class OverlayController(private val context: Context) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())

    private var root: View? = null
    private var timerView: TextView? = null
    private var timerPrefix = ""
    private var shownKey: String? = null
    private var bannerDismissed = false
    private val cycles = mutableListOf<Runnable>()

    fun render(phase: Int, remainingMs: Long, style: WindDownStyle, mode: IntensityMode) {
        if (phase != 1) bannerDismissed = false
        if (phase == 1 && bannerDismissed) { removeView(); return }

        val key = "$phase/$style/$mode"
        if (key == shownKey && root != null) {
            timerView?.text = timerPrefix + SleepLogic.formatRemaining(remainingMs)
            return
        }
        removeView()

        val view: View
        val params: WindowManager.LayoutParams
        when (phase) {
            1 -> { view = buildBanner(remainingMs, mode); params = bannerParams() }
            2 -> { view = buildDim(remainingMs, mode); params = fullParams() }
            3 -> { view = buildWindDown(remainingMs, style); params = fullParams() }
            else -> return
        }
        try {
            wm.addView(view, params)
            root = view
            shownKey = key
        } catch (_: Exception) {
            // Overlay permission revoked mid-session, or the window token was rejected. Try again next tick.
            timerView = null
            cancelCycles()
        }
    }

    fun clear() = removeView()

    private fun removeView() {
        cancelCycles()
        root?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        root = null
        timerView = null
        shownKey = null
    }

    private fun cancelCycles() {
        cycles.forEach { handler.removeCallbacks(it) }
        cycles.clear()
    }

    // ── Window params ────────────────────────────────────────────────────────

    private fun bannerParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(40) }

    private fun fullParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        // By default overlay windows stop at the status and navigation bars, leaving the app's
        // (often bright) bars showing above and below the dim / wind-down screen. Cover them too;
        // the content is centred, so nothing ends up under the system icons.
        if (Build.VERSION.SDK_INT >= 30) {
            fitInsetsTypes = 0
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }

    // ── Phase 1: banner ──────────────────────────────────────────────────────

    private fun buildBanner(remainingMs: Long, mode: IntensityMode): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = rounded(Color.parseColor("#F01E1B4B"), dp(24))
            elevation = dp(6).toFloat()
        }
        row.addView(text("🌙", 16f))
        row.addView(text("  Sleep mode starting soon — ", 14f, Color.parseColor("#E0E7FF")))
        val timer = text(SleepLogic.formatRemaining(remainingMs), 14f, Color.WHITE, bold = true)
        timerView = timer
        timerPrefix = ""
        row.addView(timer)
        if (mode != IntensityMode.STRICT) {
            row.addView(text("   ✕", 16f, Color.parseColor("#A5B4FC")).apply {
                setPadding(dp(8), dp(4), dp(4), dp(4))
                setOnClickListener { bannerDismissed = true; removeView() }
            })
        }
        return row
    }

    // ── Phase 2: dim + snooze ────────────────────────────────────────────────

    private fun buildDim(remainingMs: Long, mode: IntensityMode): View {
        val frame = FrameLayout(context).apply { setBackgroundColor(Color.parseColor("#F00B0B1E")) }
        val card = column()
        card.addView(text("🌙", 48f).center())
        card.addView(text("Time to wind down", 24f, Color.WHITE, bold = true).center().margin(top = 12))
        timerPrefix = "Closing in "
        val timer = text(timerPrefix + SleepLogic.formatRemaining(remainingMs), 16f, Color.parseColor("#C7D2FE"))
        timerView = timer
        card.addView(timer.center().margin(top = 8))
        if (mode != IntensityMode.STRICT) {
            card.addView(button("Snooze ${SleepLogic.SNOOZE_MINUTES} min", filled = false) {
                SleepController.snooze(context)
            }.margin(top = 28))
        }
        card.addView(button("Leave this app", filled = true) { goHome() }.margin(top = 12))
        frame.addView(card, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        return frame
    }

    // ── Phase 3: full wind-down ──────────────────────────────────────────────

    private fun buildWindDown(remainingMs: Long, style: WindDownStyle): View {
        val frame = FrameLayout(context).apply { setBackgroundColor(Color.parseColor("#FF060612")) }
        val card = column()
        card.addView(text(style.emoji, 64f).center())
        card.addView(text(if (style == WindDownStyle.SILENCE) "Time to sleep." else "Time to sleep",
            28f, Color.WHITE, bold = true).center().margin(top = 12))
        timerPrefix = "Closing in "
        val timer = text(timerPrefix + SleepLogic.formatRemaining(remainingMs), 16f, Color.parseColor("#C7D2FE"))
        timerView = timer
        card.addView(timer.center().margin(top = 8))

        when (style) {
            WindDownStyle.BREATHING -> addBreathing(card)
            WindDownStyle.REFLECTION -> addReflection(card)
            WindDownStyle.JOURNALING -> {
                card.addView(text("TONIGHT'S QUESTION", 12f, Color.parseColor("#818CF8"), bold = true).center().margin(top = 32))
                card.addView(prompt(WindDownContent.journalPrompt(LocalDate.now().dayOfWeek.value)).margin(top = 8))
            }
            WindDownStyle.SOUNDS ->
                card.addView(prompt("Put on some calming music or nature sounds, then close your eyes.").margin(top = 32))
            WindDownStyle.SILENCE -> Unit
        }
        card.addView(button("Go to home screen", filled = true) { goHome() }.margin(top = 40))
        frame.addView(card, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        return frame
    }

    private fun addBreathing(card: LinearLayout) {
        val ring = View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1A818CF8"))
                setStroke(dp(3), Color.parseColor("#A5B4FC"))
            }
            scaleX = 0.8f; scaleY = 0.8f
        }
        card.addView(ring, LinearLayout.LayoutParams(dp(140), dp(140)).apply {
            gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(40)
        })
        val label = text(WindDownContent.BREATH_STEPS[0], 16f, Color.parseColor("#C7D2FE")).center().margin(top = 28)
        card.addView(label)

        var step = 0
        val tick = object : Runnable {
            override fun run() {
                label.text = WindDownContent.BREATH_STEPS[step]
                val target = when (step) { 0 -> 1.25f; 2 -> 0.8f; else -> ring.scaleX }
                ring.animate().scaleX(target).scaleY(target).setDuration(2000).start()
                step = (step + 1) % WindDownContent.BREATH_STEPS.size
                handler.postDelayed(this, 2000)
            }
        }
        cycles += tick
        handler.post(tick)
    }

    private fun addReflection(card: LinearLayout) {
        val p = prompt(WindDownContent.REFLECTION_PROMPTS[0])
        card.addView(p.margin(top = 32))
        var i = 0
        val tick = object : Runnable {
            override fun run() {
                i = (i + 1) % WindDownContent.REFLECTION_PROMPTS.size
                p.animate().alpha(0f).setDuration(400).withEndAction {
                    p.text = WindDownContent.REFLECTION_PROMPTS[i]
                    p.animate().alpha(1f).setDuration(400).start()
                }.start()
                handler.postDelayed(this, 7000)
            }
        }
        cycles += tick
        handler.postDelayed(tick, 7000)
    }

    // ── View helpers ─────────────────────────────────────────────────────────

    private fun goHome() {
        context.startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun column() = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(32), dp(32), dp(32), dp(32))
    }

    private fun text(s: String, sp: Float, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(context).apply {
            text = s
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

    private fun prompt(s: String) = text(s, 20f, Color.parseColor("#E0E7FF")).apply {
        setLineSpacing(0f, 1.2f)
    }.center()

    private fun button(label: String, filled: Boolean, onClick: () -> Unit) =
        TextView(context).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(if (filled) Color.parseColor("#1E1B4B") else Color.parseColor("#E0E7FF"))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(14), dp(28), dp(14))
            background = if (filled) rounded(Color.parseColor("#C7D2FE"), dp(28))
            else GradientDrawable().apply {
                cornerRadius = dp(28).toFloat()
                setColor(Color.parseColor("#1E1B4B")) // solid, so the app underneath can't show through the label
                setStroke(dp(1), Color.parseColor("#6366F1"))
            }
            setOnClickListener { onClick() }
        }

    private fun <T : View> T.center(): T {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { gravity = Gravity.CENTER_HORIZONTAL }
        return this
    }

    private fun <T : View> T.margin(top: Int): T {
        val lp = (layoutParams as? LinearLayout.LayoutParams)
            ?: LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { gravity = Gravity.CENTER_HORIZONTAL }
        lp.topMargin = dp(top)
        layoutParams = lp
        return this
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = radius.toFloat()
    }

    private fun dp(v: Int) = (v * context.resources.displayMetrics.density).toInt()
}

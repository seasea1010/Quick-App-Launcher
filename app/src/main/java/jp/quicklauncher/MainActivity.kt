package jp.quicklauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.content.pm.ResolveInfo
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {
    private val apps = mutableListOf<ResolveInfo>()
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private val bg = Color.rgb(248, 249, 251)
    private val ink = Color.rgb(28, 31, 36)
    private val muted = Color.rgb(115, 121, 130)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        buildScreen()
        loadApps()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(20), dp(22), dp(20), dp(12))
        }

        val title = TextView(this).apply {
            text = "アプリ"
            textSize = 27f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "インストール済みアプリ"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(18))
        }
        root.addView(subtitle)

        search = EditText(this).apply {
            hint = "アプリを検索"
            textSize = 15f
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setTextColor(ink)
            setHintTextColor(muted)
            background = rounded(Color.WHITE, 14)
            setPadding(dp(16), 0, dp(16), 0)
            elevation = dp(1).toFloat()
        }
        root.addView(search, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
        ))

        val heading = TextView(this).apply {
            text = "アプリ一覧"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(muted)
            setPadding(dp(2), dp(20), 0, dp(10))
        }
        root.addView(heading)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setContentView(root)
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null)
            .addCategory(Intent.CATEGORY_LAUNCHER)
        val found = packageManager.queryIntentActivities(intent, 0)
        apps.clear()
        apps.addAll(found.sortedBy {
            it.loadLabel(packageManager).toString().lowercase()
        })
        renderApps("")
    }

    private fun renderApps(query: String) {
        list.removeAllViews()
        val filtered = apps.filter {
            it.loadLabel(packageManager).toString()
                .contains(query.trim(), ignoreCase = true)
        }

        if (filtered.isEmpty()) {
            val empty = TextView(this).apply {
                text = if (apps.isEmpty()) "アプリを取得できませんでした"
                    else "該当するアプリがありません"
                textSize = 14f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(0, dp(36), 0, dp(36))
            }
            list.addView(empty)
            return
        }

        filtered.forEach { info ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(Color.WHITE, 14)
                setPadding(dp(14), dp(10), dp(14), dp(10))
                isClickable = true
                isFocusable = true
                foreground = selectableItemBackground()
                setOnClickListener { launchApp(info) }
            }

            val icon = ImageView(this).apply {
                setImageDrawable(info.loadIcon(packageManager))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)))

            val label = TextView(this).apply {
                text = info.loadLabel(packageManager)
                textSize = 15f
                setTextColor(ink)
                maxLines = 1
                setPadding(dp(14), 0, 0, 0)
            }
            row.addView(label, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ))

            val arrow = TextView(this).apply {
                text = "›"
                textSize = 25f
                setTextColor(muted)
            }
            row.addView(arrow)

            list.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(62)
            ).apply { bottomMargin = dp(8) })
        }
    }

    private fun selectableItemBackground(): android.graphics.drawable.Drawable? {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typed = obtainStyledAttributes(attrs)
        val drawable = typed.getDrawable(0)
        typed.recycle()
        return drawable
    }

    private fun launchApp(info: ResolveInfo) {
        val packageName = info.activityInfo.packageName
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "アプリを起動できませんでした", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "このアプリは起動できません", Toast.LENGTH_SHORT).show()
        }
    }
}

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.annotation.SuppressLint
import android.content.Context
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Main
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.fragments.SettingsPage
import com.aliucord.patcher.Hook
import com.aliucord.utils.DimenUtils
import com.aliucord.views.TextInput
import com.discord.utilities.time.Clock
import com.discord.utilities.time.TimeUtils
import com.lytefast.flexinput.R
import java.text.SimpleDateFormat
import java.util.*

@AliucordPlugin
@Suppress("unused")
@SuppressLint("SimpleDateFormat")
class CustomTimestamps : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
        override fun onViewBound(view: View) {
            super.onViewBound(view)

            setActionBarTitle("CustomTimestamps")
            setPadding(0)

            val context = view.context
            val format = settings.getString("format", defaultFormat)

            val guide = TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                setPreview(format, this)
                movementMethod = LinkMovementMethod.getInstance()
            }
            linearLayout.addView(TextInput(context, "Custom Timestamp Format", format, object : TextWatcher {
                override fun afterTextChanged(s: Editable) = s.toString().let {
                    settings.setString("format", it)
                    setPreview(it, guide)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            }).apply {
                val padding = DimenUtils.defaultPadding
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { setMargins(padding, padding, padding, 0) }
            })
            linearLayout.addView(guide)
        }

        private fun setPreview(formatStr: String, view: TextView) {
            view.text = SpannableStringBuilder("Formatting guide\n\nPreview: ").apply {
                append(format(formatStr, System.currentTimeMillis()))
                setSpan(URLSpan("https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    override fun start(context: Context?) {
        patcher.patch(
            TimeUtils::class.java.getDeclaredMethod("toReadableTimeString", Context::class.java, Long::class.javaPrimitiveType, Clock::class.java),
            Hook { it.result = format(settings.getString("format", defaultFormat), it.args[1] as Long) }
        )
    }

    override fun stop(context: Context?) = patcher.unpatchAll()

    companion object {
        const val defaultFormat = "dd.MM.yyyy, HH:mm:ss"

        fun format(format: String?, time: Long): String {
            return try {
                SimpleDateFormat(format).format(Date(time))
            } catch (e: Throwable) {
                Main.logger.info("Invalid format for CustomTimestamps, using default format")
                SimpleDateFormat(defaultFormat).format(Date(time))
            }
        }
    }
}

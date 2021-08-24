/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins

import android.annotation.SuppressLint
import android.content.Context
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Main
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.fragments.SettingsPage
import com.aliucord.patcher.PinePatchFn
import com.aliucord.utils.DimenUtils
import com.aliucord.views.TextInput
import com.discord.utilities.time.Clock
import com.discord.utilities.time.TimeUtils
import com.lytefast.flexinput.R
import java.text.SimpleDateFormat
import java.util.*

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

      val guide = TextView(context, null, 0, R.h.UiKit_Settings_Item_SubText).apply {
        setPreview(format, this)
        movementMethod = LinkMovementMethod.getInstance()
      }
      linearLayout.addView(TextInput(context).apply {
        val padding = DimenUtils.getDefaultPadding()
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
          .apply { setMargins(padding, padding, padding, 0) }
        hint = "Custom Timestamp Format"
        editText.apply editText@ {
          if (this == null) return@editText
          maxLines = 1
          setText(format)
          addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) = s.toString().let {
              settings.setString("format", it)
              setPreview(it, guide)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
          })
        }
      })
      linearLayout.addView(guide)
    }

    fun setPreview(formatStr: String, view: TextView) {
      view.text = SpannableStringBuilder("Formatting guide\n\nPreview: ").apply {
        append(format(formatStr, System.currentTimeMillis()))
        setSpan(URLSpan("https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }

  override fun getManifest() = Manifest().apply {
    authors = arrayOf(Manifest.Author("Juby210", 324622488644616195L))
    description = "Custom timestamps format everywhere."
    version = "1.0.4"
    updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json"
  }

  override fun start(context: Context?) {
    patcher.patch(
      TimeUtils::class.java.getDeclaredMethod("toReadableTimeString", Context::class.java, Long::class.javaPrimitiveType, Clock::class.java),
      PinePatchFn { it.result = format(settings.getString("format", defaultFormat), it.args[1] as Long) }
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

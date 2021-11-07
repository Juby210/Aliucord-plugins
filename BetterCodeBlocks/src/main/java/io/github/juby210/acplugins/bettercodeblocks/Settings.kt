/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bettercodeblocks

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import io.github.juby210.acplugins.BetterCodeBlocks

class Settings(private val settings: SettingsAPI) : SettingsPage() {
    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("BetterCodeBlocks")
        setPadding(0)

        val context = view.context
        val layout = linearLayout
        layout.addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
            text = "Use Prism4j for:"
        })

        layout.addView(com.aliucord.Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Not supported languages", null).apply {
            val key = "notSupported"
            isChecked = settings.getBool(key, false)
            setOnCheckedListener { settings.setBool(key, it) }
        })

        for (entry in SWITCHES) {
            val lang = entry.key
            layout.addView(com.aliucord.Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, lang, null).apply {
                isChecked = settings.getBool(lang, entry.value)
                setOnCheckedListener { settings.setBool(lang, it) }
            })
        }
    }

    companion object {
        private val BLACKLIST = listOf("protobuf", "proto", "pb", "rs", "rust", "cql", "cr", "crystal")
        val SWITCHES = mapOf("js" to true, "json" to true, "go" to false)

        fun get(settings: SettingsAPI, lang: String?): Boolean {
            val notSupported = settings.getBool("notSupported", false)
            if (lang == null) return notSupported
            if (BLACKLIST.contains(lang)) return false
            val realLang = GrammarLocatorImpl.realLanguageName(lang)
            return settings.getBool(realLang, SWITCHES.getOrDefault(realLang, true)) &&
                (notSupported || BetterCodeBlocks.grammarLocator.grammar(BetterCodeBlocks.prism4j, lang) != null)
        }
    }
}

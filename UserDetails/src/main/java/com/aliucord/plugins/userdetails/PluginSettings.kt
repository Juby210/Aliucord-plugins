/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.userdetails

import android.content.Context
import android.os.Bundle
import android.view.*
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.widgets.LinearLayout
import com.discord.app.AppBottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

class PluginSettings(private val settings: SettingsAPI) : AppBottomSheet() {
    override fun getContentViewResId() = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        val context = inflater.context
        val layout = LinearLayout(context)
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

        layout.addView(createSwitch(context, "createdAt", "Display \"Created at\""))
        layout.addView(createSwitch(context, "joinedAt", "Display \"Joined at\""))
        layout.addView(createSwitch(context, "lastMessage", "Display \"Last message\""))
        return layout
    }

    private fun createSwitch(context: Context, key: String, label: String): CheckedSetting {
        val cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, null)
        cs.isChecked = settings.getBool(key, true)
        cs.setOnCheckedListener { settings.setBool(key, it) }
        return cs
    }
}

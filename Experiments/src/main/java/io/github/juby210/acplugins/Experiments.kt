/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import android.os.Bundle
import android.view.*
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.aliucord.patcher.PinePrePatchFn
import com.aliucord.widgets.LinearLayout
import com.discord.app.AppBottomSheet
import com.discord.databinding.WidgetSettingsBinding
import com.discord.stores.`StoreExperiments$getExperimentalAlpha$1`
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.discord.widgets.settings.WidgetSettings
import com.discord.widgets.settings.developer.ExperimentOverridesAdapter
import com.lytefast.flexinput.R
import top.canyie.pine.callback.MethodReplacement

@AliucordPlugin
@Suppress("UNCHECKED_CAST", "unused")
class Experiments : Plugin() {
    class Settings(private val plugin: Experiments) : AppBottomSheet() {
        override fun getContentViewResId() = 0

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
            val context = inflater.context
            val layout = LinearLayout(context)
            layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary))

            layout.addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Sort by date", null).apply {
                val key = "sort"
                isChecked = plugin.settings.getBool(key, true)
                setOnCheckedListener {
                    plugin.settings.setBool(key, it)
                    if (it) plugin.sortExperiments()
                    else {
                        plugin.sortUnpatch?.run()
                        plugin.sortUnpatch = null
                    }
                }
            })
            return layout
        }
    }

    init {
        settingsTab = SettingsTab(Settings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(this)
    }

    override fun start(context: Context?) {
        patcher.patch(`StoreExperiments$getExperimentalAlpha$1`::class.java.getDeclaredMethod("invoke"), MethodReplacement.returnConstant(true))

        val c = WidgetSettings::class.java
        val getBinding = c.getDeclaredMethod("getBinding").apply { isAccessible = true }
        patcher.patch(c.getDeclaredMethod("configureUI", WidgetSettings.Model::class.java), PinePatchFn {
            val binding = getBinding.invoke(it.thisObject) as WidgetSettingsBinding
            binding.n.visibility = View.VISIBLE
            binding.o.visibility = View.VISIBLE
            binding.m.visibility = View.VISIBLE
        })

        if (settings.getBool("sort", true)) sortExperiments()
    }

    override fun stop(context: Context?) {
        patcher.unpatchAll()
        sortUnpatch = null
    }

    var sortUnpatch: Runnable? = null
    fun sortExperiments() {
        sortUnpatch = patcher.patch(ExperimentOverridesAdapter::class.java.getDeclaredMethod("setData", List::class.java), PinePrePatchFn {
            val list = it.args[0] as MutableList<ExperimentOverridesAdapter.Item>
            list.sortWith { a, b -> b.apiName.compareTo(a.apiName) }
        })
    }
}

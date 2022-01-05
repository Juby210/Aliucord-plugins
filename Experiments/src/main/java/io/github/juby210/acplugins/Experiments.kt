/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

@file:Suppress("UNCHECKED_CAST")

package io.github.juby210.acplugins

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils
import com.aliucord.views.TextInput
import com.discord.app.AppBottomSheet
import com.discord.databinding.WidgetSettingsBinding
import com.discord.stores.`StoreExperiments$getExperimentalAlpha$1`
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.discord.widgets.settings.WidgetSettings
import com.discord.widgets.settings.developer.*
import com.lytefast.flexinput.R
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

inline val WidgetSettingsDeveloper.adapter: ExperimentOverridesAdapter
    get() = WidgetSettingsDeveloper.`access$getExperimentOverridesAdapter$p`(this)

val itemsField: Field = ExperimentOverridesAdapter::class.java.getDeclaredField("items").apply { isAccessible = true }
inline var ExperimentOverridesAdapter.items
    get() = itemsField[this] as List<ExperimentOverridesAdapter.Item>
    set(v) { itemsField[this] = v }

@AliucordPlugin
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

    val dataMap = WeakHashMap<WidgetSettingsDeveloper, MutableList<ExperimentOverridesAdapter.Item>>()
    private val editTextId = View.generateViewId()

    override fun start(context: Context?) {
        patcher.patch(`StoreExperiments$getExperimentalAlpha$1`::class.java.getDeclaredMethod("invoke"), InsteadHook.returnConstant(true))

        val c = WidgetSettings::class.java
        val getBinding = c.getDeclaredMethod("getBinding").apply { isAccessible = true }
        patcher.patch(c.getDeclaredMethod("configureUI", WidgetSettings.Model::class.java), Hook {
            with(getBinding.invoke(it.thisObject) as WidgetSettingsBinding) {
                n.visibility = View.VISIBLE
                o.visibility = View.VISIBLE
                m.visibility = View.VISIBLE
            }
        })

        patcher.patch(WidgetSettingsDeveloper::class.java, "onViewBound", arrayOf(View::class.java), Hook {
            with(((it.args[0] as ViewGroup).getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup) {
                val ctx = getContext()
                addView(TextInput(ctx, ctx.getString(R.h.search)).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        (DimenUtils.defaultPadding / 2).let { p -> setMargins(p, p, p, p) }
                    }
                    editText.apply {
                        maxLines = 1
                        id = editTextId
                    }
                }, 3)
            }
        })

        patcher.after<`WidgetSettingsDeveloper$setupExperimentSection$2`>("invoke", List::class.java) {
            `this$0`.apply {
                val a = adapter
                dataMap[this] = ArrayList(a.items)
                view!!.findViewById<EditText>(editTextId).addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        if (dataMap.containsKey(this@apply)) {
                            val old = a.items
                            val new = if (s == null || s.equals("")) dataMap[this@apply]!!
                            else {
                                val search = s.toString().lowercase().trim()
                                dataMap[this@apply]!!.filter { i -> i.apiName.contains(search) || i.name.contains(search) }
                            }
                            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                                override fun getOldListSize() = old.size
                                override fun getNewListSize() = new.size
                                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true

                                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                                    old[oldItemPosition].apiName == new[newItemPosition].apiName
                            })
                            a.items = new
                            diff.dispatchUpdatesTo(a)
                        }
                    }
                })
            }
        }

        if (settings.getBool("sort", true)) sortExperiments()
    }

    override fun stop(context: Context?) {
        patcher.unpatchAll()
        sortUnpatch = null
    }

    var sortUnpatch: Runnable? = null
    fun sortExperiments() {
        sortUnpatch = patcher.patch(ExperimentOverridesAdapter::class.java.getDeclaredMethod("setData", List::class.java), PreHook {
            val list = it.args[0] as MutableList<ExperimentOverridesAdapter.Item>
            list.sortWith { a, b -> b.apiName.compareTo(a.apiName) }
        })
    }
}

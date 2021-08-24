/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins

import android.content.Context
import android.view.View
import com.aliucord.entities.Plugin
import com.aliucord.entities.Plugin.Manifest.Author
import com.aliucord.patcher.PinePatchFn
import com.discord.databinding.WidgetSettingsBinding
import com.discord.stores.`StoreExperiments$getExperimentalAlpha$1`
import com.discord.widgets.settings.WidgetSettings
import top.canyie.pine.callback.MethodReplacement

@Suppress("unused")
class Experiments : Plugin() {
  override fun getManifest() = Manifest().apply {
    authors = arrayOf(Author("Juby210", 324622488644616195L))
    description = "Shows hidden Developer Options tab with Experiments."
    version = "1.0.3"
    updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json"
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
  }

  override fun stop(context: Context?) = patcher.unpatchAll()
}

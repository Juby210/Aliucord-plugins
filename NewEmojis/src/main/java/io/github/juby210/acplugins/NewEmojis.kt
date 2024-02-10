/*
 * Copyright (c) 2021-2023 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import android.os.Bundle
import android.view.View
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.widgets.BottomSheet
import com.discord.models.domain.Model
import com.discord.models.domain.emoji.*
import com.discord.stores.*
import com.discord.views.CheckedSetting
import de.robv.android.xposed.XC_MethodHook
import java.io.InputStreamReader

@AliucordPlugin
@Suppress("UNCHECKED_CAST")
class NewEmojis : Plugin() {
    class Settings(private val settings: SettingsAPI) : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            val context = requireContext()
            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Use new pleading", null).apply {
                val key = "newPleading"
                isChecked = settings.getBool(key, true)
                setOnCheckedListener { settings.setBool(key, it) }
            })
            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Use new face_holding_back_tears", null).apply {
                val key = "newFaceHoldingBackTears"
                isChecked = settings.getBool(key, true)
                setOnCheckedListener { settings.setBool(key, it) }
            })
        }
    }

    init {
        needsResources = true
        settingsTab = SettingsTab(Settings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    override fun start(ctx: Context?) {
        val model = Model.JsonReader(
            InputStreamReader(
                resources.openRawResource(resources.getIdentifier("emojis", "raw", "io.github.juby210.acplugins"))
            )
        ).parse(ModelEmojiUnicode.Bundle())

        val c = StoreEmoji::class.java
        val unicodeEmojisF = c.getDeclaredField("unicodeEmojis").apply { isAccessible = true }
        val unicodeEmojisNamesMap = c.getDeclaredField("unicodeEmojisNamesMap").apply { isAccessible = true }
        val unicodeEmojiSurrogateMap = c.getDeclaredField("unicodeEmojiSurrogateMap").apply { isAccessible = true }
        val unicodeEmojisPattern = c.getDeclaredField("unicodeEmojisPattern").apply { isAccessible = true }
        val compileSurrogatesPattern = c.getDeclaredMethod("compileSurrogatesPattern").apply { isAccessible = true }

        val storeEmoji = StoreStream.getEmojis()
        val unicodeEmojis = unicodeEmojisF[storeEmoji] as MutableMap<EmojiCategory, MutableList<Emoji>>
        val handleLoadedUnicodeEmojis = `StoreEmoji$handleLoadedUnicodeEmojis$1`(
            unicodeEmojiSurrogateMap[storeEmoji] as HashMap<*, *>,
            unicodeEmojisNamesMap[storeEmoji] as HashMap<*, *>
        )

        // remove old handshake emote emoji without diversities
        unicodeEmojis[EmojiCategory.PEOPLE]!!.run { removeAt(indexOfFirst { it.firstName == "handshake" }) }

        val pluginCodePoints = ArrayList<String>()
        model.emojis.forEach { (category, emojis) ->
            unicodeEmojis[category]!!.addAll(emojis)
            emojis.forEach { emoji ->
                pluginCodePoints.add(emoji.codePoints)
                handleLoadedUnicodeEmojis(emoji)
                emoji.asDiverse.forEach {
                    pluginCodePoints.add(it.codePoints)
                    handleLoadedUnicodeEmojis(it)
                }
            }
        }
        unicodeEmojisPattern[storeEmoji] = compileSurrogatesPattern.invoke(storeEmoji)

        patcher.patch(
            ModelEmojiUnicode::class.java.getDeclaredMethod("getImageUri", String::class.java, Context::class.java),
            object : XC_MethodHook(51) {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val code = param.args[0] as String
                    if (code == "1f97a") {
                        if (settings.getBool("newPleading", true)) param.result = getUrl(code, true)
                    } else if (pluginCodePoints.contains(code)) {
                        param.result = getUrl(
                            code.replace("_", "-"),
                            code != "1f979" || settings.getBool("newFaceHoldingBackTears", true)
                        )
                    }
                }

                fun getUrl(code: String, new: Boolean) =
                    "https://jdecked.github.io/twemoji/v/${if (new) "15.0.3" else "14.1.1"}/72x72/$code.png"
            }
        )
    }

    override fun stop(ctx: Context) {
        StoreStream.getEmojis().initBlocking(ctx)
        patcher.unpatchAll()
    }
}

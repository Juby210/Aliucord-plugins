/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.models.domain.Model
import com.discord.models.domain.emoji.*
import com.discord.stores.*
import de.robv.android.xposed.XC_MethodHook
import java.io.InputStreamReader

@AliucordPlugin
@Suppress("UNCHECKED_CAST")
class NewEmojis : Plugin() {
    init {
        needsResources = true
    }

    override fun start(ctx: Context?) {
        val model = Model.JsonReader(InputStreamReader(
            resources.openRawResource(resources.getIdentifier("emojis", "raw", "io.github.juby210.acplugins"))
        )).parse(ModelEmojiUnicode.Bundle())

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
                    if (pluginCodePoints.contains(code))
                        param.result = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/${code.replace("_", "-")}.png"
                }
            }
        )
    }

    override fun stop(ctx: Context) {
        StoreStream.getEmojis().initBlocking(ctx)
        patcher.unpatchAll()
    }
}

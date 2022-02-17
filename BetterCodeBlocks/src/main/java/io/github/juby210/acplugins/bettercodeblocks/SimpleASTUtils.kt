/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bettercodeblocks

import android.text.style.ForegroundColorSpan
import com.discord.simpleast.core.node.StyleNode
import com.discord.utilities.textprocessing.*
import java.util.regex.Pattern

object SimpleASTUtils {
    private fun Pattern.toMatchGroupRule(stylesProvider: StyleNode.a<*>) = b.a.t.a.d(this, 0, stylesProvider, this)

    @JvmStatic
    fun addLanguages(langMap: MutableMap<String, Any?>) {
        val createGenericCodeRules = Utils.getCreateGenericCodeRules().apply { isAccessible = true }
        val codeRulesUtils = b.a.t.a.e.f

        val commentStyleProvider = StyleNode.a<Any> { listOf(ForegroundColorSpan((0xFF808080).toInt())) }
        val literalStyleProvider = `Rules$createCodeBlockRule$codeStyleProviders$3`.INSTANCE
        val keywordStyleProvider = `Rules$createCodeBlockRule$codeStyleProviders$4`.INSTANCE
        val identifierStyleProvider = `Rules$createCodeBlockRule$codeStyleProviders$5`.INSTANCE
        val punctuationStyleProvider = StyleNode.a<Any> { listOf(ForegroundColorSpan((0XFF969696).toInt())) }
        val codeStyleProviders = b.a.t.a.f(
            `Rules$createCodeBlockRule$codeStyleProviders$1`.INSTANCE,
            commentStyleProvider,
            literalStyleProvider,
            keywordStyleProvider,
            identifierStyleProvider,
            `Rules$createCodeBlockRule$codeStyleProviders$6`.INSTANCE,
            `Rules$createCodeBlockRule$codeStyleProviders$7`.INSTANCE,
            `Rules$createCodeBlockRule$codeStyleProviders$8`.INSTANCE,
        )

        langMap["go"] = createGenericCodeRules.invoke(
            codeRulesUtils,
            codeStyleProviders,
            listOf(
                Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)
                    .toMatchGroupRule(stylesProvider = commentStyleProvider),
                Pattern.compile("""^(".*?(?<!\\)"|`[\s\S]*?(?<!\\)`)(?=\W|\s|$)""")
                    .toMatchGroupRule(stylesProvider = literalStyleProvider),
                Pattern.compile("""^func""")
                    .toMatchGroupRule(stylesProvider = keywordStyleProvider),
                Pattern.compile("""^[a-z0-9_]+(?=\()""", Pattern.CASE_INSENSITIVE)
                    .toMatchGroupRule(stylesProvider = identifierStyleProvider),
                Pattern.compile("""^[{}\[\];(),.:]""")
                    .toMatchGroupRule(stylesProvider = punctuationStyleProvider),
            ),
            arrayOf("package", "type", "var|const"),
            arrayOf(
                "nil|iota|true|false|bool",
                "byte|complex(?:64|128)|error|float(?:32|64)|rune|string|u?int(?:8|16|32|64)?|uintptr",
            ),
            arrayOf(
                "break|case|chan|continue|default|defer|else|fallthrough|for|go(?:to)?|if|import|interface|map|range|return|select|switch|struct",
                "type|var|const",
            ),
            arrayOf(" ")
        )

        langMap["js"].apply {
            langMap["json"] = this
            langMap["jsonp"] = this
        }
    }
}

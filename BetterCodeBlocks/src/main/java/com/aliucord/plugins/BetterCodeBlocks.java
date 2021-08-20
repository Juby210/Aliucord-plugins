/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.aliucord.plugins.bettercodeblocks.*;
import com.aliucord.utils.MDUtils;
import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.parser.ParseSpec;
import com.discord.simpleast.core.parser.Parser;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;

import java.util.regex.Matcher;

import io.noties.markwon.syntax.Prism4jSyntaxHighlight;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.prism4j.Prism4j;

@SuppressWarnings({ "unchecked", "unused" })
public class BetterCodeBlocks extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Makes codeblocks better by adding new languages, improving already existing and more.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) throws Throwable {
        highlight = Prism4jSyntaxHighlight.create(new Prism4j(new GrammarLocatorSourceCode()), Prism4jThemeDarkula.create());

        patcher.patch(c.a.t.a.a.class, "parse", new Class<?>[]{ Matcher.class, Parser.class, Object.class }, new PinePrePatchFn(callFrame -> {
            var matcher = (Matcher) callFrame.args[0];
            if (matcher == null) return;
            callFrame.setResult(new ParseSpec<>(
                renderCodeBlock(matcher.group(1), matcher.group(3)), callFrame.args[2]
            ));
        }));

        patcher.patch(MDUtils.class.getDeclaredMethod("renderCodeBlock", Context.class, SpannableStringBuilder.class, String.class, String.class),
            new PinePrePatchFn(callFrame -> {
                var builder = (SpannableStringBuilder) callFrame.args[1];
                int a = builder.length();
                var lang = (String) callFrame.args[2];
                var rendered = render(lang, (String) callFrame.args[3]);
                var ctx = (Context) callFrame.args[0];
                wrapInNodes(lang, rendered).render(builder, new MDUtils.RenderContext(ctx));
                if (rendered instanceof String) builder.setSpan(
                    new ForegroundColorSpan(ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorHeaderSecondary)),
                    a,
                    builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                callFrame.setResult(builder);
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private Prism4jSyntaxHighlight highlight;

    private CharSequence render(String lang, String content) {
        return highlight.highlight(lang, content == null ? "" : content);
    }

    private Node<BasicRenderContext> wrapInNodes(String lang, CharSequence content) {
        return new LangNode<>(lang, new BlockBackgroundNode<>(false, new BCBNode<>(content)));
    }

    private Node<BasicRenderContext> renderCodeBlock(String lang, String content) {
        return wrapInNodes(lang, render(lang, content));
    }
}

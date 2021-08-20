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
import com.aliucord.plugins.bettercodeblocks.*;
import com.aliucord.utils.MDUtils;
import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.parser.ParseSpec;
import com.discord.simpleast.core.parser.Parser;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import io.noties.markwon.syntax.Prism4jSyntaxHighlight;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.prism4j.Prism4j;
import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

@SuppressWarnings({ "unchecked", "unused" })
public class BetterCodeBlocks extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Makes codeblocks better by adding new languages, improving already existing and more.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) throws Throwable {
        highlight = Prism4jSyntaxHighlight.create(new Prism4j(new GrammarLocatorSourceCode()), Prism4jThemeDarkula.create());

        var specNode = ParseSpec.class.getDeclaredField("a");
        specNode.setAccessible(true);
        patcher.patch(c.a.t.a.a.class, "parse", new Class<?>[]{ Matcher.class, Parser.class, Object.class }, new MethodHook() {
            private boolean blacklisted;
            private String l;

            @Override
            public void beforeCall(Pine.CallFrame callFrame) {
                var matcher = (Matcher) callFrame.args[0];
                if (matcher == null) return;
                var lang = (String) matcher.group(1);
                if (lang != null && blacklist.contains(lang)) {
                    blacklisted = true;
                    l = lang;
                } else {
                    blacklisted = false;
                    callFrame.setResult(new ParseSpec<>(
                        renderCodeBlock(lang, matcher.group(3)), callFrame.args[2]
                    ));
                }
            }

            @Override
            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                if (blacklisted && l != null) {
                    var spec = (ParseSpec<?, ?>) callFrame.getResult();
                    specNode.set(spec, new LangNode<>(l, spec.a));
                }
            }
        });

        patcher.patch(MDUtils.class.getDeclaredMethod("renderCodeBlock", Context.class, SpannableStringBuilder.class, String.class, String.class), new MethodHook() {
            private boolean blacklisted;
            private int a;
            private String lang;

            @Override
            public void beforeCall(Pine.CallFrame callFrame) {
                var builder = (SpannableStringBuilder) callFrame.args[1];
                a = builder.length();

                lang = (String) callFrame.args[2];
                if (lang != null && blacklist.contains(lang)) {
                    blacklisted = true;
                    return;
                }
                blacklisted = false;

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
            }

            @Override
            public void afterCall(Pine.CallFrame callFrame) {
                if (blacklisted && lang != null) {
                    var tmp = new SpannableStringBuilder();
                    new LangNode<>(lang).render(tmp, new MDUtils.RenderContext((Context) callFrame.args[0]));
                    var builder = (SpannableStringBuilder) callFrame.getResult();
                    builder.insert(a, tmp);
                }
            }
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private final List<String> blacklist = Arrays.asList("protobuf", "proto", "pb", "rs", "rust", "cql", "cr", "crystal");
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

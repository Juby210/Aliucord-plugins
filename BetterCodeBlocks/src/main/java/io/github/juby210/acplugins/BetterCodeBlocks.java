/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.MDUtils;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.parser.ParseSpec;
import com.discord.simpleast.core.parser.Parser;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import io.github.juby210.acplugins.bettercodeblocks.*;
import io.noties.markwon.syntax.Prism4jSyntaxHighlight;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.prism4j.Prism4j;

@AliucordPlugin
@SuppressWarnings({ "unchecked", "unused" })
public final class BetterCodeBlocks extends Plugin {
    @Override
    public void start(Context context) throws Throwable {
        highlight = Prism4jSyntaxHighlight.create(new Prism4j(new GrammarLocatorImpl()), new Prism4jThemeDarkula());

        patcher.patch(c.a.t.a.a.class, "parse", new Class<?>[]{ Matcher.class, Parser.class, Object.class }, new PreHook(param -> {
            var matcher = (Matcher) param.args[0];
            if (matcher == null) return;
            var lang = (String) matcher.group(1);
            if (lang != null && blacklist.contains(lang)) return;
            param.setResult(new ParseSpec<>(
                renderCodeBlock(lang, matcher.group(3)), param.args[2]
            ));
        }));

        patcher.patch(BlockBackgroundNode.class.getDeclaredConstructor(boolean.class, Node[].class), new PreHook(param -> {
            var nodes = (Node<BasicRenderContext>[]) param.args[1];
            if (nodes.length == 1 && nodes[0] instanceof CodeNode) {
                nodes[0] = new LangNode<>(((CodeNode<BasicRenderContext>) nodes[0]).a, nodes[0]);
                param.args[1] = nodes;
            }
        }));

        patcher.patch(MDUtils.class.getDeclaredMethod("renderCodeBlock", Context.class, SpannableStringBuilder.class, String.class, String.class),
            new PreHook(param -> {
                var lang = (String) param.args[2];
                if (lang != null && blacklist.contains(lang)) return;

                var builder = (SpannableStringBuilder) param.args[1];
                int a = builder.length();
                var rendered = render(lang, (String) param.args[3]);
                var ctx = (Context) param.args[0];
                wrapInNodes(lang, rendered).render(builder, new MDUtils.RenderContext(ctx));
                if (rendered instanceof String) Utils.fixColor(builder, ctx, a);
                param.setResult(builder);
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    public final List<String> blacklist = Arrays.asList("protobuf", "proto", "pb", "rs", "rust", "cql", "cr", "crystal");
    private Prism4jSyntaxHighlight highlight;

    public CharSequence render(String lang, String content) {
        return highlight.highlight(lang, content == null ? "" : content);
    }

    public Node<BasicRenderContext> wrapInNodes(String lang, CharSequence content) {
        return new BlockBackgroundNode<>(false, new BCBNode<>(lang, content));
    }

    public Node<BasicRenderContext> renderCodeBlock(String lang, String content) {
        return wrapInNodes(lang, render(lang, content));
    }
}

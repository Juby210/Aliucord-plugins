/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bettercodeblocks;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

import com.discord.simpleast.core.node.Node;
import com.discord.utilities.textprocessing.node.BasicRenderContext;

public final class LangNode<RC extends BasicRenderContext> extends Node.a<RC> {
    private final String lang;

    @SafeVarargs
    public LangNode(String lang, Node<RC>... child) {
        super(child);
        this.lang = lang;
    }

    @Override
    public void render(SpannableStringBuilder builder, RC rc) {
        if (builder != null && lang != null) {
            Utils.ensureEndsWithNewline(builder);
            renderLang(builder, rc.getContext(), lang, builder.length());
        }
        super.render(builder, rc);
    }

    public static void renderLang(SpannableStringBuilder builder, Context ctx, String lang, int a) {
        builder.append(lang).append("\n");
        builder.setSpan(new RelativeSizeSpan(0.85f), a, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Utils.fixColor(builder, ctx, a);
    }
}

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.bettercodeblocks;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;

import com.discord.simpleast.core.node.Node;
import com.discord.utilities.textprocessing.node.BasicRenderContext;

public final class BCBNode<RC extends BasicRenderContext> extends Node<RC> {
    private final CharSequence content;
    private final String lang;

    public BCBNode(String lang, CharSequence content) {
        this.lang = lang;
        this.content = content;
    }

    @Override
    public final void render(SpannableStringBuilder builder, RC rc) {
        if (builder != null) {
            Utils.ensureEndsWithNewline(builder);
            int a = builder.length();
            if (lang != null) LangNode.renderLang(builder, rc.getContext(), lang, a);

            builder.append(content);
            int b = builder.length();
            builder.setSpan(new TypefaceSpan("monospace"), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.85f), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}

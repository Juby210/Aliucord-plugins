/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.bettercodeblocks;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;

import com.discord.simpleast.core.node.Node;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.dimen.DimenUtils;
import com.discord.utilities.spans.BlockBackgroundSpan;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.lytefast.flexinput.R;

public final class LangNode<RC> extends Node.a<RC> {
    private final String lang;

    @SafeVarargs
    public LangNode(String lang, Node<RC>... child) {
        super(child);
        this.lang = lang;
    }

    @Override
    public void render(SpannableStringBuilder builder, RC r) {
        if (builder != null && lang != null) {
            ensureEndsWithNewline(builder);
            int a = builder.length();
            builder.append(lang).append("\n");
            int b = builder.length();
            var ctx = ((BasicRenderContext) r).getContext();
            builder.setSpan(new ForegroundColorSpan(ColorCompat.getThemedColor(ctx, R.b.colorHeaderSecondary)), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BlockBackgroundSpan(
                ColorCompat.getThemedColor(ctx, R.b.theme_chat_code),
                ColorCompat.getThemedColor(ctx, R.b.theme_chat_codeblock_border),
                DimenUtils.dpToPixels(1),
                DimenUtils.dpToPixels(4),
                0
            ), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new LeadingMarginSpan.Standard(15), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.render(builder, r);
    }

    private static void ensureEndsWithNewline(SpannableStringBuilder builder) {
        if (builder.length() > 0) {
            int len = builder.length();
            char[] chars = new char[6];
            builder.getChars(len - 1, len, chars, 0);
            if (chars[0] != '\n') builder.append("\n");
        }
    }
}

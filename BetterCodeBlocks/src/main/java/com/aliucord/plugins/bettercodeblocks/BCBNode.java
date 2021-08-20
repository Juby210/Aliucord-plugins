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

public final class BCBNode<R> extends Node<R> {
    private final CharSequence content;

    public BCBNode(CharSequence content) {
        this.content = content;
    }

    @Override
    public void render(SpannableStringBuilder builder, R r) {
        if (builder != null) {
            int a = builder.length();
            builder.append(content);
            int b = builder.length();
            builder.setSpan(new TypefaceSpan("monospace"), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.85f), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bettercodeblocks;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.discord.utilities.color.ColorCompat;

public final class Utils {
    public static void ensureEndsWithNewline(SpannableStringBuilder builder) {
        if (builder.length() > 0) {
            int len = builder.length();
            char[] chars = new char[6];
            builder.getChars(len - 1, len, chars, 0);
            if (chars[0] != '\n') builder.append("\n");
        }
    }

    public static void fixColor(SpannableStringBuilder builder, Context ctx, int a) {
        builder.setSpan(
            new ForegroundColorSpan(ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorHeaderSecondary)),
            a,
            builder.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }
}

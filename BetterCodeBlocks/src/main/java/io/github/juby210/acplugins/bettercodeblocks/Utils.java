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

import java.lang.reflect.Method;
import java.util.List;

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

    // https://github.com/discord/SimpleAST/blob/d8987f0fede109936c429f5001817f58b827ba0f/simpleast-core/src/main/java/com/discord/simpleast/code/CodeRules.kt#L252
    public static Method getCreateGenericCodeRules() throws Throwable {
        var stringArray = String[].class;
        return b.a.t.a.e.class.getDeclaredMethod(
            "a",
            b.a.t.a.f.class, // codeStyleProviders
            List.class, // additionalRules
            stringArray, // definitions
            stringArray, // builtIns
            stringArray, // keywords
            stringArray // types
        );
    }
}

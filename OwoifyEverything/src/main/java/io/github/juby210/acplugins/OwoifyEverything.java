/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;

@AliucordPlugin
@SuppressLint("DiscouragedPrivateApi")
@SuppressWarnings({ "unused", "JavaReflectionMemberAccess" })
public final class OwoifyEverything extends Plugin {
    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(
            TextView.class.getDeclaredMethod("setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class),
            new PreHook(param -> {
                var text = param.args[0];
                if (text != null) {
                    if (text instanceof String) param.args[0] = owoify((String) text);
                    else param.args[0] = owoifyCharSeq((CharSequence) text);
                }
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private String owoify(String text) {
        return text.replace("l", "w").replace("L", "W")
            .replace("r", "w").replace("R", "W")
            .replace("o", "u").replace("O", "U");
    }

    private CharSequence owoifyCharSeq(CharSequence text) {
        var res = new SpannableStringBuilder(text);
        for (int i = 0; i < res.length(); i++) {
            var c = res.charAt(i);
            String newChar = null;
            switch (c) {
                case 'l':
                case 'r':
                    newChar = "w";
                    break;
                case 'L':
                case 'R':
                    newChar = "W";
                    break;
                case 'o':
                    newChar = "u";
                    break;
                case 'O':
                    newChar = "U";
                    break;
            }
            if (newChar != null) res.replace(i, i + 1, newChar);
        }
        return res;
    }
}

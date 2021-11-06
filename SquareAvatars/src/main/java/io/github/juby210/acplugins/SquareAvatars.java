/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.util.AttributeSet;

import com.aliucord.Constants;
import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

import c.f.g.f.a;

@AliucordPlugin
@SuppressWarnings("unused")
public final class SquareAvatars extends Plugin {
    @Override
    public void start(Context ctx) {
        var logger = new Logger("SquareAvatars");

        float _3dp = DimenUtils.dpToPx(3);

        // com.facebook.drawee.generic.GenericDraweeHierarchyInflater updateBuilder
        // https://github.com/facebook/fresco/blob/master/drawee/src/main/java/com/facebook/drawee/generic/GenericDraweeHierarchyInflater.java#L98
        var attrSet = AttributeSet.class;
        var ctxClass = Context.class;
        for (Method m : c.c.a.a0.d.class.getDeclaredMethods()) {
            var params = m.getParameterTypes();
            if (params.length == 3 && params[2] == attrSet && params[1] == ctxClass) {
                logger.debug("Found obfuscated updateBuilder method: " + m.getName());
                patcher.patch(m, new Hook(param -> {
                    var attrs = (AttributeSet) param.args[2];
                    if (attrs == null) return;

                    try {
                        var builder = (a) param.getResult();
                        var roundingParams = builder.r;

                        if (roundingParams != null && roundingParams.b) {
                            var context = (Context) param.args[1];
                            var id = attrs.getAttributeResourceValue(Constants.NAMESPACE_ANDROID, "id", 0);
                            if (id != 0 && contains(context.getResources().getResourceName(id))) {
                                roundingParams.b = false;

                                // round corners
                                var radii = roundingParams.c;
                                if (radii == null) {
                                    radii = new float[8];
                                    roundingParams.c = radii;
                                }
                                Arrays.fill(radii, _3dp);
                            }
                        }
                    } catch (Throwable e) { logger.error(e); }
                }));
                break;
            }
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private boolean contains(String s) {
        if (s.contains("id/guilds_item_profile_avatar_background")) return false;
        return s.contains("id/channels_list_item_text_actions_icon") ||
                s.contains("avatar") || s.contains("user");
    }
}

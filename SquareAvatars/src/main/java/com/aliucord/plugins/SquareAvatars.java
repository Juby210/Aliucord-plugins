/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.aliucord.Constants;
import com.aliucord.Logger;
import com.aliucord.Main;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.discord.utilities.images.MGImages;
import com.discord.views.user.UserAvatarPresenceView;

import java.util.*;

import c.f.g.f.a;
import top.canyie.pine.callback.MethodReplacement;

@SuppressWarnings("unused")
public class SquareAvatars extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Display square avatars instead of circles.";
        manifest.version = "0.0.9";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context ctx) throws Throwable {
        var logger = new Logger("SquareAvatars");

        float _3dp = Utils.dpToPx(3);

        // com.facebook.drawee.generic.GenericDraweeHierarchyInflater updateBuilder
        // https://github.com/facebook/fresco/blob/master/drawee/src/main/java/com/facebook/drawee/generic/GenericDraweeHierarchyInflater.java#L98
        patcher.patch("com.airbnb.lottie.parser.AnimatableValueParser", "I2", new Class<?>[]{ a.class, Context.class, AttributeSet.class }, new PinePatchFn(callFrame -> {
            var attrs = (AttributeSet) callFrame.args[2];
            if (attrs == null) return;

            try {
                var builder = (a) callFrame.getResult();
                var roundingParams = builder.r;

                if (roundingParams != null && roundingParams.b) {
                    var context = (Context) callFrame.args[1];
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

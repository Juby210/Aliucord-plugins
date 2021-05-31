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
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.discord.utilities.images.MGImages;
import com.discord.views.user.UserAvatarPresenceView;

import java.util.*;

import c.f.g.f.a;
import c.f.g.f.c;

@SuppressWarnings("unused")
public class SquareAvatars extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Display square avatars instead of circles.";
        manifest.version = "0.0.8";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.airbnb.lottie.parser.AnimatableValueParser";
    private static final String avatarViewClass = "com.discord.views.user.UserAvatarPresenceView";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("E2"));
        map.put(avatarViewClass, Collections.singletonList("setAvatarBackgroundColor"));
        return map;
    }

    @Override
    public void start(Context ctx) {
        Logger logger = new Logger("SquareAvatars");

        float _3dp = Utils.dpToPx(3);

        // com.facebook.drawee.generic.GenericDraweeHierarchyInflater updateBuilder
        // https://github.com/facebook/fresco/blob/master/drawee/src/main/java/com/facebook/drawee/generic/GenericDraweeHierarchyInflater.java#L98
        patcher.patch(className, "E2", (_this, args, ret) -> {
            if (args.size() < 3) return ret;
            AttributeSet attrs = (AttributeSet) args.get(2);
            if (attrs == null) return ret;

            try {
                a builder = (a) ret;
                c roundingParams = builder.r;

                if (roundingParams != null && roundingParams.b) {
                    Context context = (Context) args.get(1);
                    int id = attrs.getAttributeResourceValue(Constants.NAMESPACE_ANDROID, "id", 0);
                    if (id != 0 && contains(context.getResources().getResourceName(id))) {
                        roundingParams.b = false;

                        // round corners
                        float[] radii = roundingParams.c;
                        if (radii == null) {
                            radii = new float[8];
                            roundingParams.c = radii;
                        }
                        Arrays.fill(radii, _3dp);
                    }
                }
            } catch (Exception e) { logger.error(e); }

            return ret;
        });

        patcher.patch(avatarViewClass, "setAvatarBackgroundColor", (_this, args, ret) -> {
            MGImages.setRoundingParams(((UserAvatarPresenceView) _this).i.b, _3dp, false, (Integer) args.get(0), null, null);
            return ret;
        });
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

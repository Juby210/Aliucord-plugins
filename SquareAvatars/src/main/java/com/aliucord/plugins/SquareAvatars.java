package com.aliucord.plugins;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.aliucord.Constants;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;

import java.util.*;

@SuppressWarnings("unused")
public class SquareAvatars extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Display square avatars instead of circles.";
        manifest.version = "0.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "p.a.b.b.a";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("B0"));
        map.put("com.discord.views.user.UserAvatarPresenceView", Collections.singletonList("setAvatarBackgroundColor"));
        return map;
    }

    @Override
    public void start(Context ctx) {
        Logger logger = new Logger("SquareAvatars");

        // com.facebook.drawee.generic.GenericDraweeHierarchyInflater updateBuilder
        // https://github.com/facebook/fresco/blob/master/drawee/src/main/java/com/facebook/drawee/generic/GenericDraweeHierarchyInflater.java#L98
        patcher.patch(className, "B0", (_this, args, ret) -> {
            if (args.size() < 3) return ret;
            AttributeSet attrs = (AttributeSet) args.get(2);
            if (attrs == null) return ret;

            try {
                f.f.g.f.a builder = (f.f.g.f.a) ret;
                f.f.g.f.c roundingParams = builder.p;

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
                        Arrays.fill(radii, Utils.dpToPx(3));
                    }
                }
            } catch (Exception e) { logger.error(e); }

            return ret;
        });

        patcher.prePatch(
                "com.discord.views.user.UserAvatarPresenceView",
                "setAvatarBackgroundColor",
                (_this, args) -> new PrePatchRes(args, null)
        );
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

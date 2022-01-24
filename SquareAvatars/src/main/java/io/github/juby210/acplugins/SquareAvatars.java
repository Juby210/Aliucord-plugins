/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.content.Intent;
import android.text.*;
import android.util.AttributeSet;
import android.view.View;

import com.aliucord.Constants;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.TextInput;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;
import java.util.Arrays;

import b.f.g.f.a;

@AliucordPlugin(requiresRestart = true)
@SuppressWarnings("unused")
public final class SquareAvatars extends Plugin {
    public SquareAvatars() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    public static final class PluginSettings extends SettingsPage {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) {
            this.settings = settings;
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);
            setActionBarTitle("SquareAvatars");

            var input = new TextInput(
                view.getContext(),
                "Round corners radius (0 to disable)",
                String.valueOf(settings.getInt("roundCorners", 3)),
                new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        var str = s.toString();
                        if (!str.equals("")) {
                            settings.setInt("roundCorners", Integer.parseInt(str));
                            Snackbar.make(view, "Changes detected. Restart?", BaseTransientBottomBar.LENGTH_INDEFINITE).
                                setAction("Restart", e -> {
                                    var ctx = e.getContext();
                                    var intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                                    if (intent != null) {
                                        startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
                                        System.exit(0);
                                    }
                                }).
                                show();
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                }
            );
            input.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            getLinearLayout().addView(input);
        }
    }

    @Override
    public void start(Context ctx) {
        float roundRadius = DimenUtils.dpToPx(settings.getInt("roundCorners", 3));

        // com.facebook.drawee.generic.GenericDraweeHierarchyInflater updateBuilder
        // https://github.com/facebook/fresco/blob/master/drawee/src/main/java/com/facebook/drawee/generic/GenericDraweeHierarchyInflater.java#L98
        var attrSet = AttributeSet.class;
        var ctxClass = Context.class;
        for (Method m : b.c.a.a0.d.class.getDeclaredMethods()) {
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
                                Arrays.fill(radii, roundRadius);
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

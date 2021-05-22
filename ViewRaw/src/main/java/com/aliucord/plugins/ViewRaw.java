/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Constants;
import com.aliucord.Main;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.domain.ModelMessage;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.code.CodeNode$a;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.Rules$createCodeBlockRule$codeStyleProviders$1;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"unchecked", "unused"})
public class ViewRaw extends Plugin {
    public ViewRaw() {
        needsResources = true;
    }

    public static class Page extends SettingsPage {
        public ModelMessage message;

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setActionBarTitle("Raw message written by " + message.getAuthor().o());
            setActionBarSubtitle("View Raw");
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onViewBound(View view) {
            super.onViewBound(view);

            Context context = view.getContext();
            LinearLayout layout = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);
            int padding = Utils.getDefaultPadding();

            String content = message.getContent();
            if (content != null && !content.equals("")) {
                TextView textView = new TextView(context);
                BlockBackgroundNode<BasicRenderContext> node = new BlockBackgroundNode<>(false, new CodeNode<BasicRenderContext>(
                        new CodeNode$a.b<>(content), "", Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
                ));
                SpannableStringBuilder builder = new SpannableStringBuilder();
                node.render(builder, new RenderContext(context));
                textView.setText(builder);
                textView.setTextIsSelectable(true);
                textView.setPadding(padding, padding, padding, padding);
                layout.addView(textView);
                layout.addView(new Divider(context));
            }

            TextView header = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
            header.setText("All Raw Data");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            layout.addView(header);

            TextView textView = new TextView(context);
            BlockBackgroundNode<BasicRenderContext> node = new BlockBackgroundNode<>(false, new CodeNode<BasicRenderContext>(
                    new CodeNode$a.b<>(Utils.toJsonPretty(message)), "json", Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
            ));
            SpannableStringBuilder builder = new SpannableStringBuilder();
            node.render(builder, new RenderContext(context));
            textView.setText(builder);
            textView.setTextIsSelectable(true);
            textView.setPadding(padding, 0, padding, padding);
            layout.addView(textView);
        }
    }

    public static class RenderContext implements BasicRenderContext {
        private final Context context;
        public RenderContext(Context ctx) {
            context = ctx;
        }

        @Override
        public Context getContext() {
            return context;
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "View & Copy raw message and markdown.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.widgets.chat.list.actions.WidgetChatListActions";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Arrays.asList("configureUI", "onViewCreated"));
        return map;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context ctx) {
        Drawable icon = ResourcesCompat.getDrawable(resources,
                resources.getIdentifier("ic_viewraw", "drawable", "com.aliucord.plugins"), null);
        int id = View.generateViewId();

        try {
            Method getBinding = WidgetChatListActions.class.getDeclaredMethod("getBinding");
            getBinding.setAccessible(true);

            patcher.patch(className, "configureUI", (_this, args, ret) -> {
                try {
                    WidgetChatListActionsBinding binding = (WidgetChatListActionsBinding) getBinding.invoke(_this);
                    if (binding == null) return ret;
                    TextView viewRaw = binding.a.findViewById(id);
                    Page viewRawPage = new Page();
                    viewRawPage.message = ((WidgetChatListActions.Model) args.get(0)).getMessage();
                    viewRaw.setOnClickListener(e -> Utils.openPageWithProxy(e.getContext(), viewRawPage));
                } catch (Throwable ignored) {}
                return ret;
            });
        } catch (Throwable e) { Main.logger.error(e); }

        patcher.patch(className, "onViewCreated", (_this, args, ret) -> {
            LinearLayout linearLayout = (LinearLayout) ((NestedScrollView) args.get(0)).getChildAt(0);
            Context context = linearLayout.getContext();
            TextView viewRaw = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Icon);
            viewRaw.setText("View Raw");
            if (icon != null) icon.setTint(ColorCompat.getThemedColor(context, R$b.colorInteractiveNormal));
            viewRaw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            viewRaw.setId(id);
            linearLayout.addView(viewRaw);

            return ret;
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.utils.GsonUtils;
import com.aliucord.utils.MDUtils;
import com.aliucord.views.Divider;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.message.Message;
import com.discord.models.user.CoreUser;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R;

@SuppressWarnings({"unused"})
public class ViewRaw extends Plugin {
    public ViewRaw() {
        needsResources = true;
    }

    public static class Page extends SettingsPage {
        public final Message message;
        public Page(Message message) {
            this.message = message;
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onViewBound(View view) {
            super.onViewBound(view);
            setActionBarTitle("Raw message by " + new CoreUser(message.getAuthor()).getUsername());
            setActionBarSubtitle("View Raw");

            var context = view.getContext();
            var layout = getLinearLayout();

            var content = message.getContent();
            if (content != null && !content.equals("")) {
                var textView = new TextView(context);
                textView.setText(MDUtils.renderCodeBlock(context, new SpannableStringBuilder(), null, content));
                textView.setTextIsSelectable(true);
                layout.addView(textView);
                layout.addView(new Divider(context));
            }

            var header = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Header);
            header.setText("All Raw Data");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            header.setPadding(0, header.getPaddingTop(), header.getPaddingRight(), header.getPaddingBottom());
            layout.addView(header);

            var textView = new TextView(context);
            textView.setText(MDUtils.renderCodeBlock(context, new SpannableStringBuilder(), "js", GsonUtils.toJsonPretty(message)));
            textView.setTextIsSelectable(true);
            layout.addView(textView);
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "View & Copy raw message and markdown.";
        manifest.version = "1.0.5";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context ctx) throws Throwable {
        var icon = ResourcesCompat.getDrawable(resources,
                resources.getIdentifier("ic_viewraw", "drawable", "com.aliucord.plugins"), null);
        var id = View.generateViewId();

        var c = WidgetChatListActions.class;
        var getBinding = c.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);

        patcher.patch(c, "configureUI", new Class<?>[]{ WidgetChatListActions.Model.class }, new PinePatchFn(callFrame -> {
            try {
                var binding = (WidgetChatListActionsBinding) getBinding.invoke(callFrame.thisObject);
                if (binding == null) return;
                TextView viewRaw = binding.a.findViewById(id);
                viewRaw.setOnClickListener(e ->
                    Utils.openPageWithProxy(e.getContext(), new Page(((WidgetChatListActions.Model) callFrame.args[0]).getMessage())));
            } catch (Throwable ignored) {}
        }));

        patcher.patch(c, "onViewCreated", new Class<?>[]{ View.class, Bundle.class }, new PinePatchFn(callFrame -> {
            var linearLayout = (LinearLayout) ((NestedScrollView) callFrame.args[0]).getChildAt(0);
            var context = linearLayout.getContext();
            var viewRaw = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon);
            viewRaw.setText("View Raw");
            if (icon != null) icon.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
            viewRaw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            viewRaw.setId(id);
            linearLayout.addView(viewRaw);
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.plugins.pronoundb.Constants;
import com.aliucord.plugins.pronoundb.PluginSettings;
import com.aliucord.plugins.pronoundb.Store;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.models.domain.ModelMessage;
import com.discord.models.user.User;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

import java.lang.reflect.Field;
import java.util.*;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"unused", "JavaReflectionMemberAccess"})
public class PronounDB extends Plugin {
    public PronounDB() {
        settings = new Settings(PluginSettings.class);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "PronounDB plugin for Aliucord - Shows other's people pronouns in chat, so your chances of mis-gendering them is low. Service by pronoundb.org.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String messageClass = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage";
    private static final String userSheetClass = "com.discord.widgets.user.usersheet.WidgetUserSheet";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(messageClass, Collections.singletonList("onConfigure"));
        map.put(userSheetClass, Collections.singletonList("configureNote"));
        return map;
    }

    @Override
    public void start(Context context) {
        if (sets.getBool("displayChat", true)) try {
            injectMessages();
        } catch (Throwable e) { Main.logger.error(e); }
        if (sets.getBool("displayProfile", true)) injectProfile();
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private void injectMessages() throws Throwable {
        Field itemTimestampField = WidgetChatListAdapterItemMessage.class.getDeclaredField("itemTimestamp");
        itemTimestampField.setAccessible(true);

        patcher.patch(messageClass, "onConfigure", (_this, args, ret) -> {
            try {
                TextView itemTimestamp = (TextView) itemTimestampField.get(_this);
                if (itemTimestamp == null) return ret;

                ConstraintLayout header = (ConstraintLayout) itemTimestamp.getParent();
                TextView pronounsView = header.findViewById(viewId);
                if (pronounsView == null) {
                    pronounsView = new TextView(header.getContext(), null, 0, R$h.UiKit_TextView);
                    pronounsView.setId(viewId);
                    pronounsView.setTextSize(12);
                    pronounsView.setTextColor(ColorCompat.getThemedColor(header.getContext(), R$b.colorTextMuted));
                    header.addView(pronounsView);

                    ConstraintSet set = new ConstraintSet();
                    set.clone(header);
                    set.constrainedHeight(viewId, true);
                    set.connect(viewId, ConstraintSet.BASELINE, Utils.getResId("chat_list_adapter_item_text_name", "id"), ConstraintSet.BASELINE);
                    set.connect(viewId, ConstraintSet.START, itemTimestamp.getId(), ConstraintSet.END);
                    set.connect(viewId, ConstraintSet.END, header.getId(), ConstraintSet.END);
                    set.connect(itemTimestamp.getId(), ConstraintSet.END, viewId, ConstraintSet.END);
                    set.applyTo(header);
                }

                ModelMessage message = ((MessageEntry) args.get(1)).getMessage();
                if (message == null) return ret;
                Long userId = message.getAuthor().f();
                if (!Store.cache.containsKey(userId)) {
                    TextView finalPronounsView = pronounsView;
                    new Thread(() -> {
                        Store.fetchPronouns(userId);
                        new Handler(Looper.getMainLooper()).post(() -> addPronounsToHeader(finalPronounsView, userId));
                    }).start();
                } else addPronounsToHeader(pronounsView, userId);
            } catch (Throwable e) { Main.logger.error(e); }

            return ret;
        });
    }

    private void injectProfile() {
        patcher.patch(userSheetClass, "configureNote", (_this, args, ret) -> {
            WidgetUserSheetViewModel.ViewState.Loaded state = (WidgetUserSheetViewModel.ViewState.Loaded) args.get(0);
            User user = state.getUser();
            if (user == null) return ret;
            Long userId = user.getId();

            WidgetUserSheetBinding binding = WidgetUserSheet.access$getBinding$p((WidgetUserSheet) _this);
            if (!Store.cache.containsKey(userId)) new Thread(() -> {
                Store.fetchPronouns(userId);
                new Handler(Looper.getMainLooper()).post(() -> addPronounsToUserSheet(binding, userId));
            }).start(); else addPronounsToUserSheet(binding, userId);

            return ret;
        });
    }

    public void addPronounsToHeader(TextView pronounsView, Long userId) {
        String c = Store.cache.get(userId);
        if (c == null || c.equals("unspecified")) {
            pronounsView.setVisibility(View.GONE);
            return;
        }
        pronounsView.setVisibility(View.VISIBLE);
        pronounsView.setText(" • " + Constants.getPronouns(c, sets.getInt("format", 0)));
    }

    public void addPronounsToUserSheet(WidgetUserSheetBinding binding, Long userId) {
        String c = Store.cache.get(userId);
        if (c == null || c.equals("unspecified")) return;

        TextView noteHeader = binding.u;
        LinearLayout layout = (LinearLayout) noteHeader.getParent();

        TextView pronounsView = layout.findViewById(viewId);
        if (pronounsView == null) {
            pronounsView = new TextView(layout.getContext(), null, 0, R$h.UserProfile_Section_Header);
            pronounsView.setId(viewId);
            pronounsView.setTypeface(ResourcesCompat.getFont(layout.getContext(), com.aliucord.Constants.Fonts.whitney_semibold));
            pronounsView.setPadding(Utils.dpToPx(16), 0, 0, 0);
            layout.addView(pronounsView, layout.indexOfChild(noteHeader));
        }
        pronounsView.setText("Pronouns • " + Constants.getPronouns(c, sets.getInt("format", 0)));
    }

    public final int viewId = View.generateViewId();
}

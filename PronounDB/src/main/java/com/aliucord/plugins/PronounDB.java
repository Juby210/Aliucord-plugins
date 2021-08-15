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
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.plugins.pronoundb.*;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.models.user.CoreUser;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.lytefast.flexinput.R;

@SuppressLint("SetTextI18n")
@SuppressWarnings("unused")
public class PronounDB extends Plugin {
    public PronounDB() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "PronounDB plugin for Aliucord - Shows other's people pronouns in chat, so your chances of mis-gendering them is low. Service by pronoundb.org.";
        manifest.version = "1.0.7";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        if (settings.getBool("displayChat", true)) try {
            injectMessages();
        } catch (Throwable e) { Main.logger.error(e); }
        if (settings.getBool("displayProfile", true)) injectProfile();
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private void injectMessages() throws Throwable {
        var itemTimestampField = WidgetChatListAdapterItemMessage.class.getDeclaredField("itemTimestamp");
        itemTimestampField.setAccessible(true);

        patcher.patch(WidgetChatListAdapterItemMessage.class, "onConfigure", new Class<?>[]{ int.class, ChatListEntry.class }, new PinePatchFn(callFrame -> {
            try {
                var itemTimestamp = (TextView) itemTimestampField.get(callFrame.thisObject);
                if (itemTimestamp == null) return;

                var header = (ConstraintLayout) itemTimestamp.getParent();
                TextView pronounsView = header.findViewById(viewId);
                if (pronounsView == null) {
                    pronounsView = new TextView(header.getContext(), null, 0, R.h.UiKit_TextView);
                    pronounsView.setId(viewId);
                    pronounsView.setTextSize(12);
                    pronounsView.setTextColor(ColorCompat.getThemedColor(header.getContext(), R.b.colorTextMuted));
                    header.addView(pronounsView);

                    var set = new ConstraintSet();
                    set.clone(header);
                    set.constrainedHeight(viewId, true);
                    set.connect(viewId, ConstraintSet.BASELINE, Utils.getResId("chat_list_adapter_item_text_name", "id"), ConstraintSet.BASELINE);
                    set.connect(viewId, ConstraintSet.START, itemTimestamp.getId(), ConstraintSet.END);
                    set.connect(viewId, ConstraintSet.END, header.getId(), ConstraintSet.END);
                    set.connect(itemTimestamp.getId(), ConstraintSet.END, viewId, ConstraintSet.END);
                    set.applyTo(header);
                }

                var message = ((MessageEntry) callFrame.args[1]).getMessage();
                if (message == null) return;
                var user = new CoreUser(message.getAuthor());
                var bot = user.isBot();
                Long userId = user.getId();
                if (!bot && !Store.cache.containsKey(userId)) {
                    var finalPronounsView = pronounsView;
                    new Thread(() -> {
                        Store.fetchPronouns(userId);
                        new Handler(Looper.getMainLooper()).post(() -> addPronounsToHeader(finalPronounsView, userId, false));
                    }).start();
                } else addPronounsToHeader(pronounsView, userId, bot);
            } catch (Throwable e) { Main.logger.error(e); }
        }));
    }

    private void injectProfile() {
        patcher.patch(WidgetUserSheet.class, "configureNote", new Class<?>[]{ WidgetUserSheetViewModel.ViewState.Loaded.class }, new PinePatchFn(callFrame -> {
            var state = (WidgetUserSheetViewModel.ViewState.Loaded) callFrame.args[0];
            var user = state.getUser();
            if (user == null || user.isBot()) return;
            Long userId = user.getId();

            var binding = WidgetUserSheet.access$getBinding$p((WidgetUserSheet) callFrame.thisObject);
            if (!Store.cache.containsKey(userId)) new Thread(() -> {
                Store.fetchPronouns(userId);
                new Handler(Looper.getMainLooper()).post(() -> addPronounsToUserSheet(binding, userId));
            }).start(); else addPronounsToUserSheet(binding, userId);
        }));
    }

    public void addPronounsToHeader(TextView pronounsView, Long userId, boolean bot) {
        String c;
        if (bot || (c = Store.cache.get(userId)) == null || c.equals("unspecified")) {
            pronounsView.setVisibility(View.GONE);
            return;
        }
        pronounsView.setVisibility(View.VISIBLE);
        pronounsView.setText(" • " + Constants.getPronouns(c, settings.getInt("format", 0)));
    }

    private static final int noteHeaderId = Utils.getResId("user_sheet_note_header", "id");

    public void addPronounsToUserSheet(WidgetUserSheetBinding binding, Long userId) {
        var c = Store.cache.get(userId);
        if (c == null || c.equals("unspecified")) return;

        var noteHeader = binding.a.findViewById(noteHeaderId);
        var layout = (LinearLayout) noteHeader.getParent();

        TextView pronounsView = layout.findViewById(viewId);
        if (pronounsView == null) {
            pronounsView = new TextView(layout.getContext(), null, 0, R.h.UserProfile_Section_Header);
            pronounsView.setId(viewId);
            pronounsView.setTypeface(ResourcesCompat.getFont(layout.getContext(), com.aliucord.Constants.Fonts.whitney_semibold));
            pronounsView.setPadding(Utils.dpToPx(16), 0, 0, 0);
            layout.addView(pronounsView, layout.indexOfChild(noteHeader));
        }
        pronounsView.setText("Pronouns • " + Constants.getPronouns(c, settings.getInt("format", 0)));
    }

    public final int viewId = View.generateViewId();
}

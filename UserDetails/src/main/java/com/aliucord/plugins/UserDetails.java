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
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.*;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.plugins.userdetails.CachedData;
import com.aliucord.plugins.userdetails.PluginSettings;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.utils.RxUtils;
import com.aliucord.wrappers.ChannelWrapper;
import com.aliucord.wrappers.GuildMemberWrapper;
import com.discord.api.channel.Channel;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.utcdatetime.UtcDateTime;
import com.discord.databinding.UserProfileHeaderViewBinding;
import com.discord.models.message.Message;
import com.discord.models.user.CoreUser;
import com.discord.models.user.User;
import com.discord.stores.*;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.search.network.SearchFetcher;
import com.discord.utilities.search.network.SearchQuery;
import com.discord.utilities.time.ClockFactory;
import com.discord.utilities.time.TimeUtils;
import com.discord.widgets.user.profile.UserProfileHeaderView;
import com.discord.widgets.user.profile.UserProfileHeaderViewModel;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

import java.util.*;

import rx.Subscription;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"unused"})
public class UserDetails extends Plugin {
    public UserDetails() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Displays when user created account, joined to server and when sent last message in selected server / dm.";
        manifest.version = "1.0.8";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context ctx) {
        patcher.patch(StoreGuilds.class, "handleGuildMember", new Class<?>[]{ GuildMember.class, long.class }, new PinePatchFn(callFrame -> {
            var member = (GuildMember) callFrame.args[0];
            UtcDateTime joinedAt;
            com.discord.api.user.User user;
            if ((joinedAt = GuildMemberWrapper.getJoinedAt(member)) != null && (user = GuildMemberWrapper.getUser(member)) != null) {
                var id = new CoreUser(user).getId();
                cacheData((Long) callFrame.args[1], id, joinedAt.g(), 0);
                if (lastRequestedMember == id && forceUpdate != null) {
                    lastRequestedMember = 0;
                    Utils.mainThread.post(forceUpdate);
                }
            }
        }));

        subscription = RxUtils.subscribe(RxUtils.onBackpressureBuffer(StoreStream.getGatewaySocket().getMessageCreate()), RxUtils.createActionSubscriber(message -> {
            if (message == null) return;
            var msg = new Message(message);
            var guildId = msg.getGuildId();
            cacheData(guildId == null ? msg.getChannelId() : guildId, new CoreUser(msg.getAuthor()).getId(), 0, SnowflakeUtils.toTimestamp(msg.getId()));
        }));

        patcher.patch(UserProfileHeaderView.class, "updateViewState", new Class<?>[]{ UserProfileHeaderViewModel.ViewState.Loaded.class }, new PinePatchFn(callFrame -> {
            try {
                addDetails((UserProfileHeaderView) callFrame.thisObject, ((UserProfileHeaderViewModel.ViewState.Loaded) callFrame.args[0]).getUser());
            } catch (Throwable e) { Main.logger.error(e); }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
        if (subscription != null) subscription.unsubscribe();
        if (searchSubscription != null) searchSubscription.unsubscribe();
    }

    public final Map<Long, Map<Long, CachedData>> cache = new HashMap<>();
    public long lastRequestedMember;
    public long lastRequestedSearch;
    public Runnable forceUpdate;

    public Subscription subscription;
    public Subscription searchSubscription;

    private final int viewId = View.generateViewId();
    private final int settingsHeaderId = Utils.getResId("user_settings_profile_header_view", "id");

    @SuppressWarnings("ConstantConditions")
    public void cacheData(Long guildId, Long id, long joinedAt, long lastMessage) {
        if (!cache.containsKey(guildId)) cache.put(guildId, new HashMap<>());
        var guildCache = cache.get(guildId);
        if (guildCache.containsKey(id)) {
            var cachedData = guildCache.get(id);
            if (joinedAt != 0) cachedData.joinedAt = joinedAt;
            if (lastMessage != 0) cachedData.lastMessage = lastMessage;
        } else guildCache.put(id, new CachedData(joinedAt, lastMessage));
    }

    public void addDetails(UserProfileHeaderView _this, User user) throws Throwable {
        if (user == null) return;
        var settingsHeader = _this.getId() == settingsHeaderId;
        var displayCreatedAt = settings.getBool("createdAt", true);
        if (settingsHeader && !displayCreatedAt) return;
        var binding = (UserProfileHeaderViewBinding) ReflectUtils.getField(UserProfileHeaderView.class, _this, "binding", true);
        if (binding == null) return;

        var customStatus = binding.a.findViewById(Utils.getResId("user_profile_header_custom_status", "id"));
        var layout = (LinearLayout) customStatus.getParent();
        var context = layout.getContext();
        TextView detailsView = layout.findViewById(viewId);
        if (detailsView == null) {
            detailsView = new TextView(context, null, 0, R$h.UiKit_TextView_Semibold);
            detailsView.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            detailsView.setTextColor(ColorCompat.getThemedColor(context, R$b.colorTextMuted));
            detailsView.setId(viewId);
            layout.addView(detailsView, layout.indexOfChild(customStatus));
        }

        var uId = user.getId();
        var clock = ClockFactory.get();
        var text = new StringBuilder();
        if (displayCreatedAt) {
            text.append("Created at: ");
            text.append(TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(uId), clock));
        }
        if (settingsHeader) {
            detailsView.setText(text);
            return;
        }

        var gId = StoreStream.getGuildSelected().getSelectedGuildId();
        var dm = gId == 0;
        if (settings.getBool("joinedAt", true) && !dm) {
            Map<Long, CachedData> guildCache;
            CachedData data;
            if ((guildCache = cache.get(gId)) != null && (data = guildCache.get(uId)) != null && data.joinedAt != 0) {
                if (text.length() > 0) text.append("\n");
                text.append("Joined at: ");
                text.append(TimeUtils.toReadableTimeString(context, data.joinedAt, clock));
            } else if (lastRequestedMember != uId) {
                lastRequestedMember = uId;
                forceUpdate = () -> {
                    try {
                        addDetails(_this, user);
                    } catch (Throwable e) { Main.logger.error(e); }
                };
                //noinspection ResultOfMethodCallIgnored
                StoreStream.getGatewaySocket().requestGuildMembers(gId, null, Collections.singletonList(uId));
            }
        }

        if (settings.getBool("lastMessage", true)) {
            var cId = StoreStream.getChannelsSelected().getId();
            Channel channel;
            var dontFetchLast = dm && (cId < 1 || StoreStream.getUsers().getMe().getId() != uId &&
                ((channel = StoreStream.getChannels().getChannel(cId)) == null || CollectionUtils.findIndex(ChannelWrapper.getRecipients(channel), u -> new CoreUser(u).getId() == uId) == -1));
            if (dontFetchLast) appendLastMessage(text, "-");
            else {
                var cached = cache.get(dm ? cId : gId);
                CachedData data;
                if (cached != null && (data = cached.get(uId)) != null && data.lastMessage != 0) {
                    appendLastMessage(text, data.lastMessage == -1 ? "-" : TimeUtils.toReadableTimeString(context, data.lastMessage, clock));
                } else if (lastRequestedSearch != uId) {
                    lastRequestedSearch = uId;
                    forceUpdate = () -> {
                        try {
                            addDetails(_this, user);
                        } catch (Throwable e) { Main.logger.error(e); }
                    };
                    search(uId, dm ? cId : gId, dm);
                }
            }
        }

        detailsView.setText(text);
    }

    private static void appendLastMessage(StringBuilder text, CharSequence lastMessage) {
        if (text.length() > 0) text.append("\n");
        text.append("Last message: ");
        text.append(lastMessage);
    }

    private void search(Long authorId, long id, boolean dm) throws Throwable {
        if (searchSubscription != null) searchSubscription.unsubscribe();
        var searchFetcher = (SearchFetcher) ReflectUtils.getField(StoreStream.getSearch().getStoreSearchQuery(), "searchFetcher", true);
        var params = new HashMap<String, List<String>>();
        params.put("author_id", Collections.singletonList(authorId.toString()));
        searchSubscription = RxUtils.subscribe(searchFetcher.makeQuery(
            new StoreSearch.SearchTarget(dm ? StoreSearch.SearchTarget.Type.CHANNEL : StoreSearch.SearchTarget.Type.GUILD, id),
            null,
            new SearchQuery(params, true)
        ), RxUtils.createActionSubscriber(res -> {
            if (res == null || res.getErrorCode() != null) return;
            if (res.getTotalResults() == 0) {
                cacheData(id, authorId, 0, -1);
            } else if (res.getHits() != null) {
                var hit = res.getHits().get(0);
                cacheData(id, authorId, 0, SnowflakeUtils.toTimestamp(new Message(hit).getId()));
            } else return;
            if (lastRequestedSearch == authorId && forceUpdate != null) {
                lastRequestedSearch = 0;
                new Handler(Looper.getMainLooper()).post(forceUpdate);
            }
            searchSubscription.unsubscribe();
        }));
    }
}

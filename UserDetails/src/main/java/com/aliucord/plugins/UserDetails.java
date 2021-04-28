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
import com.aliucord.plugins.userdetails.PluginSettings;
import com.discord.api.channel.Channel;
import com.discord.api.guildmember.GuildMember;
import com.discord.databinding.UserProfileHeaderViewBinding;
import com.discord.models.domain.ModelMessage;
import com.discord.models.user.User;
import com.discord.stores.StoreSearch;
import com.discord.stores.StoreSearchQuery;
import com.discord.stores.StoreStream;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.search.network.SearchFetcher;
import com.discord.utilities.search.network.SearchQuery;
import com.discord.utilities.time.Clock;
import com.discord.utilities.time.ClockFactory;
import com.discord.utilities.time.TimeUtils;
import com.discord.widgets.user.profile.UserProfileHeaderView;
import com.discord.widgets.user.profile.UserProfileHeaderViewModel;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

import java.util.*;

import i0.l.e.b;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"unused"})
public class UserDetails extends Plugin {
    public static final class CachedData {
        public long joinedAt;
        public long lastMessage;

        public CachedData(long j, long m) {
            joinedAt = j;
            lastMessage = m;
        }
    }

    public UserDetails() {
        settings = new Settings(PluginSettings.class, Settings.Type.BOTTOMSHEET);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Displays when user created account, joined to server and when sent last message in selected server / dm.";
        manifest.version = "1.0.0";
        return manifest;
    }

    private static final String storeGuildsClass = "com.discord.stores.StoreGuilds";
    private static final String userProfileHeaderClass = "com.discord.widgets.user.profile.UserProfileHeaderView";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(storeGuildsClass, Collections.singletonList("handleGuildMember"));
        map.put(userProfileHeaderClass, Collections.singletonList("updateViewState"));
        return map;
    }

    @Override
    public void start(Context ctx) {
        patcher.patch(storeGuildsClass, "handleGuildMember", (_this, args, ret) -> {
            GuildMember member = (GuildMember) args.get(0);
            if (member.c() != null && member.h() != null) {
                long id = member.h().f();
                cacheData((Long) args.get(1), id, member.c().f(), 0);
                if (lastRequestedMember == id && forceUpdate != null) {
                    lastRequestedMember = 0;
                    new Handler(Looper.getMainLooper()).post(forceUpdate);
                }
            }
            return ret;
        });

        subscription = StoreStream.getGatewaySocket().getMessageCreate().I().S(new b<>(message -> {
            if (message == null) return;
            Long guildId = message.getGuildId();
            cacheData(guildId == null ? message.getChannelId() : guildId, message.getAuthor().f(), 0, SnowflakeUtils.toTimestamp(message.getId()));
        }, onError, onCompleted));

        patcher.patch(userProfileHeaderClass, "updateViewState", (_this, args, ret) -> {
            try {
                addDetails((UserProfileHeaderView) _this, ((UserProfileHeaderViewModel.ViewState.Loaded) args.get(0)).getUser());
            } catch (Throwable e) { Main.logger.error(e); }
            return ret;
        });
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

    private final Action1<Throwable> onError = e -> {};
    private final Action0 onCompleted = () -> {};
    public Subscription subscription;
    public Subscription searchSubscription;

    private final int viewId = View.generateViewId();
    private final int settingsHeaderId = Utils.getResId("user_settings_profile_header_view", "id");

    public void cacheData(Long guildId, Long id, long joinedAt, long lastMessage) {
        if (!cache.containsKey(guildId)) cache.put(guildId, new HashMap<>());
        Map<Long, CachedData> guildCache = cache.get(guildId);
        if (guildCache.containsKey(id)) {
            CachedData cachedData = guildCache.get(id);
            if (joinedAt != 0) cachedData.joinedAt = joinedAt;
            if (lastMessage != 0) cachedData.lastMessage = lastMessage;
        } else guildCache.put(id, new CachedData(joinedAt, lastMessage));
    }

    public void addDetails(UserProfileHeaderView _this, User user) throws Throwable {
        if (user == null) return;
        UserProfileHeaderViewBinding binding = (UserProfileHeaderViewBinding) Utils.getPrivateField(UserProfileHeaderView.class, _this, "binding");
        if (binding == null) return;

        LinearLayout layout = (LinearLayout) binding.d.getParent();
        Context context = layout.getContext();
        TextView detailsView = layout.findViewById(viewId);
        if (detailsView == null) {
            detailsView = new TextView(context, null, 0, R$h.UiKit_TextView_Semibold);
            detailsView.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            detailsView.setTextColor(ColorCompat.getThemedColor(context, R$b.colorTextMuted));
            detailsView.setId(viewId);
            layout.addView(detailsView, layout.indexOfChild(binding.d));
        }

        Long uId = user.getId();
        Clock clock = ClockFactory.get();
        StringBuilder text = new StringBuilder();
        if (sets.getBool("createdAt", true)) {
            text.append("Created at: ");
            text.append(TimeUtils.toReadableTimeString(context, SnowflakeUtils.toTimestamp(uId), clock));
        }

        long gId = StoreStream.getGuildSelected().getSelectedGuildId();
        boolean dm = gId == 0;
        if (sets.getBool("joinedAt", true) && !dm) {
            Map<Long, CachedData> guildCache;
            CachedData data;
            if ((guildCache = cache.get(gId)) != null && (data = guildCache.get(uId)) != null && data.joinedAt != 0) {
                if (text.length() > 0) text.append("\n");
                text.append("Joined at: ");
                text.append(TimeUtils.toReadableTimeString(context, guildCache.get(uId).joinedAt, clock));
            } else if (_this.getId() != settingsHeaderId && lastRequestedMember != uId) {
                lastRequestedMember = uId;
                forceUpdate = () -> {
                    try {
                        addDetails(_this, user);
                    } catch (Throwable e) { Main.logger.error(e); }
                };
                //noinspection ResultOfMethodCallIgnored
                StoreStream.getGatewaySocket().requestGuildMembers(Collections.singletonList(gId), null, Collections.singletonList(uId));
            }
        }

        if (sets.getBool("lastMessage", true)) {
            long cId = StoreStream.getChannelsSelected().getId();
            Channel channel;
            boolean dontFetchLast = dm && (cId < 1 || StoreStream.getUsers().getMe().getId() != uId &&
                    ((channel = StoreStream.getChannels().getChannel(cId)) == null || CollectionUtils.findIndex(channel.v(), u -> u.f() == uId) == -1));
            if (dontFetchLast) appendLastMessage(text, "=");
            else {
                Map<Long, CachedData> cached = cache.get(dm ? cId : gId);
                CachedData data;
                if (cached != null && (data = cached.get(uId)) != null && data.lastMessage != 0) {
                    appendLastMessage(text, data.lastMessage == -1 ? "-" : TimeUtils.toReadableTimeString(context, cached.get(uId).lastMessage, clock));
                } else if (_this.getId() != settingsHeaderId && lastRequestedSearch != uId) {
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
        SearchFetcher searchFetcher = (SearchFetcher) Utils.getPrivateField(StoreSearchQuery.class, StoreStream.getSearch().getStoreSearchQuery(), "searchFetcher");
        Map<String, List<String>> params = new HashMap<>();
        params.put("author_id", Collections.singletonList(authorId.toString()));
        searchSubscription = searchFetcher.makeQuery(
                new StoreSearch.SearchTarget(dm ? StoreSearch.SearchTarget.Type.CHANNEL : StoreSearch.SearchTarget.Type.GUILD, id),
                null,
                new SearchQuery(params, true)
        ).S(new b<>(res -> {
            if (res == null || res.getErrorCode() != null) return;
            if (res.getTotalResults() == 0) {
                cacheData(id, authorId, 0, -1);
            } else if (res.getHits() != null) {
                ModelMessage hit = res.getHits().get(0);
                cacheData(id, authorId, 0, SnowflakeUtils.toTimestamp(hit.getId()));
            } else return;
            if (lastRequestedSearch == authorId && forceUpdate != null) {
                lastRequestedSearch = 0;
                new Handler(Looper.getMainLooper()).post(forceUpdate);
            }
            searchSubscription.unsubscribe();
        }, onError, onCompleted));
    }
}

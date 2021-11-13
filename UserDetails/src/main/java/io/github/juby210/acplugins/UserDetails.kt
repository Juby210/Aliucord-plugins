/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.RxUtils
import com.aliucord.utils.RxUtils.onBackpressureBuffer
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.ChannelWrapper.Companion.recipients
import com.aliucord.wrappers.GuildMemberWrapper.Companion.joinedAt
import com.aliucord.wrappers.GuildMemberWrapper.Companion.user
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.models.message.Message
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreSearch
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.search.network.SearchFetcher
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.lytefast.flexinput.R
import io.github.juby210.acplugins.userdetails.CachedData
import io.github.juby210.acplugins.userdetails.PluginSettings
import rx.Subscription
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

val cache = HashMap<Long, HashMap<Long, CachedData>>()
var lastRequestedMember: Long = 0
var lastRequestedSearch: Long = 0
var forceUpdate: Runnable? = null

var memberSubscription: Subscription? = null
var messageSubscription: Subscription? = null
val searchSubscription: AtomicReference<Subscription?> = AtomicReference()

val viewId = View.generateViewId()
val customStatusViewId = Utils.getResId("user_profile_header_custom_status", "id")
val settingsHeaderId = Utils.getResId("user_settings_profile_header_view", "id")

private val profileHeaderBinding: Field = UserProfileHeaderView::class.java.getDeclaredField("binding").apply { isAccessible = true }
val UserProfileHeaderView.binding
    get() = profileHeaderBinding[this] as UserProfileHeaderViewBinding?

@AliucordPlugin
@Suppress("unused")
class UserDetails : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    override fun start(ctx: Context?) {
        val gatewaySocket = StoreStream.getGatewaySocket()

        memberSubscription = gatewaySocket.guildMembersChunk.onBackpressureBuffer().subscribe(RxUtils.createActionSubscriber({
            it.b()?.forEach { m ->
                val id = CoreUser(m.user).id
                cacheData(guildId = it.a(), id = id, joinedAt = m.joinedAt?.g() ?: -1)
                if (lastRequestedMember == id && forceUpdate != null) {
                    lastRequestedMember = 0
                    Utils.mainThread.post(forceUpdate!!)
                }
            }
        }))

        messageSubscription = gatewaySocket.messageCreate.onBackpressureBuffer().subscribe(RxUtils.createActionSubscriber({
            val msg = if (it == null) return@createActionSubscriber else Message(it)
            cacheData(guildId = msg.guildId ?: msg.channelId, id = CoreUser(msg.author).id, lastMessage = SnowflakeUtils.toTimestamp(msg.id))
        }))

        patcher.patch(UserProfileHeaderView::class.java, "updateViewState", arrayOf(UserProfileHeaderViewModel.ViewState.Loaded::class.java), Hook {
            addDetails(it.thisObject as UserProfileHeaderView, (it.args[0] as UserProfileHeaderViewModel.ViewState.Loaded).user)
        })
    }

    override fun stop(ctx: Context?) {
        patcher.unpatchAll()
        memberSubscription?.unsubscribe()
        messageSubscription?.unsubscribe()
        searchSubscription.getAndSet(null)?.unsubscribe()
    }

    private fun cacheData(guildId: Long, id: Long, joinedAt: Long? = null, lastMessage: Long? = null) {
        val guildCache = cache.getOrPut(guildId, { HashMap() })
        if (guildCache.containsKey(id)) {
            val cachedData = guildCache[id]!!
            if (joinedAt != null) cachedData.joinedAt = joinedAt
            if (lastMessage != null) cachedData.lastMessage = lastMessage
        } else guildCache[id] = CachedData(joinedAt, lastMessage)
    }

    private fun addDetails(view: UserProfileHeaderView, user: User) {
        val settingsHeader = view.id == settingsHeaderId
        val displayCreatedAt = settings.getBool("createdAt", true)
        if (settingsHeader && !displayCreatedAt) return
        val binding = view.binding ?: return

        val customStatus = binding.a.findViewById<View>(customStatusViewId)
        val layout = customStatus.parent as LinearLayout
        val context = layout.context
        val detailsView = layout.findViewById(viewId) ?: TextView(context, null, 0, R.i.UiKit_TextView_Semibold).apply {
            id = viewId
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
            setTextColor(ColorCompat.getThemedColor(context, R.b.colorTextMuted))
            layout.addView(this)
        }

        val uId = user.id
        val text = StringBuilder()
        if (displayCreatedAt) text.append("Created at: ").append(toReadable(context, SnowflakeUtils.toTimestamp(uId)))
        if (settingsHeader) {
            detailsView.text = text
            return
        }

        val gId = StoreStream.getGuildSelected().selectedGuildId
        val dm = gId == 0L
        if (settings.getBool("joinedAt", true) && !dm) cache[gId]?.get(uId)?.joinedAt?.let {
            if (text.isNotEmpty()) text.append("\n")
            text.append("Joined at: ").append(if (it == -1L) "-" else toReadable(context, it))
        } ?: if (lastRequestedMember != uId) {
            lastRequestedMember = uId
            forceUpdate = Runnable { addDetails(view, user) }
            StoreStream.getGatewaySocket().requestGuildMembers(gId, null, listOf(uId))
        }

        if (settings.getBool("lastMessage", true)) {
            val cId = StoreStream.getChannelsSelected().id
            val dontFetchLast = dm && (cId < 1 ||
                StoreStream.getUsers().me.id != uId &&
                StoreStream.getChannels().getChannel(cId)?.recipients?.any { CoreUser(it).id == uId }?.let { !it } ?: true)
            if (dontFetchLast) appendLastMessage(text, "-")
            else {
                val cIdOrGId = if (dm) cId else gId
                cache[cIdOrGId]?.get(uId)?.lastMessage?.let {
                    appendLastMessage(text, if (it == -1L) "-" else toReadable(context, it))
                } ?: if (lastRequestedSearch != uId) {
                    lastRequestedSearch = uId
                    forceUpdate = Runnable { addDetails(view, user) }
                    search(uId, cIdOrGId, dm)
                }
            }
        }

        detailsView.text = text
    }

    private fun appendLastMessage(text: StringBuilder, lastMessage: CharSequence) {
        if (text.isNotEmpty()) text.append("\n")
        text.append("Last message: ").append(lastMessage)
    }

    private var searchFetcher: SearchFetcher? = null
    private fun search(authorId: Long, id: Long, dm: Boolean) {
        val fetcher = searchFetcher ?: ReflectUtils.getField(StoreStream.getSearch().storeSearchQuery, "searchFetcher").let {
            (it as SearchFetcher).apply { searchFetcher = this }
        }
        searchSubscription.getAndSet(fetcher.makeQuery(
            StoreSearch.SearchTarget(if (dm) StoreSearch.SearchTarget.Type.CHANNEL else StoreSearch.SearchTarget.Type.GUILD, id),
            null,
            SearchQuery(mapOf("author_id" to listOf(authorId.toString())), true)
        ).subscribe(RxUtils.createActionSubscriber({
            searchSubscription.getAndSet(null)?.unsubscribe()
            if (it == null || it.errorCode != null) return@createActionSubscriber
            if (it.totalResults == 0) cacheData(guildId = id, id = authorId, lastMessage = -1)
            else it.hits?.run {
                if (size > 0) cacheData(guildId = id, id = authorId, lastMessage = SnowflakeUtils.toTimestamp(Message(get(0)).id))
            } ?: return@createActionSubscriber
            if (lastRequestedSearch == authorId && forceUpdate != null) {
                lastRequestedSearch = 0
                Utils.mainThread.post(forceUpdate!!)
            }
        })))?.unsubscribe()
    }

    private fun toReadable(context: Context, timestamp: Long): String {
        val clock = ClockFactory.get()
        var readable = TimeUtils.toReadableTimeString(context, timestamp, ClockFactory.get()).toString()
        if (settings.getBool("showDaysAgo", true)) {
            val days = TimeUnit.DAYS.convert(clock.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
            readable += when (days) {
                0L -> " (Today)"
                1L -> " (Yesterday)"
                else -> " ($days days ago)"
            }
        }
        return readable
    }
}

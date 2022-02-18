/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.discord.api.presence.ClientStatus
import com.discord.api.presence.ClientStatuses
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.databinding.WidgetChannelsListItemChannelPrivateBinding
import com.discord.models.presence.Presence
import com.discord.utilities.presence.PresenceUtils
import com.discord.views.StatusView
import com.discord.views.user.UserAvatarPresenceView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.lytefast.flexinput.R
import io.github.juby210.acplugins.bsi.*

@AliucordPlugin
@SuppressLint("UseCompatLoadingForDrawables")
class BetterStatusIndicators : Plugin() {
    init {
        needsResources = true
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(this)
    }

    private lateinit var mobile: Array<Drawable>
    private lateinit var desktop: Array<Drawable>
    private lateinit var web: Array<Drawable>
    private lateinit var filled: Array<Drawable>
    private lateinit var radialDrawables: Array<Drawable>
    private lateinit var radialDrawablesRect: Array<Drawable>

    private val usernameTextId = Utils.getResId("username_text", "id")

    override fun start(context: Context) {
        if (isPluginEnabled("BetterStatus")) {
            Utils.showToast(
                "BetterStatusIndicators automatically disabled BetterStatus for you as those plugins are incompatible each other.",
                true
            )
            PluginManager.disablePlugin("BetterStatus")
        }

        val res = context.resources
        mobile = res.getStatusDrawables(res.getDrawable(R.e.ic_mobile, null))
        filled = res.getStatusDrawables(res.getDrawable(R.e.ic_status_online_16dp, null))

        resources.run {
            desktop = res.getStatusDrawables(getPluginDrawable("ic_desktop"))
            web = res.getStatusDrawables(getPluginDrawable("ic_web"))
            radialDrawables = res.getStatusDrawables(getPluginDrawable("ic_radial_status"))
            radialDrawablesRect = res.getStatusDrawables(getPluginDrawable("ic_radial_status_rect"))
        }

        val square = isPluginEnabled("SquareAvatars")

        patchStatusView()
        patchMembersList(square)

        // User profile
        patcher.patch(
            UserProfileHeaderView::class.java.getDeclaredMethod("configurePrimaryName", UserProfileHeaderViewModel.ViewState.Loaded::class.java),
            Hook {
                val presence = (it.args[0] as UserProfileHeaderViewModel.ViewState.Loaded).presence ?: return@Hook
                val clientStatuses = presence.clientStatuses ?: return@Hook

                val usernameText = (it.thisObject as View).findViewById<SimpleDraweeSpanTextView?>(usernameTextId) ?: return@Hook
                addIndicators(usernameText, clientStatuses, 32)
            }
        )

        patchDMsList(square)
        patchChatStatus()
        patchChatStatusPlatforms()
        patchRadialStatus(settings.getBool("radialStatus", false), square)
    }

    override fun stop(context: Context?) = patcher.unpatchAll()

    private fun Resources.getStatusDrawables(drawable: Drawable) = arrayOf(
        drawable,
        drawable.clone().apply { setTint(getColor(R.c.status_yellow, null)) },
        drawable.clone().apply { setTint(getColor(R.c.status_red, null)) }
    )

    private fun ClientStatus.getDrawable(drawables: Array<Drawable>) = when (this) {
        ClientStatus.ONLINE -> drawables[0]
        ClientStatus.IDLE -> drawables[1]
        ClientStatus.DND -> drawables[2]
        else -> null
    }

    private fun TextView.appendIcon(icon: Drawable, size: Int, width: Int = size) {
        append(" ")
        append(SpannableStringBuilder().append(" ", ImageSpan(
            icon.clone().apply { setBounds(0, 0, width, size) }, 1
        ), 0))
    }

    private fun addIndicators(
        view: TextView,
        clientStatuses: ClientStatuses,
        size: Int,
        noAvatarStatus: Boolean = !settings.getBool("avatarStatus", true)
    ) {
        val isMobile = clientStatuses.mobile?.isActive ?: false
        if (noAvatarStatus && isMobile) clientStatuses.mobile?.let { mobileStatus ->
            mobileStatus.getDrawable(mobile)?.apply { view.appendIcon(this, size, (size / 1.5).toInt()) }
        }
        if (noAvatarStatus || isMobile) clientStatuses.desktop?.let { desktopStatus ->
            desktopStatus.getDrawable(desktop)?.apply { view.appendIcon(this, size) }
        }
        if (noAvatarStatus || isMobile || clientStatuses.desktop?.isActive == true) clientStatuses.web?.let { webStatus ->
            webStatus.getDrawable(web)?.apply { view.appendIcon(this, size) }
        }
    }

    private var unpatchStatusView: Runnable? = null
    fun patchStatusView() {
        unpatchStatusView?.run()

        val m by lazy { StatusView::class.java.getDeclaredMethod("setPresence", Presence::class.java) }
        if (settings.getBool("avatarStatus", true))
            unpatchStatusView = patcher.patch(m, Hook {
                val presence = it.args[0] as Presence? ?: return@Hook
                val clientStatuses = presence.clientStatuses ?: return@Hook

                val statusView = it.thisObject as StatusView

                clientStatuses.mobile?.let { mobileStatus ->
                    if (PresenceUtils.INSTANCE.isMobile(clientStatuses)) return@Hook
                    mobileStatus.getDrawable(mobile)?.apply {
                        statusView.setImageDrawable(this)
                        return@Hook
                    }
                }

                clientStatuses.desktop?.let { desktopStatus ->
                    desktopStatus.getDrawable(desktop)?.apply {
                        statusView.setImageDrawable(this)
                        return@Hook
                    }
                }

                clientStatuses.web?.let { webStatus ->
                    webStatus.getDrawable(web)?.apply { statusView.setImageDrawable(this) }
                }
            })
        else if (settings.getBool("filledColors", false))
            unpatchStatusView = patcher.patch(m, Hook {
                val presence = it.args[0] as Presence? ?: return@Hook
                presence.status?.getDrawable(filled)?.apply { (it.thisObject as StatusView).setImageDrawable(this) }
            })
    }

    private fun patchMembersList(square: Boolean) {
        val avatarId = Utils.getResId("channel_members_list_item_avatar", "id")
        val memberViewHolder = ChannelMembersListViewHolderMember::class.java
        val memberViewHolderBinding = memberViewHolder.getDeclaredField("binding").apply { isAccessible = true }
        patcher.patch(
            memberViewHolder.getDeclaredMethod("bind", ChannelMembersListAdapter.Item.Member::class.java, Function0::class.java),
            Hook {
                val presence = (it.args[0] as ChannelMembersListAdapter.Item.Member).presence ?: return@Hook
                val binding = memberViewHolderBinding[it.thisObject] as WidgetChannelMembersListItemUserBinding

                presence.clientStatuses?.let { clientStatuses ->
                    val usernameText = binding.a.findViewById<SimpleDraweeSpanTextView?>(usernameTextId) ?: return@let
                    addIndicators(usernameText, clientStatuses, 24)
                }

                if (settings.getBool("radialStatus", false))
                    presence.status?.let { status -> setRadialStatus(status, binding.a.findViewById(avatarId) ?: return@let, square) }
            }
        )
    }

    private fun patchDMsList(square: Boolean) {
        val id = Utils.getResId("channels_list_item_private_name", "id")
        val avatarId = Utils.getResId("channels_list_item_private_avatar", "id")
        val channelPrivateItem = WidgetChannelsListAdapter.ItemChannelPrivate::class.java
        val channelPrivateItemBinding = channelPrivateItem.getDeclaredField("binding").apply { isAccessible = true }
        patcher.patch(
            channelPrivateItem.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChannelListItem::class.java),
            Hook {
                val presence = (it.args[1] as ChannelListItemPrivate).presence
                val binding = channelPrivateItemBinding[it.thisObject] as WidgetChannelsListItemChannelPrivateBinding

                presence?.clientStatuses?.let { clientStatuses -> addIndicators(binding.a.findViewById(id), clientStatuses, 24) }

                if (settings.getBool("radialStatus", false))
                    setRadialStatus(presence?.status, binding.a.findViewById(avatarId), square)
            }
        )
    }

    private var unpatchChatStatus: Runnable? = null
    fun patchChatStatus() {
        unpatchChatStatus?.run()
        if (!settings.getBool("chatStatus", true)) return

        val itemMessage = WidgetChatListAdapterItemMessage::class.java
        val itemNameField = itemMessage.getDeclaredField("itemName").apply { isAccessible = true }
        unpatchChatStatus = patcher.patch(
            itemMessage.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChatListEntry::class.java),
            Hook {
                val itemName = itemNameField[it.thisObject] as TextView? ?: return@Hook
                val entry = it.args[1] as MessageEntry

                val presence = entry.message.author.presence ?: return@Hook
                presence.status?.getDrawable(filled)?.apply { itemName.appendIcon(this, 16) }
            }
        )
    }

    private var unpatchChatStatusPlatforms: Runnable? = null
    fun patchChatStatusPlatforms() {
        unpatchChatStatusPlatforms?.run()
        if (!settings.getBool("chatStatusPlatforms", false)) return

        val itemMessage = WidgetChatListAdapterItemMessage::class.java
        val itemNameField = itemMessage.getDeclaredField("itemName").apply { isAccessible = true }
        unpatchChatStatusPlatforms = patcher.patch(
            itemMessage.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChatListEntry::class.java),
            Hook {
                val itemName = itemNameField[it.thisObject] as TextView? ?: return@Hook
                val entry = it.args[1] as MessageEntry

                val presence = entry.message.author.presence ?: return@Hook
                addIndicators(itemName, presence.clientStatuses ?: return@Hook, 24, true)
            }
        )
    }

    private var unpatchAvatarPresenceView: Runnable? = null
    private var unpatchChatRadialStatus: Runnable? = null
    fun patchRadialStatus(radialStatus: Boolean, square: Boolean) {
        unpatchAvatarPresenceView?.run()
        unpatchChatRadialStatus?.run()
        if (!radialStatus) return

        // User profile
        val avatarCutoutId = Utils.getResId("avatar_cutout", "id")
        unpatchAvatarPresenceView = patcher.patch(
            UserAvatarPresenceView::class.java.getDeclaredMethod("a", UserAvatarPresenceView.a::class.java),
            Hook {
                val data = it.args[0] as UserAvatarPresenceView.a
                val status = data.b?.status

                val avatarView = (it.thisObject as View).findViewById<View>(avatarCutoutId)
                setRadialStatus(status, avatarView)
            }
        )

        // Chat
        val itemMessage = WidgetChatListAdapterItemMessage::class.java
        val itemAvatarField = itemMessage.getDeclaredField("itemAvatar").apply { isAccessible = true }
        unpatchChatRadialStatus = patcher.patch(
            itemMessage.getDeclaredMethod("onConfigure", Int::class.javaPrimitiveType, ChatListEntry::class.java),
            Hook {
                val itemAvatar = itemAvatarField[it.thisObject] as View? ?: return@Hook
                val entry = it.args[1] as MessageEntry

                setRadialStatus(entry.message.author.presence?.status, itemAvatar, square)
            }
        )
    }

    private fun setRadialStatus(status: ClientStatus?, avatarView: View, square: Boolean = false) {
        val failed = status?.getDrawable(if (square) radialDrawablesRect else radialDrawables)?.run {
            avatarView.setPadding(8, 8, 8, 8)
            avatarView.background = clone()
            false
        } ?: true
        if (failed) avatarView.apply {
            setPadding(0, 0, 0, 0)
            background = null
        }
    }
}

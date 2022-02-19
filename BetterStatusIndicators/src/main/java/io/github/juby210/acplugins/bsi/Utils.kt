/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bsi

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.aliucord.PluginManager
import com.aliucord.api.SettingsAPI
import com.discord.api.presence.ClientStatus
import com.discord.api.presence.ClientStatuses
import com.discord.api.user.User
import com.discord.models.presence.Presence
import com.discord.stores.StoreStream

inline val ClientStatuses.desktop: ClientStatus?
    get() = a()

inline val ClientStatuses.mobile: ClientStatus?
    get() = b()

inline val ClientStatuses.web: ClientStatus?
    get() = c()

val ClientStatus.isActive
    get() = this != ClientStatus.OFFLINE && this != ClientStatus.INVISIBLE

@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.getPluginDrawable(name: String): Drawable =
    getDrawable(getIdentifier(name, "drawable", "io.github.juby210.acplugins"), null)

fun Drawable.clone() = constantState!!.newDrawable().mutate()

fun isPluginEnabled(name: String) = PluginManager.plugins.containsKey(name) && PluginManager.isPluginEnabled(name)

val User.presence: Presence?
    get() = StoreStream.getPresences().presences[id] as Presence?

inline val SettingsAPI.radialStatus
    get() = getBool("radialStatus", false)

val SettingsAPI.radialStatusDMs
    get() = radialStatus && getBool("radialStatusDMs", true)

val SettingsAPI.radialStatusFriendsList
    get() = radialStatus && getBool("radialStatusFriendsList", true)

val SettingsAPI.radialStatusMembersList
    get() = radialStatus && getBool("radialStatusMembersList", true)

fun ClientStatus.getDrawable(drawables: Array<Drawable>) = when (this) {
    ClientStatus.ONLINE -> drawables[0]
    ClientStatus.IDLE -> drawables[1]
    ClientStatus.DND -> drawables[2]
    else -> null
}

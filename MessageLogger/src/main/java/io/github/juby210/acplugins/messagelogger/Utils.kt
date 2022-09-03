/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.messagelogger

import com.discord.stores.StoreStream

fun isChannelMuted(guildId: Long, channelId: Long) =
    StoreStream.getUserGuildSettings().guildSettings[guildId]?.getChannelOverride(channelId)?.isMuted ?: false

fun isGuildMuted(guildId: Long) =
    StoreStream.getUserGuildSettings().guildSettings[guildId]?.isMuted ?: false

/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.userdetails;

public final class CachedData {
    public long joinedAt;
    public long lastMessage;

    public CachedData(long j, long m) {
        joinedAt = j;
        lastMessage = m;
    }
}

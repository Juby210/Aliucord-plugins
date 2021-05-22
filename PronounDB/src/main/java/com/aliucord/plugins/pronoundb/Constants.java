/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.pronoundb;

import android.text.TextUtils;

import java.util.*;

public final class Constants {
    public static final String WEBSITE = "https://pronoundb.org";

    public static class Endpoints {
        public static String LOOKUP_BULK(Object[] ids) { return WEBSITE + "/api/v1/lookup-bulk?platform=discord&ids=" + TextUtils.join(",", ids); }
    }

    private static final Map<String, List<String>> pronouns = new HashMap<String, List<String>>(){{
        put("hh", Arrays.asList("he/him", "He/Him"));
        put("hi", Arrays.asList("he/it", "He/It"));
        put("hs", Arrays.asList("he/she", "He/She"));
        put("ht", Arrays.asList("he/they", "He/They"));
        put("ih", Arrays.asList("it/him", "It/Him"));
        put("ii", Arrays.asList("it/its", "It/Its"));
        put("is", Arrays.asList("it/she", "It/She"));
        put("it", Arrays.asList("it/they", "It/They"));
        put("shh", Arrays.asList("she/he", "She/He"));
        put("sh", Arrays.asList("she/her", "She/Her"));
        put("si", Arrays.asList("she/it", "She/It"));
        put("st", Arrays.asList("she/they", "She/They"));
        put("th", Arrays.asList("they/he", "They/He"));
        put("ti", Arrays.asList("they/it", "They/It"));
        put("ts", Arrays.asList("they/she", "They/She"));
        put("tt", Arrays.asList("they/them", "They/Them"));

        put("any", Collections.singletonList("Any pronouns"));
        put("other", Collections.singletonList("Other pronouns"));
        put("ask", Collections.singletonList("Ask me my pronouns"));
        put("avoid", Collections.singletonList("Avoid pronouns, use my name"));
    }};
    public static String getPronouns(String p, int format) {
        List<String> list = pronouns.get(p);
        if (list == null) return null;
        return list.size() > format ? list.get(format) : list.get(0);
    }
}

package com.nyceapps.beachscores.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lugosi on 02.06.17.
 */

public class PreferencesUtils {
    private final static String PREF_KEY_TIME_ZONE_COUNT = "pref_key_time_zone_count";
    private static final String PREF_KEY_PREFIX_TIME_ZONE = "pref_key_time_zone_";

    private PreferencesUtils() {
    }

    public static Map<String, DateTimeZone> getTimeZones(Context pContext) {
        Map<String, DateTimeZone> timeZones = new HashMap<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        int timeZoneCount = sharedPreferences.getInt(PREF_KEY_TIME_ZONE_COUNT, 0);
        for (int i = 0; i < timeZoneCount; i++) {
            String key = PREF_KEY_PREFIX_TIME_ZONE + i;
            String value = sharedPreferences.getString(key, null);
            if (!TextUtils.isEmpty(value)) {
                String[] split = value.split("___");
                String timeZoneKey = split[0];
                String timeZoneId = split[1];
                DateTimeZone timeZone = DateTimeZone.forID(timeZoneId);
                if (timeZone != null) {
                    timeZones.put(timeZoneKey, timeZone);
                }
            }
        }

        return timeZones;
    }

    public static void setTimeZones(Map<String, DateTimeZone> pTimeZones, Context pContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_KEY_TIME_ZONE_COUNT, pTimeZones.size());
        int i = 0;
        for (Map.Entry<String, DateTimeZone> entry : pTimeZones.entrySet()) {
            String key = PREF_KEY_PREFIX_TIME_ZONE + i;
            String timeZoneId = entry.getValue().getID();
            String value = entry.getKey() + "___" + timeZoneId;
            editor.putString(key, value);
            i++;
        }
        editor.commit();
    }
}

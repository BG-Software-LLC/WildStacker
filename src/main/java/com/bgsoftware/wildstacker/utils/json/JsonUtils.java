package com.bgsoftware.wildstacker.utils.json;

import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public final class JsonUtils {

    public static double getDouble(JSONObject jsonObject, String key, double def) {
        Object object = jsonObject.getOrDefault(key, def);
        return object instanceof Number ? ((Number) object).doubleValue() : (double) object;
    }

    public static short getShort(JSONObject jsonObject, String key, short def) {
        Object object = jsonObject.getOrDefault(key, def);
        return object instanceof Number ? ((Number) object).shortValue() : (short) object;
    }

    public static int getInt(JSONObject jsonObject, String key, int def) {
        Object object = jsonObject.getOrDefault(key, def);
        return object instanceof Number ? ((Number) object).intValue() : (int) object;
    }

}

package com.hackorama.plethora.server.data.common;

import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import com.hackorama.plethora.common.Log;

public final class JSONParser {
    private final static String ERROR_MSG = "JSON value parsing error";

    private JSONParser() {
        // no public instances
    }

    public static String getJsonValue(String source, String name) {
        JSONObject json = getJson(source);
        try {
            return json == null ? null : json.getString(name);
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    public static Object getJsonValueObject(String source, String name) {
        JSONObject json = getJson(source);
        try {
            return json == null ? null : json.get(name);
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    public static Long getJsonValueLong(String source, String name) {
        JSONObject json = getJson(source);
        try {
            return json == null ? null : json.getLong(name);
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    /*
     * Using null return to indicate error, making this a three value boolean, and not proud of it. TODO
     */
    public static Boolean getJsonValueBoolean(String source, String name) {
        JSONObject json = getJson(source);
        try {
            return json == null ? null : json.getBoolean(name);
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    public static JSONObject getJson(String data) {
        try {
            if (data != null) {
                return new JSONObject(data);
            }
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    private static void handleException(JSONException exception) {
        Log.getLogger().log(Level.FINEST, ERROR_MSG, exception);
    }

}

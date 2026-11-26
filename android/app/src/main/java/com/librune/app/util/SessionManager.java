package com.librune.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.librune.app.model.User;

public class SessionManager {

    private static final String PREF_NAME = "librune_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER = "current_user";
    private static final String KEY_BASE_URL = "base_url";
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public void updateUser(User user) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        return json != null ? gson.fromJson(json, User.class) : null;
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clearSession() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply();
    }

    public String getBaseUrl() {
        String url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        return url.endsWith("/") ? url : url + "/";
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        String normalized = baseUrl.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        prefs.edit().putString(KEY_BASE_URL, normalized).apply();
    }

    public String resolveImageUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            return null;
        }
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }
        String base = getBaseUrl();
        String trimmed = pathOrUrl.startsWith("/") ? pathOrUrl.substring(1) : pathOrUrl;
        return base + trimmed;
    }
}

package com.librune.app.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import retrofit2.Response;

public class ErrorUtils {

    private static final Gson GSON = new Gson();

    public static String extractMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                Map<String, String> map = GSON.fromJson(raw, new TypeToken<Map<String, String>>() {}.getType());
                if (map != null && map.containsKey("error")) {
                    return map.get("error");
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}

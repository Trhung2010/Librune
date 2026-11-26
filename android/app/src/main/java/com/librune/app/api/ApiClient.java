package com.librune.app.api;

import android.content.Context;
import com.librune.app.util.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static String cachedBaseUrl;

    public static synchronized ApiService getService(Context context) {
        SessionManager session = new SessionManager(context);
        String baseUrl = session.getBaseUrl();

        if (retrofit == null || !baseUrl.equals(cachedBaseUrl)) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = session.getToken();
                        if (token != null && !token.isEmpty()) {
                            Request authorized = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(authorized);
                        }
                        return chain.proceed(original);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            cachedBaseUrl = baseUrl;
        }
        return retrofit.create(ApiService.class);
    }

    public static synchronized void reset() {
        retrofit = null;
        cachedBaseUrl = null;
    }
}

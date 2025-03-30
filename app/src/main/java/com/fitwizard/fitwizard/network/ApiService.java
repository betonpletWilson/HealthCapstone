package com.fitwizard.fitwizard.network;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiService {

    private static final OkHttpClient client = new OkHttpClient();

    public interface ApiCallback {
        void onSuccess(String responseBody);
        void onFailure(String errorMessage);
    }

    public static void doApiRequest(String functionName, String columnName, String queryValue, ApiCallback callback) {
        String url = "http://3.148.77.114:3000/"
                + functionName
                + "?"
                + columnName
                + "="
                + queryValue;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure("Request failed: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (callback != null) {
                        callback.onFailure("HTTP error code: " + response.code());
                    }
                } else {
                    String responseData = response.body() != null
                            ? response.body().string()
                            : null;

                    if (callback != null) {
                        callback.onSuccess(responseData);
                    }
                }
            }
        });
    }
}

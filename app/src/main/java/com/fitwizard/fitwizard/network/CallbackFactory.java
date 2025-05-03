package com.fitwizard.fitwizard.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CallbackFactory {

    private static final Gson gson = new Gson();
    public static <T> Callback createCallback(
            final ApiService.ApiCallback<T> callback,
            final Class<T> clazz
    ) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure("Request failed: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    if (callback != null) {
                        callback.onFailure("HTTP error code: " + response.code());
                    }
                    return;
                }

                String json;
                try (ResponseBody body = response.body()) {
                    json = (body != null) ? body.string() : null;
                } catch (IOException e) {
                    if (callback != null) {
                        callback.onFailure("IO error: " + e.getMessage());
                    }
                    return;
                }

                if (json == null || json.isEmpty()) {
                    if (callback != null) {
                        callback.onFailure("Empty response body");
                    }
                    return;
                }

                final T obj;
                try {
                    obj = gson.fromJson(json, clazz);
                } catch (JsonSyntaxException e) {
                    if (callback != null) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                    return;
                }

                if (callback != null) {
                    callback.onSuccess(obj);
                }
            }
        };
    }
}

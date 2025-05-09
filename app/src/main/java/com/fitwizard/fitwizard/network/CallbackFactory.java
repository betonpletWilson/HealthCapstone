package com.fitwizard.fitwizard.network;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    public static <T> Callback createListCallback(
            final Class<T[]> arrayClazz,
            final ApiService.ApiCallback<List<T>> cb) {

        return new Callback() {
            @Override public void onFailure(Call c, IOException e) {
                if (cb != null) cb.onFailure("Request failed: " + e.getMessage());
            }
            @Override public void onResponse(Call c, Response r) {
                if (!r.isSuccessful()) {
                    if (cb != null) cb.onFailure("HTTP " + r.code());
                    return;
                }
                String json;
                try (ResponseBody body = r.body()) {
                    json = (body != null) ? body.string() : null;
                } catch (IOException io) { cb.onFailure(io.getMessage()); return; }

                if (json == null || json.isEmpty()) { cb.onFailure("Empty body"); return; }

                try {
                    List<T> list;
                    if (json.trim().startsWith("[")) {                 // ← array
                        T[] arr = gson.fromJson(json, arrayClazz);
                        list = Arrays.asList(arr);
                    } else {                                          // ← single obj
                        Class<T> comp = (Class<T>) arrayClazz.getComponentType();
                        T obj = gson.fromJson(json, comp);
                        list = java.util.Collections.singletonList(obj);
                    }
                    cb.onSuccess(list);
                } catch (JsonSyntaxException ex) {
                    cb.onFailure("Parse error: " + ex.getMessage());
                }
            }
        };
    }
    public static Callback createVoidCallback() {
        return new Callback() {
            @Override public void onFailure(Call c, IOException e) { /* no‑op */ }
            @Override public void onResponse(Call c, Response r)   { r.close(); }
        };
    }
}

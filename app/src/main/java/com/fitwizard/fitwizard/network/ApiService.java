package com.fitwizard.fitwizard.network;

import com.google.gson.Gson;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiService {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final String TAG = "ApiService";

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onFailure(String errorMessage);
    }

    public static <T> void DBRequest(
            String tableName,
            String columnName,
            String queryValue,
            Class<T> clazz,
            ApiCallback<T> callback
    ) {

        String url = "http://3.148.77.114:3000/getSimple"
                + "?tableName="  + tableName
                + "&columnName=" + columnName
                + "&queryValue=" + queryValue;

        // Log the full URL for debugging
        Log.d(TAG, "DBRequest URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(CallbackFactory.createCallback(callback, clazz));
    }
}

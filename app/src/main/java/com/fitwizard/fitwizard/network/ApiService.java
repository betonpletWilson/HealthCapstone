package com.fitwizard.fitwizard.network;
import com.fitwizard.fitwizard.BuildConfig;
import Recipe_Logging.FoodData;

import com.google.gson.Gson;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.net.URLEncoder;
import java.util.List;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

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

    public static <T> void DBSearch(String table, String col, String val,
                                    Class<T[]> arrayClazz,
                                    ApiCallback<List<T>> cb) {

        String url = baseUrl() + "/searchSimple"
                + "?tableName="  + table
                + "&columnName=" + col
                + "&queryValue=" + encode(val);

        Request req = new Request.Builder().url(url).build();

        client.newCall(req).enqueue(
                CallbackFactory.createListCallback(arrayClazz, new ApiCallback<List<T>>() {

                    @Override
                    public void onSuccess(List<T> dbRows) {

                        // if table not subject to external search, return immediately
                        if (!(table.equals("food") || table.equals("medication") || table.equals("workout"))) {
                            cb.onSuccess(dbRows);          // nothing else to add
                            return;
                        }

                        // always query the external API as well
                        fallbackSearch(table, val, arrayClazz, new ApiCallback<List<T>>() {
                            @Override
                            public void onSuccess(List<T> extRows) {
                                // remove any that already exist in DB (by name, case‑insensitive)
                                java.util.Set<String> names = new java.util.HashSet<>();
                                for (T o : dbRows) names.add(((FoodData)o).getName().toLowerCase());

                                java.util.List<T> merged = new java.util.ArrayList<>(dbRows);
                                for (T o : extRows) {
                                    if (!names.contains(((FoodData)o).getName().toLowerCase()))
                                        merged.add(o);
                                }
                                cb.onSuccess(merged);      // DB rows first, then extras
                            }
                            @Override public void onFailure(String e) {
                                // external failed → just give DB rows
                                cb.onSuccess(dbRows);
                            }
                        });
                    }

                    @Override public void onFailure(String e) {
                        // no DB rows – fall back like before
                        fallbackSearch(table, val, arrayClazz, cb);
                    }
                }));
    }

    private static <T> void fallbackSearch(String table, String query,
                                           Class<T[]> arrayClazz,
                                           ApiCallback<List<T>> cb) {
        switch (table) {
            case "food":
                externalUSDA(query, arrayClazz, cb);
                break;
            case "medication":
                externalOpenFDA(query, arrayClazz, cb);
                break;
            case "workout":
                externalNinjaWorkout(query, arrayClazz, cb);
                break;
            default:
                cb.onFailure("No external API for table " + table);
        }
    }


    private static <T> void externalUSDA(
            String query,
            Class<T[]> arrayClazz,
            ApiCallback<List<T>> cb) {

        // Delegate to the concrete 2‑arg version that returns List<FoodData>
        externalUSDA(query, new ApiCallback<List<FoodData>>() {
            @Override public void onSuccess(List<FoodData> foods) {
                @SuppressWarnings("unchecked")           // safe: only called for FoodData
                List<T> cast = (List<T>)(List<?>) foods;
                cb.onSuccess(cast);
            }
            @Override public void onFailure(String err) { cb.onFailure(err); }
        });
    }

    private static void externalUSDA(
            String query,
            ApiCallback<List<FoodData>> cb) {

        String url = "https://api.nal.usda.gov/fdc/v1/foods/search"
                + "?api_key=Y1zHeXGbhmfI0h82H8ymfGgCGnjeCW84DjHTaCra"
                + "&query="   + encode(query)
                + "&pageSize=20";

        Request req = new Request.Builder().url(url).build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call c, IOException e) {
                cb.onFailure("USDA fail: " + e.getMessage());
            }
            @Override public void onResponse(Call c, Response r) {
                if (!r.isSuccessful()) { cb.onFailure("USDA HTTP " + r.code()); return; }

                String json;
                try (ResponseBody b = r.body()) {
                    json = (b != null) ? b.string() : null;
                } catch (IOException io) { cb.onFailure(io.getMessage()); return; }

                JsonArray arr = gson.fromJson(json, JsonObject.class)
                        .getAsJsonArray("foods");
                if (arr == null || arr.size() == 0) {
                    cb.onFailure("No USDA hits"); return;
                }

                List<FoodData> list = new java.util.ArrayList<>();
                for (JsonElement e : arr)
                    if (e.isJsonObject())
                        list.add(mapFdcToFoodData(e.getAsJsonObject()));

                cb.onSuccess(list);              // ← only return, no DB insert
            }
        });
    }




    private static <T> void externalOpenFDA(String q, Class<T[]> arr,
                                            ApiCallback<List<T>> cb) {
        String api = "https://api.fda.gov/drug/label.json"
                + "?api_key=zL0hMetU7UXn8LUTLLa1iJuUQsgZNg1M0O1EzGb9"
                + "&search=openfda.brand_name:" + encode(q);
        callExternal(api, arr, cb, "medication");
    }
    private static <T> void externalNinjaWorkout(String q, Class<T[]> arr,
                                                 ApiCallback<List<T>> cb) {
        String api = "https://api.api-ninjas.com/v1/caloriesburned?activity="
                + encode(q);
        Request req = new Request.Builder()
                .url(api)
                .addHeader("X-Api-Key",
                        "dSXo++BDJHNsTceA7o/NjA==kKhP3yW0pWDssUZ9")
                .build();
        enqueueExternal(req, arr, cb, "workout");
    }

    private static <T> void callExternal(String url, Class<T[]> arr,
                                         ApiCallback<List<T>> cb, String table){
        Request req = new Request.Builder().url(url).build();
        enqueueExternal(req, arr, cb, table);
    }

    private static <T> void enqueueExternal(Request req, Class<T[]> arr,
                                            ApiCallback<List<T>> cb, String table){
        client.newCall(req).enqueue(
                CallbackFactory.createListCallback(arr, new ApiCallback<List<T>>() {
                    @Override public void onSuccess(List<T> list) {
                        if (list.isEmpty()) { cb.onFailure("No match in ext API"); return; }
                        // TODO: POST list[0].. to /addSimple to cache in DB if desired
                        cb.onSuccess(list);
                    }
                    @Override public void onFailure(String e) { cb.onFailure(e); }
                }));
    }

    private static String encode(String s){
        try { return URLEncoder.encode(s,"UTF-8"); } catch(Exception e){ return s;}
    }
    private static String baseUrl() {
        return BuildConfig.SERVER_BASE_URL;
    }

    /* ─── SAFE & FLEXIBLE: FDC record → FoodData ───────────────────────── */
    private static FoodData mapFdcToFoodData(JsonObject jo) {

        String name  = jo.has("description") ? jo.get("description").getAsString()
                : "Unknown food";
        String brand = jo.has("brandOwner")  ? jo.get("brandOwner").getAsString()
                : "";

        /* serving string */
        String serving = "1 serving";
        if (jo.has("servingSize") && jo.has("servingSizeUnit")
                && !jo.get("servingSize").isJsonNull()
                && !jo.get("servingSizeUnit").isJsonNull()) {

            serving = jo.get("servingSize").getAsString() + " "
                    + jo.get("servingSizeUnit").getAsString();
        }

        float protein = 0, fat = 0, carbs = 0;
        int   calories = 0;

        if (jo.has("foodNutrients") && jo.get("foodNutrients").isJsonArray()) {
            for (JsonElement el : jo.getAsJsonArray("foodNutrients")) {
                if (!el.isJsonObject()) continue;
                JsonObject n = el.getAsJsonObject();

                if (!n.has("nutrientId") || n.get("nutrientId").isJsonNull()
                        || !n.has("value")   || n.get("value").isJsonNull())
                    continue;

                int   id  = n.get("nutrientId").getAsInt();
                float val = n.get("value").getAsFloat();

                switch (id) {
                    case 1003: protein  = val; break;              // protein
                    case 1004: fat      = val; break;              // fat
                    case 1005: carbs    = val; break;              // carbs
                    case 1008: calories = Math.round(val); break;  // kcal
                }
            }
        }

        return new FoodData(name, calories, protein, fat, carbs, serving);
    }

    public static void addFoodToDb(FoodData fd) {
        Request req = new Request.Builder()
                .url(baseUrl() + "/addFood")
                .post(RequestBody.create(
                        gson.toJson(fd),
                        MediaType.parse("application/json")))
                .build();

        client.newCall(req).enqueue(CallbackFactory.createVoidCallback());
    }
}

package Login_Register;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "current_user_id";

    // Save the currently logged-in user's ID (e.g., email)
    public static void login(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    // Clear user session on logout
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    // Retrieve the currently logged-in user's ID
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    // Check if a user is logged in
    public static boolean isLoggedIn(Context context) {
        return getCurrentUserId(context) != null;
    }

    // Generate a key specific to the logged-in user for preferences
    public static String getUserKey(Context context, String baseKey) {
        String userId = getCurrentUserId(context);
        return userId != null ? userId + "_" + baseKey : baseKey;
    }

}

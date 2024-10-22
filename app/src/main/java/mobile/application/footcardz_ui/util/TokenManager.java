package mobile.application.footcardz_ui.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public String getRefreshToken() {
        return prefs.getString(REFRESH_TOKEN_KEY, null);
    }

    public void saveRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.apply();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(REFRESH_TOKEN_KEY);
        editor.apply();
    }
}

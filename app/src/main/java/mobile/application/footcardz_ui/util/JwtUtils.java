package mobile.application.footcardz_ui.util;

import android.util.Log;
import com.auth0.android.jwt.JWT;

import java.util.Date;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    public static String getSubject(String token) {
        try {
            JWT jwt = new JWT(token);
            return jwt.getSubject();
        } catch (Exception e) {
            Log.e(TAG, "Error getting subject from token: " + e.getMessage(), e);
            return null;
        }
    }

    public static boolean isTokenExpired(String token) {
        try {
            JWT jwt = new JWT(token);
            Date expirationDate = jwt.getExpiresAt();
            return expirationDate != null && expirationDate.before(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error checking token expiration: " + e.getMessage(), e);
            return true;
        }
    }
}

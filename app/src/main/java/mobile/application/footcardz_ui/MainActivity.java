package mobile.application.footcardz_ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import mobile.application.footcardz_ui.activity.authentication.LoginActivity;
import mobile.application.footcardz_ui.activity.home.HomeActivity;
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.LoginResponse;
import mobile.application.footcardz_ui.model.RefreshTokenRequest;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.JwtUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import mobile.application.footcardz_ui.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        checkAuthentication();
    }

    private void checkAuthentication() {
        String bearerToken = tokenManager.getToken();
        if (bearerToken == null) {
            goToLogin();
            return;
        }

        if (!JwtUtils.isTokenExpired(bearerToken)) {
            goToHome();
            return;
        }

        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) {
            goToLogin();
            return;
        }

        tokenManager.clearTokens();
        refreshAccessToken(refreshToken);
    }

    private void refreshAccessToken(String refreshToken) {
        apiService.refreshToken(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse tokenResponse = response.body();
                    tokenManager.saveToken(tokenResponse.getBearer());
                    tokenManager.saveRefreshToken(tokenResponse.getRefresh());
                    goToHome();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(MainActivity.this, response);
                    Log.e("RefreshAccessToken", "Error: " + errorResponse.getMessage());
                    goToLogin();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("RefreshAccessToken", "Error: " + t.getMessage());
                goToLogin();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

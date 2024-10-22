package mobile.application.footcardz_ui.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import mobile.application.footcardz_ui.R;
import mobile.application.footcardz_ui.activity.home.HomeActivity;
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.LoginRequest;
import mobile.application.footcardz_ui.model.LoginResponse;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import mobile.application.footcardz_ui.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        TextView forgotPasswordTextView = findViewById(R.id.forgot_password);
        TextView signupLinkTextView = findViewById(R.id.signup_link);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = new TokenManager(this);

        loginButton.setOnClickListener(view -> attemptLogin());

        forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
        signupLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.username_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    tokenManager.saveToken(loginResponse.getBearer());
                    tokenManager.saveRefreshToken(loginResponse.getRefresh());

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(LoginActivity.this, response);
                    Toast.makeText(LoginActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

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
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.SignupRequest;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        Button signupButton = findViewById(R.id.signup_button);
        TextView loginLinkTextView = findViewById(R.id.login_link);

        apiService = RetrofitClient.getApiService(this);

        signupButton.setOnClickListener(view -> attemptSignup());

        loginLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void attemptSignup() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.username_required));
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.password_mismatch));
            return;
        }

        SignupRequest signupRequest = new SignupRequest(username, email, password);

        apiService.signup(signupRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(SignupActivity.this, ActivationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(SignupActivity.this, response);
                    Toast.makeText(SignupActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(SignupActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

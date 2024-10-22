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
import mobile.application.footcardz_ui.model.ResendCodeRequest;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailEditText = findViewById(R.id.email);
        Button resetPasswordButton = findViewById(R.id.reset_password_button);
        TextView loginLinkTextView = findViewById(R.id.login_link);

        apiService = RetrofitClient.getApiService(this);

        resetPasswordButton.setOnClickListener(view -> attemptResetPassword());

        loginLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void attemptResetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            return;
        }

        ResendCodeRequest resetPasswordCode = new ResendCodeRequest(email);
        apiService.resetPassword(resetPasswordCode).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ResetPasswordActivity.this, ConfirmPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(ResetPasswordActivity.this, response);
                    Toast.makeText(ResetPasswordActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

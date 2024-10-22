package mobile.application.footcardz_ui.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import mobile.application.footcardz_ui.R;
import mobile.application.footcardz_ui.model.ConfirmPasswordRequest;
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.ResendCodeRequest;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPasswordActivity extends AppCompatActivity {

    private EditText codeEditText;
    private EditText newPasswordEditText;
    private String email;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);

        codeEditText = findViewById(R.id.code);
        newPasswordEditText = findViewById(R.id.new_password);
        Button confirmButton = findViewById(R.id.confirm_button);
        Button resendCodeButton = findViewById(R.id.resend_code_button);

        apiService = RetrofitClient.getApiService(this);

        email = getIntent().getStringExtra("email");

        confirmButton.setOnClickListener(view -> attemptConfirmPassword());

        resendCodeButton.setOnClickListener(view -> resendCode());
    }

    private void attemptConfirmPassword() {
        String code = codeEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            codeEditText.setError(getString(R.string.reset_code_required));
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError(getString(R.string.password_required));
            return;
        }

        ConfirmPasswordRequest confirmPasswordRequest = new ConfirmPasswordRequest(code, email, newPassword);

        apiService.confirmPasswordReset(confirmPasswordRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ConfirmPasswordActivity.this, getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(ConfirmPasswordActivity.this, response);
                    Toast.makeText(ConfirmPasswordActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ConfirmPasswordActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendCode() {
        ResendCodeRequest resendCodeRequest = new ResendCodeRequest(email);
        apiService.resetPassword(resendCodeRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ConfirmPasswordActivity.this, getString(R.string.reset_password_code_resent), Toast.LENGTH_SHORT).show();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(ConfirmPasswordActivity.this, response);
                    Toast.makeText(ConfirmPasswordActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ConfirmPasswordActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

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
import mobile.application.footcardz_ui.model.ActivationRequest;
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.ResendCodeRequest;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivationActivity extends AppCompatActivity {

    private EditText codeEditText;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        codeEditText = findViewById(R.id.code);
        Button activateButton = findViewById(R.id.activate_button);
        Button resendCodeButton = findViewById(R.id.resend_code_button);

        apiService = RetrofitClient.getApiService(this);

        email = getIntent().getStringExtra("email");

        activateButton.setOnClickListener(view -> attemptActivation());

        resendCodeButton.setOnClickListener(view -> resendActivationCode());
    }

    private void attemptActivation() {
        String activationCode = codeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(activationCode)) {
            codeEditText.setError(getString(R.string.code_required));
            return;
        }

        ActivationRequest activationRequest = new ActivationRequest(activationCode, email);

        apiService.activateAccount(activationRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivationActivity.this, getString(R.string.activation_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(ActivationActivity.this, response);
                    Toast.makeText(ActivationActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ActivationActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendActivationCode() {
        ResendCodeRequest resendRequest = new ResendCodeRequest(email);

        apiService.resendActivationCode(resendRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivationActivity.this, getString(R.string.code_resent), Toast.LENGTH_SHORT).show();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(ActivationActivity.this, response);
                    Toast.makeText(ActivationActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ActivationActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

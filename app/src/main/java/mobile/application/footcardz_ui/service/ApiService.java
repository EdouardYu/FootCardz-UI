package mobile.application.footcardz_ui.service;

import mobile.application.footcardz_ui.model.ActivationRequest;
import mobile.application.footcardz_ui.model.ConfirmPasswordRequest;
import mobile.application.footcardz_ui.model.LoginRequest;
import mobile.application.footcardz_ui.model.LoginResponse;
import mobile.application.footcardz_ui.model.RefreshTokenRequest;
import mobile.application.footcardz_ui.model.ResendCodeRequest;
import mobile.application.footcardz_ui.model.SignupRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("token/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    @POST("signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("signup")
    Call<Void> signup(@Body SignupRequest signupRequest);

    @POST("activate")
    Call<Void> activateAccount(@Body ActivationRequest activationRequest);

    @POST("activate/new")
    Call<Void> resendActivationCode(@Body ResendCodeRequest resendActivationRequest);

    @POST("signout")
    Call<Void> logout();

    @POST("password/reset")
    Call<Void> resetPassword(@Body ResendCodeRequest resetPasswordRequest);

    @POST("password/new")
    Call<Void> confirmPasswordReset(@Body ConfirmPasswordRequest confirmPasswordRequest);
}


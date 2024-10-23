package mobile.application.footcardz_ui.service;

import mobile.application.footcardz_ui.model.ActivationRequest;
import mobile.application.footcardz_ui.model.ConfirmPasswordRequest;
import mobile.application.footcardz_ui.model.LoginRequest;
import mobile.application.footcardz_ui.model.LoginResponse;
import mobile.application.footcardz_ui.model.Player;
import mobile.application.footcardz_ui.model.PlayerResponse;
import mobile.application.footcardz_ui.model.RefreshTokenRequest;
import mobile.application.footcardz_ui.model.ResendCodeRequest;
import mobile.application.footcardz_ui.model.SignupRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // Define the endpoint to get players of a specific user
    @GET("users/{userId}/players")
    Call<PlayerResponse> getPlayers(@Path("userId") int userId,
                                    @Query("page") int page,
                                    @Query("size") int size);

    // Add this method to search players for a specific user with a search term
    @GET("users/{userId}/players/search")
    Call<PlayerResponse> searchPlayersForUser(@Path("userId") int userId,
                                              @Query("term") String searchTerm,
                                              @Query("page") int page);

    // Add this method to perform a search
    @GET("players/search")
    Call<PlayerResponse> searchPlayers(@Query("term") String searchTerm,
                                       @Query("page") int page);

    @GET("players")
    Call<PlayerResponse> getAllPlayers(@Query("page") int page, @Query("size") int size);

    @GET("players/daily")
    Call<Player> getDailyPlayer();
}


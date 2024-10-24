package mobile.application.footcardz_ui.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import mobile.application.footcardz_ui.R;
import mobile.application.footcardz_ui.activity.authentication.LoginActivity;
import mobile.application.footcardz_ui.model.ErrorResponse;
import mobile.application.footcardz_ui.model.Player;
import mobile.application.footcardz_ui.model.PlayerAdapter;
import mobile.application.footcardz_ui.model.PlayerResponse;
import mobile.application.footcardz_ui.service.ApiService;
import mobile.application.footcardz_ui.util.ApiErrorUtils;
import mobile.application.footcardz_ui.util.JwtUtils;
import mobile.application.footcardz_ui.util.RetrofitClient;
import mobile.application.footcardz_ui.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private ApiService apiService;
    private PlayerAdapter playerAdapter;
    private TokenManager tokenManager;

    private boolean isLoading = false;
    private int currentPage = 0;
    private int totalPages = 1;
    private int userId;
    private boolean isDailyPlayerShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_nav);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = new TokenManager(this);

        userId = Integer.parseInt(JwtUtils.getSubject(tokenManager.getToken()));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        playerAdapter = new PlayerAdapter(new ArrayList<>());
        recyclerView.setAdapter(playerAdapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() < 2) {
                    currentPage = 0;
                    loadPlayers(userId, currentPage);
                } else {
                    currentPage = 0;
                    searchPlayersForUser(query, currentPage);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() < 2) {
                    currentPage = 0;
                    loadPlayers(userId, currentPage);
                } else {
                    currentPage = 0;
                    searchPlayersForUser(newText, currentPage);
                }
                return true;
            }
        });

        fetchAndDisplayDailyPlayer();

        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && layoutManager.findLastVisibleItemPosition() == playerAdapter.getItemCount() - 1) {
                    if (currentPage < totalPages - 1) {
                        currentPage++;
                        loadPlayers(userId, currentPage);
                    }
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_all_players) {
                startActivity(new Intent(UserActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                attemptLogout();
                return true;
            }
            return false;
        });
    }

    private void attemptLogout() {
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                tokenManager.clearTokens();

                if (response.isSuccessful()) {
                    Toast.makeText(UserActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(UserActivity.this, response);
                    Log.e("Logout", "Error: " + errorResponse.getMessage());
                }

                redirectToLogin();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(UserActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadPlayers(int userId, int page) {
        if (isDailyPlayerShown) {
            isLoading = true;

            apiService.getPlayers(userId, page, 30).enqueue(new Callback<PlayerResponse>() {
                @Override
                public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                    isLoading = false;
                    if (response.isSuccessful() && response.body() != null) {
                        List<Player> players = response.body().getContent();
                        totalPages = response.body().getPage().getTotalPages();
                        if (page == 0) {
                            playerAdapter.setPlayers(players);
                        } else {
                            playerAdapter.addPlayers(players);
                        }
                    } else {
                        ErrorResponse errorResponse = ApiErrorUtils.parseError(UserActivity.this, response);
                        Toast.makeText(UserActivity.this, errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                    isLoading = false;
                    Toast.makeText(UserActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void searchPlayersForUser(String searchTerm, int page) {
        if (isDailyPlayerShown) {
            isLoading = true;

            apiService.searchPlayersForUser(userId, searchTerm, page).enqueue(new Callback<PlayerResponse>() {
                @Override
                public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                    isLoading = false;
                    if (response.isSuccessful() && response.body() != null) {
                        List<Player> players = response.body().getContent();
                        totalPages = response.body().getPage().getTotalPages();
                        if (page == 0) {
                            playerAdapter.setPlayers(players);
                        } else {
                            playerAdapter.addPlayers(players);
                        }
                    } else {
                        ErrorResponse errorResponse = ApiErrorUtils.parseError(UserActivity.this, response);
                        Toast.makeText(UserActivity.this, errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                    isLoading = false;
                    Toast.makeText(UserActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchAndDisplayDailyPlayer() {
        apiService.getDailyPlayer().enqueue(new Callback<Player>() {
            @Override
            public void onResponse(@NonNull Call<Player> call, @NonNull Response<Player> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Player dailyPlayer = response.body();

                    View popupView = getLayoutInflater().inflate(R.layout.player_card_layout, null);
                    ImageView playerImage = popupView.findViewById(R.id.playerImage);
                    TextView playerName = popupView.findViewById(R.id.playerName);
                    TextView playerPosition = popupView.findViewById(R.id.playerPosition);
                    ImageView leagueImage = popupView.findViewById(R.id.leagueImage);
                    ImageView teamImage = popupView.findViewById(R.id.teamImage);
                    ImageView nationalityImage = popupView.findViewById(R.id.nationalityImage);

                    playerName.setText(dailyPlayer.getName());
                    playerPosition.setText(dailyPlayer.getPosition());

                    PlayerAdapter playerAdapter = new PlayerAdapter(new ArrayList<>());
                    playerAdapter.loadPlayerImage(playerImage, "http://10.0.2.2:8080" + dailyPlayer.getImageUrl());
                    playerAdapter.loadPlayerImage(leagueImage, "http://10.0.2.2:8080" + dailyPlayer.getLeagueImageUrl());
                    playerAdapter.loadPlayerImage(teamImage, "http://10.0.2.2:8080" + dailyPlayer.getTeamImageUrl());
                    playerAdapter.loadPlayerImage(nationalityImage, "http://10.0.2.2:8080" + dailyPlayer.getNationalityImageUrl());

                    new AlertDialog.Builder(UserActivity.this)
                            .setView(popupView)
                            .setPositiveButton("OK", (dialog, which) -> {
                                isDailyPlayerShown = true;
                                loadPlayers(userId, 0);
                            })
                            .show();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(UserActivity.this, response);
                    Log.e("FetchAndDisplayDailyPlayer", "Error: " + errorResponse.getMessage());
                    isDailyPlayerShown = true;
                    loadPlayers(userId, 0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Player> call, @NonNull Throwable t) {
                Log.e("FetchAndDisplayDailyPlayer", getString(R.string.network_error), t);
                isDailyPlayerShown = true;
                loadPlayers(userId, 0);
            }
        });
    }
}

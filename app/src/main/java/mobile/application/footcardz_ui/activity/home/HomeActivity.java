package mobile.application.footcardz_ui.activity.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

public class HomeActivity extends AppCompatActivity {

    private ApiService apiService;
    private TokenManager tokenManager;

    private PlayerAdapter playerAdapter;
    private int currentTabId = R.id.nav_all_players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = new TokenManager(this);
        int userId = Integer.parseInt(Objects.requireNonNull(JwtUtils.getSubject(tokenManager.getToken())));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        playerAdapter = new PlayerAdapter(new ArrayList<>());
        recyclerView.setAdapter(playerAdapter);

        SearchView searchViewAllPlayers = findViewById(R.id.searchViewAllPlayers);
        SearchView searchViewMyPlayers = findViewById(R.id.searchViewMyPlayers);

        searchViewAllPlayers.setVisibility(View.GONE);
        searchViewMyPlayers.setVisibility(View.VISIBLE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            currentTabId = item.getItemId();

            int itemId = item.getItemId();

            if (itemId == R.id.nav_all_players) {
                loadAllPlayers(0);
                return true;
            } else if (itemId == R.id.nav_user_players) {
                fetchAndDisplayDailyPlayer();
                loadPlayers(userId, 0);
                return true;
            } else if (itemId == R.id.nav_logout) {
                attemptLogout();
                return true;
            } else {
                return false;
            }
        });


        searchViewMyPlayers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlayersForUser(userId, query, 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadPlayers(userId, 0);
                } else {
                    searchPlayersForUser(userId, newText, 0);
                }
                return true;
            }
        });

        searchViewAllPlayers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (currentTabId == R.id.nav_all_players) {
                    searchAllPlayers(query, 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentTabId == R.id.nav_all_players) {
                    if (newText.isEmpty()) {
                        loadAllPlayers(0);
                    } else {
                        searchAllPlayers(newText, 0);
                    }
                }
                return true;
            }
        });


        loadAllPlayers(0);


    }

    private void loadAllPlayers(int page) {
        apiService.getAllPlayers(page, 30).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_all_players) {
                    Log.d("HomeActivity", "Ignoring all players load for inactive tab");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                    if (page < response.body().getPage().getTotalPages() - 1) {
                        loadAllPlayers(page + 1);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                if (currentTabId != R.id.nav_all_players) {
                    Log.d("HomeActivity", "Ignoring failure for inactive tab");
                    return;
                }
                Toast.makeText(HomeActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayers(int userId, int page) {
        apiService.getPlayers(userId, page, 100).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("HomeActivity", "Ignoring user players load for inactive tab");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("HomeActivity", "Ignoring failure for inactive tab");
                    return;
                }
                Toast.makeText(HomeActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPlayersForUser(int userId, String searchTerm, int page) {
        apiService.searchPlayersForUser(userId, searchTerm, page).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_user_players) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "No players found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("HomeActivity", "Ignoring search failure for inactive tab");
                    return;
                }
                Toast.makeText(HomeActivity.this, "Error searching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAllPlayers(String searchTerm, int page) {
        apiService.searchPlayers(searchTerm, page).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlayerResponse> call, @NonNull Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "No players found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlayerResponse> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Error during search", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogout() {
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                tokenManager.clearTokens();

                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(HomeActivity.this, response);
                    Log.e("Logout", "Error: " + errorResponse.getMessage());
                }

                redirectToLogin();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayDailyPlayer() {
        apiService.getDailyPlayer().enqueue(new Callback<Player>() {
            @Override
            public void onResponse(Call<Player> call, Response<Player> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Player dailyPlayer = response.body();

                    // Inflate the custom layout for the player card popup
                    View popupView = getLayoutInflater().inflate(R.layout.player_card_layout, null);
                    ImageView playerImage = popupView.findViewById(R.id.playerImage);
                    TextView playerName = popupView.findViewById(R.id.playerName);
                    TextView playerPosition = popupView.findViewById(R.id.playerPosition);
                    ImageView leagueImage = popupView.findViewById(R.id.leagueImage);
                    ImageView teamImage = popupView.findViewById(R.id.teamImage);
                    ImageView nationalityImage = popupView.findViewById(R.id.nationalityImage);

                    // Set the player details
                    playerName.setText(dailyPlayer.getName());
                    playerPosition.setText(dailyPlayer.getPosition());
                    Log.e("daily",dailyPlayer.toString());

                    // Load the images using the existing `loadPlayerImage` method
                    PlayerAdapter playerAdapter = new PlayerAdapter(new ArrayList<>());
                    playerAdapter.loadPlayerImage(playerImage, "http://10.0.2.2:8080" + dailyPlayer.getImageUrl());
                    playerAdapter.loadPlayerImage(leagueImage, "http://10.0.2.2:8080" + dailyPlayer.getLeagueImageUrl());
                    playerAdapter.loadPlayerImage(teamImage, "http://10.0.2.2:8080" + dailyPlayer.getTeamImageUrl());
                    playerAdapter.loadPlayerImage(nationalityImage, "http://10.0.2.2:8080" + dailyPlayer.getNationalityImageUrl());

                    // Show the popup using an AlertDialog
                    new AlertDialog.Builder(HomeActivity.this)
                            .setView(popupView)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Log.d("HomeActivity", "No daily player available or error occurred.");
                }
            }

            @Override
            public void onFailure(Call<Player> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching daily player", t);
            }
        });

    }



    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

package mobile.application.footcardz_ui.activity.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import mobile.application.footcardz_ui.MainActivity;
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

    private RecyclerView recyclerView;
    private PlayerAdapter playerAdapter;
    private int currentTabId = R.id.nav_all_players; // initial tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = new TokenManager(this);
        int userId = Integer.parseInt(Objects.requireNonNull(JwtUtils.getSubject(tokenManager.getToken())));
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        playerAdapter = new PlayerAdapter(new ArrayList<>());
        recyclerView.setAdapter(playerAdapter);

        SearchView searchViewAllPlayers = findViewById(R.id.searchViewAllPlayers);
        SearchView searchViewMyPlayers = findViewById(R.id.searchViewMyPlayers);

        searchViewAllPlayers.setVisibility(View.GONE);  // Commencez par cacher une des deux
        searchViewMyPlayers.setVisibility(View.VISIBLE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            currentTabId = item.getItemId();

            int itemId = item.getItemId();

            if (itemId == R.id.nav_all_players) {
                loadAllPlayers(0);
                return true;
            } else if (itemId == R.id.nav_user_players) {
                // fetchAndDisplayDailyPlayer();
                loadPlayers(userId, 0);
                return true;
            } else if (itemId == R.id.nav_logout) {
                attemptLogout();
                return true;
            } else {
                // Handle other cases if necessary
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
                if (currentTabId == R.id.nav_all_players) { // Assurez-vous que l'onglet All Players est actif
                    searchAllPlayers(query, 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentTabId == R.id.nav_all_players) { // Idem pour la modification du texte
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
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
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
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                if (currentTabId != R.id.nav_all_players) {
                    Log.d("HomeActivity", "Ignoring failure for inactive tab");
                    return;
                }
                Toast.makeText(HomeActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayers(int userId, int page) {
        apiService.getPlayers(userId, page, 30).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
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
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
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
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
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
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
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
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
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
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error during search", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogout() {
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearTokens();
                    Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    ErrorResponse errorResponse = ApiErrorUtils.parseError(HomeActivity.this, response);
                    Toast.makeText(HomeActivity.this, "Error: " + errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayerImage(ImageView imageView, String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("HomeActivity", "Image load failed for url: " + imageUrl, e);
                        return false;
                    }

                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }


    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

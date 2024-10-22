package mobile.application.footcardz_ui.util;

import android.content.Context;

import androidx.annotation.NonNull;

import mobile.application.footcardz_ui.service.ApiService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            Interceptor authInterceptor = getAuthInterceptor(context);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static @NonNull Interceptor getAuthInterceptor(Context context) {
        TokenManager tokenManager = new TokenManager(context);

        return chain -> {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder();

            String token = tokenManager.getToken();
            if (token != null)
                builder.addHeader("Authorization", "Bearer " + token);

            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        };
    }

    public static ApiService getApiService(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}

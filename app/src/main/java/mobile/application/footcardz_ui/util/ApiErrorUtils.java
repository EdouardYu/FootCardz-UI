package mobile.application.footcardz_ui.util;

import android.content.Context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Objects;

import mobile.application.footcardz_ui.model.ErrorResponse;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ApiErrorUtils {

    public static ErrorResponse parseError(Context context, Response<?> response) {
        Converter<ResponseBody, ErrorResponse> converter =
                RetrofitClient.getInstance(context)
                        .responseBodyConverter(ErrorResponse.class, new Annotation[0]);

        ErrorResponse errorResponse;

        try {
            errorResponse = converter.convert(Objects.requireNonNull(response.errorBody()));
        } catch (IOException | NullPointerException e) {
            return new ErrorResponse("An unknown error occurred");
        }

        return errorResponse;
    }
}

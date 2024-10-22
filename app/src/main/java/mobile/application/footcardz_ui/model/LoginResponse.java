package mobile.application.footcardz_ui.model;

public class LoginResponse {
    private final String bearer;
    private final String refresh;

    public LoginResponse(String bearer, String refresh) {
        this.bearer = bearer;
        this.refresh = refresh;
    }

    public String getBearer() {
        return bearer;
    }

    public String getRefresh() {
        return refresh;
    }
}

package mobile.application.footcardz_ui.model;

public class SignupRequest {
    private final String username;
    private final String email;
    private final String password;

    public SignupRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

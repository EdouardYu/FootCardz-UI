package mobile.application.footcardz_ui.model;

public class ConfirmPasswordRequest {
    private final String code;
    private final String email;
    private final String password;

    public ConfirmPasswordRequest(String code, String email, String password) {
        this.code = code;
        this.email = email;
        this.password = password;
    }
}

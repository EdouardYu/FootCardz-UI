package mobile.application.footcardz_ui.model;

public class ActivationRequest {
    private final String code;
    private final String email;

    public ActivationRequest(String code, String email) {
        this.code = code;
        this.email = email;
    }
}

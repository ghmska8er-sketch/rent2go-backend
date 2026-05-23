package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

public class CreateIntentResponse {
    private String clientSecret;
    private String id;

    public CreateIntentResponse() {}

    public CreateIntentResponse(String clientSecret, String id) {
        this.clientSecret = clientSecret;
        this.id = id;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

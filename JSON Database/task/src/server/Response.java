package server;

public class Response {
    private final String response;
    private final String reason;
    private final String value;

    public Response(String response, String reason, String value) {
        this.response = response;
        this.reason = reason;
        this.value = value;
    }

    public String getResponse() {
        return response;
    }

    public String getReason() {
        return reason;
    }

    public String getValue() {
        return value;
    }
}

package nl.kb.dare.http;

public class HttpResponseException extends Exception {

    private final int errorStatus;

    public HttpResponseException(String message, int errorStatus) {
        super(message);

        this.errorStatus = errorStatus;
    }


    public int getErrorStatus() {
        return errorStatus;
    }
}

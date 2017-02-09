package nl.kb.dare.http;

import nl.kb.dare.model.statuscodes.ErrorStatus;

public class HttpResponseException extends Exception {

    private final ErrorStatus errorStatus;

    public HttpResponseException(String message, ErrorStatus errorStatus) {
        super(message);

        this.errorStatus = errorStatus;
    }


    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }
}

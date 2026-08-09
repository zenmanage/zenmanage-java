package com.zenmanage.sdk.errors;

/**
 * Thrown when rules cannot be fetched from the API/CDN.
 */
public class FetchRulesException extends ZenmanageException {
    private final Integer statusCode;

    public FetchRulesException(String message) {
        super(message);
        this.statusCode = null;
    }

    public FetchRulesException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FetchRulesException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

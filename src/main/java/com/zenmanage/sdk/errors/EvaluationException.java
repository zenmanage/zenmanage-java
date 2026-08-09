package com.zenmanage.sdk.errors;

/**
 * Thrown when a flag cannot be evaluated.
 */
public class EvaluationException extends ZenmanageException {
    public EvaluationException(String message) {
        super(message);
    }
}

package com.nikialeksey.jood;

public class JbException extends Exception {
    public JbException(String message) {
        super(message);
    }

    public JbException(String message, Throwable cause) {
        super(message, cause);
    }
}

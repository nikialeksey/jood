package com.nikialeksey.jood;

public class JdException extends Exception {
    public JdException(String message) {
        super(message);
    }

    public JdException(String message, Throwable cause) {
        super(message, cause);
    }
}

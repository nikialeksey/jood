package com.nikialeksey.jood;

@SuppressWarnings("inheritancefree")
public class JdException extends Exception {
    public JdException(String message) {
        super(message);
    }

    public JdException(String message, Throwable cause) {
        super(message, cause);
    }
}

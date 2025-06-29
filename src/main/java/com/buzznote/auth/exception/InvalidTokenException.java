package com.buzznote.auth.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("UNAUTHORIZED");
    }

    public InvalidTokenException(String s) {
        super(s);
    }

}

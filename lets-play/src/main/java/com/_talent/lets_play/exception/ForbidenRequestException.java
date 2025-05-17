package com._talent.lets_play.exception;

public class ForbidenRequestException extends RuntimeException {
    public ForbidenRequestException(String message) {
        super(message);
    }
}

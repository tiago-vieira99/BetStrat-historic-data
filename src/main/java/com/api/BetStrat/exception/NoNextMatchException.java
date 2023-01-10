package com.api.BetStrat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoNextMatchException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public NoNextMatchException() {
        super();
    }

    public NoNextMatchException(String message) {
        super(message);
    }

    public NoNextMatchException(Throwable cause) {
        super(cause);
    }

    public NoNextMatchException(String message, Throwable cause) {
        super(message,cause);
    }

}

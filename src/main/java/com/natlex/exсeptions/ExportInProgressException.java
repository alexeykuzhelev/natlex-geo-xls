package com.natlex.exсeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class ExportInProgressException extends RuntimeException {

    public ExportInProgressException(String message) {
        super(message);
    }
}

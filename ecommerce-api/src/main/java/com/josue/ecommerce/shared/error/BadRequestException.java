package com.josue.ecommerce.shared.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(HttpStatus status, String title, String detail) {
        super(status, title, detail);
    }
}

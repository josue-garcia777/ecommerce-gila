package com.josue.ecommerce.shared.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String title, String detail) {
        super(HttpStatus.NOT_FOUND, title, detail);
    }
}

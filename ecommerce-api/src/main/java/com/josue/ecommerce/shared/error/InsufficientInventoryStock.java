package com.josue.ecommerce.shared.error;

import org.springframework.http.HttpStatus;

public class InsufficientInventoryStock extends ApiException {
    public InsufficientInventoryStock(String title, String detail) {
        super(HttpStatus.CONFLICT, title, detail);
    }
}

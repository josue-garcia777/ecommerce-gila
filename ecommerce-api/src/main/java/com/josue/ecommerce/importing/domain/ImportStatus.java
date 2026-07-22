package com.josue.ecommerce.importing.domain;

public enum ImportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED;

    public boolean terminal() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS || this == FAILED;
    }
}

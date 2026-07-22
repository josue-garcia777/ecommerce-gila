package com.josue.ecommerce.importing.dto;

public record RejectedRowResponse(int rowNumber, String sku, String reason) {
}

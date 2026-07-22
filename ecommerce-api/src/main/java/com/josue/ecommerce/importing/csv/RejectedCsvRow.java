package com.josue.ecommerce.importing.csv;

public record RejectedCsvRow(int rowNumber, String sku, String reason) {
}

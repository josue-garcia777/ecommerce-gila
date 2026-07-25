package com.josue.ecommerce.importing.csv.impl;

public record RejectedCsvRow(int rowNumber, String sku, String reason) {
}

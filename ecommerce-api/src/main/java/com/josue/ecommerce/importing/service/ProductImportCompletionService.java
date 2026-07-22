package com.josue.ecommerce.importing.service;

import com.josue.ecommerce.importing.csv.ProductCsvParseResult;

import java.util.UUID;

public interface ProductImportCompletionService {
    void completeImport(UUID importId, ProductCsvParseResult parseResult);
    void failImport(UUID importId);
}

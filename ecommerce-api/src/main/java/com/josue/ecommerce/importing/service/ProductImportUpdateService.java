package com.josue.ecommerce.importing.service;

import com.josue.ecommerce.importing.csv.impl.ProductCsvParseResult;

import java.util.UUID;

public interface ProductImportUpdateService {
    void buildAndUpsertImportsResults(UUID importId, ProductCsvParseResult parseResult);

    void failImport(UUID importId);
}

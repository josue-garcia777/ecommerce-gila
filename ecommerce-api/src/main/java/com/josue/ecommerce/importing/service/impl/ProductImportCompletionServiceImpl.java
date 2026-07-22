package com.josue.ecommerce.importing.service.impl;

import com.josue.ecommerce.importing.csv.ProductCsvParseResult;
import com.josue.ecommerce.importing.domain.ProductImport;
import com.josue.ecommerce.importing.domain.ProductImportError;
import com.josue.ecommerce.importing.repository.ProductImportErrorRepository;
import com.josue.ecommerce.importing.repository.ProductImportRepository;
import com.josue.ecommerce.importing.service.ProductImportCompletionService;
import com.josue.ecommerce.product.service.cmd.ProductImportResult;
import com.josue.ecommerce.product.service.ProductImportUpsertService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImportCompletionServiceImpl implements ProductImportCompletionService {

    private final ProductImportRepository productImportRepository;
    private final ProductImportErrorRepository errorRepository;
    private final ProductImportUpsertService productUpsertService;

    public ProductImportCompletionServiceImpl(ProductImportRepository productImportRepository,
                                          ProductImportErrorRepository errorRepository,
                                          ProductImportUpsertService productUpsertService) {
        this.productImportRepository = productImportRepository;
        this.errorRepository = errorRepository;
        this.productUpsertService = productUpsertService;
    }

    @Transactional
    public void completeImport(UUID importId, ProductCsvParseResult parseResult) {
        ProductImport productImport = productImportRepository.findById(importId)
                .orElseThrow(() -> new IllegalStateException("Processing import was not found"));

        ProductImportResult result = productUpsertService.upsert(parseResult.acceptedRows());

        errorRepository.saveAll(parseResult.rejectedRows().stream()
                .map(row -> new ProductImportError(
                        UUID.randomUUID(), importId, row.rowNumber(), row.sku(), row.reason()))
                .toList());

        productImport.complete(result.created(), result.updated(), parseResult.rejectedRows().size(), Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failImport(UUID importId) {
        ProductImport productImport = productImportRepository.findById(importId).orElse(null);
        if (productImport != null) {
            productImport.fail(Instant.now());
        }
    }
}

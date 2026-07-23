package com.josue.ecommerce.importing.service.impl;

import com.josue.ecommerce.importing.csv.ProductCsvParseResult;
import com.josue.ecommerce.importing.csv.ProductCsvParser;

import java.util.UUID;

import com.josue.ecommerce.importing.service.ProductImportUpdateService;
import com.josue.ecommerce.importing.service.ProductImportStart;
import com.josue.ecommerce.importing.service.ProductImportService;
import com.josue.ecommerce.importing.service.cmd.ImportWorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductImportProcessingServiceImpl implements ProductImportStart {

    private static final Logger log = LoggerFactory.getLogger(ProductImportStart.class);

    private final ProductImportService productImportService;
    private final ProductCsvParser csvParser;
    private final ProductImportUpdateService productCompleteService;

    public ProductImportProcessingServiceImpl(ProductImportService productImportService, ProductCsvParser csvParser,
                                              ProductImportUpdateService completionService) {
        this.productImportService = productImportService;
        this.csvParser = csvParser;
        this.productCompleteService = completionService;
    }

    @Override
    public void process(UUID importId) {
        try {
            ImportWorkItem workItem = productImportService.findImportItem(importId);

            ProductCsvParseResult result = csvParser.parse(workItem.content());

            productCompleteService.buildAndUpsertImportsResults(importId, result);
        } catch (Exception exception) {
            log.error("Product import {} failed", importId, exception);
            try {
                productCompleteService.failImport(importId);
            } catch (Exception failureException) {
                log.error("Product import {} could not be marked failed", importId, failureException);
            }
        }
    }
}

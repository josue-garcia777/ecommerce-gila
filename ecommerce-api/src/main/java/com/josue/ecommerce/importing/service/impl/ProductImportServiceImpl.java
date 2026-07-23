package com.josue.ecommerce.importing.service.impl;

import com.josue.ecommerce.importing.csv.ProductCsvParseResult;
import com.josue.ecommerce.importing.domain.ImportStatus;
import com.josue.ecommerce.importing.domain.ProductImport;
import com.josue.ecommerce.importing.domain.ProductImportError;
import com.josue.ecommerce.importing.dto.ImportStatusResponse;
import com.josue.ecommerce.importing.dto.ImportSummaryResponse;
import com.josue.ecommerce.importing.dto.RejectedRowResponse;
import com.josue.ecommerce.importing.repository.ImportMetadata;
import com.josue.ecommerce.importing.repository.ProductImportErrorRepository;
import com.josue.ecommerce.importing.repository.ProductImportRepository;
import com.josue.ecommerce.importing.repository.specification.ProductImportErrorSpecifications;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.josue.ecommerce.importing.service.ProductImportService;
import com.josue.ecommerce.importing.service.ProductImportUpdateService;
import com.josue.ecommerce.importing.service.cmd.ImportWorkItem;
import com.josue.ecommerce.product.service.ProductImportUpsertService;
import com.josue.ecommerce.product.service.cmd.ProductImportResult;
import com.josue.ecommerce.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImportServiceImpl implements ProductImportService, ProductImportUpdateService {

    private final ProductImportRepository productImportRepository;
    private final ProductImportErrorRepository errorRepository;
    private final ProductImportUpsertService productUpsertService;


    public ProductImportServiceImpl(ProductImportRepository productImportRepository,
                                    ProductImportErrorRepository errorRepository,
                                    ProductImportUpsertService productUpsertService) {
        this.productImportRepository = productImportRepository;
        this.errorRepository = errorRepository;
        this.productUpsertService = productUpsertService;
    }

    @Transactional
    @Override
    public ImportWorkItem findImportItem(UUID importId) {
        ProductImport productImport = productImportRepository.findById(importId)
                .orElseThrow(() -> new IllegalStateException("Submitted import was not found"));
        productImport.start();
        byte[] content = productImport.getFileContent();
        if (content == null) {
            throw new IllegalStateException("Submitted import has no file content");
        }
        return new ImportWorkItem(importId, content);
    }


    @Transactional(readOnly = true)
    public ImportStatusResponse getImportStatus(UUID importId) {

        ImportMetadata metadata = productImportRepository.findMetadataById(importId)
                .orElseThrow(() -> new NotFoundException("Import not found",
                        "No product import exists with the supplied ID"));

        boolean completedWithRows = metadata.getStatus() == ImportStatus.COMPLETED_WITH_ERRORS;

        List<RejectedRowResponse> rejectedRows = completedWithRows
                ? errorRepository.findAll(
                        ProductImportErrorSpecifications.forImport(importId), Sort.by("rowNumber")).stream()
                .map(error -> new RejectedRowResponse(
                        error.getRowNumber(), error.getSku(), error.getReason()))
                .toList()
                : List.of();

        boolean terminal = metadata.getStatus().terminal();

        ImportSummaryResponse summary = new ImportSummaryResponse(
                terminal ? metadata.getCreatedCount() : 0,
                terminal ? metadata.getUpdatedCount() : 0,
                terminal ? metadata.getRejectedCount() : 0
        );

        return new ImportStatusResponse(
                metadata.getId(), metadata.getStatus(), metadata.getFilename(), summary,
                metadata.getSubmittedAt(), metadata.getCompletedAt(), rejectedRows
        );
    }


    @Transactional
    @Override
    public void buildAndUpsertImportsResults(UUID importId, ProductCsvParseResult parseResult) {
        ProductImport productImport = productImportRepository.findById(importId)
                .orElseThrow(() -> new IllegalStateException("Processing import was not found"));

        ProductImportResult result = productUpsertService.upsert(parseResult.acceptedRows());

        List<ProductImportError> errors = parseResult.rejectedRows().stream()
                .map(row -> productImport.addError(row.rowNumber(), row.sku(), row.reason()))
                .toList();
        errorRepository.saveAll(errors);

        productImport.complete(result.created(), result.updated(), parseResult.rejectedRows().size(), Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void failImport(UUID importId) {
        ProductImport productImport = productImportRepository.findById(importId).orElse(null);
        if (productImport != null) {
            productImport.fail(Instant.now());
        }
    }

}

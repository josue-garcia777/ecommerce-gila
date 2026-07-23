package com.josue.ecommerce.importing.service.impl;

import com.josue.ecommerce.importing.csv.CsvHeaderValidator;
import com.josue.ecommerce.importing.domain.ProductImport;
import com.josue.ecommerce.importing.dto.ImportSubmissionResponse;
import com.josue.ecommerce.importing.event.ProductImportSubmitted;
import com.josue.ecommerce.importing.repository.ProductImportRepository;
import com.josue.ecommerce.importing.service.ProductImportSubmissionService;
import com.josue.ecommerce.shared.error.ApiException;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImportSubmissionServiceImpl implements ProductImportSubmissionService {

    private static final long MAXIMUM_BYTES = 5L * 1024L * 1024L;

    private final ProductImportRepository productImportRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CsvHeaderValidator csvHeaderValidator;

    public ProductImportSubmissionServiceImpl(ProductImportRepository productImportRepository,
                                              ApplicationEventPublisher eventPublisher,
                                              CsvHeaderValidator csvHeaderValidator) {
        this.productImportRepository = productImportRepository;
        this.eventPublisher = eventPublisher;
        this.csvHeaderValidator = csvHeaderValidator;
    }

    @Transactional
    @Override
    public ImportSubmissionResponse validateAndSubmitProducts(MultipartFile file) {
        validateFile(file);

        byte[] content = content(file);

        csvHeaderValidator.validate(content);

        String filename = safeFilename(file.getOriginalFilename());

        UUID importId = UUID.randomUUID();

        Instant submittedAt = Instant.now();

        ProductImport productImport = new ProductImport(importId, filename, content, submittedAt);

        productImportRepository.save(productImport);

        eventPublisher.publishEvent(new ProductImportSubmitted(importId));

        String statusUrl = "/api/v1/product-imports/" + importId;

        return new ImportSubmissionResponse(importId, productImport.getStatus(), submittedAt, statusUrl);
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid CSV file", "The CSV file is empty");
        }

        if (file.getSize() > MAXIMUM_BYTES) {
            throw new ApiException(HttpStatus.CONTENT_TOO_LARGE, "File too large",
                    "CSV files must not exceed 5 MB");
        }

        String filename = safeFilename(file.getOriginalFilename());

        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BadRequestException(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid CSV file",
                    "The uploaded filename must end with .csv");
        }
    }

    private byte[] content(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Unreadable upload", "The uploaded file could not be read");
        }
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid CSV file",
                    "The uploaded file must have a filename");
        }
        String normalized = filename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (basename.isEmpty() || basename.length() > 255) {
            throw new BadRequestException(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid CSV file",
                    "The uploaded filename is invalid");
        }
        return basename;
    }
}

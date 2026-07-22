package com.josue.ecommerce.importing.controller;

import com.josue.ecommerce.importing.dto.ImportStatusResponse;
import com.josue.ecommerce.importing.dto.ImportSubmissionResponse;
import com.josue.ecommerce.importing.service.ProductImportService;
import com.josue.ecommerce.importing.service.ProductImportSubmissionService;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/product-imports")
public class ProductImportController {

    private final ProductImportSubmissionService submissionService;
    private final ProductImportService statusService;

    public ProductImportController(ProductImportSubmissionService submissionService,
                                   ProductImportService statusService) {
        this.submissionService = submissionService;
        this.statusService = statusService;
    }

    @PostMapping
    ResponseEntity<ImportSubmissionResponse> submit(@RequestPart("file") MultipartFile file) {
        ImportSubmissionResponse response = submissionService.validateAndSubmitProducts(file);
        return ResponseEntity.accepted()
                .location(URI.create(response.statusUrl()))
                .body(response);
    }

    @GetMapping("/{importId}")
    ImportStatusResponse status(@PathVariable UUID importId) {
        return statusService.getImportStatus(importId);
    }
}

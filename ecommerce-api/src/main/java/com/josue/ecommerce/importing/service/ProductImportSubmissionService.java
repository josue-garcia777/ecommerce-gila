package com.josue.ecommerce.importing.service;

import com.josue.ecommerce.importing.dto.ImportSubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportSubmissionService {
    ImportSubmissionResponse validateAndSubmitFile(MultipartFile file);
}

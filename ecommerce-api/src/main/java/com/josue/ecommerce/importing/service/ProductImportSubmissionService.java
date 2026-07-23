package com.josue.ecommerce.importing.service;

import com.josue.ecommerce.importing.dto.ImportSubmissionResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportSubmissionService {
    ImportSubmissionResponse validateAndSubmitProducts(MultipartFile file);
}

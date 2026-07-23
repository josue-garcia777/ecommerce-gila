package com.josue.ecommerce.importing.event;

import com.josue.ecommerce.importing.service.ProductImportStart;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductImportEventListener {

    private final ProductImportStart processingService;

    public ProductImportEventListener(ProductImportStart processingService) {
        this.processingService = processingService;
    }

    @Async("productImportExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(ProductImportSubmitted event) {
        processingService.process(event.importId());
    }
}

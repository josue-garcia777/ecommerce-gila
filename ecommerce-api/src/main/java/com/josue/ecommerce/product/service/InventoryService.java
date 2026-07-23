package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.service.cmd.ProductDetails;
import java.util.Map;
import java.util.UUID;

public interface InventoryService {
    Map<UUID, ProductDetails> decrementInventoryAndLoad(Map<UUID, Integer> quantitiesByProductId);
}

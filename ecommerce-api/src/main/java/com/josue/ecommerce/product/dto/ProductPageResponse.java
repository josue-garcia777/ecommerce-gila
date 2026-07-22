package com.josue.ecommerce.product.dto;

import java.util.List;

public record ProductPageResponse(List<ProductResponse> items, String nextCursor, boolean hasMore) {
}

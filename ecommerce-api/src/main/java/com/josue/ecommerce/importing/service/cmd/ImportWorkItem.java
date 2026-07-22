package com.josue.ecommerce.importing.service.cmd;

import java.util.UUID;

public record ImportWorkItem(UUID importId, byte[] content) {
}

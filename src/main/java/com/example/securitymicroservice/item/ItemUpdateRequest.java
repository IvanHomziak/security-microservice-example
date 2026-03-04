package com.example.securitymicroservice.item;

import jakarta.validation.constraints.Size;

public record ItemUpdateRequest(
        @Size(max = 120) String name,
        String description
) {
}

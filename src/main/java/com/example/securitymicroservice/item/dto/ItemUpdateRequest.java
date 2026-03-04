package com.example.securitymicroservice.item.dto;

import jakarta.validation.constraints.Size;

public record ItemUpdateRequest(
        @Size(max = 120) String name,
        String description
) {
}

package com.example.securitymicroservice.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request payload for creating an item. */
public record ItemCreateRequest(
        @NotBlank @Size(max = 120) String name,
        String description
) {
}

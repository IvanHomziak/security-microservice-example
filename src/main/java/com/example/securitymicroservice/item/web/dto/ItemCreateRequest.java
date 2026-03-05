package com.example.securitymicroservice.item.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request payload for creating an item. */
public record ItemCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description
) {
}

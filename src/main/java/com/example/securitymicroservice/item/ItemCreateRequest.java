package com.example.securitymicroservice.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(
        @NotBlank @Size(max = 120) String name,
        String description
) {
}

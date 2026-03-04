package com.example.securitymicroservice.item.dto;

import jakarta.validation.constraints.Size;

/** Request payload for partial/full item updates. */
public record ItemUpdateRequest(
        @Size(max = 120) String name,
        String description
) {
}

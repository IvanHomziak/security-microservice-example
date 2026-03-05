package com.example.securitymicroservice.item.dto;

import jakarta.validation.constraints.Size;

/** Request payload for partial/full item updates. */
public record ItemUpdateRequest(
        @Size(min = 1, max = 120) String name,
        @Size(max = 1000) String description
) {
}

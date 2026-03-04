package com.example.securitymicroservice.item.dto;

import com.example.securitymicroservice.item.entity.Item;

import java.time.LocalDateTime;

/** Response payload for an item entity. */
public record ItemResponse(Long id, String name, String description, LocalDateTime updatedAt) {

    /** Maps JPA item entity to API response DTO. */
    public static ItemResponse fromEntity(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getDescription(), item.getUpdatedAt());
    }
}

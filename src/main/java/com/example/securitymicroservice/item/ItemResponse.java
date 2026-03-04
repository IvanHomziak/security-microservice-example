package com.example.securitymicroservice.item;

import java.time.LocalDateTime;

public record ItemResponse(Long id, String name, String description, LocalDateTime updatedAt) {
    public static ItemResponse fromEntity(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getDescription(), item.getUpdatedAt());
    }
}

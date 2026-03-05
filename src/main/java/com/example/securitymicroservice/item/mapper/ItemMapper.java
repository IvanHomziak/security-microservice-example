package com.example.securitymicroservice.item.mapper;

import com.example.securitymicroservice.item.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.dto.ItemResponse;
import com.example.securitymicroservice.item.dto.ItemUpdateRequest;
import com.example.securitymicroservice.item.entity.Item;
import org.springframework.stereotype.Component;

/** Maps item DTOs to entities and back. */
@Component
public class ItemMapper {

    public Item toEntity(ItemCreateRequest request) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        return item;
    }

    public void applyUpdates(Item item, ItemUpdateRequest request) {
        if (request.name() != null) {
            item.setName(request.name());
        }
        if (request.description() != null) {
            item.setDescription(request.description());
        }
    }

    public ItemResponse toResponse(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getDescription(), item.getUpdatedAt());
    }
}

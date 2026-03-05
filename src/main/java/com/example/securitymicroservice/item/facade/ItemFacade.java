package com.example.securitymicroservice.item.facade;

import com.example.securitymicroservice.item.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.dto.ItemResponse;
import com.example.securitymicroservice.item.dto.ItemUpdateRequest;
import com.example.securitymicroservice.item.entity.Item;
import com.example.securitymicroservice.item.mapper.ItemMapper;
import com.example.securitymicroservice.item.service.ItemService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

/** Facade that orchestrates item use-cases and DTO mapping. */
@Component
public class ItemFacade {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemFacade(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    public List<ItemResponse> list() {
        return itemService.list().stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    public ItemResponse get(Long id) {
        return itemMapper.toResponse(itemService.get(id));
    }

    public ItemResponse create(ItemCreateRequest request) {
        Item item = itemMapper.toEntity(request);
        return itemMapper.toResponse(itemService.create(item));
    }

    public ItemResponse replace(Long id, ItemCreateRequest request) {
        Item item = itemMapper.toEntity(request);
        return itemMapper.toResponse(itemService.update(id, item));
    }

    public ItemResponse patch(Long id, ItemUpdateRequest request) {
        Item existing = itemService.get(id);
        itemMapper.applyUpdates(existing, request);
        return itemMapper.toResponse(itemService.update(id, existing));
    }

    public void delete(Long id) {
        itemService.delete(id);
    }
}

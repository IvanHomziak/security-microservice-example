package com.example.securitymicroservice.item.service;

import com.example.securitymicroservice.item.entity.Item;
import com.example.securitymicroservice.item.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.dto.ItemResponse;
import com.example.securitymicroservice.item.dto.ItemUpdateRequest;
import com.example.securitymicroservice.item.repository.ItemRepository;
import com.example.securitymicroservice.item.exception.ItemNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
/** Business service for CRUD operations over items with permission checks. */
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Returns all items available to the caller. */
    @PreAuthorize("hasAuthority('DATA_READ')")
    @Transactional(readOnly = true)
    public List<ItemResponse> list() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    /** Returns a single item by identifier. */
    @PreAuthorize("hasAuthority('DATA_READ')")
    @Transactional(readOnly = true)
    public ItemResponse get(Long id) {
        return ItemResponse.fromEntity(findById(id));
    }

    /** Creates a new item. */
    @PreAuthorize("hasAuthority('DATA_CREATE')")
    public ItemResponse create(ItemCreateRequest request) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    /** Updates an existing item using partial update semantics for name. */
    @PreAuthorize("hasAuthority('DATA_UPDATE')")
    public ItemResponse update(Long id, ItemUpdateRequest request) {
        Item item = findById(id);
        if (request.name() != null) {
            item.setName(request.name());
        }
        item.setDescription(request.description());
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    /** Deletes an item by identifier. */
    @PreAuthorize("hasAuthority('DATA_DELETE')")
    public void delete(Long id) {
        Item item = findById(id);
        itemRepository.delete(item);
    }

    private Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}

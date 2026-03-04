package com.example.securitymicroservice.item.service;

import com.example.securitymicroservice.item.entity.Item;
import com.example.securitymicroservice.item.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.dto.ItemResponse;
import com.example.securitymicroservice.item.dto.ItemUpdateRequest;
import com.example.securitymicroservice.item.repository.ItemRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @PreAuthorize("hasAuthority('DATA_READ')")
    @Transactional(readOnly = true)
    public List<ItemResponse> list() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    @PreAuthorize("hasAuthority('DATA_READ')")
    @Transactional(readOnly = true)
    public ItemResponse get(Long id) {
        return ItemResponse.fromEntity(findById(id));
    }

    @PreAuthorize("hasAuthority('DATA_CREATE')")
    public ItemResponse create(ItemCreateRequest request) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @PreAuthorize("hasAuthority('DATA_UPDATE')")
    public ItemResponse update(Long id, ItemUpdateRequest request) {
        Item item = findById(id);
        if (request.name() != null) {
            item.setName(request.name());
        }
        item.setDescription(request.description());
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @PreAuthorize("hasAuthority('DATA_DELETE')")
    public void delete(Long id) {
        Item item = findById(id);
        itemRepository.delete(item);
    }

    private Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found: " + id));
    }
}

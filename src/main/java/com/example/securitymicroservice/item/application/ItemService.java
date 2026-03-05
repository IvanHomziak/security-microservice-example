package com.example.securitymicroservice.item.application;

import com.example.securitymicroservice.item.domain.Item;
import com.example.securitymicroservice.item.domain.ItemNotFoundException;
import com.example.securitymicroservice.item.infrastructure.ItemRepository;
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
    public List<Item> list() {
        return itemRepository.findAll();
    }

    /** Returns a single item by identifier. */
    @PreAuthorize("hasAuthority('DATA_READ')")
    @Transactional(readOnly = true)
    public Item get(Long id) {
        return findById(id);
    }

    /** Creates a new item. */
    @PreAuthorize("hasAuthority('DATA_CREATE')")
    public Item create(Item item) {
        return itemRepository.save(item);
    }

    /** Updates an existing item. */
    @PreAuthorize("hasAuthority('DATA_UPDATE')")
    public Item update(Long id, Item updatedItem) {
        Item existing = findById(id);
        existing.setName(updatedItem.getName());
        existing.setDescription(updatedItem.getDescription());
        return itemRepository.save(existing);
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

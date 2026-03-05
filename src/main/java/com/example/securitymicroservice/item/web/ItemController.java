package com.example.securitymicroservice.item.web;

import com.example.securitymicroservice.item.web.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.web.dto.ItemResponse;
import com.example.securitymicroservice.item.web.dto.ItemUpdateRequest;
import com.example.securitymicroservice.item.application.ItemFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/items")
/** REST controller for item CRUD endpoints. */
public class ItemController {

    private final ItemFacade itemFacade;

    public ItemController(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    /** Lists all items. */
    @GetMapping
    public ResponseEntity<List<ItemResponse>> list() {
        return ResponseEntity.ok(itemFacade.list());
    }

    /** Returns item by id. */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(itemFacade.get(id));
    }

    /** Creates a new item. */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER') ")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemCreateRequest request) {
        return ResponseEntity.status(CREATED).body(itemFacade.create(request));
    }

    /** Replaces fields of an item using PUT request payload. */
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> put(@PathVariable Long id, @Valid @RequestBody ItemCreateRequest request) {
        return ResponseEntity.ok(itemFacade.replace(id, request));
    }

    /** Partially updates an item. */
    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> patch(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest request) {
        return ResponseEntity.ok(itemFacade.patch(id, request));
    }

    /** Deletes an item. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}

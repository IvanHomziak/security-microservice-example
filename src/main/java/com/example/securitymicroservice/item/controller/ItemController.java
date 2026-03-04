package com.example.securitymicroservice.item.controller;

import com.example.securitymicroservice.item.service.ItemService;
import com.example.securitymicroservice.item.dto.ItemCreateRequest;
import com.example.securitymicroservice.item.dto.ItemResponse;
import com.example.securitymicroservice.item.dto.ItemUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemResponse> list() {
        return itemService.list();
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) {
        return itemService.get(id);
    }

    @PostMapping
    public ItemResponse create(@Valid @RequestBody ItemCreateRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    public ItemResponse put(@PathVariable Long id, @Valid @RequestBody ItemCreateRequest request) {
        return itemService.update(id, new ItemUpdateRequest(request.name(), request.description()));
    }

    @PatchMapping("/{id}")
    public ItemResponse patch(@PathVariable Long id, @RequestBody ItemUpdateRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}

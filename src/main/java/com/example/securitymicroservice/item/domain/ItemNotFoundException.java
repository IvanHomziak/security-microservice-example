package com.example.securitymicroservice.item.domain;

/** Business exception for missing item entity. */
public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item not found: " + id);
    }
}

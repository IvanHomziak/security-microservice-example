package com.example.securitymicroservice.item.repository;

import com.example.securitymicroservice.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

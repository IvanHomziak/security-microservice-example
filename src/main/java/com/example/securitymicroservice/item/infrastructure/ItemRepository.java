package com.example.securitymicroservice.item.infrastructure;

import com.example.securitymicroservice.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

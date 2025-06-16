package com.example.repository;

import com.example.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObjectRepository extends JpaRepository<Item, Long> {
    List<Item> findByNameContaining(String name);
} 
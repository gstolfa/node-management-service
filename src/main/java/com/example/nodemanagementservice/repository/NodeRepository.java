package com.example.nodemanagementservice.repository;

import com.example.nodemanagementservice.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {
    Optional<Node> findByName(String name);
}

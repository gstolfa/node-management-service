package com.example.nodemanagementservice.repository;

import com.example.nodemanagementservice.entity.NodeRelationship;
import com.example.nodemanagementservice.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NodeRelationshipRepository extends JpaRepository<NodeRelationship, Long> {

    @Query("SELECT r FROM NodeRelationship r WHERE r.ancestor = :ancestor")
    List<NodeRelationship> findByAncestor(Node ancestor);

    @Query("SELECT r FROM NodeRelationship r WHERE r.descendant = :descendant")
    List<NodeRelationship> findByDescendant(Node descendant);

    @Query("SELECT r FROM NodeRelationship r WHERE r.ancestor = :ancestor ORDER BY r.id ASC")
    List<NodeRelationship> findByAncestorOrderedById(Node ancestor);

    @Query("SELECT r FROM NodeRelationship r WHERE r.descendant = :descendant AND r.depth = 1")
    Optional<NodeRelationship> findByDescendantWithDepthOne(Node descendant);

    void deleteByDescendant(Node descendant);
    void deleteByAncestor(Node ancestor);
    void deleteByAncestorAndDescendant(Node ancestor, Node descendant);

}

package com.example.nodemanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "node_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", nullable = false)
    private Node ancestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", nullable = false)
    private Node descendant;

    @Column(nullable = false)
    private int depth; // 0 for direct children, > 0 for indirect relationships
}
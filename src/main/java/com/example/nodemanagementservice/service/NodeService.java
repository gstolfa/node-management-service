package com.example.nodemanagementservice.service;

import com.example.nodemanagementservice.dto.ChildNodeRequest;
import com.example.nodemanagementservice.entity.Node;

import java.util.List;

public interface NodeService {
    Node addChild(String parentName, ChildNodeRequest request);
    boolean deleteChild(String parentName, String childName);
    void moveNode(String nodeId, String newParentId);
    List<String> getDescendants(String ancestorName);
}
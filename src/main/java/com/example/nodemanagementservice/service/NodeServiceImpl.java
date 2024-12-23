package com.example.nodemanagementservice.service;

import com.example.nodemanagementservice.dto.ChildNodeRequest;
import com.example.nodemanagementservice.entity.Node;
import com.example.nodemanagementservice.entity.NodeRelationship;
import com.example.nodemanagementservice.exception.NodeAlreadyExistsException;
import com.example.nodemanagementservice.exception.ResourceNotFoundException;
import com.example.nodemanagementservice.repository.NodeRelationshipRepository;
import com.example.nodemanagementservice.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for managing nodes and their relationships in a node management system.
 * Provides methods to add, delete, move, and retrieve descendants of nodes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final NodeRelationshipRepository relationshipRepository;

    /**
     * Adds a child node under a specified parent node.
     *
     * @param parentName the name of the parent node
     * @param request the details of the child node to be added
     * @return the newly created child node
     * @throws ResourceNotFoundException if the parent node does not exist
     * @throws NodeAlreadyExistsException if the child node already exists
     */
    @Transactional(rollbackFor = {ResourceNotFoundException.class, NodeAlreadyExistsException.class})
    @Override
    public Node addChild(String parentName, ChildNodeRequest request) {
        log.info("Adding child node '{}' under parent '{}'", request.getChildName(), parentName);

        // Retrieve the parent node, throwing an exception if it doesn't exist.
        var parentNode = findNodeByName(parentName);

        // Create a new child node or throw an exception if it already exists.
        var newChildNode = createNodeOrThrowIfAlreadyExists(request.getChildName());

        // Establish the relationship between the child node and its parent/ancestors.
        addRelationshipChildToAncestors(newChildNode, parentNode);

        log.info("Successfully added child node '{}' under parent '{}'", newChildNode.getName(), parentNode.getName());
        return newChildNode;
    }

    /**
     * Deletes a child node from a specified parent node and all its descendants.
     *
     * @param parentName the name of the parent node
     * @param childName the name of the child node to be deleted
     * @return true if the deletion was successful
     * @throws ResourceNotFoundException if the parent or child node does not exist
     */
    @Transactional(rollbackFor = ResourceNotFoundException.class)
    @Override
    public boolean deleteChild(String parentName, String childName) {
        log.info("Deleting child node '{}' from parent '{}'", childName, parentName);

        // Retrieve parent and child nodes, throwing exceptions if they don't exist.
        var parentNode = findNodeByName(parentName);
        var childNode = findNodeByName(childName);

        // Remove the direct relationship between parent and child.
        relationshipRepository.deleteByAncestorAndDescendant(parentNode, childNode);
        log.debug("Deleted direct relationship between parent '{}' and child '{}'", parentName, childName);

        // Prepare for cascading deletion of the child node and its descendants.
        List<Node> nodesToDelete = new ArrayList<>();
        nodesToDelete.add(childNode);
        while (!nodesToDelete.isEmpty()) {
            Node currentNode = nodesToDelete.remove(nodesToDelete.size() - 1);
            log.debug("Processing node '{}' for deletion", currentNode.getName());

            // Retrieve all descendants of the current node and mark them for deletion.
            List<NodeRelationship> descendants = relationshipRepository.findByAncestor(currentNode);
            descendants.forEach(descendantRel -> nodesToDelete.add(descendantRel.getDescendant()));

            // Delete relationships and the current node itself.
            relationshipRepository.deleteByAncestor(currentNode);
            relationshipRepository.deleteByDescendant(currentNode);
            nodeRepository.delete(currentNode);
            log.debug("Deleted node '{}' and its relationships", currentNode.getName());
        }

        log.info("Successfully deleted child node '{}' and all its descendants", childName);
        return true;
    }

    /**
     * Moves a child node to a new parent node.
     *
     * @param childName the name of the child node to be moved
     * @param newParentName the name of the new parent node
     * @throws NodeAlreadyExistsException if the child node is already under the new parent
     * @throws ResourceNotFoundException if the child or parent node does not exist
     */
    @Transactional(rollbackFor = {NodeAlreadyExistsException.class, ResourceNotFoundException.class})
    @Override
    public void moveNode(String childName, String newParentName) {
        log.info("Moving child node '{}' to new parent '{}'", childName, newParentName);

        // Retrieve the child node and the new parent node, throwing exceptions if necessary.
        var childNode = findNodeByName(childName);
        var newParentNode = findNodeByName(newParentName);

        // Ensure the child isn't already under the same parent.
        checkOrThrowIfSameParent(childNode, newParentNode);

        // Update the parent relationship.
        moveToDirectParent(childNode, newParentNode);
        log.info("Successfully moved child node '{}' to new parent '{}'", childName, newParentName);
    }

    /**
     * Retrieves the descendants of a specified ancestor node.
     *
     * @param ancestorName the name of the ancestor node
     * @return a list of names of the descendant nodes
     * @throws ResourceNotFoundException if the ancestor node does not exist
     */
    @Transactional(readOnly = true)
    @Override
    public List<String> getDescendants(String ancestorName) {
        log.info("Retrieving descendants for ancestor '{}'", ancestorName);

        // Retrieve the ancestor node, throwing an exception if it doesn't exist.
        var ancestorNode = findNodeByName(ancestorName);

        // Fetch all descendants and map them to their names.
        List<NodeRelationship> relationships = relationshipRepository.findByAncestor(ancestorNode);
        List<String> descendants = relationships.stream()
                .map(r -> r.getDescendant().getName())
                .toList();

        log.info("Found {} descendants for ancestor '{}'", descendants.size(), ancestorName);
        return descendants;
    }

    /**
     * Establishes a relationship between a child node and its parent/ancestors.
     *
     * @param childNode the child node
     * @param parentNode the parent node
     */
    private void addRelationshipChildToAncestors(Node childNode, Node parentNode) {
        log.debug("Adding relationship between child '{}' and parent '{}'", childNode.getName(), parentNode.getName());

        // Create and save the direct relationship.
        NodeRelationship directRelationship = NodeRelationship.builder()
                .ancestor(parentNode)
                .descendant(childNode)
                .depth(1)
                .build();
        relationshipRepository.save(directRelationship);
        log.trace("Saved direct relationship between '{}' and '{}'", parentNode.getName(), childNode.getName());

        // Propagate the relationship to the parent's ancestors.
        List<NodeRelationship> parentAncestors = relationshipRepository.findByDescendant(parentNode);
        for (NodeRelationship ancestorRel : parentAncestors) {
            NodeRelationship ancestorRelationship = NodeRelationship.builder()
                    .ancestor(ancestorRel.getAncestor())
                    .descendant(childNode)
                    .depth(ancestorRel.getDepth() + 1)
                    .build();
            relationshipRepository.save(ancestorRelationship);
            log.trace("Saved ancestor relationship between '{}' and '{}'", ancestorRel.getAncestor().getName(), childNode.getName());
        }

        log.debug("Successfully added relationships for child '{}' with parent '{}' and its ancestors", childNode.getName(), parentNode.getName());
    }

    /**
     * Moves a node to a new direct parent and updates its relationships with descendants.
     *
     * @param childNode the child node to be moved
     * @param newParentNode the new parent node
     */
    private void moveToDirectParent(Node childNode, Node newParentNode) {
        log.debug("Moving node '{}' to new direct parent '{}'", childNode.getName(), newParentNode.getName());

        // Remove existing relationships for the child node.
        relationshipRepository.deleteByDescendant(childNode);
        log.trace("Deleted existing relationships for node '{}'", childNode.getName());

        // Add new relationships with the new parent and its ancestors.
        addRelationshipChildToAncestors(childNode, newParentNode);

        // Update relationships for the descendants of the child node.
        List<Node> nodesToProcess = new ArrayList<>();
        nodesToProcess.add(childNode);
        while (!nodesToProcess.isEmpty()) {
            Node currentNode = nodesToProcess.remove(0);
            log.trace("Processing node '{}' for descendant relationship update", currentNode.getName());

            // Retrieve and update descendant relationships.
            List<NodeRelationship> descendantRelationships = relationshipRepository.findByAncestorOrderedById(currentNode);
            for (NodeRelationship descendantRel : descendantRelationships) {
                Node descendantNode = descendantRel.getDescendant();
                var directParentRel = relationshipRepository.findByDescendantWithDepthOne(descendantNode);

                if (directParentRel.isPresent()) {
                    Node currentParentNode = directParentRel.get().getAncestor();
                    relationshipRepository.deleteByDescendant(descendantNode);
                    addRelationshipChildToAncestors(descendantNode, currentParentNode);
                    nodesToProcess.add(descendantNode);
                }
            }
        }

        log.debug("Successfully moved node '{}' under new parent '{}'", childNode.getName(), newParentNode.getName());
    }

    /**
     * Finds a node by its name.
     *
     * @param nodeName the name of the node to be found
     * @return the node with the specified name
     * @throws ResourceNotFoundException if the node is not found
     */
    private Node findNodeByName(String nodeName) {
        log.debug("Searching for node with name '{}'", nodeName);
        return nodeRepository.findByName(nodeName).orElseThrow(
                () -> {
                    log.error("Node with name '{}' not found", nodeName);
                    return new ResourceNotFoundException("Node", "name", nodeName);
                });
    }

    /**
     * Checks if the child node is already under the same parent and throws an exception if so.
     *
     * @param childNode the child node
     * @param newParentNode the new parent node
     * @throws NodeAlreadyExistsException if the child node is already under the new parent
     */
    private void checkOrThrowIfSameParent(Node childNode, Node newParentNode) {
        var currentParentNode = relationshipRepository.findByDescendantWithDepthOne(childNode).get().getAncestor();
        if (newParentNode.getName().equals(currentParentNode.getName())) {
            log.warn("Node '{}' is already under parent '{}'", childNode.getName(), newParentNode.getName());
            throw new NodeAlreadyExistsException("You are trying to move the node to the same parent.");
        }
    }

    /**
     * Creates a new node or throws an exception if a node with the same name already exists.
     *
     * @param childName the name of the child node to be created
     * @return the newly created child node
     * @throws NodeAlreadyExistsException if the node already exists
     */
    private Node createNodeOrThrowIfAlreadyExists(String childName) {
        log.debug("Creating node with name '{}'", childName);
        // Check if the node already exists, throwing an exception if it does.
        if (nodeRepository.findByName(childName).isPresent()) {
            log.error("Node with name '{}' already exists", childName);
            throw new NodeAlreadyExistsException("Node already registered with given name " + childName);
        }
        // Create and save the new node.
        var newChildNode = nodeRepository.save(Node.builder().name(childName).build());
        log.debug("Successfully created node with name '{}'", newChildNode.getName());
        return newChildNode;
    }
}

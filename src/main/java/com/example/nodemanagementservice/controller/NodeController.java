package com.example.nodemanagementservice.controller;

import com.example.nodemanagementservice.constants.NodeManagementConstants;
import com.example.nodemanagementservice.dto.ChildNodeRequest;
import com.example.nodemanagementservice.dto.ErrorResponse;
import com.example.nodemanagementservice.dto.NodeResponse;
import com.example.nodemanagementservice.exception.NodeAlreadyExistsException;
import com.example.nodemanagementservice.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/nodes")
@AllArgsConstructor
public class NodeController {

    private final NodeService nodeService;

    @Operation(
            summary = "Add a new child node to the given parent node",
            description = "This API allows you to add a new child node under a specified parent node. If successful, a 201 Created response will be returned.",
            tags = {"Node Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED: The child node was successfully added",
                    content = @Content(
                            schema = @Schema(implementation = NodeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "HTTP Status Not Found: ResourceNotFoundException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "HTTP Status Bad Request: NodeAlreadyExistsException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error: An unexpected error occurred",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping(value = "/{parentName}/children", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<NodeResponse> addChildNode(@PathVariable String parentName, @RequestBody ChildNodeRequest childNodeRequest) {
        var createdNode = nodeService.addChild(parentName, childNodeRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new NodeResponse(NodeManagementConstants.STATUS_201, NodeManagementConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Delete a child node under a specified parent node",
            description = "This API allows you to delete a specified child node under the given parent node.",
            tags = {"Node Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK: The child node was successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "HTTP Status Not Found: ResourceNotFoundException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error: An unexpected error occurred while deleting the node",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping(value = "/{parentName}/children/{childName}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<NodeResponse> deleteChildNode(@PathVariable String parentName, @PathVariable String childName) {
        boolean isDeleted = nodeService.deleteChild(parentName, childName);
        if(isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new NodeResponse(NodeManagementConstants.STATUS_200, NodeManagementConstants.MESSAGE_200));
        }else{
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new NodeResponse(NodeManagementConstants.STATUS_417, NodeManagementConstants.MESSAGE_417_DELETE));
        }
    }

    @Operation(
            summary = "Move a node to a new parent",
            description = "This API moves a specified node to a new parent node. It validates that the new parent exists and is different from the current parent.",
            tags = {"Node Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK: The node was successfully moved to the new parent"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "HTTP Status Not Found: ResourceNotFoundException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error: An unexpected error occurred while moving the node",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping(value = "/{nodeName}/parent/{parentName}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<NodeResponse> moveNodeToNewParent(@PathVariable String nodeName, @PathVariable String parentName) {
        nodeService.moveNode(nodeName, parentName);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new NodeResponse(NodeManagementConstants.STATUS_200, NodeManagementConstants.MESSAGE_200));
    }

    @Operation(
            summary = "Get all descendants of a node",
            description = "This API retrieves a list of all descendant node names for a given node.",
            tags = {"Node Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK: The descendants of the node were successfully retrieved",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "HTTP Status Not Found: ResourceNotFoundException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error: An unexpected error occurred while retrieving descendants",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping(value = "/{nodeName}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<String>> getDescendants(@PathVariable String nodeName) {
        List<String> responses = nodeService.getDescendants(nodeName);
        return ResponseEntity.ok(responses);
    }
}

package com.example.nodemanagementservice.controller;

import com.example.nodemanagementservice.dto.ChildNodeRequest;
import com.example.nodemanagementservice.entity.Node;
import com.example.nodemanagementservice.repository.NodeRelationshipRepository;
import com.example.nodemanagementservice.repository.NodeRepository;
import com.example.nodemanagementservice.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private NodeRelationshipRepository nodeRelationshipRepository;

    @Autowired
    private NodeService nodeService;

    @BeforeEach
    void setUp() {
        nodeRelationshipRepository.deleteAll();
        nodeRepository.deleteAll();
        nodeRepository.save(Node.builder().name("root-test").build());
        nodeService.addChild("root-test", ChildNodeRequest.builder().childName("childNode").build());
    }

    // ----- ADD CHILD NODE TESTS -----
    @Test
    void testAddChildNode_Success() throws Exception {
        String jsonPayload = """
            {
                "childName": "A"
            }
            """;

        mockMvc.perform(post("/api/nodes/root-test/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201"))
                .andExpect(jsonPath("$.statusMsg").value("Node created and added successfully"));
    }

    @Test
    void testAddChildNode_Failure_NodeAlreadyExists() throws Exception {
        String jsonPayload = """
            {
                "childName": "childNode"
            }
            """;

        mockMvc.perform(post("/api/nodes/root-test/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errorMessage").value("Node already registered with given name childNode"));
    }

    // ----- DELETE CHILD NODE TESTS -----
    @Test
    void testDeleteChildNode_Success() throws Exception {
        mockMvc.perform(delete("/api/nodes/root-test/children/childNode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.statusMsg").value("Request processed successfully"));
    }

    @Test
    void testDeleteChildNode_Failure_NodeNotFound() throws Exception {
        mockMvc.perform(delete("/api/nodes/root-test/children/nonExistentNode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Node not found with the given input data name : 'nonExistentNode'"));
    }

    // ----- MOVE CHILD NODE TESTS -----
    @Test
    void testMoveNodeToNewParent_Success() throws Exception {
        nodeService.addChild("root-test", ChildNodeRequest.builder().childName("newParentNode").build());

        mockMvc.perform(put("/api/nodes/childNode/parent/newParentNode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.statusMsg").value("Request processed successfully"));
    }

    @Test
    void testMoveNodeToNewParent_Failure_NodeNotFound() throws Exception {
        mockMvc.perform(put("/api/nodes/nonExistentNode/parent/newParentNode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Node not found with the given input data name : 'nonExistentNode'"));
    }

    @Test
    void testMoveNodeToNewParent_Failure_NewParentNotFound() throws Exception {
        mockMvc.perform(put("/api/nodes/childNode/parent/nonExistentParent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Node not found with the given input data name : 'nonExistentParent'"));
    }

    // ----- GET DESCENDANTS TESTS -----
    @Test
    void testGetDescendants_Success() throws Exception {
        nodeService.addChild("childNode", ChildNodeRequest.builder().childName("A").build());
        nodeService.addChild("childNode", ChildNodeRequest.builder().childName("B").build());

        mockMvc.perform(get("/api/nodes/root-test/descendants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("childNode"))
                .andExpect(jsonPath("$[1]").value("A"))
                .andExpect(jsonPath("$[2]").value("B"));
    }

    @Test
    void testGetDescendants_NodeNotFound() throws Exception {
        mockMvc.perform(get("/api/nodes/nonExistentNode/descendants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Node not found with the given input data name : 'nonExistentNode'"));
    }
}
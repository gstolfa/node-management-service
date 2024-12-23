package com.example.nodemanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Child Node",
        description = "Schema to hold Child Node information"
)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChildNodeRequest {
    @NotEmpty(message = "Name can not be a null or empty")
    @Schema(
            description = "Name of Child Node", example = "A"
    )
    private String childName;
}
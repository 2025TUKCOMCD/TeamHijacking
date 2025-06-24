package com.example.back.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedRouteResponseDTO {
    private boolean success;
    private String message;
    private String savedRouteName; // 이미 존재하는 경우 해당 이름 반환
    private boolean alreadyExists; // Flag to indicate if the route already existed

}

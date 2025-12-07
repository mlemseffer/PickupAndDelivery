package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentsRequest {
    private List<Assignment> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assignment {
        private String demandId;
        /**
         * Peut être null ou vide pour "non assigné".
         */
        private String courierId;
    }
}


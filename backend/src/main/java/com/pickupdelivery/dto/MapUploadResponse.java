package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'upload de fichier carte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapUploadResponse {
    private int nodeCount;
    private int segmentCount;
    private String mapName;
}

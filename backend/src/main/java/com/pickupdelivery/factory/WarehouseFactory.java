package com.pickupdelivery.factory;

import com.pickupdelivery.model.Warehouse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Factory pour la création d'instances de Warehouse avec validation
 * 
 * Gère la création et la validation des entrepôts, notamment
 * la validation du format de l'heure de départ.
 */
public class WarehouseFactory {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:m:s");
    
    /**
     * Crée un nouvel entrepôt après validation des paramètres
     * 
     * @param id L'identifiant unique de l'entrepôt
     * @param nodeId L'identifiant du nœud où se trouve l'entrepôt
     * @param departureTime L'heure de départ (format "H:m:s" ex: "8:0:0")
     * @return Une nouvelle instance de Warehouse valide
     * @throws IllegalArgumentException Si un paramètre est invalide
     */
    public static Warehouse createWarehouse(String id, String nodeId, String departureTime) {
        validateId(id);
        validateNodeId(nodeId);
        validateDepartureTime(departureTime);
        
        return new Warehouse(id, nodeId, departureTime);
    }
    
    /**
     * Valide l'identifiant de l'entrepôt
     * 
     * @param id L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID est null ou vide
     */
    private static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant de l'entrepôt ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide l'identifiant du nœud
     * 
     * @param nodeId L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID du nœud est null ou vide
     */
    private static void validateNodeId(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud de l'entrepôt ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide le format de l'heure de départ
     * 
     * @param departureTime L'heure à valider (format "H:m:s")
     * @throws IllegalArgumentException Si le format est invalide
     */
    private static void validateDepartureTime(String departureTime) {
        if (departureTime == null || departureTime.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'heure de départ ne peut pas être null ou vide"
            );
        }
        
        try {
            LocalTime.parse(departureTime, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                String.format("Format d'heure invalide : %s (format attendu : H:m:s, exemple : 8:0:0)", departureTime),
                e
            );
        }
    }
}

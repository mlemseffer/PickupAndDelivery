package com.pickupdelivery.factory;

import com.pickupdelivery.model.Node;

/**
 * Factory pour la création d'instances de Node avec validation
 * 
 * Ce pattern Factory permet d'isoler la logique de création et de validation
 * des nœuds, conformément au principe de responsabilité unique (SRP).
 */
public class NodeFactory {
    
    /**
     * Crée un nouveau nœud après validation des paramètres
     * 
     * @param id L'identifiant unique du nœud
     * @param latitude La latitude du nœud (doit être entre -90 et 90)
     * @param longitude La longitude du nœud (doit être entre -180 et 180)
     * @return Une nouvelle instance de Node valide
     * @throws IllegalArgumentException Si un paramètre est invalide
     */
    public static Node createNode(String id, double latitude, double longitude) {
        validateId(id);
        validateCoordinates(latitude, longitude);
        return new Node(id, latitude, longitude);
    }
    
    /**
     * Valide l'identifiant du nœud
     * 
     * @param id L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID est null ou vide
     */
    private static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide les coordonnées géographiques
     * 
     * @param latitude La latitude à valider
     * @param longitude La longitude à valider
     * @throws IllegalArgumentException Si les coordonnées sont invalides
     */
    private static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                String.format("Latitude invalide : %.6f (doit être entre -90 et 90)", latitude)
            );
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                String.format("Longitude invalide : %.6f (doit être entre -180 et 180)", longitude)
            );
        }
    }
}

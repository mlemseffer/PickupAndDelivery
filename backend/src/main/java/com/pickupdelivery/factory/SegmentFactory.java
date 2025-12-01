package com.pickupdelivery.factory;

import com.pickupdelivery.model.Segment;

/**
 * Factory pour la création d'instances de Segment avec validation
 * 
 * Gère la logique de création et de validation des segments de route,
 * en assurant l'intégrité des données conformément aux exigences métier.
 */
public class SegmentFactory {
    
    /**
     * Crée un nouveau segment après validation des paramètres
     * 
     * @param origin L'identifiant du nœud d'origine
     * @param destination L'identifiant du nœud de destination
     * @param length La longueur du segment en mètres (doit être positive)
     * @param name Le nom de la rue
     * @return Une nouvelle instance de Segment valide
     * @throws IllegalArgumentException Si un paramètre est invalide
     */
    public static Segment createSegment(String origin, String destination, 
                                       double length, String name) {
        validateOrigin(origin);
        validateDestination(destination);
        validateLength(length);
        // Le nom de rue est optionnel, pas besoin de le valider
        validateDifferentNodes(origin, destination);
        
        return new Segment(origin, destination, length, name);
    }
    
    /**
     * Valide l'identifiant du nœud d'origine
     * 
     * @param origin L'identifiant à valider
     * @throws IllegalArgumentException Si l'origine est null ou vide
     */
    private static void validateOrigin(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud d'origine ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide l'identifiant du nœud de destination
     * 
     * @param destination L'identifiant à valider
     * @throws IllegalArgumentException Si la destination est null ou vide
     */
    private static void validateDestination(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud de destination ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide la longueur du segment
     * 
     * @param length La longueur à valider
     * @throws IllegalArgumentException Si la longueur n'est pas strictement positive
     */
    private static void validateLength(double length) {
        if (length <= 0.0) {
            throw new IllegalArgumentException(
                String.format("La longueur du segment doit être strictement positive (reçu : %.2f)", length)
            );
        }
        
        if (Double.isNaN(length) || Double.isInfinite(length)) {
            throw new IllegalArgumentException(
                "La longueur du segment doit être un nombre fini valide"
            );
        }
    }
    
    /**
     * SUPPRIMÉ : La validation du nom de rue n'est plus nécessaire
     * car le nom de rue est un attribut optionnel dans le modèle
     */
    
    /**
     * Valide que les nœuds d'origine et de destination sont différents
     * 
     * @param origin Le nœud d'origine
     * @param destination Le nœud de destination
     * @throws IllegalArgumentException Si les deux nœuds sont identiques
     */
    private static void validateDifferentNodes(String origin, String destination) {
        if (origin.equals(destination)) {
            throw new IllegalArgumentException(
                String.format("Le nœud d'origine et de destination ne peuvent pas être identiques (%s)", origin)
            );
        }
    }
}

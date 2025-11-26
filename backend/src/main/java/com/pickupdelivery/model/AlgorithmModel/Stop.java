package com.pickupdelivery.model.AlgorithmModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Optional;

/**
 * Représente un stop (noeud de pickup, delivery ou warehouse)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    
    public enum TypeStop {
        PICKUP,
        DELIVERY,
        WAREHOUSE
    }
    
    private String idNode;
    // Null pour les entrepôts (warehouse) qui n'ont pas d'idDemande
    private String idDemande;
    private TypeStop typeStop;

    /**
     * Retourne l'ID de la demande sous forme d'Optional
     * @return Optional contenant l'ID de la demande, ou Optional.empty() si null (warehouse)
     */
    public Optional<String> getIdDemandeOptional() {
        return Optional.ofNullable(idDemande);
    }

    /**
     * Implémentation personnalisée de equals pour permettre l'utilisation de Stop comme clé de HashMap
     * Deux stops sont égaux s'ils ont le même idNode, typeStop et idDemande
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return Objects.equals(idNode, stop.idNode) &&
               Objects.equals(idDemande, stop.idDemande) &&
               typeStop == stop.typeStop;
    }

    /**
     * Implémentation personnalisée de hashCode cohérente avec equals
     */
    @Override
    public int hashCode() {
        return Objects.hash(idNode, idDemande, typeStop);
    }
}

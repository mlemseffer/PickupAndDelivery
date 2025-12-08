package com.pickupdelivery.service;

import com.pickupdelivery.exception.ValidationException;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.model.DemandeSet;
import com.pickupdelivery.model.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de validation pour les demandes de livraison
 * Vérifie que tous les nœuds existent dans la carte chargée
 */
@Service
public class ValidationService {

    /**
     * Valide qu'un ensemble de demandes de livraison est compatible avec la carte chargée
     * 
     * @param requestSet L'ensemble des demandes à valider
     * @param cityMap La carte de la ville
     * @throws ValidationException Si des nœuds n'existent pas dans la carte
     */
    public void validateDeliveryRequests(DemandeSet requestSet, CityMap cityMap) {
        if (requestSet == null) {
            throw new ValidationException("L'ensemble de demandes est null");
        }
        
        if (cityMap == null || cityMap.getNodes() == null || cityMap.getNodes().isEmpty()) {
            throw new ValidationException("Aucune carte n'est chargée. Veuillez d'abord charger un plan.");
        }

        // Créer un Set des IDs de nœuds disponibles
        Set<String> availableNodeIds = cityMap.getNodes().stream()
            .map(Node::getId)
            .collect(Collectors.toSet());
            
        List<String> missingNodes = new ArrayList<>();

        // Valider le nœud de l'entrepôt
        if (requestSet.getWarehouse() != null) {
            String warehouseNodeId = requestSet.getWarehouse().getNodeId();
            if (warehouseNodeId != null && !availableNodeIds.contains(warehouseNodeId)) {
                missingNodes.add("Entrepôt (nœud: " + warehouseNodeId + ")");
            }
        }

        // Valider les nœuds de chaque demande
        if (requestSet.getDemands() != null) {
            for (int i = 0; i < requestSet.getDemands().size(); i++) {
                Demand demand = requestSet.getDemands().get(i);
                int demandNumber = i + 1;

                // Vérifier le nœud de pickup
                if (demand.getPickupNodeId() != null && !availableNodeIds.contains(demand.getPickupNodeId())) {
                    missingNodes.add("Demande #" + demandNumber + " - Pickup (nœud: " + demand.getPickupNodeId() + ")");
                }

                // Vérifier le nœud de delivery
                if (demand.getDeliveryNodeId() != null && !availableNodeIds.contains(demand.getDeliveryNodeId())) {
                    missingNodes.add("Demande #" + demandNumber + " - Delivery (nœud: " + demand.getDeliveryNodeId() + ")");
                }
            }
        }

        // Si des nœuds manquent, lever une exception avec le détail
        if (!missingNodes.isEmpty()) {
            String errorMessage = String.format(
                "❌ Impossible de charger les demandes : %d nœud(s) n'existent pas dans le plan chargé.\n\n" +
                "Nœuds manquants :\n%s\n\n" +
                "💡 Solution : Chargez un plan plus grand (ex: moyenPlan.xml ou grandPlan.xml) qui contient ces nœuds.",
                missingNodes.size(),
                String.join("\n", missingNodes.stream()
                    .limit(10) // Limiter à 10 pour ne pas surcharger le message
                    .collect(Collectors.toList()))
            );
            
            if (missingNodes.size() > 10) {
                errorMessage += String.format("\n... et %d autre(s) nœud(s)", missingNodes.size() - 10);
            }
            
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Compte le nombre de demandes valides (dont tous les nœuds existent)
     * 
     * @param requestSet L'ensemble des demandes
     * @param cityMap La carte de la ville
     * @return Le nombre de demandes valides
     */
    public int countValidDemands(DemandeSet requestSet, CityMap cityMap) {
        if (requestSet == null || requestSet.getDemands() == null || cityMap == null) {
            return 0;
        }

        Set<String> availableNodeIds = cityMap.getNodes().stream()
            .map(Node::getId)
            .collect(Collectors.toSet());
        
        return (int) requestSet.getDemands().stream()
            .filter(demand -> 
                availableNodeIds.contains(demand.getPickupNodeId()) &&
                availableNodeIds.contains(demand.getDeliveryNodeId())
            )
            .count();
    }

    /**
     * Filtre les demandes pour ne garder que celles dont les nœuds existent
     * 
     * @param requestSet L'ensemble des demandes
     * @param cityMap La carte de la ville
     * @return Un nouvel ensemble avec uniquement les demandes valides
     */
    public DemandeSet filterValidDemands(DemandeSet requestSet, CityMap cityMap) {
        if (requestSet == null || cityMap == null) {
            return requestSet;
        }

        Set<String> availableNodeIds = cityMap.getNodes().stream()
            .map(Node::getId)
            .collect(Collectors.toSet());
        
        // Valider l'entrepôt
        boolean warehouseValid = requestSet.getWarehouse() == null || 
            availableNodeIds.contains(requestSet.getWarehouse().getNodeId());

        // Filtrer les demandes valides
        List<Demand> validDemands = requestSet.getDemands().stream()
            .filter(demand -> 
                availableNodeIds.contains(demand.getPickupNodeId()) &&
                availableNodeIds.contains(demand.getDeliveryNodeId())
            )
            .collect(Collectors.toList());

        DemandeSet filtered = new DemandeSet();
        filtered.setWarehouse(warehouseValid ? requestSet.getWarehouse() : null);
        filtered.setDemands(validDemands);
        
        return filtered;
    }
}

package com.pickupdelivery.service;

import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.xmlparser.DeliveryRequestXmlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les demandes de livraison
 * Contient la logique métier pour le traitement des livraisons
 */
@Service
public class DeliveryService {

    private List<DeliveryRequest> currentRequests = new ArrayList<>();
    private DeliveryRequestSet currentRequestSet;
    
    @Autowired
    private DeliveryRequestXmlParser deliveryRequestXmlParser;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private MapService mapService;

    /**
     * Parse un fichier XML contenant les demandes de livraison
     * @param file Le fichier XML uploadé
     * @return La liste des demandes de livraison
     * @throws Exception Si le parsing échoue
     */
    public List<DeliveryRequest> parseDeliveryRequestsFromXML(MultipartFile file) throws Exception {
        // Déléguer le parsing au DeliveryRequestXmlParser
        List<DeliveryRequest> requests = deliveryRequestXmlParser.parseDeliveryRequestsFromXML(file);
        this.currentRequests = requests;
        return requests;
    }

    /**
     * Récupère les demandes de livraison actuelles
     * @return La liste des demandes de livraison
     */
    public List<DeliveryRequest> getCurrentRequests() {
        return currentRequests;
    }

    /**
     * Ajoute une nouvelle demande de livraison
     * @param request La demande à ajouter
     */
    public void addDeliveryRequest(DeliveryRequest request) {
        // Ajout à la liste simple
        currentRequests.add(request);
        // Ajout à la tournée courante (currentRequestSet)
        if (currentRequestSet != null) {
            // Génère un id UUID si absent
            if (request.getId() == null || request.getId().isEmpty()) {
                request.setId(java.util.UUID.randomUUID().toString());
            }
            // Convertit en Demand si besoin
            com.pickupdelivery.model.Demand demand = new com.pickupdelivery.model.Demand();
            demand.setId(request.getId());
            demand.setPickupNodeId(request.getPickupAddress());
            demand.setDeliveryNodeId(request.getDeliveryAddress());
            demand.setPickupDurationSec(request.getPickupDuration());
            demand.setDeliveryDurationSec(request.getDeliveryDuration());
            demand.setCourierId(null);
            currentRequestSet.getDemands().add(demand);
        }
    }

    /**
     * Réinitialise les demandes de livraison
     */
    public void clearRequests() {
        this.currentRequests.clear();
        this.currentRequestSet = null;
    }

    /**
     * Charge un ensemble de demandes de livraison depuis un fichier XML
     * Valide que tous les nœuds existent dans la carte chargée
     * @param file Le fichier XML contenant les demandes
     * @return L'ensemble des demandes avec l'entrepôt
     * @throws Exception Si le parsing échoue ou si des nœuds sont manquants
     */
    public DeliveryRequestSet loadDeliveryRequests(MultipartFile file) throws Exception {
        // Parser le fichier XML
        DeliveryRequestSet requestSet = deliveryRequestXmlParser.parseDeliveryRequestFromXML(file);
        
        // Valider que tous les nœuds existent dans la carte chargée
        validationService.validateDeliveryRequests(requestSet, mapService.getCurrentMap());
        
        // Si validation OK, sauvegarder
        this.currentRequestSet = requestSet;
        return requestSet;
    }

    /**
     * Récupère l'ensemble des demandes actuelles
     * @return L'ensemble des demandes avec l'entrepôt
     */
    public DeliveryRequestSet getCurrentRequestSet() {
        return currentRequestSet;
    }

    /**
     * Supprime une demande de livraison par son id
     * @param deliveryId L'id de la demande à supprimer
     * @return L'ensemble des demandes mis à jour
     * @throws IllegalStateException Si la demande n'existe pas
     */
    public DeliveryRequestSet removeDemand(String deliveryId) {
    if (currentRequestSet == null || currentRequestSet.getDemands() == null) {
        throw new IllegalStateException("Aucune demande à supprimer");
    }

    System.out.println("[SUPPRESSION] id reçu: " + deliveryId);
    System.out.println("[SUPPRESSION] ids présents: ");
    for (com.pickupdelivery.model.Demand d : currentRequestSet.getDemands()) {
        System.out.println("  - " + d.getId());
    }

    boolean removed = currentRequestSet.getDemands().removeIf(d -> d.getId().equals(deliveryId));

    if (!removed) {
        throw new IllegalStateException("Livraison introuvable : " + deliveryId);
    }

    return currentRequestSet;
}

}

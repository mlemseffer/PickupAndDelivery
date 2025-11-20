package com.pickupdelivery.service;

import com.pickupdelivery.model.DeliveryRequest;
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
    
    @Autowired
    private DeliveryRequestXmlParser deliveryRequestXmlParser;

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
        currentRequests.add(request);
    }

    /**
     * Réinitialise les demandes de livraison
     */
    public void clearRequests() {
        this.currentRequests.clear();
    }
}

package com.pickupdelivery.service;

import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.Tour;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les demandes de livraison
 * Contient la logique métier pour le traitement des livraisons
 */
@Service
public class DeliveryService {

    private List<DeliveryRequest> currentRequests = new ArrayList<>();

    /**
     * Parse un fichier XML contenant les demandes de livraison
     * @param file Le fichier XML uploadé
     * @return La liste des demandes de livraison
     * @throws Exception Si le parsing échoue
     */
    public List<DeliveryRequest> parseDeliveryRequestsFromXML(MultipartFile file) throws Exception {
        List<DeliveryRequest> requests = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Parse les livraisons
            NodeList deliveryList = document.getElementsByTagName("livraison");
            for (int i = 0; i < deliveryList.getLength(); i++) {
                Element element = (Element) deliveryList.item(i);
                String pickupAddress = element.getAttribute("adresseEnlevement");
                String deliveryAddress = element.getAttribute("adresseLivraison");
                int pickupDuration = Integer.parseInt(element.getAttribute("dureeEnlevement"));
                int deliveryDuration = Integer.parseInt(element.getAttribute("dureeLivraison"));
                
                requests.add(new DeliveryRequest(pickupAddress, deliveryAddress, pickupDuration, deliveryDuration));
            }
        }

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

package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.DeliveryRequest;
import org.springframework.stereotype.Component;
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
 * Parser XML pour les fichiers de demandes de livraison
 * Responsable de la lecture et de l'interprétation des fichiers XML contenant les demandes de livraison
 */
@Component
public class DeliveryRequestXmlParser {

    /**
     * Parse un fichier XML contenant les demandes de livraison
     * @param file Le fichier XML uploadé
     * @return La liste des demandes de livraison parsées
     * @throws Exception Si le parsing échoue
     */
    public List<DeliveryRequest> parseDeliveryRequestsFromXML(MultipartFile file) throws Exception {
        List<DeliveryRequest> deliveryRequests = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Parse les demandes de livraison
            NodeList requestList = document.getElementsByTagName("demande");
            for (int i = 0; i < requestList.getLength(); i++) {
                Element element = (Element) requestList.item(i);
                
                String pickupAddress = element.getAttribute("adresseEnlevement");
                String deliveryAddress = element.getAttribute("adresseLivraison");
                int pickupDuration = Integer.parseInt(element.getAttribute("dureeEnlevement"));
                int deliveryDuration = Integer.parseInt(element.getAttribute("dureeLivraison"));
                
                deliveryRequests.add(new DeliveryRequest(
                    pickupAddress, 
                    deliveryAddress, 
                    pickupDuration, 
                    deliveryDuration
                ));
            }
        }

        return deliveryRequests;
    }
}

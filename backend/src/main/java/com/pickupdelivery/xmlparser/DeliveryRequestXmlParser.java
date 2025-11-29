package com.pickupdelivery.xmlparser;

import com.pickupdelivery.factory.DemandFactory;
import com.pickupdelivery.factory.WarehouseFactory;
import com.pickupdelivery.model.*;
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
import java.util.UUID;

/**
 * Parser XML pour les fichiers de demandes de livraison
 * Responsable de la lecture et de l'interprétation des fichiers XML contenant les demandes de livraison
 */
@Component
public class DeliveryRequestXmlParser {

    /**
     * Parse un fichier XML contenant les demandes de livraison
     * @param file Le fichier XML uploadé
     * @return L'ensemble des demandes avec l'entrepôt
     * @throws Exception Si le parsing échoue
     */
    public DeliveryRequestSet parseDeliveryRequestFromXML(MultipartFile file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Vérifier que c'est bien un fichier de demandes de livraison
            String rootElement = document.getDocumentElement().getNodeName();
            if (!"demandeDeLivraisons".equals(rootElement)) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : le fichier doit être une demande de livraison.\n\n" +
                    "Format attendu : <demandeDeLivraisons>\n" +
                    "Format détecté : <" + rootElement + ">\n\n" +
                    "💡 Astuce : Vous avez peut-être chargé un plan (carte) au lieu d'une demande de livraison.\n" +
                    "   • Pour charger un plan : utilisez l'icône 🏠 (Charger Plan)\n" +
                    "   • Pour charger une demande : utilisez l'icône 🚴 (Charger Demandes)"
                );
            }

            DeliveryRequestSet requestSet = new DeliveryRequestSet();
            
            // Parser l'entrepôt
            NodeList entrepotList = document.getElementsByTagName("entrepot");
            if (entrepotList.getLength() == 0) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : aucun élément <entrepot> trouvé.\n\n" +
                    "Le fichier doit contenir un entrepôt avec l'attribut 'adresse'."
                );
            }
            
            Element entrepotElement = (Element) entrepotList.item(0);
            String adresseEntrepot = entrepotElement.getAttribute("adresse");
            if (adresseEntrepot == null || adresseEntrepot.isEmpty()) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : l'entrepôt doit avoir un attribut 'adresse'."
                );
            }
            
            // Utilisation de WarehouseFactory pour créer et valider l'entrepôt
            String heureDepart = entrepotElement.getAttribute("heureDepart");
            if (heureDepart == null || heureDepart.isEmpty()) {
                heureDepart = "8:0:0"; // Valeur par défaut
            }
            
            try {
                Warehouse warehouse = WarehouseFactory.createWarehouse(
                    UUID.randomUUID().toString(),
                    adresseEntrepot,
                    heureDepart
                );
                requestSet.setWarehouse(warehouse);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "❌ Validation de l'entrepôt échouée : " + e.getMessage()
                );
            }

            // Parser les demandes de livraison
            NodeList livraisonList = document.getElementsByTagName("livraison");
            
            if (livraisonList.getLength() == 0) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : aucune demande de livraison trouvée.\n\n" +
                    "Le fichier doit contenir au moins un élément <livraison>."
                );
            }
            
            List<Demand> demands = new ArrayList<>();
            
            for (int i = 0; i < livraisonList.getLength(); i++) {
                Element livraisonElement = (Element) livraisonList.item(i);
                
                // Valider les attributs requis
                String adresseEnlevement = livraisonElement.getAttribute("adresseEnlevement");
                String adresseLivraison = livraisonElement.getAttribute("adresseLivraison");
                String dureeEnlevement = livraisonElement.getAttribute("dureeEnlevement");
                String dureeLivraison = livraisonElement.getAttribute("dureeLivraison");
                
                if (adresseEnlevement.isEmpty() || adresseLivraison.isEmpty() || 
                    dureeEnlevement.isEmpty() || dureeLivraison.isEmpty()) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : la livraison #" + (i + 1) + " est incomplète.\n\n" +
                        "Chaque <livraison> doit avoir les attributs :\n" +
                        "  • adresseEnlevement\n" +
                        "  • adresseLivraison\n" +
                        "  • dureeEnlevement\n" +
                        "  • dureeLivraison"
                    );
                }
                
                try {
                    int pickupDuration = Integer.parseInt(dureeEnlevement);
                    int deliveryDuration = Integer.parseInt(dureeLivraison);
                    
                    Demand demand = DemandFactory.createDemand(
                        UUID.randomUUID().toString(),
                        adresseEnlevement,
                        adresseLivraison,
                        pickupDuration,
                        deliveryDuration,
                        null // courierId
                    );
                    
                    demands.add(demand);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : les durées de la livraison #" + (i + 1) + 
                        " doivent être des nombres entiers."
                    );
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                        "❌ Validation de la livraison #" + (i + 1) + " échouée : " + e.getMessage()
                    );
                }
            }
            
            requestSet.setDemands(demands);
            return requestSet;
        }
    }

    /**
     * Ancienne méthode maintenue pour compatibilité
     * @deprecated Utiliser parseDeliveryRequestFromXML à la place
     */
    @Deprecated
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

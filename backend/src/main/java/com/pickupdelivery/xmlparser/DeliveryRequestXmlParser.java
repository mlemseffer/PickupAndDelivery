package com.pickupdelivery.xmlparser;

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

    // Palette de couleurs pour différencier les demandes
    private static final String[] COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
        "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B739", "#52B788",
        "#E63946", "#A8DADC", "#457B9D", "#F4A261", "#2A9D8F",
        "#E76F51", "#264653", "#E9C46A", "#F4A259", "#BC4B51"
    };

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
            
            Warehouse warehouse = new Warehouse();
            warehouse.setId(UUID.randomUUID().toString());
            warehouse.setNodeId(adresseEntrepot);
            warehouse.setDepartureTime(entrepotElement.getAttribute("heureDepart"));
            requestSet.setWarehouse(warehouse);

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
                
                Demand demand = new Demand();
                demand.setId(UUID.randomUUID().toString());
                demand.setPickupNodeId(adresseEnlevement);
                demand.setDeliveryNodeId(adresseLivraison);
                
                try {
                    demand.setPickupDurationSec(Integer.parseInt(dureeEnlevement));
                    demand.setDeliveryDurationSec(Integer.parseInt(dureeLivraison));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : les durées de la livraison #" + (i + 1) + 
                        " doivent être des nombres entiers."
                    );
                }
                
                demand.setCourierId(null);
                
                // Assigner une couleur cyclique
                demand.setColor(COLORS[i % COLORS.length]);
                
                demands.add(demand);
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

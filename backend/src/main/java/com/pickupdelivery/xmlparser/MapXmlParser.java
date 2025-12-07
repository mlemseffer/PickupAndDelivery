package com.pickupdelivery.xmlparser;

import com.pickupdelivery.factory.NodeFactory;
import com.pickupdelivery.factory.SegmentFactory;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.Node;
import com.pickupdelivery.model.Segment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * Parser XML pour les fichiers de carte
 * Responsable de la lecture et de l'interprétation des fichiers XML contenant les plans de ville
 */
@Component
public class MapXmlParser {

    /**
     * Parse un fichier XML contenant les données de la carte
     * @param file Le fichier XML uploadé
     * @return La carte parsée avec tous ses nœuds et segments
     * @throws Exception Si le parsing échoue
     */
    public CityMap parseMapFromXML(MultipartFile file) throws Exception {
        CityMap map = new CityMap();
        int skippedSegmentsCount = 0;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Vérifier que c'est bien un fichier de plan (carte)
            String rootElement = document.getDocumentElement().getNodeName();
            if (!"reseau".equals(rootElement)) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : le fichier doit être un plan de ville.\n\n" +
                    "Format attendu : <reseau>\n" +
                    "Format détecté : <" + rootElement + ">\n\n" +
                    "💡 Astuce : Vous avez peut-être chargé une demande de livraison au lieu d'un plan.\n" +
                    "   • Pour charger un plan : utilisez l'icône 🏠 (Charger Plan)\n" +
                    "   • Pour charger une demande : utilisez l'icône 🚴 (Charger Demandes)"
                );
            }

            // Parse les nœuds (intersections)
            NodeList nodeList = document.getElementsByTagName("noeud");
            
            if (nodeList.getLength() == 0) {
                throw new IllegalArgumentException(
                    "❌ Format XML incorrect : aucun nœud trouvé.\n\n" +
                    "Le fichier doit contenir au moins un élément <noeud> avec les attributs :\n" +
                    "  • id\n" +
                    "  • latitude\n" +
                    "  • longitude"
                );
            }
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String id = element.getAttribute("id");
                String latStr = element.getAttribute("latitude");
                String lonStr = element.getAttribute("longitude");
                
                if (id.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : le nœud #" + (i + 1) + " est incomplet.\n\n" +
                        "Chaque <noeud> doit avoir les attributs :\n" +
                        "  • id\n" +
                        "  • latitude\n" +
                        "  • longitude"
                    );
                }
                
                try {
                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);
                    // Utilisation de NodeFactory pour créer et valider le nœud
                    Node node = NodeFactory.createNode(id, latitude, longitude);
                    map.getNodes().add(node);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : les coordonnées du nœud #" + (i + 1) + 
                        " doivent être des nombres décimaux."
                    );
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                        "❌ Validation du nœud #" + (i + 1) + " échouée : " + e.getMessage()
                    );
                }
            }

            // Parse les segments (tronçons de rue)
            NodeList segmentList = document.getElementsByTagName("troncon");
            
            for (int i = 0; i < segmentList.getLength(); i++) {
                Element element = (Element) segmentList.item(i);
                String origin = element.getAttribute("origine");
                String destination = element.getAttribute("destination");
                String lengthStr = element.getAttribute("longueur");
                String name = element.getAttribute("nomRue");
                
                if (origin.isEmpty() || destination.isEmpty() || lengthStr.isEmpty()) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : le tronçon #" + (i + 1) + " est incomplet.\n\n" +
                        "Chaque <troncon> doit avoir les attributs :\n" +
                        "  • origine\n" +
                        "  • destination\n" +
                        "  • longueur\n" +
                        "  • nomRue (optionnel)"
                    );
                }
                
                try {
                    double length = Double.parseDouble(lengthStr);
                    // Utilisation de SegmentFactory pour créer et valider le segment
                    Segment segment = SegmentFactory.createSegment(origin, destination, length, name);
                    map.getSegments().add(segment);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "❌ Format XML incorrect : la longueur du tronçon #" + (i + 1) + 
                        " doit être un nombre décimal."
                    );
                } catch (IllegalArgumentException e) {
                    // Ignorer les segments invalides (ex: origine = destination)
                    // et continuer le parsing
                    skippedSegmentsCount++;
                }
            }
            
            // Afficher un résumé si des segments ont été ignorés
            if (skippedSegmentsCount > 0) {
                System.out.println("ℹ️  Carte chargée avec succès : " + 
                    map.getNodes().size() + " nœuds, " + 
                    map.getSegments().size() + " segments valides (" + 
                    skippedSegmentsCount + " segment(s) invalide(s) ignoré(s))");
            } else {
                System.out.println("✅ Carte chargée avec succès : " + 
                    map.getNodes().size() + " nœuds, " + 
                    map.getSegments().size() + " segments");
            }
        }

        return map;
    }
}

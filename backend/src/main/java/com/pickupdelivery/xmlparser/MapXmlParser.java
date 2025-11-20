package com.pickupdelivery.xmlparser;

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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try (InputStream inputStream = file.getInputStream()) {
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Parse les nœuds (intersections)
            NodeList nodeList = document.getElementsByTagName("noeud");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String id = element.getAttribute("id");
                double latitude = Double.parseDouble(element.getAttribute("latitude"));
                double longitude = Double.parseDouble(element.getAttribute("longitude"));
                
                map.getNodes().add(new Node(id, latitude, longitude));
            }

            // Parse les segments (tronçons de rue)
            NodeList segmentList = document.getElementsByTagName("troncon");
            for (int i = 0; i < segmentList.getLength(); i++) {
                Element element = (Element) segmentList.item(i);
                String origin = element.getAttribute("origine");
                String destination = element.getAttribute("destination");
                double length = Double.parseDouble(element.getAttribute("longueur"));
                String name = element.getAttribute("nomRue");
                
                map.getSegments().add(new Segment(origin, destination, length, name));
            }
        }

        return map;
    }
}

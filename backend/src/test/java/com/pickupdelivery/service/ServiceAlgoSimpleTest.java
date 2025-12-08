package com.pickupdelivery.service;

import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.DemandeSet;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import com.pickupdelivery.xmlparser.DeliveryRequestXmlParser;
import com.pickupdelivery.xmlparser.MapXmlParser;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.util.Map;

/**
 * Classe de test simple pour vérifier le Graph sans passer par l'UI
 * Exécuter directement avec : java ServiceAlgoSimpleTest
 */
public class ServiceAlgoSimpleTest {

    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(80));
            System.out.println("🧪 TEST DU GRAPH - Sans UI");
            System.out.println("=".repeat(80));

            // 1. Charger la carte depuis un fichier XML
            System.out.println("\n📂 Chargement de la carte...");
            String mapPath = "/Users/diegoaquino/IF4/PickupAndDelivery/fichiersXMLPickupDelivery/petitPlan.xml";
            MapXmlParser mapParser = new MapXmlParser();
            CityMap cityMap = mapParser.parseMapFromXML(createMockFile(mapPath));
            
            System.out.println("✅ Carte chargée :");
            System.out.println("   - Nœuds : " + cityMap.getNodes().size());
            System.out.println("   - Segments : " + cityMap.getSegments().size());

            // 2. Charger les demandes depuis un fichier XML
            System.out.println("\n📂 Chargement des demandes...");
            String requestPath = "/Users/diegoaquino/IF4/PickupAndDelivery/fichiersXMLPickupDelivery/demandePetit1.xml";
            DeliveryRequestXmlParser requestParser = new DeliveryRequestXmlParser();
            DemandeSet DemandeSet = requestParser.parseDeliveryRequestFromXML(createMockFile(requestPath));
            
            System.out.println("✅ Demandes chargées :");
            System.out.println("   - Warehouse : " + DemandeSet.getWarehouse().getNodeId());
            System.out.println("   - Nombre de demandes : " + DemandeSet.getDemands().size());

            // 3. Créer le ServiceAlgo et générer le StopSet
            System.out.println("\n🔄 Création du StopSet...");
            ServiceAlgo serviceAlgo = new ServiceAlgo();
            StopSet stopSet = serviceAlgo.getStopSet(DemandeSet);
            
            System.out.println("✅ StopSet créé :");
            System.out.println("   - Nombre total de stops : " + stopSet.getStops().size());
            
            long warehouseCount = stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE).count();
            long pickupCount = stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP).count();
            long deliveryCount = stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY).count();
            
            System.out.println("   - Warehouses : " + warehouseCount);
            System.out.println("   - Pickups : " + pickupCount);
            System.out.println("   - Deliveries : " + deliveryCount);

            // 4. Construire le Graph
            System.out.println("\n🔄 Construction du Graph (matrice de distances)...");
            long startTime = System.currentTimeMillis();
            Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
            long endTime = System.currentTimeMillis();
            
            System.out.println("✅ Graph construit en " + (endTime - startTime) + " ms");
            System.out.println("   - Stop de départ (Warehouse) : " + graph.getStopDepart().getIdNode());
            System.out.println("   - Coût initial : " + graph.getCout());

            // 5. Afficher la matrice de distances
            System.out.println("\n📊 MATRICE DE DISTANCES COMPLÈTE :");
            System.out.println("-".repeat(80));
            
            Map<Stop, Map<Stop, Trajet>> matrix = graph.getDistancesMatrix();
            
            for (Stop source : matrix.keySet()) {
                System.out.println("\n📍 DE : " + formatStop(source));
                Map<Stop, Trajet> destinations = matrix.get(source);
                
                for (Stop dest : destinations.keySet()) {
                    Trajet trajet = destinations.get(dest);
                    System.out.printf("    ➜ VERS : %-40s | Distance : %8.2f m | Segments : %2d%n",
                            formatStop(dest),
                            trajet.getDistance(),
                            trajet.getSegments().size()
                    );
                }
            }

            // 6. Vérifications
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🔍 VÉRIFICATIONS :");
            System.out.println("=".repeat(80));
            
            int expectedStops = stopSet.getStops().size();
            int actualMatrixSize = matrix.size();
            System.out.println("✓ Taille de la matrice : " + actualMatrixSize + " (attendu : " + expectedStops + ")");
            
            boolean allCorrect = true;
            for (Stop source : matrix.keySet()) {
                Map<Stop, Trajet> destinations = matrix.get(source);
                int expectedDests = expectedStops - 1; // Tous sauf lui-même
                if (destinations.size() != expectedDests) {
                    System.out.println("✗ Erreur : " + formatStop(source) + " a " + 
                            destinations.size() + " destinations (attendu : " + expectedDests + ")");
                    allCorrect = false;
                }
                
                // Vérifier qu'aucune distance n'est infinie ou négative
                for (Trajet trajet : destinations.values()) {
                    if (trajet.getDistance() <= 0 || trajet.getDistance() == Double.POSITIVE_INFINITY) {
                        System.out.println("✗ Erreur : Distance invalide (" + trajet.getDistance() + ") de " + 
                                formatStop(source) + " vers " + formatStop(trajet.getStopArrivee()));
                        allCorrect = false;
                    }
                }
            }
            
            if (allCorrect) {
                System.out.println("✓ Toutes les distances sont valides et positives");
                System.out.println("✓ Chaque stop a exactement " + (expectedStops - 1) + " destinations");
            }

            System.out.println("\n" + "=".repeat(80));
            System.out.println("✅ TEST TERMINÉ AVEC SUCCÈS !");
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("❌ ERREUR DURANT LE TEST :");
            System.err.println("=".repeat(80));
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String formatStop(Stop stop) {
        String type = stop.getTypeStop().toString();
        String node = stop.getIdNode();
        String demand = stop.getIdDemande() != null ? " (Demande: " + stop.getIdDemande() + ")" : "";
        return String.format("%10s @ %-10s%s", type, node, demand);
    }

    private static MockMultipartFile createMockFile(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] content = fis.readAllBytes();
            return new MockMultipartFile("file", filePath, "text/xml", content);
        }
	}
}

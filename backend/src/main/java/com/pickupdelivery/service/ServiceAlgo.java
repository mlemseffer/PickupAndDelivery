package com.pickupdelivery.service;

import com.pickupdelivery.dto.ShortestPathResult;
import com.pickupdelivery.model.*;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service implémentant les algorithmes de calcul de chemin et d'optimisation
 */
@Service
public class ServiceAlgo {

    /**
     * Calcule le plus court chemin entre deux nœuds en utilisant l'algorithme de Dijkstra
     *
     * @param start   Le nœud de départ
     * @param end     Le nœud d'arrivée
     * @param cityMap La carte de la ville contenant tous les nœuds et segments
     * @return Un objet ShortestPathResult contenant la distance totale et la liste des segments du chemin
     */
    public ShortestPathResult dijkstra(Node start, Node end, CityMap cityMap) {
        if (start == null || end == null || cityMap == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        // Construction d'un graphe d'adjacence
        Map<String, List<SegmentInfo>> adjacencyList = buildAdjacencyList(cityMap);
        
        return dijkstraWithAdjacency(start, end, adjacencyList, cityMap.getNodes());
    }

    /**
     * Version optimisée de Dijkstra qui accepte une liste d'adjacence pré-calculée
     * Utilisée par buildGraph() pour éviter de recalculer adjacencyList à chaque appel
     *
     * @param start         Le nœud de départ
     * @param end           Le nœud d'arrivée
     * @param adjacencyList La liste d'adjacence pré-calculée
     * @param allNodes      La liste de tous les nœuds
     * @return Un objet ShortestPathResult contenant la distance totale et la liste des segments du chemin
     */
    private ShortestPathResult dijkstraWithAdjacency(Node start, Node end, 
                                                      Map<String, List<SegmentInfo>> adjacencyList,
                                                      List<Node> allNodes) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        String startId = start.getId();
        String endId = end.getId();

        // Structures de données pour Dijkstra
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Segment> segmentFromPredecessor = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        Set<String> visited = new HashSet<>();

        // Initialisation
        for (Node node : allNodes) {
            distances.put(node.getId(), Double.POSITIVE_INFINITY);
        }
        distances.put(startId, 0.0);
        queue.add(new NodeDistance(startId, 0.0));

        // Algorithme de Dijkstra
        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            String currentNodeId = current.nodeId;

            if (visited.contains(currentNodeId)) {
                continue;
            }

            visited.add(currentNodeId);

            // Si on a atteint le nœud de destination, on peut arrêter
            if (currentNodeId.equals(endId)) {
                break;
            }

            // Exploration des voisins
            List<SegmentInfo> neighbors = adjacencyList.getOrDefault(currentNodeId, Collections.emptyList());
            for (SegmentInfo segmentInfo : neighbors) {
                String neighborId = segmentInfo.destinationId;
                double newDistance = distances.get(currentNodeId) + segmentInfo.segment.getLength();

                if (newDistance < distances.get(neighborId)) {
                    distances.put(neighborId, newDistance);
                    predecessors.put(neighborId, currentNodeId);
                    segmentFromPredecessor.put(neighborId, segmentInfo.segment);
                    queue.add(new NodeDistance(neighborId, newDistance));
                }
            }
        }

        // Reconstruction du chemin
        double totalDistance = distances.get(endId);
        if (totalDistance == Double.POSITIVE_INFINITY) {
            // Pas de chemin trouvé
            return new ShortestPathResult(Double.POSITIVE_INFINITY, Collections.emptyList());
        }

        List<Segment> pathSegments = new ArrayList<>();
        String currentNodeId = endId;

        while (!currentNodeId.equals(startId)) {
            Segment segment = segmentFromPredecessor.get(currentNodeId);
            if (segment == null) {
                break;
            }
            pathSegments.add(0, segment); // Ajout au début pour avoir le bon ordre
            currentNodeId = predecessors.get(currentNodeId);
        }

        return new ShortestPathResult(totalDistance, pathSegments);
    }

    /**
     * Construit une liste d'adjacence à partir des segments de la carte
     *
     * @param cityMap La carte de la ville
     * @return Une map où chaque nœud est associé à la liste de ses voisins avec les segments correspondants
     */
    private Map<String, List<SegmentInfo>> buildAdjacencyList(CityMap cityMap) {
        Map<String, List<SegmentInfo>> adjacencyList = new HashMap<>();

        for (Segment segment : cityMap.getSegments()) {
            adjacencyList.computeIfAbsent(segment.getOrigin(), k -> new ArrayList<>())
                    .add(new SegmentInfo(segment.getDestination(), segment));
        }

        return adjacencyList;
    }

    /**
     * Classe interne pour représenter un nœud avec sa distance dans la file de priorité
     */
    private static class NodeDistance {
        String nodeId;
        double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    /**
     * Classe interne pour stocker les informations d'un segment dans la liste d'adjacence
     */
    private static class SegmentInfo {
        String destinationId;
        Segment segment;

        SegmentInfo(String destinationId, Segment segment) {
            this.destinationId = destinationId;
            this.segment = segment;
        }
    }

    /**
     * Récupère un StopSet contenant tous les stops (pickup, delivery et warehouse)
     * à partir d'un DeliveryRequestSet
     *
     * @param deliveryRequestSet L'ensemble des demandes de livraison avec l'entrepôt
     * @return Un StopSet contenant tous les stops
     */
    public StopSet getStopSet(DeliveryRequestSet deliveryRequestSet) {
        if (deliveryRequestSet == null) {
            throw new IllegalArgumentException("DeliveryRequestSet ne peut pas être null");
        }

        StopSet stopSet = new StopSet();
        List<Stop> stops = new ArrayList<>();

        // Ajouter le warehouse comme stop
        if (deliveryRequestSet.getWarehouse() != null) {
            Stop warehouseStop = new Stop();
            warehouseStop.setIdNode(deliveryRequestSet.getWarehouse().getNodeId());
            warehouseStop.setIdDemande(null); // null pour le warehouse
            warehouseStop.setTypeStop(Stop.TypeStop.WAREHOUSE);
            stops.add(warehouseStop);
        }

        // Ajouter tous les pickups et deliveries des demandes
        if (deliveryRequestSet.getDemands() != null) {
            for (Demand demand : deliveryRequestSet.getDemands()) {
                // Ajouter le pickup
                Stop pickupStop = new Stop();
                pickupStop.setIdNode(demand.getPickupNodeId());
                pickupStop.setIdDemande(demand.getId());
                pickupStop.setTypeStop(Stop.TypeStop.PICKUP);
                stops.add(pickupStop);

                // Ajouter le delivery
                Stop deliveryStop = new Stop();
                deliveryStop.setIdNode(demand.getDeliveryNodeId());
                deliveryStop.setIdDemande(demand.getId());
                deliveryStop.setTypeStop(Stop.TypeStop.DELIVERY);
                stops.add(deliveryStop);
            }
        }

        stopSet.setStops(stops);
        return stopSet;
    }

    /**
     * Construit un graphe complet avec tous les trajets entre les stops
     * Calcule efficacement les distances entre tous les stops en utilisant Dijkstra
     *
     * @param stopSet L'ensemble des stops (pickup, delivery, warehouse)
     * @param cityMap La carte de la ville
     * @return Un Graph contenant tous les trajets entre les stops
     */
    public Graph buildGraph(StopSet stopSet, CityMap cityMap) {
        if (stopSet == null || cityMap == null) {
            throw new IllegalArgumentException("StopSet et CityMap ne peuvent pas être null");
        }

        List<Stop> stops = stopSet.getStops();
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("StopSet ne peut pas être vide");
        }

        // PRÉ-CALCUL : Créer la liste d'adjacence UNE SEULE FOIS (optimisation critique)
        Map<String, List<SegmentInfo>> adjacencyList = buildAdjacencyList(cityMap);
        
        // PRÉ-CALCUL : Créer une map pour trouver rapidement les nodes par leur ID
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : cityMap.getNodes()) {
            nodeMap.put(node.getId(), node);
        }

        // Trouver le stop warehouse (stop de départ)
        Stop warehouseStop = stops.stream()
                .filter(stop -> stop.getTypeStop() == Stop.TypeStop.WAREHOUSE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aucun warehouse trouvé dans le StopSet"));

        // Initialiser le graphe
        Graph graph = new Graph();
        graph.setStopDepart(warehouseStop);
        graph.setCout(0.0);
        
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();

        // Calculer tous les trajets entre tous les stops
        // Pour chaque stop source
        for (Stop stopSource : stops) {
            Map<Stop, Trajet> trajetsFromSource = new HashMap<>();
            Node nodeSource = nodeMap.get(stopSource.getIdNode());
            
            if (nodeSource == null) {
                throw new IllegalArgumentException("Node non trouvé pour le stop: " + stopSource.getIdNode());
            }

            // Pour chaque stop destination (différent de la source)
            for (Stop stopDestination : stops) {
                if (stopSource.equals(stopDestination)) {
                    continue; // Pas de trajet vers soi-même
                }

                Node nodeDestination = nodeMap.get(stopDestination.getIdNode());
                
                if (nodeDestination == null) {
                    throw new IllegalArgumentException("Node non trouvé pour le stop: " + stopDestination.getIdNode());
                }

                // OPTIMISATION : Utiliser dijkstraWithAdjacency avec la liste d'adjacence pré-calculée
                ShortestPathResult result = dijkstraWithAdjacency(
                    nodeSource, nodeDestination, adjacencyList, cityMap.getNodes());

                // Créer le trajet
                Trajet trajet = new Trajet();
                trajet.setStopDepart(stopSource);
                trajet.setStopArrivee(stopDestination);
                trajet.setSegments(result.getSegments());
                trajet.setDistance(result.getTotalDistance());

                // Ajouter dans la map
                trajetsFromSource.put(stopDestination, trajet);
            }

            distancesMatrix.put(stopSource, trajetsFromSource);
        }

        graph.setDistancesMatrix(distancesMatrix);
        return graph;
    }
}

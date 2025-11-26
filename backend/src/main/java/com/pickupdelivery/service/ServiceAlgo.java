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
                trajet.setDistance(result.getDistance());

                // Ajouter dans la map
                trajetsFromSource.put(stopDestination, trajet);
            }

            distancesMatrix.put(stopSource, trajetsFromSource);
        }

        graph.setDistancesMatrix(distancesMatrix);
        return graph;
    }

    // =========================================================================
    // PHASE 1: PRÉPARATION DES DONNÉES POUR L'ALGORITHME TSP
    // =========================================================================

    /**
     * Extrait le stop de type WAREHOUSE depuis le Graph
     * 
     * @param graph Le graphe contenant tous les stops
     * @return Le Stop warehouse
     * @throws IllegalStateException Si aucun warehouse n'est trouvé
     */
    private Stop extractWarehouse(Graph graph) {
        if (graph == null || graph.getDistancesMatrix() == null) {
            throw new IllegalArgumentException("Graph ne peut pas être null");
        }

        return graph.getDistancesMatrix().keySet().stream()
                .filter(stop -> stop.getTypeStop() == Stop.TypeStop.WAREHOUSE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun entrepôt (warehouse) trouvé dans le Graph"));
    }

    /**
     * Extrait tous les stops sauf le warehouse
     * 
     * @param graph Le graphe contenant tous les stops
     * @return Liste des stops (pickups et deliveries uniquement)
     */
    private List<Stop> extractNonWarehouseStops(Graph graph) {
        if (graph == null || graph.getDistancesMatrix() == null) {
            throw new IllegalArgumentException("Graph ne peut pas être null");
        }

        return graph.getDistancesMatrix().keySet().stream()
                .filter(stop -> stop.getTypeStop() != Stop.TypeStop.WAREHOUSE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Organise les pickups par ID de demande
     * Permet de retrouver facilement tous les pickups associés à une demande
     * 
     * @param stops Liste de tous les stops (pickups et deliveries)
     * @return Map avec clé = idDemande, valeur = liste des stops pickup de cette demande
     */
    private Map<String, List<Stop>> buildPickupsByRequestId(List<Stop> stops) {
        if (stops == null) {
            throw new IllegalArgumentException("La liste de stops ne peut pas être null");
        }

        return stops.stream()
                .filter(stop -> stop.getTypeStop() == Stop.TypeStop.PICKUP)
                .collect(java.util.stream.Collectors.groupingBy(Stop::getIdDemande));
    }

    /**
     * Organise les deliveries par ID de demande
     * Permet de retrouver facilement le delivery associé à une demande
     * 
     * @param stops Liste de tous les stops (pickups et deliveries)
     * @return Map avec clé = idDemande, valeur = stop delivery de cette demande
     */
    private Map<String, Stop> buildDeliveryByRequestId(List<Stop> stops) {
        if (stops == null) {
            throw new IllegalArgumentException("La liste de stops ne peut pas être null");
        }

        return stops.stream()
                .filter(stop -> stop.getTypeStop() == Stop.TypeStop.DELIVERY)
                .collect(java.util.stream.Collectors.toMap(
                        Stop::getIdDemande, 
                        java.util.function.Function.identity()
                ));
    }

    // =========================================================================
    // PHASE 2: FONCTIONS UTILITAIRES POUR L'ALGORITHME TSP
    // =========================================================================

    /**
     * Récupère la distance entre deux stops depuis la matrice d'adjacence du Graph
     * 
     * @param a Le stop de départ
     * @param b Le stop d'arrivée
     * @param graph Le graphe contenant la matrice de distances
     * @return La distance entre les deux stops
     * @throws IllegalArgumentException Si les paramètres sont null ou si la distance n'existe pas
     */
    private double distance(Stop a, Stop b, Graph graph) {
        if (a == null || b == null || graph == null) {
            throw new IllegalArgumentException("Les stops et le graph ne peuvent pas être null");
        }

        Map<Stop, Map<Stop, Trajet>> matrix = graph.getDistancesMatrix();
        if (matrix == null || !matrix.containsKey(a)) {
            throw new IllegalArgumentException("Stop source introuvable dans le graph: " + a.getIdNode());
        }

        Map<Stop, Trajet> destinations = matrix.get(a);
        if (!destinations.containsKey(b)) {
            throw new IllegalArgumentException("Pas de trajet trouvé entre " + a.getIdNode() + " et " + b.getIdNode());
        }

        Trajet trajet = destinations.get(b);
        return trajet.getDistance();
    }

    /**
     * Calcule la distance totale d'une tournée (route)
     * 
     * @param route Liste ordonnée des stops formant la tournée
     * @param graph Le graphe contenant les distances entre stops
     * @return La distance totale de la tournée en mètres
     * @throws IllegalArgumentException Si les paramètres sont null ou si la route est invalide
     */
    private double computeRouteDistance(List<Stop> route, Graph graph) {
        if (route == null || graph == null) {
            throw new IllegalArgumentException("Route et graph ne peuvent pas être null");
        }

        if (route.size() < 2) {
            return 0.0; // Une route avec 0 ou 1 stop a une distance de 0
        }

        double totalDistance = 0.0;

        for (int i = 0; i < route.size() - 1; i++) {
            Stop current = route.get(i);
            Stop next = route.get(i + 1);
            totalDistance += distance(current, next, graph);
        }

        return totalDistance;
    }

    /**
     * Vérifie si un stop (en particulier une delivery) peut être visité
     * Une delivery ne peut être visitée que si tous ses pickups correspondants ont déjà été visités
     * Les pickups et le warehouse sont toujours faisables
     * 
     * @param stop Le stop à vérifier
     * @param visited Ensemble des stops déjà visités
     * @param pickupsByRequestId Map des pickups organisés par ID de demande
     * @return true si le stop peut être visité, false sinon
     */
    private boolean isStopFeasible(
            Stop stop,
            Set<Stop> visited,
            Map<String, List<Stop>> pickupsByRequestId
    ) {
        if (stop == null || visited == null || pickupsByRequestId == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        // Les pickups et le warehouse sont toujours faisables
        if (stop.getTypeStop() == Stop.TypeStop.PICKUP || 
            stop.getTypeStop() == Stop.TypeStop.WAREHOUSE) {
            return true;
        }

        // Pour une delivery, vérifier que tous ses pickups ont été visités
        if (stop.getTypeStop() == Stop.TypeStop.DELIVERY) {
            String requestId = stop.getIdDemande();
            List<Stop> requiredPickups = pickupsByRequestId.get(requestId);

            if (requiredPickups == null || requiredPickups.isEmpty()) {
                // Pas de pickup requis (cas anormal, mais on considère comme faisable)
                return true;
            }

            // Tous les pickups de cette demande doivent être dans visited
            return visited.containsAll(requiredPickups);
        }

        return false;
    }

    /**
     * Vérifie si une tournée respecte les contraintes de précédence
     * Chaque delivery doit être visitée APRÈS tous les pickups de sa demande
     * 
     * @param route Liste ordonnée des stops formant la tournée
     * @param pickupsByRequestId Map des pickups organisés par ID de demande
     * @param deliveryByRequestId Map des deliveries organisés par ID de demande
     * @return true si toutes les contraintes de précédence sont respectées, false sinon
     */
    private boolean respectsPrecedence(
            List<Stop> route,
            Map<String, List<Stop>> pickupsByRequestId,
            Map<String, Stop> deliveryByRequestId
    ) {
        if (route == null || pickupsByRequestId == null || deliveryByRequestId == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        Set<Stop> visited = new HashSet<>();

        for (Stop stop : route) {
            // Vérifier que le stop est faisable avec les stops déjà visités
            if (!isStopFeasible(stop, visited, pickupsByRequestId)) {
                return false; // Violation de contrainte : delivery avant son pickup
            }
            visited.add(stop);
        }

        return true;
    }

    /**
     * Effectue un swap 2-opt sur une route
     * Inverse le segment de route entre les indices i et k (inclus)
     * 
     * Exemple:
     *   Route originale: [W, A, B, C, D, E, W]
     *   twoOptSwap(route, 1, 4) → [W, D, C, B, A, E, W]
     *   (inverse le segment A→B→C→D)
     * 
     * @param route La route originale
     * @param i Index de début du segment à inverser (inclus)
     * @param k Index de fin du segment à inverser (inclus)
     * @return Une nouvelle route avec le segment inversé
     * @throws IllegalArgumentException Si les indices sont invalides
     */
    private List<Stop> twoOptSwap(List<Stop> route, int i, int k) {
        if (route == null) {
            throw new IllegalArgumentException("Route ne peut pas être null");
        }

        if (i < 0 || k >= route.size() || i >= k) {
            throw new IllegalArgumentException(
                "Indices invalides: i=" + i + ", k=" + k + ", taille route=" + route.size() + 
                " (requis: 0 <= i < k < size)"
            );
        }

        List<Stop> newRoute = new ArrayList<>();

        // Segment 1: début → i-1 (inchangé)
        newRoute.addAll(route.subList(0, i));

        // Segment 2: i → k (inversé)
        List<Stop> segmentToReverse = new ArrayList<>(route.subList(i, k + 1));
        Collections.reverse(segmentToReverse);
        newRoute.addAll(segmentToReverse);

        // Segment 3: k+1 → fin (inchangé)
        if (k + 1 < route.size()) {
            newRoute.addAll(route.subList(k + 1, route.size()));
        }

        return newRoute;
    }

    // =========================================================================
    // PHASE 3: CONSTRUCTION DE LA TOURNÉE INITIALE (ALGORITHME GLOUTON)
    // =========================================================================

    /**
     * Construit une tournée initiale en utilisant l'algorithme glouton du plus proche voisin
     * 
     * Algorithme:
     * 1. Commencer à l'entrepôt (warehouse)
     * 2. Tant qu'il reste des stops non visités:
     *    - Trouver le stop faisable le plus proche du stop courant
     *    - Un stop est faisable si:
     *      * C'est un PICKUP (toujours faisable)
     *      * C'est une DELIVERY dont tous les pickups ont été visités
     * 3. Retourner à l'entrepôt
     * 
     * @param graph Le graphe contenant les distances entre stops
     * @param warehouse Le stop entrepôt (point de départ/arrivée)
     * @param stops Liste de tous les stops à visiter (hors warehouse)
     * @param pickupsByRequestId Map des pickups organisés par ID de demande
     * @return Une route (tournée) valide commençant et finissant au warehouse
     * @throws IllegalArgumentException Si les paramètres sont invalides
     * @throws IllegalStateException Si aucun stop faisable n'est trouvé (bug logique)
     */
    private List<Stop> buildInitialRoute(
            Graph graph,
            Stop warehouse,
            List<Stop> stops,
            Map<String, List<Stop>> pickupsByRequestId
    ) {
        if (graph == null || warehouse == null || stops == null || pickupsByRequestId == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        if (stops.isEmpty()) {
            // Cas spécial: pas de stops à visiter, juste aller-retour au warehouse
            return Arrays.asList(warehouse, warehouse);
        }

        List<Stop> route = new ArrayList<>();
        Set<Stop> visited = new HashSet<>();
        Set<Stop> remaining = new HashSet<>(stops);

        // 1️⃣ Commencer à l'entrepôt
        route.add(warehouse);
        visited.add(warehouse);

        // 2️⃣ Tant qu'il reste des stops non visités
        while (!remaining.isEmpty()) {
            Stop current = route.get(route.size() - 1);
            Stop nearest = null;
            double minDistance = Double.MAX_VALUE;

            // 3️⃣ Chercher le stop faisable le plus proche
            for (Stop candidate : remaining) {
                // Vérifier si le stop est faisable (contraintes de précédence)
                if (!isStopFeasible(candidate, visited, pickupsByRequestId)) {
                    continue; // Delivery dont le pickup n'a pas encore été visité
                }

                // Calculer la distance
                double dist = distance(current, candidate, graph);

                // Garder le plus proche
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = candidate;
                }
            }

            // 4️⃣ Vérifier qu'on a trouvé un stop faisable
            if (nearest == null) {
                // Cela ne devrait jamais arriver si la logique est correcte
                // Car il devrait toujours y avoir au moins un pickup faisable
                throw new IllegalStateException(
                    "Aucun stop faisable trouvé. Stops restants: " + remaining.size() + 
                    ", Stops visités: " + visited.size() + 
                    ". Cela indique un bug dans la logique de faisabilité."
                );
            }

            // 5️⃣ Ajouter le stop le plus proche à la route
            route.add(nearest);
            visited.add(nearest);
            remaining.remove(nearest);
        }

        // 6️⃣ Retour à l'entrepôt
        route.add(warehouse);

        return route;
    }

    // =========================================================================
    // PHASE 5: INTÉGRATION - MÉTHODE PRINCIPALE DE CALCUL DE TOURNÉE
    // =========================================================================

    /**
     * Calcule les tournées optimales pour un nombre donné de livreurs
     * 
     * IMPLÉMENTATION ACTUELLE: Algorithme glouton uniquement (1 livreur)
     * - Utilise l'algorithme du plus proche voisin pour construire une tournée initiale
     * - Respecte les contraintes de précédence (pickup avant delivery)
     * - Retourne une liste contenant une seule tournée
     * 
     * AMÉLIORATIONS FUTURES:
     * - Support multi-livreurs (clustering des demandes)
     * - Optimisation 2-opt pour améliorer la qualité de la solution
     * - Fenêtres horaires et autres contraintes
     * 
     * @param graph Le graphe contenant les distances et chemins entre tous les stops
     * @param courierCount Nombre de livreurs (uniquement 1 supporté actuellement)
     * @return Liste des tournées optimisées (1 seule pour l'instant)
     * @throws IllegalArgumentException Si le graphe est null ou invalide
     * @throws UnsupportedOperationException Si courierCount != 1
     */
    public List<com.pickupdelivery.model.AlgorithmModel.Tour> calculateOptimalTours(Graph graph, int courierCount) {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1️⃣ VALIDATION
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        if (graph == null) {
            throw new IllegalArgumentException("Le graphe ne peut pas être null");
        }

        if (courierCount != 1) {
            throw new UnsupportedOperationException(
                "Multi-livreurs pas encore implémenté. " +
                "Utilisez courierCount = 1 pour le moment. " +
                "Fonctionnalité prévue pour une prochaine version."
            );
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2️⃣ PRÉPARATION DES DONNÉES (PHASE 1)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     CALCUL DE TOURNÉE OPTIMALE - ALGORITHME GLOUTON           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        
        System.out.println("\n📊 Phase 1: Préparation des données...");
        
        Stop warehouse = extractWarehouse(graph);
        List<Stop> stops = extractNonWarehouseStops(graph);
        
        if (stops.isEmpty()) {
            System.out.println("⚠️  Aucune demande de livraison à traiter");
            throw new IllegalStateException("Aucune demande de livraison à traiter");
        }
        
        Map<String, List<Stop>> pickupsByRequestId = buildPickupsByRequestId(stops);
        Map<String, Stop> deliveryByRequestId = buildDeliveryByRequestId(stops);
        
        System.out.println("   ✓ Entrepôt (warehouse): " + warehouse.getIdNode());
        System.out.println("   ✓ Nombre de stops à visiter: " + stops.size());
        System.out.println("   ✓ Nombre de demandes: " + pickupsByRequestId.size());

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3️⃣ CONSTRUCTION DE LA TOURNÉE INITIALE - GLOUTON (PHASE 3)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n🛣️  Phase 3: Construction de la tournée (algorithme glouton)...");
        
        long startTime = System.currentTimeMillis();
        
        List<Stop> initialRoute = buildInitialRoute(graph, warehouse, stops, pickupsByRequestId);
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        System.out.println("   ✓ Tournée construite en " + elapsedTime + " ms");
        System.out.println("   ✓ Nombre de stops dans la tournée: " + initialRoute.size());
        System.out.println("   ✓ Ordre de visite: " + formatRouteForLog(initialRoute));

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 4️⃣ VALIDATION ET CALCUL DE DISTANCE (PHASE 2)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n✅ Phase 2: Validation et calcul de distance...");
        
        double initialDistance = computeRouteDistance(initialRoute, graph);
        boolean isValid = respectsPrecedence(initialRoute, pickupsByRequestId, deliveryByRequestId);
        
        if (!isValid) {
            throw new IllegalStateException(
                "La tournée construite ne respecte pas les contraintes de précédence. " +
                "Cela indique un bug dans l'algorithme."
            );
        }
        
        System.out.println("   ✓ Distance totale: " + String.format("%.2f", initialDistance) + " m");
        System.out.println("   ✓ Contraintes de précédence: RESPECTÉES");

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 5️⃣ CONSTRUCTION DE L'OBJET TOUR
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n📦 Phase 5: Construction de l'objet Tour...");
        
        com.pickupdelivery.model.AlgorithmModel.Tour tour = buildTour(initialRoute, initialDistance, graph);
        tour.setCourierId(1); // Premier livreur
        
        System.out.println("   ✓ Tour créé avec succès");
        System.out.println("   ✓ Livreur ID: " + tour.getCourierId());
        System.out.println("   ✓ Nombre de trajets: " + tour.getTrajets().size());

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 6️⃣ RÉSUMÉ ET RETOUR
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RÉSULTAT DU CALCUL                          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Distance totale : " + String.format("%10.2f", initialDistance) + " m                          ║");
        System.out.println("║  Nombre de stops : " + String.format("%10d", initialRoute.size()) + "                                ║");
        System.out.println("║  Demandes        : " + String.format("%10d", pickupsByRequestId.size()) + "                                ║");
        System.out.println("║  Temps de calcul : " + String.format("%10d", elapsedTime) + " ms                             ║");
        System.out.println("║  Algorithme      : Glouton (plus proche voisin)              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        return Arrays.asList(tour);
    }

    /**
     * Construit un objet Tour à partir d'une route et de sa distance
     * Récupère les trajets détaillés depuis la matrice du Graph
     * 
     * @param route Liste ordonnée des stops
     * @param totalDistance Distance totale de la tournée
     * @param graph Le graphe contenant les trajets détaillés
     * @return Un objet Tour complet avec tous les trajets
     */
    private com.pickupdelivery.model.AlgorithmModel.Tour buildTour(List<Stop> route, double totalDistance, Graph graph) {
        if (route == null || graph == null) {
            throw new IllegalArgumentException("Route et graph ne peuvent pas être null");
        }

        com.pickupdelivery.model.AlgorithmModel.Tour tour = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour.setStops(route);
        tour.setTotalDistance(totalDistance);
        
        // Construire la liste des trajets détaillés entre chaque paire de stops consécutifs
        List<Trajet> trajets = new ArrayList<>();
        
        for (int i = 0; i < route.size() - 1; i++) {
            Stop from = route.get(i);
            Stop to = route.get(i + 1);
            
            // Récupérer le trajet depuis la matrice du Graph
            Trajet trajet = graph.getDistancesMatrix().get(from).get(to);
            
            if (trajet == null) {
                throw new IllegalStateException(
                    "Trajet non trouvé dans le graph entre " + from.getIdNode() + " et " + to.getIdNode()
                );
            }
            
            trajets.add(trajet);
        }
        
        tour.setTrajets(trajets);
        
        return tour;
    }

    /**
     * Formate une route pour l'affichage dans les logs
     * Affiche W pour warehouse, P1/P2/... pour pickups, D1/D2/... pour deliveries
     * 
     * @param route La route à formater
     * @return Une chaîne formatée (ex: "W → P1 → P2 → D1 → D2 → W")
     */
    private String formatRouteForLog(List<Stop> route) {
        if (route == null || route.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.size(); i++) {
            Stop stop = route.get(i);
            
            if (stop.getTypeStop() == Stop.TypeStop.WAREHOUSE) {
                sb.append("W");
            } else {
                sb.append(stop.getTypeStop() == Stop.TypeStop.PICKUP ? "P" : "D");
                // Extraire le numéro de la demande (ex: "D1" → "1")
                String requestId = stop.getIdDemande();
                if (requestId != null && requestId.length() > 1) {
                    sb.append(requestId.substring(1));
                }
            }
            
            if (i < route.size() - 1) {
                sb.append(" → ");
            }
        }
        
        return sb.toString();
    }
}

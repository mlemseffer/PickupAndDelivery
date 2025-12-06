package com.pickupdelivery.service;

import com.pickupdelivery.dto.DistributionWarnings;
import com.pickupdelivery.dto.ShortestPathResult;
import com.pickupdelivery.dto.TourDistributionResult;
import com.pickupdelivery.dto.TourMetrics;
import com.pickupdelivery.exception.AlgorithmException;
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

    // =========================================================================
    // CONSTANTES
    // =========================================================================
    
    /** Distance représentant l'absence de chemin entre deux points */
    private static final double NO_PATH_DISTANCE = Double.POSITIVE_INFINITY;
    
    /** Distance initiale pour les nœuds non encore explorés dans Dijkstra */
    private static final double UNVISITED_DISTANCE = Double.POSITIVE_INFINITY;
    
    /** ID du premier livreur (pour l'instant seul supporté) */
    private static final int DEFAULT_COURIER_ID = 1;
    
    /** Seuil de warning pour le temps de calcul de Dijkstra (en ms) */
    private static final long DIJKSTRA_SLOW_THRESHOLD_MS = 100;
    
    /** Seuil de warning pour le nombre d'itérations dans la file de priorité */
    private static final int DIJKSTRA_ITERATIONS_WARNING_THRESHOLD = 1000;
    
    /** Taille maximale du cache LRU pour les résultats de Dijkstra */
    private static final int DIJKSTRA_CACHE_SIZE = 500;
    
    // =========================================================================
    // CONSTANTES POUR CALCUL DE TEMPS (PHASE 1)
    // =========================================================================
    
    /** Vitesse du coursier en m/s (15 km/h = 4.17 m/s) */
    private static final double COURIER_SPEED_MS = 15.0 / 3.6; // 4.166666... m/s
    
    /** Limite de temps pour une tournée en secondes (4 heures) */
    private static final double TIME_LIMIT_SEC = 4 * 3600; // 14400 secondes

    // =========================================================================
    // CACHE POUR DIJKSTRA
    // =========================================================================
    
    /**
     * Cache LRU (Least Recently Used) pour stocker les résultats de Dijkstra
     * Évite de recalculer les chemins déjà calculés
     * Thread-safe grâce à Collections.synchronizedMap pour la parallélisation
     */
    private final Map<String, ShortestPathResult> dijkstraCache = Collections.synchronizedMap(
        new LinkedHashMap<String, ShortestPathResult>(DIJKSTRA_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ShortestPathResult> eldest) {
                return size() > DIJKSTRA_CACHE_SIZE;
            }
        }
    );
    
    /**
     * Génère une clé unique pour le cache Dijkstra
     * Format: "startNodeId|endNodeId"
     */
    private String generateCacheKey(String startNodeId, String endNodeId) {
        return startNodeId + "|" + endNodeId;
    }
    
    /**
     * Réinitialise le cache Dijkstra
     * Utile pour les tests ou lorsque la carte change
     */
    public void clearDijkstraCache() {
        dijkstraCache.clear();
        System.out.println("🗑️  Cache Dijkstra vidé");
    }
    
    /**
     * Obtient les statistiques du cache
     */
    public String getCacheStats() {
        return String.format("Cache Dijkstra: %d entrées / %d max", 
                           dijkstraCache.size(), DIJKSTRA_CACHE_SIZE);
    }

    // =========================================================================
    // DIJKSTRA - CALCUL DU PLUS COURT CHEMIN
    // =========================================================================
    
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
     * OPTIMISATION: Utilise un cache LRU pour éviter de recalculer les mêmes chemins
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
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // OPTIMISATION: Vérifier le cache avant de calculer
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        String cacheKey = generateCacheKey(startId, endId);
        ShortestPathResult cachedResult = dijkstraCache.get(cacheKey);
        
        if (cachedResult != null) {
            // Cache hit ! Pas besoin de recalculer
            return cachedResult;
        }

        // Cache miss, on doit calculer

        // Structures de données pour Dijkstra
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Segment> segmentFromPredecessor = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        Set<String> visited = new HashSet<>();

        // Initialisation : seulement le nœud de départ (lazy initialization pour les autres)
        distances.put(startId, 0.0);
        queue.add(new NodeDistance(startId, 0.0));

        // Métriques de performance
        long startTime = System.currentTimeMillis();
        int iterations = 0;

        // Algorithme de Dijkstra
        while (!queue.isEmpty()) {
            iterations++;
            NodeDistance current = queue.poll();
            String currentNodeId = current.nodeId();

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

                // Utiliser getOrDefault pour lazy initialization
                double currentNeighborDistance = distances.getOrDefault(neighborId, UNVISITED_DISTANCE);
                
                if (newDistance < currentNeighborDistance) {
                    distances.put(neighborId, newDistance);
                    predecessors.put(neighborId, currentNodeId);
                    segmentFromPredecessor.put(neighborId, segmentInfo.segment);
                    queue.add(new NodeDistance(neighborId, newDistance));
                }
            }
        }

        // Métriques de performance (pour debugging/monitoring)
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > DIJKSTRA_SLOW_THRESHOLD_MS || iterations > DIJKSTRA_ITERATIONS_WARNING_THRESHOLD) {
            System.out.println("⚠️  Dijkstra lent: " + elapsedTime + "ms, " + iterations + " itérations pour " + 
                             startId + " → " + endId);
        }

        // Reconstruction du chemin
        double totalDistance = distances.getOrDefault(endId, NO_PATH_DISTANCE);
        if (totalDistance == NO_PATH_DISTANCE) {
            // Pas de chemin trouvé
            return new ShortestPathResult(NO_PATH_DISTANCE, Collections.emptyList());
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

        ShortestPathResult result = new ShortestPathResult(totalDistance, pathSegments);
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // OPTIMISATION: Mettre le résultat en cache
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        dijkstraCache.put(cacheKey, result);
        
        return result;
    }

    /**
     * Construit une liste d'adjacence à partir des segments de la carte
     * 
     * IMPORTANT: Le graphe est NON-DIRIGÉ (bidirectionnel)
     * Chaque segment du XML représente une rue qui peut être empruntée dans les deux sens
     *
     * @param cityMap La carte de la ville
     * @return Une map où chaque nœud est associé à la liste de ses voisins avec les segments correspondants
     */
    private Map<String, List<SegmentInfo>> buildAdjacencyList(CityMap cityMap) {
        Map<String, List<SegmentInfo>> adjacencyList = new HashMap<>();

        for (Segment segment : cityMap.getSegments()) {
            // Direction origine → destination
            adjacencyList.computeIfAbsent(segment.getOrigin(), k -> new ArrayList<>())
                    .add(new SegmentInfo(segment.getDestination(), segment));
            
            // Direction inverse: destination → origine (graphe non-dirigé)
            adjacencyList.computeIfAbsent(segment.getDestination(), k -> new ArrayList<>())
                    .add(new SegmentInfo(segment.getOrigin(), segment));
        }

        return adjacencyList;
    }

    /**
     * Record représentant un nœud avec sa distance dans la file de priorité de Dijkstra
     * Utilisé pour l'algorithme de recherche du plus court chemin
     */
    private record NodeDistance(String nodeId, double distance) {}

    /**
     * Record pour stocker les informations d'un segment dans la liste d'adjacence
     * Associe une destination à un segment pour naviguer efficacement dans le graphe
     */
    private record SegmentInfo(String destinationId, Segment segment) {}

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
     * OPTIMISATION: Calcul parallélisé des trajets pour améliorer les performances
     * sur les cartes avec beaucoup de stops
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
        
        System.out.println("🔗 Construction du Graph avec " + stops.size() + " stops...");
        long startTime = System.currentTimeMillis();

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
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // OPTIMISATION: Calcul parallélisé avec ConcurrentHashMap pour thread-safety
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new java.util.concurrent.ConcurrentHashMap<>();

        // Calculer tous les trajets entre tous les stops EN PARALLÈLE
        // Pour chaque stop source
        stops.parallelStream().forEach(stopSource -> {
            Map<Stop, Trajet> trajetsFromSource = new java.util.concurrent.ConcurrentHashMap<>();
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
                // + cache automatique pour éviter les recalculs
                ShortestPathResult result = dijkstraWithAdjacency(
                    nodeSource, nodeDestination, adjacencyList, cityMap.getNodes());

                // Créer le trajet
                Trajet trajet = new Trajet();
                trajet.setStopDepart(stopSource);
                trajet.setStopArrivee(stopDestination);
                trajet.setSegments(result.getSegments());
                trajet.setDistance(result.getDistance());
                
                // PHASE 1: Calculer la durée du trajet (temps de déplacement uniquement)
                trajet.setDurationSec(calculateTravelTime(result.getDistance()));

                // Ajouter dans la map (thread-safe avec ConcurrentHashMap)
                trajetsFromSource.put(stopDestination, trajet);
            }

            distancesMatrix.put(stopSource, trajetsFromSource);
        });

        graph.setDistancesMatrix(distancesMatrix);
        
        // PHASE 1: Construire la map des demandes pour le calcul de temps
        // Parcourir les stops et extraire les demandes uniques
        Map<String, Demand> demandMap = new HashMap<>();
        for (Stop stop : stops) {
            if (stop.getTypeStop() != Stop.TypeStop.WAREHOUSE && stop.getIdDemande() != null) {
                // Cette information n'est pas disponible ici, elle sera ajoutée par le controller
                // On laisse null pour l'instant
            }
        }
        graph.setDemandMap(demandMap); // Map vide pour l'instant, sera remplie par le controller
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        int totalPaths = stops.size() * (stops.size() - 1);
        
        System.out.println("   ✓ Graph construit en " + elapsedTime + " ms");
        System.out.println("   ✓ Nombre de trajets calculés: " + totalPaths);
        System.out.println("   ✓ " + getCacheStats());
        
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
                .orElseThrow(() -> new AlgorithmException(
                    AlgorithmException.ErrorType.NO_WAREHOUSE,
                    "Aucun entrepôt (warehouse) trouvé dans le Graph"
                ));
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
            throw new AlgorithmException(
                AlgorithmException.ErrorType.STOP_NOT_FOUND,
                "Stop source introuvable dans le graph: " + a.getIdNode()
            );
        }

        Map<Stop, Trajet> destinations = matrix.get(a);
        if (!destinations.containsKey(b)) {
            throw new AlgorithmException(
                AlgorithmException.ErrorType.NO_PATH_FOUND,
                "Pas de trajet trouvé entre " + a.getIdNode() + " et " + b.getIdNode()
            );
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

    // =========================================================================
    // CALCUL DE TEMPS (PHASE 1)
    // =========================================================================

    /**
     * Calcule le temps de trajet entre deux stops (temps de déplacement uniquement)
     * 
     * @param distance Distance en mètres
     * @return Temps en secondes
     * @throws IllegalArgumentException Si la distance est négative
     */
    private double calculateTravelTime(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("La distance ne peut pas être négative: " + distance);
        }
        if (distance == NO_PATH_DISTANCE || distance == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        return distance / COURIER_SPEED_MS; // temps = distance / vitesse
    }

    /**
     * Récupère la demande associée à un stop
     * 
     * @param stop Le stop
     * @param demandMap Map des demandes par ID
     * @return La demande ou null si le stop est un warehouse
     */
    private Demand getDemandByStop(Stop stop, Map<String, Demand> demandMap) {
        if (stop.getTypeStop() == Stop.TypeStop.WAREHOUSE) {
            return null;
        }
        return demandMap.get(stop.getIdDemande());
    }

    /**
     * Calcule la durée totale d'une tournée (route)
     * Inclut : temps de déplacement + temps de service (pickup + delivery)
     * 
     * @param route Liste ordonnée des stops formant la tournée
     * @param graph Le graphe contenant les distances entre stops
     * @param demandMap Map des demandes par ID pour récupérer les durées de service
     * @return La durée totale de la tournée en secondes
     * @throws IllegalArgumentException Si les paramètres sont null ou si la route est invalide
     */
    private double computeRouteDuration(List<Stop> route, Graph graph, Map<String, Demand> demandMap) {
        if (route == null || graph == null || demandMap == null) {
            throw new IllegalArgumentException("Route, graph et demandMap ne peuvent pas être null");
        }

        if (route.size() < 2) {
            return 0.0; // Une route avec 0 ou 1 stop a une durée de 0
        }

        double totalTime = 0.0;

        for (int i = 0; i < route.size() - 1; i++) {
            Stop current = route.get(i);
            Stop next = route.get(i + 1);
            
            // 1. Temps de trajet entre current et next
            double distance = distance(current, next, graph);
            totalTime += calculateTravelTime(distance);
            
            // 2. Temps de service au stop current
            if (current.getTypeStop() == Stop.TypeStop.PICKUP) {
                Demand demand = getDemandByStop(current, demandMap);
                if (demand != null) {
                    totalTime += demand.getPickupDurationSec();
                }
            } else if (current.getTypeStop() == Stop.TypeStop.DELIVERY) {
                Demand demand = getDemandByStop(current, demandMap);
                if (demand != null) {
                    totalTime += demand.getDeliveryDurationSec();
                }
            }
            // Le warehouse n'a pas de temps de service
        }

        return totalTime;
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
                throw new AlgorithmException(
                    AlgorithmException.ErrorType.NO_FEASIBLE_STOP,
                    "Aucun stop faisable trouvé. Stops restants: " + remaining.size() + 
                    ", Stops visités: " + visited.size() + 
                    ". Vérifiez que toutes les deliveries ont des pickups correspondants."
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
    // PHASE 4: OPTIMISATION 2-OPT
    // =========================================================================

    /**
     * Optimise une tournée en utilisant l'algorithme 2-opt
     * 
     * L'algorithme 2-opt tente d'améliorer la tournée en éliminant les croisements:
     * - Pour chaque paire de segments (i, i+1) et (k, k+1)
     * - Teste si inverser le segment entre i+1 et k réduit la distance totale
     * - Continue jusqu'à ce qu'aucune amélioration ne soit possible
     * 
     * CONTRAINTES:
     * - Le premier et dernier stop (warehouse) ne sont jamais déplacés
     * - Les contraintes de précédence (pickup avant delivery) doivent être respectées
     * 
     * @param route La tournée initiale à optimiser
     * @param graph Le graphe contenant les distances
     * @param pickupsByRequestId Map des pickups organisés par ID de demande
     * @param deliveryByRequestId Map des deliveries organisés par ID de demande
     * @return La tournée optimisée
     */
    private List<Stop> optimizeWith2Opt(
            List<Stop> route,
            Graph graph,
            Map<String, List<Stop>> pickupsByRequestId,
            Map<String, Stop> deliveryByRequestId
    ) {
        if (route == null || route.size() <= 3) {
            // Une route avec 3 stops ou moins ne peut pas être optimisée par 2-opt
            // (warehouse → stop → warehouse)
            return route;
        }

        System.out.println("\n🔧 Phase 4: Optimisation 2-opt...");
        
        List<Stop> bestRoute = new ArrayList<>(route);
        double bestDistance = computeRouteDistance(bestRoute, graph);
        
        System.out.println("   📏 Distance initiale: " + String.format("%.2f", bestDistance) + " m");
        
        boolean improved = true;
        int iteration = 0;
        int totalImprovements = 0;
        
        // Répéter jusqu'à ce qu'aucune amélioration ne soit trouvée
        while (improved) {
            improved = false;
            iteration++;
            
            // Essayer toutes les paires de segments possibles
            // Note: on ne touche pas au premier (0) et dernier stop (size-1) qui sont le warehouse
            for (int i = 1; i < bestRoute.size() - 2; i++) {
                for (int k = i + 1; k < bestRoute.size() - 1; k++) {
                    // Tester le swap 2-opt
                    List<Stop> newRoute = twoOptSwap(bestRoute, i, k);
                    
                    // Vérifier les contraintes de précédence
                    if (!respectsPrecedence(newRoute, pickupsByRequestId, deliveryByRequestId)) {
                        continue; // Ce swap viole les contraintes, on passe au suivant
                    }
                    
                    // Calculer la nouvelle distance
                    double newDistance = computeRouteDistance(newRoute, graph);
                    
                    // Si c'est mieux, on garde cette solution
                    if (newDistance < bestDistance) {
                        bestRoute = newRoute;
                        bestDistance = newDistance;
                        improved = true;
                        totalImprovements++;
                        
                        System.out.println("   ✓ Amélioration trouvée (itération " + iteration + 
                                         ", swap [" + i + ", " + k + "]): " + 
                                         String.format("%.2f", newDistance) + " m " +
                                         "(" + String.format("%.2f", (bestDistance - newDistance)) + " m gagnés)");
                    }
                }
            }
        }
        
        if (totalImprovements > 0) {
            System.out.println("   ✓ Optimisation terminée après " + iteration + " itérations");
            System.out.println("   ✓ Nombre total d'améliorations: " + totalImprovements);
            System.out.println("   📏 Distance finale: " + String.format("%.2f", bestDistance) + " m");
            
            double initialDistance = computeRouteDistance(route, graph);
            double gain = initialDistance - bestDistance;
            double gainPercent = (gain / initialDistance) * 100;
            
            System.out.println("   🎯 Gain total: " + String.format("%.2f", gain) + " m " +
                             "(" + String.format("%.1f", gainPercent) + "%)");
        } else {
            System.out.println("   ✓ Aucune amélioration trouvée (tournée déjà optimale)");
        }
        
        return bestRoute;
    }

    // =========================================================================
    // DISTRIBUTION FIFO MULTI-COURSIERS (PHASE 2)
    // =========================================================================

    /**
     * Distribue une route globale optimisée entre N coursiers selon l'algorithme FIFO strict
     * 
     * PRINCIPE:
     * 1. Parcours séquentiel de la route optimisée (ordre FIFO)
     * 2. Pour chaque pickup rencontré, évaluer si la demande complète (pickup + delivery) 
     *    peut être ajoutée à la tournée actuelle sans dépasser 4h
     * 3. Si oui: ajouter pickup ET delivery à la tournée actuelle
     * 4. Si non: fermer la tournée actuelle, passer au coursier suivant
     * 5. Si plus de coursiers disponibles: marquer la demande comme non assignée
     * 
     * CONTRAINTES RESPECTÉES:
     * - Contrainte temporelle: aucune tournée > 4h (14400 secondes)
     * - Contrainte de précédence: pickup et delivery d'une même demande toujours dans la même tournée
     * - Ordre FIFO strict: pas d'optimisation d'équilibrage
     * 
     * @param globalOptimizedRoute Route globale optimisée (tous les stops)
     * @param graph Graphe avec distances
     * @param courierCount Nombre de coursiers disponibles (1-10)
     * @param pickupsByRequestId Map des pickups par ID de demande
     * @param deliveryByRequestId Map des deliveries par ID de demande
     * @param demandMap Map des demandes complètes
     * @param warehouse Stop warehouse
     * @return Résultat de la distribution avec tours, métriques et warnings
     */
    private TourDistributionResult distributeFIFO(
            List<Stop> globalOptimizedRoute,
            Graph graph,
            int courierCount,
            Map<String, List<Stop>> pickupsByRequestId,
            Map<String, Stop> deliveryByRequestId,
            Map<String, Demand> demandMap,
            Stop warehouse
    ) {
        System.out.println("\n📦 Phase FIFO: Distribution multi-coursiers...");
        System.out.println("   Nombre de coursiers disponibles: " + courierCount);
        System.out.println("   Contrainte temporelle: " + (TIME_LIMIT_SEC / 3600) + " heures");
        
        // Structures de résultat
        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours = new ArrayList<>();
        List<String> unassignedDemandIds = new ArrayList<>();
        Map<Integer, TourMetrics> metricsByCourier = new HashMap<>();
        DistributionWarnings warnings = new DistributionWarnings();
        
        // État du coursier actuel
        int currentCourierId = 1;
        List<Stop> currentTourStops = new ArrayList<>();
        currentTourStops.add(warehouse); // Départ depuis le warehouse
        double currentTourTime = 0.0;
        Set<String> processedDemands = new HashSet<>();
        
        System.out.println("\n   Parcours FIFO de la route optimisée...");
        
        // Parcours FIFO de la route optimisée
        for (int i = 1; i < globalOptimizedRoute.size() - 1; i++) {
            Stop stop = globalOptimizedRoute.get(i);
            
            // Ignorer les deliveries (traitées avec leurs pickups)
            if (stop.getTypeStop() == Stop.TypeStop.DELIVERY) {
                continue;
            }
            
            // Traiter uniquement les pickups
            if (stop.getTypeStop() == Stop.TypeStop.PICKUP) {
                String demandId = stop.getIdDemande();
                
                // Vérifier si déjà traité
                if (processedDemands.contains(demandId)) {
                    continue;
                }
                
                // Trouver le delivery correspondant dans la route
                Stop deliveryStop = findDeliveryInRoute(demandId, globalOptimizedRoute, i);
                
                if (deliveryStop == null) {
                    System.out.println("   ⚠️  Delivery non trouvé pour pickup " + demandId);
                    throw new IllegalStateException(
                        "Delivery non trouvé pour pickup " + demandId);
                }
                
                // Récupérer la demande pour les temps de service
                Demand demand = demandMap.get(demandId);
                if (demand == null) {
                    System.out.println("   ⚠️  Demande " + demandId + " non trouvée dans demandMap");
                    throw new IllegalStateException("Demande " + demandId + " non trouvée");
                }
                
                // Calculer le temps pour cette demande complète
                Stop lastStop = currentTourStops.get(currentTourStops.size() - 1);
                double demandTime = calculateDemandTime(
                    lastStop, stop, deliveryStop, globalOptimizedRoute, graph, demand);
                
                // Temps avec retour au warehouse
                double timeWithReturn = currentTourTime + demandTime 
                    + calculateReturnTime(deliveryStop, warehouse, graph);
                
                // Vérifier contrainte 4h
                if (timeWithReturn > TIME_LIMIT_SEC) {
                    System.out.println("   ⚠️  Ajout de " + demandId + " dépasserait 4h (" + 
                        String.format("%.2f", timeWithReturn / 3600) + "h)");
                    
                    // Fermer la tournée actuelle
                    if (currentTourStops.size() > 1) {
                        currentTourStops.add(warehouse);
                        double tourDistance = computeRouteDistance(currentTourStops, graph);
                        com.pickupdelivery.model.AlgorithmModel.Tour completedTour = 
                            buildTour(currentTourStops, tourDistance, graph);
                        completedTour.setCourierId(currentCourierId);
                        tours.add(completedTour);
                        
                        System.out.println("   ✓ Tournée coursier " + currentCourierId + " fermée: " +
                            String.format("%.2f", completedTour.getTotalDurationHours()) + "h, " +
                            String.format("%.0f", tourDistance) + "m, " +
                            completedTour.getRequestCount() + " demandes");
                    }
                    
                    // Passer au coursier suivant
                    if (currentCourierId < courierCount) {
                        currentCourierId++;
                        currentTourStops = new ArrayList<>();
                        currentTourStops.add(warehouse);
                        currentTourTime = 0.0;
                        
                        System.out.println("   → Passage au coursier " + currentCourierId);
                        
                        // Réessayer d'ajouter cette demande
                        i--;
                        continue;
                    } else {
                        // Plus de coursiers disponibles
                        System.out.println("   ❌ Plus de coursiers disponibles, demande " + 
                            demandId + " non assignée");
                        unassignedDemandIds.add(demandId);
                        processedDemands.add(demandId);
                        warnings.setHasUnassignedDemands(true);
                        warnings.addMessage("Demande " + demandId + 
                            " non assignée (contrainte 4h et tous coursiers utilisés)");
                        continue;
                    }
                }
                
                // Ajouter la demande complète à la tournée actuelle
                currentTourStops.add(stop); // Pickup
                currentTourStops.add(deliveryStop); // Delivery
                currentTourTime += demandTime;
                processedDemands.add(demandId);
                
                System.out.println("   ✓ Demande " + demandId + " assignée au coursier " + 
                    currentCourierId + " (temps accumulé: " + 
                    String.format("%.2f", currentTourTime / 3600) + "h)");
            }
        }
        
        // Fermer la dernière tournée
        if (currentTourStops.size() > 1) {
            currentTourStops.add(warehouse);
            double tourDistance = computeRouteDistance(currentTourStops, graph);
            com.pickupdelivery.model.AlgorithmModel.Tour lastTour = 
                buildTour(currentTourStops, tourDistance, graph);
            lastTour.setCourierId(currentCourierId);
            tours.add(lastTour);
            
            System.out.println("   ✓ Tournée coursier " + currentCourierId + " (finale) fermée: " +
                String.format("%.2f", lastTour.getTotalDurationHours()) + "h, " +
                String.format("%.0f", tourDistance) + "m, " +
                lastTour.getRequestCount() + " demandes");
        }
        
        // Construire les métriques
        for (com.pickupdelivery.model.AlgorithmModel.Tour tour : tours) {
            TourMetrics metrics = new TourMetrics(
                tour.getCourierId(),
                tour.getTotalDistance(),
                tour.getTotalDurationSec(),
                tour.getRequestCount(),
                tour.getStopCount(),
                tour.exceedsTimeLimit()
            );
            metricsByCourier.put(tour.getCourierId(), metrics);
            
            if (tour.exceedsTimeLimit()) {
                warnings.setHasTimeLimitExceeded(true);
                warnings.addMessage("Coursier " + tour.getCourierId() + 
                    " dépasse la limite de 4h (" + 
                    String.format("%.2f", tour.getTotalDurationHours()) + "h)");
            }
        }
        
        System.out.println("\n   📊 Résumé de la distribution:");
        System.out.println("      Coursiers utilisés: " + tours.size() + "/" + courierCount);
        System.out.println("      Demandes assignées: " + processedDemands.size());
        System.out.println("      Demandes non assignées: " + unassignedDemandIds.size());
        
        TourDistributionResult result = new TourDistributionResult(
            tours, unassignedDemandIds, metricsByCourier, warnings);
        
        return result;
    }

    // =========================================================================
    // PHASE 5: INTÉGRATION - MÉTHODE PRINCIPALE DE CALCUL DE TOURNÉE
    // =========================================================================

    /**
     * Calcule les tournées optimales pour un nombre donné de livreurs
     * 
     * IMPLÉMENTATION ACTUELLE: Algorithme glouton + optimisation 2-opt (1 livreur)
     * - Utilise l'algorithme du plus proche voisin pour construire une tournée initiale
     * - Applique l'optimisation 2-opt pour améliorer la solution
     * - Respecte les contraintes de précédence (pickup avant delivery)
     * - Retourne une liste contenant une seule tournée
     * 
     * AMÉLIORATIONS FUTURES:
     * - Support multi-livreurs (clustering des demandes)
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

        if (courierCount < 1 || courierCount > 10) {
            throw new IllegalArgumentException(
                "Le nombre de coursiers doit être entre 1 et 10 (reçu: " + courierCount + ")"
            );
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2️⃣ PRÉPARATION DES DONNÉES (PHASE 1)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     CALCUL DE TOURNÉE OPTIMALE - GLOUTON + 2-OPT             ║");
        System.out.println("║     Mode: " + (courierCount == 1 ? "1 COURSIER" : courierCount + " COURSIERS (FIFO)") + "                                        ║");
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
        
        double initialDistance = computeRouteDistance(initialRoute, graph);
        System.out.println("   📏 Distance de la tournée gloutonne: " + String.format("%.2f", initialDistance) + " m");

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 4️⃣ OPTIMISATION 2-OPT (PHASE 4)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        List<Stop> optimizedRoute = optimizeWith2Opt(initialRoute, graph, pickupsByRequestId, deliveryByRequestId);
        
        System.out.println("   ✓ Ordre de visite après optimisation: " + formatRouteForLog(optimizedRoute));

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 5️⃣ VALIDATION ET CALCUL DE DISTANCE FINALE (PHASE 2)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n✅ Phase 5: Validation finale et calcul de distance...");
        
        double finalDistance = computeRouteDistance(optimizedRoute, graph);
        boolean isValid = respectsPrecedence(optimizedRoute, pickupsByRequestId, deliveryByRequestId);
        
        if (!isValid) {
            throw new AlgorithmException(
                AlgorithmException.ErrorType.PRECEDENCE_VIOLATION,
                "La tournée optimisée ne respecte pas les contraintes de précédence. " +
                "Une delivery a été placée avant son pickup correspondant."
            );
        }
        
        System.out.println("   ✓ Distance totale: " + String.format("%.2f", finalDistance) + " m");
        System.out.println("   ✓ Contraintes de précédence: RESPECTÉES");

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 6️⃣ CONSTRUCTION DE L'OBJET TOUR
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n📦 Phase 6: Construction de l'objet Tour...");
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 6️⃣ CONSTRUCTION DES TOURS (MONO OU MULTI-COURSIER)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n📦 Phase 6: Construction des tours...");
        
        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours;
        
        if (courierCount == 1) {
            // MODE MONO-COURSIER : comportement classique
            System.out.println("   Mode: 1 coursier (tournée unique)");
            
            com.pickupdelivery.model.AlgorithmModel.Tour tour = buildTour(optimizedRoute, finalDistance, graph);
            tour.setCourierId(DEFAULT_COURIER_ID);
            
            System.out.println("   ✓ Tour créé avec succès");
            System.out.println("   ✓ Livreur ID: " + tour.getCourierId());
            System.out.println("   ✓ Nombre de trajets: " + tour.getTrajets().size());
            System.out.println("   ⏱️  Durée totale: " + String.format("%.2f", tour.getTotalDurationHours()) + " h " +
                             "(" + String.format("%.0f", tour.getTotalDurationSec()) + " s)");
            System.out.println("   ✓ Respect de la contrainte 4h: " + (!tour.exceedsTimeLimit() ? "OUI" : "NON ⚠️"));
            
            tours = Arrays.asList(tour);
            
        } else {
            // MODE MULTI-COURSIERS : distribution FIFO
            System.out.println("   Mode: " + courierCount + " coursiers (distribution FIFO)");
            
            TourDistributionResult distributionResult = distributeFIFO(
                optimizedRoute,
                graph,
                courierCount,
                pickupsByRequestId,
                deliveryByRequestId,
                graph.getDemandMap(),
                warehouse
            );
            
            tours = distributionResult.getTours();
            
            // Afficher warnings si présents
            if (distributionResult.getWarnings().hasWarnings()) {
                System.out.println("\n   ⚠️  AVERTISSEMENTS:");
                for (String message : distributionResult.getWarnings().getMessages()) {
                    System.out.println("      - " + message);
                }
            }
            
            if (!distributionResult.getUnassignedDemandIds().isEmpty()) {
                System.out.println("\n   ❌ DEMANDES NON ASSIGNÉES (" + 
                    distributionResult.getUnassignedDemandIds().size() + "):");
                for (String demandId : distributionResult.getUnassignedDemandIds()) {
                    System.out.println("      - " + demandId);
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 7️⃣ RÉSUMÉ ET RETOUR
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RÉSULTAT DU CALCUL                          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Distance initiale (glouton) : " + String.format("%10.2f", initialDistance) + " m              ║");
        System.out.println("║  Distance finale (2-opt)     : " + String.format("%10.2f", finalDistance) + " m              ║");
        
        double gain = initialDistance - finalDistance;
        double gainPercent = (gain / initialDistance) * 100;
        
        System.out.println("║  Gain d'optimisation         : " + String.format("%10.2f", gain) + " m              ║");
        System.out.println("║  Amélioration                : " + String.format("%9.1f", gainPercent) + " %                ║");
        System.out.println("║  Nombre de stops             : " + String.format("%10d", optimizedRoute.size()) + "                    ║");
        System.out.println("║  Demandes                    : " + String.format("%10d", pickupsByRequestId.size()) + "                    ║");
        
        if (courierCount == 1) {
            com.pickupdelivery.model.AlgorithmModel.Tour tour = tours.get(0);
            System.out.println("║  Durée de la tournée         : " + String.format("%10.2f", tour.getTotalDurationHours()) + " h                ║");
            System.out.println("║  Contrainte 4h               : " + (tour.exceedsTimeLimit() ? "⚠️  DÉPASSÉE" : "✓ RESPECTÉE") + "          ║");
        } else {
            System.out.println("║  Nombre de coursiers utilisés: " + String.format("%9d", tours.size()) + "                    ║");
            double totalDistanceAll = tours.stream().mapToDouble(t -> t.getTotalDistance()).sum();
            double maxDuration = tours.stream().mapToDouble(t -> t.getTotalDurationSec()).max().orElse(0);
            System.out.println("║  Distance totale cumulée     : " + String.format("%10.2f", totalDistanceAll) + " m              ║");
            System.out.println("║  Durée max (coursier)        : " + String.format("%10.2f", maxDuration / 3600) + " h                ║");
        }
        
        System.out.println("║  Temps de calcul total       : " + String.format("%10d", totalTime) + " ms                 ║");
        System.out.println("║  Algorithme                  : Glouton + 2-opt " + 
            (courierCount > 1 ? "+ FIFO" : "       ") + "       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        return tours;
    }

    /**
     * Construit un objet Tour à partir d'une route et de sa distance
     * Récupère les trajets détaillés depuis la matrice du Graph
     * PHASE 1: Calcule également la durée totale de la tournée
     * 
     * @param route Liste ordonnée des stops
     * @param totalDistance Distance totale de la tournée
     * @param graph Le graphe contenant les trajets détaillés et les demandes
     * @return Un objet Tour complet avec tous les trajets et la durée
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
                throw new AlgorithmException(
                    AlgorithmException.ErrorType.INVALID_GRAPH,
                    "Trajet non trouvé dans le graph entre " + from.getIdNode() + " et " + to.getIdNode()
                );
            }
            
            trajets.add(trajet);
        }
        
        tour.setTrajets(trajets);
        
        // PHASE 1: Calculer la durée totale de la tournée
        if (graph.getDemandMap() != null && !graph.getDemandMap().isEmpty()) {
            double totalDuration = computeRouteDuration(route, graph, graph.getDemandMap());
            tour.setTotalDurationSec(totalDuration);
        } else {
            // Si pas de demandes (cas de test), durée = 0
            tour.setTotalDurationSec(0.0);
        }
        
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

    // =========================================================================
    // MÉTHODES AUXILIAIRES POUR DISTRIBUTION FIFO (PHASE 2)
    // =========================================================================

    /**
     * Trouve le stop delivery correspondant à un pickup dans une route optimisée
     * 
     * @param demandId ID de la demande
     * @param route Route optimisée contenant tous les stops
     * @param fromIndex Index à partir duquel commencer la recherche
     * @return Le stop delivery ou null si non trouvé
     */
    private Stop findDeliveryInRoute(String demandId, List<Stop> route, int fromIndex) {
        if (demandId == null || route == null) {
            return null;
        }
        
        for (int i = fromIndex + 1; i < route.size(); i++) {
            Stop stop = route.get(i);
            if (stop.getTypeStop() == Stop.TypeStop.DELIVERY && 
                demandId.equals(stop.getIdDemande())) {
                return stop;
            }
        }
        
        return null;
    }

    /**
     * Trouve l'index d'un delivery dans la route
     * 
     * @param demandId ID de la demande
     * @param route Route complète
     * @return Index du delivery ou -1 si non trouvé
     */
    private int findDeliveryIndex(String demandId, List<Stop> route) {
        if (demandId == null || route == null) {
            return -1;
        }
        
        for (int i = 0; i < route.size(); i++) {
            Stop stop = route.get(i);
            if (stop.getTypeStop() == Stop.TypeStop.DELIVERY && 
                demandId.equals(stop.getIdDemande())) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Calcule le temps nécessaire pour effectuer une demande complète (pickup + delivery)
     * à partir de la position actuelle dans la tournée
     * 
     * @param currentStop Stop actuel dans la tournée (dernier stop ajouté)
     * @param pickupStop Stop de pickup de la demande
     * @param deliveryStop Stop de delivery de la demande
     * @param globalRoute Route globale optimisée pour référence
     * @param graph Graphe avec distances
     * @param demand Objet Demand contenant les durées de service
     * @return Temps total en secondes pour cette demande
     */
    private double calculateDemandTime(
            Stop currentStop,
            Stop pickupStop,
            Stop deliveryStop,
            List<Stop> globalRoute,
            Graph graph,
            Demand demand
    ) {
        double totalTime = 0.0;
        
        // 1. Temps de trajet du stop actuel vers le pickup
        double distanceToPickup = distance(currentStop, pickupStop, graph);
        totalTime += calculateTravelTime(distanceToPickup);
        
        // 2. Temps de service au pickup
        totalTime += demand.getPickupDurationSec();
        
        // 3. Temps de trajet du pickup vers le delivery
        double distancePickupToDelivery = distance(pickupStop, deliveryStop, graph);
        totalTime += calculateTravelTime(distancePickupToDelivery);
        
        // 4. Temps de service au delivery
        totalTime += demand.getDeliveryDurationSec();
        
        return totalTime;
    }

    /**
     * Calcule le temps de retour vers le warehouse depuis un stop donné
     * 
     * @param fromStop Stop de départ
     * @param warehouse Stop warehouse
     * @param graph Graphe avec distances
     * @return Temps de retour en secondes
     */
    private double calculateReturnTime(Stop fromStop, Stop warehouse, Graph graph) {
        if (fromStop == null || warehouse == null || graph == null) {
            return 0.0;
        }
        
        double distanceToWarehouse = distance(fromStop, warehouse, graph);
        return calculateTravelTime(distanceToWarehouse);
    }
}

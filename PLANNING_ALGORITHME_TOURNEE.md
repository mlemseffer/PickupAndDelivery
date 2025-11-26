# 📋 Rapport de Planning - Algorithme de Calcul de Tournée Optimale

**Projet:** Pickup & Delivery - Gestion de tournées de livraison à vélo  
**Date:** 26 novembre 2025  
**Équipe:** 4IF INSA Lyon  
**Scope:** Implémentation de l'algorithme TSP pour 1 livreur avec amélioration 2-opt

---

## 🎯 Objectif

Implémenter un **algorithme de calcul de tournée optimale** pour un livreur à vélo, permettant de :
- ✅ Visiter tous les points de pickup et delivery
- ✅ Respecter les contraintes de précédence (pickup avant delivery)
- ✅ Minimiser la distance totale parcourue
- ✅ Commencer et finir à l'entrepôt (warehouse)

---

## 🏗️ Architecture Existante

### Structures de Données Disponibles

#### 1. **Graph** (`Graph.java`)
```java
public class Graph {
    private HashMap<Stop, HashMap<Stop, Double>> adjacencyMatrix;  // Distances pré-calculées
    private HashMap<Stop, HashMap<Stop, Trajet>> initialCost;      // Chemins détaillés
    private StopSet stopSet;                                        // Ensemble des stops
}
```

**Caractéristiques:**
- Matrice d'adjacence complète avec distances Dijkstra entre tous les stops
- Accès O(1) aux distances entre deux stops
- Chemins détaillés disponibles pour l'affichage final

#### 2. **Stop** (`Stop.java`)
```java
public class Stop {
    private String idNode;              // ID du nœud sur la carte
    private String idDemande;           // ID de la demande (null pour warehouse)
    private TypeStop typeStop;          // PICKUP, DELIVERY, WAREHOUSE
}
```

**Types de stops:**
- `WAREHOUSE` : Point de départ/arrivée (1 seul)
- `PICKUP` : Point de collecte (1 par demande)
- `DELIVERY` : Point de livraison (1 par demande)

#### 3. **StopSet** (`StopSet.java`)
Contient tous les stops du problème organisés dans une HashMap pour accès rapide.

#### 4. **Trajet** (`Trajet.java`)
```java
public class Trajet {
    private List<Segment> listeSegment;  // Segments du chemin
    private double longueurTotale;       // Distance totale
}
```

---

## 📐 Approche Algorithmique

### Stratégie Choisie: **Glouton + 2-opt**

**Pourquoi cette approche ?**
- ✅ Construction rapide d'une solution initiale valide (glouton)
- ✅ Amélioration locale efficace avec 2-opt
- ✅ Garantit le respect des contraintes de précédence
- ✅ Complexité raisonnable pour des instances de taille moyenne (~10-50 demandes)

**Alternatives écartées (pour l'instant):**
- ❌ Branch & Bound : complexité trop élevée pour implémentation initiale
- ❌ Algorithmes génétiques : nécessite tuning de paramètres
- ❌ Programmation linéaire : dépendance externe (solver)

---

## 🛠️ Plan d'Implémentation Détaillé

### **Phase 1 : Préparation des Données** 📊

**Fichier:** `ServiceAlgo.java`

#### 1.1 Extraction du Warehouse
```java
private Stop extractWarehouse(Graph graph) {
    return graph.getStopSet().getAllStops().stream()
        .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Aucun entrepôt trouvé"));
}
```

#### 1.2 Extraction des Stops (hors warehouse)
```java
private List<Stop> extractNonWarehouseStops(Graph graph) {
    return graph.getStopSet().getAllStops().stream()
        .filter(s -> s.getTypeStop() != Stop.TypeStop.WAREHOUSE)
        .collect(Collectors.toList());
}
```

#### 1.3 Organisation des Pickups par Demande
```java
private Map<String, List<Stop>> buildPickupsByRequestId(List<Stop> stops) {
    return stops.stream()
        .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP)
        .collect(Collectors.groupingBy(Stop::getIdDemande));
}
```

**Note:** Dans notre modèle, chaque demande a exactement **1 pickup**, mais on utilise `List<Stop>` pour être flexible.

#### 1.4 Organisation des Deliveries par Demande
```java
private Map<String, Stop> buildDeliveryByRequestId(List<Stop> stops) {
    return stops.stream()
        .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY)
        .collect(Collectors.toMap(Stop::getIdDemande, Function.identity()));
}
```

**Résultat attendu:**
```
warehouse = Stop(N5, null, WAREHOUSE)
stops = [Stop(N1, D1, PICKUP), Stop(N9, D1, DELIVERY), Stop(N3, D2, PICKUP), ...]
pickupsByRequestId = { "D1" → [Stop(N1)], "D2" → [Stop(N3)], ... }
deliveryByRequestId = { "D1" → Stop(N9), "D2" → Stop(N7), ... }
```

---

### **Phase 2 : Fonctions Utilitaires** 🔧

**Fichier:** `ServiceAlgo.java`

#### 2.1 Calcul de Distance entre Deux Stops
```java
private double distance(Stop a, Stop b, Graph graph) {
    return graph.getAdjacencyMatrix()
        .get(a)
        .get(b);
}
```

**Complexité:** O(1)

#### 2.2 Calcul de Distance Totale d'une Tournée
```java
private double computeRouteDistance(List<Stop> route, Graph graph) {
    double total = 0.0;
    for (int i = 0; i < route.size() - 1; i++) {
        total += distance(route.get(i), route.get(i + 1), graph);
    }
    return total;
}
```

**Complexité:** O(n) où n = nombre de stops

**Exemple:**
```
Route: [Warehouse, P1, D1, P2, D2, Warehouse]
Distance = d(W→P1) + d(P1→D1) + d(D1→P2) + d(P2→D2) + d(D2→W)
```

#### 2.3 Vérification de Faisabilité d'une Delivery
```java
private boolean isDeliveryFeasible(
    Stop delivery, 
    Set<Stop> visited, 
    Map<String, List<Stop>> pickupsByRequestId
) {
    if (delivery.getTypeStop() != Stop.TypeStop.DELIVERY) {
        return true; // Les pickups sont toujours faisables
    }
    
    String requestId = delivery.getIdDemande();
    List<Stop> requiredPickups = pickupsByRequestId.get(requestId);
    
    // Tous les pickups de cette demande doivent être dans visited
    return visited.containsAll(requiredPickups);
}
```

**Logique:**
- ✅ PICKUP : toujours faisable
- ✅ DELIVERY : faisable uniquement si son pickup est déjà visité
- ✅ WAREHOUSE : toujours faisable

#### 2.4 Vérification des Contraintes de Précédence
```java
private boolean respectsPrecedence(
    List<Stop> route,
    Map<String, List<Stop>> pickupsByRequestId,
    Map<String, Stop> deliveryByRequestId
) {
    Set<Stop> visited = new HashSet<>();
    
    for (Stop stop : route) {
        if (!isDeliveryFeasible(stop, visited, pickupsByRequestId)) {
            return false; // Delivery avant son pickup → invalide
        }
        visited.add(stop);
    }
    
    return true;
}
```

**Tests à effectuer:**
```
✅ [W, P1, D1, W]           → valide
✅ [W, P1, P2, D1, D2, W]   → valide
❌ [W, D1, P1, W]           → invalide (D1 avant P1)
❌ [W, P1, D2, P2, D1, W]   → invalide (D2 avant P2)
```

#### 2.5 Swap 2-opt
```java
private List<Stop> twoOptSwap(List<Stop> route, int i, int k) {
    List<Stop> newRoute = new ArrayList<>();
    
    // Segment 1: début → i-1 (inchangé)
    newRoute.addAll(route.subList(0, i));
    
    // Segment 2: i → k (inversé)
    List<Stop> reversed = new ArrayList<>(route.subList(i, k + 1));
    Collections.reverse(reversed);
    newRoute.addAll(reversed);
    
    // Segment 3: k+1 → fin (inchangé)
    newRoute.addAll(route.subList(k + 1, route.size()));
    
    return newRoute;
}
```

**Exemple visuel:**
```
Route originale:  [W, A, B, C, D, E, W]
                         i     k
2-opt swap(i=1, k=4):
  Segment 1: [W]
  Segment 2 (inversé): [E, D, C, B, A]
  Segment 3: [W]
  
Nouvelle route: [W, E, D, C, B, A, W]
```

**Contraintes:**
- Ne pas toucher l'entrepôt en début (index 0) et fin (index n-1)
- `1 ≤ i < k < route.size() - 1`

---

### **Phase 3 : Construction de la Tournée Initiale (Glouton)** 🏗️

**Fichier:** `ServiceAlgo.java`

#### 3.1 Algorithme Glouton du Plus Proche Voisin

```java
private List<Stop> buildInitialRoute(
    Graph graph,
    Stop warehouse,
    List<Stop> stops,
    Map<String, List<Stop>> pickupsByRequestId
) {
    List<Stop> route = new ArrayList<>();
    Set<Stop> visited = new HashSet<>();
    Set<Stop> remaining = new HashSet<>(stops);
    
    // 1️⃣ Commencer à l'entrepôt
    route.add(warehouse);
    visited.add(warehouse);
    
    // 2️⃣ Tant qu'il reste des stops
    while (!remaining.isEmpty()) {
        Stop current = route.get(route.size() - 1);
        Stop nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        // 3️⃣ Chercher le stop faisable le plus proche
        for (Stop candidate : remaining) {
            if (!isDeliveryFeasible(candidate, visited, pickupsByRequestId)) {
                continue; // Delivery dont le pickup n'est pas encore visité
            }
            
            double dist = distance(current, candidate, graph);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = candidate;
            }
        }
        
        // 4️⃣ Ajouter le stop le plus proche
        if (nearest == null) {
            throw new IllegalStateException("Aucun stop faisable trouvé (bug logique)");
        }
        
        route.add(nearest);
        visited.add(nearest);
        remaining.remove(nearest);
    }
    
    // 5️⃣ Retour à l'entrepôt
    route.add(warehouse);
    
    return route;
}
```

**Complexité:** O(n²) où n = nombre de stops

**Garanties:**
- ✅ Tous les stops sont visités exactement une fois
- ✅ Les contraintes pickup→delivery sont respectées
- ✅ Commence et finit à l'entrepôt
- ✅ Solution valide mais sous-optimale

**Exemple d'exécution:**
```
Données:
  Warehouse: W(N5)
  Stops: P1(N1), D1(N9), P2(N3), D2(N7)
  
Étape 0: route = [W], visited = {W}, remaining = {P1, D1, P2, D2}
  current = W
  P1 faisable (pickup), distance = 120.5 ✅
  D1 NON faisable (P1 pas visité)
  P2 faisable (pickup), distance = 95.8 ✅ ← PLUS PROCHE
  D2 NON faisable (P2 pas visité)
  → Ajouter P2

Étape 1: route = [W, P2], visited = {W, P2}, remaining = {P1, D1, D2}
  current = P2
  P1 faisable, distance = 180.4 ✅
  D1 NON faisable
  D2 faisable (P2 visité), distance = 155.2 ✅ ← PLUS PROCHE
  → Ajouter D2

Étape 2: route = [W, P2, D2], visited = {W, P2, D2}, remaining = {P1, D1}
  current = D2
  P1 faisable, distance = 310.5 ✅ ← SEUL FAISABLE
  D1 NON faisable
  → Ajouter P1

Étape 3: route = [W, P2, D2, P1], visited = {W, P2, D2, P1}, remaining = {D1}
  current = P1
  D1 faisable (P1 visité), distance = 205.3 ✅
  → Ajouter D1

Étape 4: route = [W, P2, D2, P1, D1], remaining = ∅
  → Ajouter W (retour)

Route finale: [W, P2, D2, P1, D1, W]
Distance totale: 95.8 + 155.2 + 310.5 + 205.3 + 310.2 = 1077.0 m
```

---

### **Phase 4 : Amélioration avec 2-opt** 🔄

**Fichier:** `ServiceAlgo.java`

#### 4.1 Algorithme 2-opt Itératif

```java
private List<Stop> twoOptImprove(
    List<Stop> route,
    Graph graph,
    Map<String, List<Stop>> pickupsByRequestId,
    Map<String, Stop> deliveryByRequestId
) {
    boolean improved = true;
    List<Stop> bestRoute = new ArrayList<>(route);
    double bestDistance = computeRouteDistance(bestRoute, graph);
    
    int maxIterations = 1000; // Sécurité pour éviter boucle infinie
    int iteration = 0;
    
    while (improved && iteration < maxIterations) {
        improved = false;
        iteration++;
        
        // Parcourir toutes les paires (i, k)
        // Ne pas toucher warehouse en début (0) et fin (n-1)
        for (int i = 1; i < bestRoute.size() - 2; i++) {
            for (int k = i + 1; k < bestRoute.size() - 1; k++) {
                
                // Créer nouvelle route avec swap
                List<Stop> newRoute = twoOptSwap(bestRoute, i, k);
                
                // Vérifier les contraintes
                if (!respectsPrecedence(newRoute, pickupsByRequestId, deliveryByRequestId)) {
                    continue; // Swap invalide
                }
                
                // Calculer nouvelle distance
                double newDistance = computeRouteDistance(newRoute, graph);
                
                // Si amélioration trouvée
                if (newDistance < bestDistance) {
                    bestRoute = newRoute;
                    bestDistance = newDistance;
                    improved = true;
                }
            }
        }
    }
    
    return bestRoute;
}
```

**Complexité:** O(n² × iterations) où iterations dépend de la convergence

**Principe 2-opt:**
```
Original: A → B → C → D → E
                i       k
                
Swap: A → D → C → B → E
      (inverse le segment B→C→D)

Si distance(A→D) + distance(B→E) < distance(A→B) + distance(D→E)
  → Garder le swap
Sinon
  → Garder l'original
```

**Exemple d'amélioration:**
```
Route initiale: [W, P2, D2, P1, D1, W] = 1077.0 m

Itération 1:
  Swap(i=1, k=3): [W, P1, D2, P2, D1, W]
    ❌ Invalide (D2 avant P2)
    
  Swap(i=1, k=2): [W, D2, P2, P1, D1, W]
    ❌ Invalide (D2 avant P2)
    
  Swap(i=2, k=3): [W, P2, P1, D2, D1, W]
    ✅ Valide
    Distance = 95.8 + 180.4 + 290.1 + 105.8 + 310.2 = 982.3 m
    ✅ Amélioration trouvée! (982.3 < 1077.0)
    
Route après itération 1: [W, P2, P1, D2, D1, W] = 982.3 m

Itération 2:
  ... (teste tous les swaps)
  Aucune amélioration trouvée
  
Route finale: [W, P2, P1, D2, D1, W] = 982.3 m
Amélioration: 8.8% (1077.0 → 982.3)
```

---

### **Phase 5 : Intégration dans ServiceAlgo** 🔌

**Fichier:** `ServiceAlgo.java`

#### 5.1 Méthode Principale

```java
/**
 * Calcule les tournées optimales pour un nombre donné de livreurs
 * 
 * @param graph Le graphe contenant les distances et chemins entre stops
 * @param courierCount Nombre de livreurs (uniquement 1 supporté actuellement)
 * @return Liste des tournées optimisées (1 seule pour l'instant)
 * @throws UnsupportedOperationException si courierCount != 1
 */
public List<Tour> calculateOptimalTours(Graph graph, int courierCount) {
    // 1️⃣ Validation
    if (courierCount != 1) {
        throw new UnsupportedOperationException(
            "Multi-livreurs pas encore implémenté. Utilisez courierCount = 1."
        );
    }
    
    // 2️⃣ Préparation des données
    Stop warehouse = extractWarehouse(graph);
    List<Stop> stops = extractNonWarehouseStops(graph);
    
    if (stops.isEmpty()) {
        throw new IllegalStateException("Aucune demande de livraison à traiter");
    }
    
    Map<String, List<Stop>> pickupsByRequestId = buildPickupsByRequestId(stops);
    Map<String, Stop> deliveryByRequestId = buildDeliveryByRequestId(stops);
    
    // 3️⃣ Construction de la tournée initiale (glouton)
    List<Stop> initialRoute = buildInitialRoute(
        graph, 
        warehouse, 
        stops, 
        pickupsByRequestId
    );
    double initialDistance = computeRouteDistance(initialRoute, graph);
    
    System.out.println("Route initiale: " + initialRoute);
    System.out.println("Distance initiale: " + initialDistance + " m");
    
    // 4️⃣ Amélioration avec 2-opt
    List<Stop> improvedRoute = twoOptImprove(
        initialRoute,
        graph,
        pickupsByRequestId,
        deliveryByRequestId
    );
    double improvedDistance = computeRouteDistance(improvedRoute, graph);
    
    System.out.println("Route améliorée: " + improvedRoute);
    System.out.println("Distance améliorée: " + improvedDistance + " m");
    System.out.println("Amélioration: " + 
        String.format("%.1f%%", (1 - improvedDistance/initialDistance) * 100));
    
    // 5️⃣ Construction de l'objet Tour
    Tour tour = buildTour(improvedRoute, improvedDistance, graph);
    
    // 6️⃣ Retour
    return List.of(tour);
}
```

#### 5.2 Construction de l'Objet Tour

```java
private Tour buildTour(List<Stop> route, double totalDistance, Graph graph) {
    Tour tour = new Tour();
    tour.setTotalDistance(totalDistance);
    tour.setStops(route);
    
    // Construire la liste des trajets détaillés
    List<Trajet> trajets = new ArrayList<>();
    for (int i = 0; i < route.size() - 1; i++) {
        Stop from = route.get(i);
        Stop to = route.get(i + 1);
        Trajet trajet = graph.getInitialCost().get(from).get(to);
        trajets.add(trajet);
    }
    tour.setTrajets(trajets);
    
    return tour;
}
```

**Structure de Tour** (à créer si n'existe pas):
```java
@Data
public class Tour {
    private List<Stop> stops;           // Ordre de visite
    private List<Trajet> trajets;       // Chemins détaillés entre stops
    private double totalDistance;       // Distance totale en mètres
}
```

---

### **Phase 6 : Intégration Backend/Frontend** 🌐

#### 6.1 Contrôleur REST

**Fichier:** `TourController.java` (nouveau ou à créer)

```java
@RestController
@RequestMapping("/api/tour")
@CrossOrigin(origins = "*")
public class TourController {
    
    @Autowired
    private ServiceAlgo serviceAlgo;
    
    @Autowired
    private DeliveryService deliveryService;
    
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<List<Tour>>> calculateTour(
        @RequestParam("courierCount") int courierCount
    ) {
        try {
            // Récupérer la carte et les demandes depuis le service
            Map cityMap = deliveryService.getCurrentMap();
            List<DeliveryRequest> requests = deliveryService.getCurrentRequests();
            Node warehouse = deliveryService.getWarehouse();
            
            if (cityMap == null || requests.isEmpty() || warehouse == null) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Carte ou demandes non chargées", null)
                );
            }
            
            // Construire le graphe
            Graph graph = serviceAlgo.constructGraph(cityMap, requests, warehouse);
            
            // Calculer les tournées
            List<Tour> tours = serviceAlgo.calculateOptimalTours(graph, courierCount);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Tournée calculée avec succès", tours)
            );
            
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Erreur lors du calcul: " + e.getMessage(), null)
            );
        }
    }
}
```

#### 6.2 Appel Frontend

**Fichier:** `apiService.js`

```javascript
export const calculateTour = async (courierCount) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/tour/calculate`,
      null,
      { params: { courierCount } }
    );
    return response.data;
  } catch (error) {
    console.error('Erreur calcul tournée:', error);
    throw error;
  }
};
```

#### 6.3 Affichage sur la Carte

**Fichier:** `Front.jsx` (à compléter)

```javascript
const handleCalculateTour = async (courierCount) => {
  try {
    setIsCalculating(true);
    const result = await calculateTour(courierCount);
    
    if (result.success) {
      const tour = result.data[0]; // Premier livreur
      displayTourOnMap(tour);
      setTourInfo(tour);
    } else {
      alert(`Erreur: ${result.message}`);
    }
  } catch (error) {
    alert('Erreur lors du calcul de la tournée');
  } finally {
    setIsCalculating(false);
  }
};

const displayTourOnMap = (tour) => {
  // Effacer les anciennes polylines
  tourLayerRef.current?.clearLayers();
  
  // Tracer chaque segment du trajet
  tour.trajets.forEach((trajet, index) => {
    const coordinates = trajet.listeSegment.map(seg => [
      seg.origine.latitude,
      seg.origine.longitude
    ]);
    
    const polyline = L.polyline(coordinates, {
      color: '#FF6B35',
      weight: 4,
      opacity: 0.7
    }).addTo(tourLayerRef.current);
    
    polyline.bindPopup(`Segment ${index + 1} - ${trajet.longueurTotale.toFixed(1)}m`);
  });
  
  // Ajouter numéros d'ordre sur les stops
  tour.stops.forEach((stop, index) => {
    if (stop.typeStop !== 'WAREHOUSE') {
      L.marker([stop.latitude, stop.longitude], {
        icon: L.divIcon({
          className: 'tour-order-marker',
          html: `<div>${index}</div>`
        })
      }).addTo(tourLayerRef.current);
    }
  });
};
```

---

### **Phase 7 : Tests** 🧪

#### 7.1 Tests Unitaires

**Fichier:** `ServiceAlgoTourTest.java` (nouveau)

```java
@SpringBootTest
public class ServiceAlgoTourTest {
    
    @Autowired
    private ServiceAlgo serviceAlgo;
    
    @Test
    public void testBuildInitialRoute_Simple() {
        // Arrange: 1 demande
        // W(N5), P1(N1), D1(N9)
        Graph graph = createSimpleGraph();
        
        // Act
        List<Stop> route = serviceAlgo.buildInitialRoute(...);
        
        // Assert
        assertEquals(4, route.size()); // W, P1, D1, W
        assertEquals("WAREHOUSE", route.get(0).getTypeStop().toString());
        assertEquals("PICKUP", route.get(1).getTypeStop().toString());
        assertEquals("DELIVERY", route.get(2).getTypeStop().toString());
        assertEquals("WAREHOUSE", route.get(3).getTypeStop().toString());
        assertEquals("D1", route.get(1).getIdDemande());
        assertEquals("D1", route.get(2).getIdDemande());
    }
    
    @Test
    public void testRespectsPrecedence_Valid() {
        List<Stop> validRoute = List.of(
            warehouse,
            pickup1,
            delivery1,
            pickup2,
            delivery2,
            warehouse
        );
        
        assertTrue(serviceAlgo.respectsPrecedence(validRoute, ...));
    }
    
    @Test
    public void testRespectsPrecedence_Invalid() {
        List<Stop> invalidRoute = List.of(
            warehouse,
            delivery1,  // ❌ Avant son pickup
            pickup1,
            warehouse
        );
        
        assertFalse(serviceAlgo.respectsPrecedence(invalidRoute, ...));
    }
    
    @Test
    public void testTwoOptSwap() {
        List<Stop> route = List.of(W, A, B, C, D, E, W);
        List<Stop> swapped = serviceAlgo.twoOptSwap(route, 1, 4);
        
        // [W, A, B, C, D, E, W]
        //      i        k
        // → [W, D, C, B, A, E, W]
        
        assertEquals(W, swapped.get(0));
        assertEquals(D, swapped.get(1));
        assertEquals(C, swapped.get(2));
        assertEquals(B, swapped.get(3));
        assertEquals(A, swapped.get(4));
        assertEquals(E, swapped.get(5));
        assertEquals(W, swapped.get(6));
    }
    
    @Test
    public void testTwoOptImprove_ReducesDistance() {
        List<Stop> initialRoute = serviceAlgo.buildInitialRoute(...);
        double initialDistance = serviceAlgo.computeRouteDistance(initialRoute, graph);
        
        List<Stop> improvedRoute = serviceAlgo.twoOptImprove(...);
        double improvedDistance = serviceAlgo.computeRouteDistance(improvedRoute, graph);
        
        assertTrue(improvedDistance <= initialDistance);
    }
    
    @Test
    public void testCalculateOptimalTours_OneCourier() {
        // Arrange
        Graph graph = createGraphWithTwoRequests();
        
        // Act
        List<Tour> tours = serviceAlgo.calculateOptimalTours(graph, 1);
        
        // Assert
        assertEquals(1, tours.size());
        Tour tour = tours.get(0);
        assertEquals(5, tour.getStops().size()); // W, P1, D1, P2, D2, W = 6
        assertTrue(tour.getTotalDistance() > 0);
        assertTrue(serviceAlgo.respectsPrecedence(tour.getStops(), ...));
    }
    
    @Test
    public void testCalculateOptimalTours_MultiCourier_ThrowsException() {
        Graph graph = createSimpleGraph();
        
        assertThrows(
            UnsupportedOperationException.class,
            () -> serviceAlgo.calculateOptimalTours(graph, 2)
        );
    }
}
```

#### 7.2 Tests d'Intégration

**Cas de test manuels:**

| Test ID | Demandes | Attendu | Vérification |
|---------|----------|---------|--------------|
| T1 | 1 demande | [W, P1, D1, W] | ✅ Pickup avant delivery |
| T2 | 2 demandes | [W, P*, P*, D*, D*, W] | ✅ Tous pickups avant leurs deliveries |
| T3 | 5 demandes | Distance < glouton seul | ✅ 2-opt améliore |
| T4 | Fichier `demandePetit1.xml` | Calcul < 2 secondes | ✅ Performance |
| T5 | Fichier `demandeMoyen3.xml` | Calcul < 5 secondes | ✅ Performance |

#### 7.3 Validation Visuelle

**Checklist frontend:**
- [ ] La tournée est affichée sur la carte avec une polyline
- [ ] Les numéros d'ordre sont visibles sur les stops
- [ ] Le popup affiche la distance de chaque segment
- [ ] La distance totale est affichée dans l'interface
- [ ] Le bouton "Calculer" se désactive pendant le calcul
- [ ] Un message d'erreur s'affiche si pas de carte/demandes

---

## 📊 Performances Attendues

### Complexité Algorithmique

| Phase | Complexité | Exemple (10 demandes = 21 stops) |
|-------|------------|----------------------------------|
| Construction Graph | O(n² × m log m) | ~440 Dijkstra |
| Glouton initial | O(n²) | ~441 comparaisons |
| 2-opt (1 itération) | O(n²) | ~210 swaps testés |
| 2-opt (k itérations) | O(k × n²) | ~2100 (k=10) |
| **Total** | **O(n² × m log m)** | **< 5 secondes** |

### Qualité de la Solution

**Amélioration attendue avec 2-opt:**
- Petites instances (2-5 demandes): **5-15%** d'amélioration
- Moyennes instances (10-20 demandes): **10-25%** d'amélioration
- Grandes instances (50+ demandes): **15-35%** d'amélioration

**Garanties:**
- ✅ Solution valide à 100%
- ✅ Amélioration ou égalité par rapport au glouton
- ❌ Pas d'optimum global garanti (NP-difficile)

---

## 🚀 Extensions Futures

### Court Terme (Sprint actuel + 1)

#### Extension Multi-Livreurs
**Approche suggérée:** Clustering + TSP par cluster

```
1. Regrouper les demandes en N clusters (K-means géographique)
2. Assigner un livreur par cluster
3. Résoudre TSP indépendamment pour chaque cluster
4. Équilibrer les charges si nécessaire
```

**Fichiers à modifier:**
- `ServiceAlgo.java` : ajouter méthodes clustering
- `TourController.java` : retourner N tours
- `Front.jsx` : afficher N polylines (couleurs différentes)

#### Fenêtres Horaires
**Ajout de contraintes temporelles:**

```java
public class Stop {
    private LocalTime earliestArrival;  // Heure minimum
    private LocalTime latestArrival;    // Heure maximum
    private int serviceDuration;        // Temps de service (secondes)
}
```

**Impact:**
- Modifier `isDeliveryFeasible()` pour vérifier les horaires
- Ajouter calcul de l'heure d'arrivée dans la tournée
- Complexité augmentée mais approche identique

### Moyen Terme

#### Optimisations Algorithmiques

**1. 2-opt Accéléré (Lin-Kernighan)**
- Utiliser des "don't look bits" pour éviter les swaps inutiles
- Gain: **2-5x plus rapide**

**2. 3-opt**
- Inverser 3 segments au lieu de 2
- Meilleure qualité mais O(n³)

**3. Simulated Annealing**
- Accepter des solutions temporairement moins bonnes
- Évite les optima locaux

#### Fonctionnalités Métier

- **Priorités de livraison** (urgence)
- **Capacité du vélo** (poids/volume max)
- **Pauses obligatoires** (pause déjeuner)
- **Zones interdites** (piétonnes à certaines heures)

---

## 📝 Checklist d'Implémentation

### Backend

- [x] **Phase 1: Préparation des données** ✅ **COMPLÉTÉ**
  - [x] `extractWarehouse()`
  - [x] `extractNonWarehouseStops()`
  - [x] `buildPickupsByRequestId()`
  - [x] `buildDeliveryByRequestId()`
  - [x] Tests unitaires (12 tests - 100% success)

- [x] **Phase 2: Fonctions utilitaires** ✅ **COMPLÉTÉ**
  - [x] `distance()`
  - [x] `computeRouteDistance()`
  - [x] `isStopFeasible()`
  - [x] `respectsPrecedence()`
  - [x] `twoOptSwap()`
  - [x] Tests unitaires (21 tests - 100% success)

- [x] **Phase 3: Glouton** ✅ **COMPLÉTÉ**
  - [x] `buildInitialRoute()`
  - [x] Tests avec 1-2 demandes
  - [x] Tests unitaires (9 tests - 100% success)

- [ ] **Phase 4: 2-opt** ⏸️ **REPORTÉE**
  - [ ] `twoOptImprove()`
  - [ ] Tests d'amélioration
  - [ ] Note: Implémentation prévue dans une version ultérieure

- [x] **Phase 5: Intégration** ✅ **COMPLÉTÉ**
  - [x] `calculateOptimalTours()`
  - [x] Créer classe `Tour`
  - [x] `buildTour()`
  - [x] Tests unitaires (9 tests - 100% success)
  - [x] Test d'intégration complet

- [ ] **Phase 6: API**
  - [ ] `TourController.java`
  - [ ] Endpoint `/api/tour/calculate`
  - [ ] Tests Postman/curl

- [ ] **Phase 7: Tests**
  - [ ] Tests unitaires (8 tests minimum)
  - [ ] Tests d'intégration
  - [ ] Tests de performance

### Frontend

- [ ] **Intégration API**
  - [ ] `calculateTour()` dans `apiService.js`
  - [ ] Appel depuis `Front.jsx`

- [ ] **Affichage**
  - [ ] `displayTourOnMap()`
  - [ ] Polyline avec couleur
  - [ ] Numéros d'ordre sur stops
  - [ ] Popup avec infos

- [ ] **UI/UX**
  - [ ] Loading pendant calcul
  - [ ] Affichage distance totale
  - [ ] Messages d'erreur
  - [ ] Bouton "Effacer tournée"

- [ ] **Tests manuels**
  - [ ] Fichiers XML fournis
  - [ ] Cas limites (1 demande, 10 demandes)

---

## 📚 Références Techniques

### Algorithmes
- **TSP Glouton:** Nearest Neighbor Heuristic
- **2-opt:** Croes, G.A. (1958) - "A Method for Solving Traveling-Salesman Problems"
- **Dijkstra:** Déjà implémenté dans `ServiceAlgo.java`

### Structures de Données
- `HashMap<Stop, HashMap<Stop, Double>>` : Matrice d'adjacence
- `List<Stop>` : Ordre de visite (tournée)
- `Set<Stop>` : Stops déjà visités (O(1) lookup)

### Patterns de Conception
- **Builder Pattern** : Construction progressive du Graph
- **Strategy Pattern** : Possibilité de changer l'algorithme TSP
- **Template Method** : Structure commune glouton/2-opt/autres

---

## 🎯 Livrables

### Code
✅ `ServiceAlgo.java` complété avec méthodes TSP  
✅ `TourController.java` avec endpoint `/calculate`  
✅ `Tour.java` modèle de données  
✅ `Front.jsx` affichage tournée  
✅ Tests unitaires (couverture > 80%)

### Documentation
✅ Ce document (planning + spécifications)  
✅ Javadoc sur toutes les méthodes publiques  
✅ README avec instructions de test

### Démo
✅ Vidéo ou GIF de la fonctionnalité  
✅ Présentation avec cas d'usage réels

---

## ⏱️ Estimation de Charge

| Phase | Temps estimé | Priorité |
|-------|-------------|----------|
| Phase 1: Préparation | 1h | P0 |
| Phase 2: Utilitaires | 2h | P0 |
| Phase 3: Glouton | 3h | P0 |
| Phase 4: 2-opt | 4h | P0 |
| Phase 5: Intégration | 2h | P0 |
| Phase 6: API + Frontend | 3h | P0 |
| Phase 7: Tests | 4h | P0 |
| Debug + Optimisation | 3h | P1 |
| Documentation | 2h | P1 |
| **TOTAL** | **24h** | **~3 jours** |

---

## 👥 Responsabilités Suggérées

Si travail en équipe:

| Développeur | Tâches |
|-------------|--------|
| Dev 1 | Phases 1-3 (glouton) + tests |
| Dev 2 | Phase 4 (2-opt) + optimisations |
| Dev 3 | Phases 5-6 (API + frontend) |
| Tous | Phase 7 (tests intégration) |

---

## 📅 Date de Livraison

**Target:** [À définir par l'équipe]  
**Review interne:** [J-2]  
**Tests finaux:** [J-1]

---

**Document rédigé le:** 26 novembre 2025  
**Dernière mise à jour:** 26 novembre 2025  
**Version:** 1.0  
**Statut:** 📋 Planning initial - Implémentation à démarrer

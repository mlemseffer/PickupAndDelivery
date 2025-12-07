# 📋 Planning Professionnel - Extension Multi-Coursiers

**Projet :** Application Pickup & Delivery  
**Branche :** zeliecoupey  
**Date de planification :** 6 décembre 2025  
**Objectif :** Extension de 1 coursier → N coursiers (1-10) avec distribution automatique FIFO et réassignation manuelle  

---

## 📊 Vue d'ensemble

### État actuel
- ✅ Calcul de tournée optimisée pour **1 seul coursier**
- ✅ Algorithme : Glouton + 2-opt
- ✅ Interface frontend fonctionnelle
- ✅ API REST opérationnelle
- ❌ Calcul de temps **non implémenté** (uniquement distances)
- ❌ Multi-coursiers **non fonctionnel** (infrastructure partielle)

### Objectif final
- ✅ Support de **1 à 10 coursiers simultanés**
- ✅ Distribution automatique **FIFO strict** après optimisation globale
- ✅ Contrainte temporelle **4 heures maximum** par tournée
- ✅ Calcul de temps intégré (distance + temps de service)
- ✅ Interface utilisateur avec sélecteur et visualisation multi-tours
- ✅ Réassignation manuelle (hors scope initial mais infrastructure prévue)

---

## 🎯 Contraintes Critiques

### ⚠️ ORDRE IMPÉRATIF DES OPÉRATIONS

```
1. Optimisation GLOBALE (tous les stops ensemble)
   ↓
2. Distribution FIFO STRICTE (Courier 1, puis 2, puis 3...)
   ↓
3. Affichage des N tournées
```

**❌ NE JAMAIS :**
- Distribuer AVANT l'optimisation
- Optimiser chaque coursier séparément
- Répartir de manière équilibrée

**✅ TOUJOURS :**
- Optimiser globalement d'abord
- Distribuer après en FIFO pur
- Respecter la contrainte 4h

### 🕐 Contrainte Temporelle

| Paramètre | Valeur |
|-----------|--------|
| **Temps max par tournée** | 4 heures (14 400 secondes) |
| **Vitesse coursier** | 15 km/h = 4.17 m/s |
| **Temps pickup** | ~5 minutes (300 secondes) |
| **Temps delivery** | ~5 minutes (300 secondes) |
| **Retour entrepôt** | Inclus dans le calcul |

**Formule de calcul :**
```java
tempsTournee = Σ(distance_trajet / 4.17) 
             + Σ(pickupDurationSec) 
             + Σ(deliveryDurationSec)
```

### 🔗 Contrainte de Précédence

- **Paire indivisible :** pickup et delivery d'une même demande DOIVENT être dans la même tournée
- **Ordre respecté :** pickup AVANT delivery
- **Si dépassement 4h :** toute la demande (pickup + delivery) va au coursier suivant

---

## 📈 Architecture de la Solution

### Flux Algorithmique

```
┌─────────────────────────────────────────────────────────────┐
│ 1. PRÉPARATION                                              │
│    - Validation (1 ≤ courierCount ≤ 10)                    │
│    - Construction StopSet (warehouse + pickups + deliveries)│
│    - Construction Graph (matrice de distances Dijkstra)     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. OPTIMISATION GLOBALE (1 seule tournée géante)           │
│    - Algorithme glouton (plus proche voisin)               │
│    - Optimisation 2-opt (élimination croisements)          │
│    - Résultat : route[warehouse, s1, s2, ..., sN, warehouse]│
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. CALCUL DE TEMPS (NOUVEAU)                               │
│    - Pour chaque trajet : temps = distance / vitesse       │
│    - Pour chaque stop : ajouter pickupDuration/deliveryDuration│
│    - Résultat : tempsAccumulé[i] pour chaque stop          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. DISTRIBUTION FIFO STRICTE (NOUVEAU)                     │
│    Courier currentCourier = 1                               │
│    double tempsActuel = 0                                   │
│    List<Stop> tourActuelle = [warehouse]                    │
│                                                             │
│    POUR chaque stop dans route optimisée :                 │
│      SI (typeStop == PICKUP) :                             │
│        - Calculer tempsAvecDemande (pickup + delivery + trajets)│
│        - SI (tempsActuel + tempsAvecDemande > 4h) :        │
│            * Fermer tourActuelle (retour warehouse)        │
│            * SI (currentCourier < courierCount) :          │
│                + currentCourier++                           │
│                + Créer nouvelle tourActuelle               │
│            * SINON : marquer demande NON ASSIGNÉE          │
│        - SINON :                                            │
│            * Ajouter pickup à tourActuelle                  │
│            * Ajouter delivery (plus loin) à tourActuelle   │
│            * tempsActuel += tempsAvecDemande               │
│                                                             │
│    Fermer la dernière tournée                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. CONSTRUCTION DES TOURS                                  │
│    - Pour chaque tournée : buildTour(stops, distance, temps)│
│    - Assignation courierId (1, 2, 3, ...)                  │
│    - Calcul métriques (distance, durée, nb demandes)       │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. RETOUR AU FRONTEND                                      │
│    - List<Tour> avec 1 à N tours                           │
│    - Chaque tour a son courierId, stops, trajets, métriques│
│    - Warnings si demandes non assignées                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗓️ Planning de Développement

### Phase 1 : Infrastructure Calcul de Temps (Jour 1-2) ⏱️ 8-12h

#### 1.1 Backend - Calcul de temps
**Fichiers à modifier :**
- `ServiceAlgo.java`
- `Tour.java`
- `Trajet.java`

**Tâches :**
- [ ] Ajouter constante `COURIER_SPEED_MS = 4.17` (15 km/h)
- [ ] Créer méthode `calculateTravelTime(double distance)` → temps en secondes
- [ ] Modifier `Trajet` : ajouter champ `private double durationSec`
- [ ] Modifier `Tour` : ajouter champs `private double totalDurationSec` et `private double totalDurationHours`
- [ ] Implémenter `computeRouteDuration(List<Stop> route, Graph graph, Map<String, Demand> demands)`
  ```java
  double totalTime = 0;
  for (int i = 0; i < route.size() - 1; i++) {
      Stop current = route.get(i);
      Stop next = route.get(i + 1);
      
      // Temps de trajet
      double distance = distance(current, next, graph);
      totalTime += distance / COURIER_SPEED_MS;
      
      // Temps de service au stop current
      if (current.getTypeStop() == TypeStop.PICKUP) {
          Demand demand = getDemandByStop(current, demands);
          totalTime += demand.getPickupDurationSec();
      } else if (current.getTypeStop() == TypeStop.DELIVERY) {
          Demand demand = getDemandByStop(current, demands);
          totalTime += demand.getDeliveryDurationSec();
      }
  }
  return totalTime;
  ```
- [ ] Modifier `buildTour()` pour calculer et stocker la durée totale

**Tests :**
- [ ] Test unitaire : `testCalculateTravelTime()` avec distance connue
- [ ] Test unitaire : `testComputeRouteDuration()` avec route simple
- [ ] Test validation : temps cohérent avec distance (distance/vitesse ≤ temps ≤ distance/vitesse + temps_service)

**Livrable :** Calcul de temps fonctionnel pour 1 coursier

---

### Phase 2 : Distribution FIFO Multi-Coursiers (Jour 2-4) ⏱️ 12-16h

#### 2.1 Backend - Algorithme de distribution

**Fichiers à créer/modifier :**
- `ServiceAlgo.java` (méthode `distributeFIFO()`)
- `TourDistributionResult.java` (nouveau DTO)

**Tâches :**

- [ ] Créer classe `TourDistributionResult`
  ```java
  public class TourDistributionResult {
      private List<Tour> tours;
      private List<String> unassignedDemandIds;
      private Map<Integer, TourMetrics> metricsByCourier;
      private DistributionWarnings warnings;
  }
  ```

- [ ] Créer classe `TourMetrics`
  ```java
  public class TourMetrics {
      private int courierId;
      private double totalDistance;
      private double totalDurationSec;
      private int requestCount;
      private int stopCount;
      private boolean exceedsTimeLimit;
  }
  ```

- [ ] Créer classe `DistributionWarnings`
  ```java
  public class DistributionWarnings {
      private boolean hasUnassignedDemands;
      private boolean hasTimeLimitExceeded;
      private List<String> messages;
  }
  ```

- [ ] Implémenter méthode `distributeFIFO()` (CRITIQUE)
  ```java
  private TourDistributionResult distributeFIFO(
      List<Stop> globalOptimizedRoute,
      Graph graph,
      int courierCount,
      Map<String, List<Stop>> pickupsByRequestId,
      Map<String, Stop> deliveryByRequestId,
      Map<String, Demand> demandMap
  ) {
      // Constantes
      final double TIME_LIMIT_SEC = 4 * 3600; // 4 heures
      
      // Structures de données
      List<Tour> tours = new ArrayList<>();
      List<String> unassignedDemandIds = new ArrayList<>();
      Map<Integer, TourMetrics> metricsByCourier = new HashMap<>();
      
      // État du coursier actuel
      int currentCourierId = 1;
      List<Stop> currentTourStops = new ArrayList<>();
      currentTourStops.add(warehouse); // Départ warehouse
      double currentTourTime = 0;
      Set<String> processedDemands = new HashSet<>();
      
      // Parcours FIFO de la route optimisée
      for (int i = 1; i < globalOptimizedRoute.size() - 1; i++) {
          Stop stop = globalOptimizedRoute.get(i);
          
          // Ignorer si déjà traité (cas delivery déjà ajouté avec son pickup)
          if (stop.getTypeStop() == TypeStop.DELIVERY) {
              continue; // Traité avec son pickup
          }
          
          // C'est un PICKUP : on doit évaluer toute la demande
          if (stop.getTypeStop() == TypeStop.PICKUP) {
              String demandId = stop.getIdDemande();
              
              if (processedDemands.contains(demandId)) {
                  continue; // Déjà assigné
              }
              
              // Trouver le delivery correspondant dans la route
              Stop deliveryStop = findDeliveryInRoute(
                  demandId, globalOptimizedRoute, i);
              
              if (deliveryStop == null) {
                  throw new IllegalStateException(
                      "Delivery non trouvé pour pickup " + demandId);
              }
              
              // Calculer le temps pour cette demande complète
              double demandTime = calculateDemandTime(
                  currentTourStops.get(currentTourStops.size() - 1),
                  stop,
                  deliveryStop,
                  globalOptimizedRoute,
                  graph,
                  demandMap.get(demandId)
              );
              
              // Temps avec retour warehouse
              double timeWithReturn = currentTourTime + demandTime 
                  + calculateReturnTime(deliveryStop, warehouse, graph);
              
              // Vérifier contrainte 4h
              if (timeWithReturn > TIME_LIMIT_SEC) {
                  // Fermer la tournée actuelle
                  currentTourStops.add(warehouse);
                  Tour completedTour = buildTour(
                      currentTourStops, 
                      computeRouteDistance(currentTourStops, graph),
                      currentTourTime,
                      graph
                  );
                  completedTour.setCourierId(currentCourierId);
                  tours.add(completedTour);
                  
                  // Passer au coursier suivant
                  if (currentCourierId < courierCount) {
                      currentCourierId++;
                      currentTourStops = new ArrayList<>();
                      currentTourStops.add(warehouse);
                      currentTourTime = 0;
                      
                      // Réessayer d'ajouter cette demande
                      i--; // Reculer pour retraiter ce pickup
                      continue;
                  } else {
                      // Plus de coursiers disponibles
                      unassignedDemandIds.add(demandId);
                      processedDemands.add(demandId);
                      continue;
                  }
              }
              
              // Ajouter la demande complète à la tournée actuelle
              currentTourStops.add(stop); // Pickup
              // Ajouter tous les stops entre pickup et delivery
              for (int j = i + 1; j <= findDeliveryIndex(
                  demandId, globalOptimizedRoute); j++) {
                  if (!currentTourStops.contains(globalOptimizedRoute.get(j))) {
                      currentTourStops.add(globalOptimizedRoute.get(j));
                  }
              }
              currentTourTime += demandTime;
              processedDemands.add(demandId);
          }
      }
      
      // Fermer la dernière tournée
      if (currentTourStops.size() > 1) {
          currentTourStops.add(warehouse);
          Tour lastTour = buildTour(
              currentTourStops,
              computeRouteDistance(currentTourStops, graph),
              currentTourTime,
              graph
          );
          lastTour.setCourierId(currentCourierId);
          tours.add(lastTour);
      }
      
      // Construire le résultat
      DistributionWarnings warnings = new DistributionWarnings();
      warnings.setHasUnassignedDemands(!unassignedDemandIds.isEmpty());
      // ... remplir warnings
      
      return new TourDistributionResult(
          tours, unassignedDemandIds, metricsByCourier, warnings);
  }
  ```

- [ ] Créer méthodes auxiliaires :
  - `findDeliveryInRoute()`
  - `calculateDemandTime()`
  - `calculateReturnTime()`
  - `findDeliveryIndex()`

- [ ] Modifier `calculateOptimalTours()` pour gérer courierCount > 1
  ```java
  public List<Tour> calculateOptimalTours(Graph graph, int courierCount) {
      // Validation
      if (courierCount < 1 || courierCount > 10) {
          throw new IllegalArgumentException(
              "courierCount doit être entre 1 et 10");
      }
      
      // ... phases existantes (préparation, glouton, 2-opt) ...
      
      // NOUVEAU : Distribution FIFO si multi-coursiers
      if (courierCount == 1) {
          // Comportement actuel (1 seul tour)
          Tour tour = buildTour(...);
          return Arrays.asList(tour);
      } else {
          // Multi-coursiers : distribution FIFO
          TourDistributionResult result = distributeFIFO(
              optimizedRoute, graph, courierCount, 
              pickupsByRequestId, deliveryByRequestId, demandMap
          );
          
          // Logging des warnings
          if (result.getWarnings().isHasUnassignedDemands()) {
              System.out.println("⚠️  ATTENTION : " + 
                  result.getUnassignedDemandIds().size() + 
                  " demandes non assignées (contrainte 4h)");
          }
          
          return result.getTours();
      }
  }
  ```

**Tests :**
- [ ] Test unitaire : `testDistributeFIFO_OneCourier()` (doit être identique au comportement actuel)
- [ ] Test unitaire : `testDistributeFIFO_TwoCouriers_ExactSplit()` (route qui tombe pile à 4h)
- [ ] Test unitaire : `testDistributeFIFO_ThreeCouriers_Overflow()` (demandes restantes)
- [ ] Test contrainte : `testFIFO_PairNotSplit()` (pickup et delivery dans même tournée)
- [ ] Test contrainte : `testFIFO_TimeLimit()` (aucune tournée > 4h)
- [ ] Test edge case : `testFIFO_ZeroCouriers()` → exception
- [ ] Test edge case : `testFIFO_ElevenCouriers()` → exception
- [ ] Test edge case : `testFIFO_SingleDemandTooLong()` → non assigné

**Livrable :** Distribution FIFO fonctionnelle avec validation temporelle

---

### Phase 3 : Backend - Endpoints et API (Jour 4-5) ⏱️ 6-8h

#### 3.1 Modification TourController

**Fichiers à modifier :**
- `TourController.java`

**Tâches :**

- [ ] Modifier endpoint `POST /api/tours/calculate`
  ```java
  @PostMapping("/calculate")
  public ResponseEntity<ApiResponse<List<Tour>>> calculateTour(
      @RequestParam(value = "courierCount", defaultValue = "1") int courierCount
  ) {
      try {
          // ... validations existantes ...
          
          // Validation courierCount
          if (courierCount < 1 || courierCount > 10) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.error(
                      "Le nombre de coursiers doit être entre 1 et 10"));
          }
          
          // ... construction StopSet et Graph ...
          
          // Calcul multi-tours
          List<Tour> tours = serviceAlgo.calculateOptimalTours(
              graph, courierCount);
          
          // Statistiques globales
          double totalDistance = tours.stream()
              .mapToDouble(Tour::getTotalDistance).sum();
          double maxDuration = tours.stream()
              .mapToDouble(Tour::getTotalDurationSec).max().orElse(0);
          
          System.out.println("✅ " + tours.size() + " tournée(s) calculée(s)");
          System.out.println("   Distance totale : " + totalDistance + " m");
          System.out.println("   Durée max : " + (maxDuration/3600) + " h");
          
          return ResponseEntity.ok(
              ApiResponse.success(
                  tours.size() + " tournée(s) calculée(s)", tours));
          
      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ApiResponse.error(e.getMessage()));
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ApiResponse.error("Erreur : " + e.getMessage()));
      }
  }
  ```

- [ ] Ajouter endpoint `GET /api/tours/metrics`
  ```java
  @GetMapping("/metrics")
  public ResponseEntity<ApiResponse<TourMetricsSummary>> getMetrics() {
      // Retourne statistiques globales de la dernière tournée calculée
      // (nécessite stockage en mémoire ou cache)
  }
  ```

**Tests :**
- [ ] Test API : `POST /calculate?courierCount=1` → 1 tour
- [ ] Test API : `POST /calculate?courierCount=3` → 1-3 tours
- [ ] Test API : `POST /calculate?courierCount=0` → erreur 400
- [ ] Test API : `POST /calculate?courierCount=11` → erreur 400
- [ ] Test intégration : appel complet carte + demandes + calcul

**Livrable :** API multi-coursiers fonctionnelle

---

### Phase 4 : Frontend - Sélecteur de Coursiers (Jour 5-6) ⏱️ 6-8h

#### 4.1 Composant CourierCountSelector

**Fichiers à créer/modifier :**
- `frontend/src/components/CourierCountSelector.jsx` (actuellement vide)
- `frontend/src/components/CourierCountModal.jsx` (à améliorer)

**Tâches :**

- [ ] Implémenter `CourierCountSelector.jsx`
  ```jsx
  import React from 'react';
  
  /**
   * Sélecteur visuel pour choisir le nombre de coursiers (1-10)
   */
  const CourierCountSelector = ({ value, onChange, disabled }) => {
    const courierOptions = Array.from({length: 10}, (_, i) => i + 1);
    
    return (
      <div className="courier-count-selector">
        <label className="block text-sm font-medium mb-2">
          Nombre de coursiers
        </label>
        
        {/* Sélecteur visuel avec boutons */}
        <div className="flex gap-2 flex-wrap">
          {courierOptions.map(count => (
            <button
              key={count}
              onClick={() => onChange(count)}
              disabled={disabled}
              className={`
                px-4 py-2 rounded-lg font-semibold transition-all
                ${value === count 
                  ? 'bg-blue-600 text-white scale-110 shadow-lg' 
                  : 'bg-gray-600 text-gray-300 hover:bg-gray-500'}
                ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
              `}
            >
              {count}
            </button>
          ))}
        </div>
        
        {/* Slider alternatif */}
        <div className="mt-4">
          <input
            type="range"
            min="1"
            max="10"
            value={value}
            onChange={(e) => onChange(parseInt(e.target.value))}
            disabled={disabled}
            className="w-full"
          />
          <div className="flex justify-between text-xs text-gray-400 mt-1">
            <span>1</span>
            <span>5</span>
            <span>10</span>
          </div>
        </div>
        
        {/* Indicateur de sélection */}
        <div className="mt-3 text-center">
          <span className="text-2xl font-bold text-blue-400">
            {value} coursier{value > 1 ? 's' : ''}
          </span>
        </div>
      </div>
    );
  };
  
  export default CourierCountSelector;
  ```

- [ ] Intégrer dans `Front.jsx`
  ```jsx
  // Ajouter dans la section actions
  <CourierCountSelector
    value={courierCount}
    onChange={setCourierCount}
    disabled={isCalculatingTour}
  />
  ```

**Tests :**
- [ ] Test UI : clic sur chaque bouton change la valeur
- [ ] Test UI : slider fonctionne correctement
- [ ] Test UI : désactivation quand calcul en cours
- [ ] Test visuel : bouton sélectionné bien mis en évidence

**Livrable :** Sélecteur de coursiers opérationnel

---

### Phase 5 : Frontend - Visualisation Multi-Tours (Jour 6-8) ⏱️ 12-16h

#### 5.1 Composant TourTabs

**Fichiers à créer :**
- `frontend/src/components/TourTabs.jsx`
- `frontend/src/components/TourStatistics.jsx`
- `frontend/src/components/CourierTourCard.jsx`

**Tâches :**

- [ ] Créer `TourTabs.jsx`
  ```jsx
  import React, { useState } from 'react';
  
  const TourTabs = ({ tours, deliveryRequestSet, onTourSelect }) => {
    const [selectedCourierId, setSelectedCourierId] = useState(
      tours.length > 0 ? tours[0].courierId : null
    );
    
    const handleTabClick = (courierId) => {
      setSelectedCourierId(courierId);
      onTourSelect(tours.find(t => t.courierId === courierId));
    };
    
    return (
      <div className="tour-tabs">
        {/* Onglets en haut */}
        <div className="flex border-b border-gray-600 mb-4">
          <button
            onClick={() => handleTabClick(null)}
            className={`px-4 py-2 font-medium ${
              selectedCourierId === null
                ? 'border-b-2 border-blue-500 text-blue-400'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            Vue globale
          </button>
          
          {tours.map(tour => (
            <button
              key={tour.courierId}
              onClick={() => handleTabClick(tour.courierId)}
              className={`px-4 py-2 font-medium flex items-center gap-2 ${
                selectedCourierId === tour.courierId
                  ? 'border-b-2 border-blue-500 text-blue-400'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              <span className="w-3 h-3 rounded-full" 
                    style={{backgroundColor: getCourierColor(tour.courierId)}} />
              Coursier {tour.courierId}
            </button>
          ))}
        </div>
        
        {/* Contenu de l'onglet */}
        <div className="tour-tab-content">
          {selectedCourierId === null ? (
            <GlobalStatistics tours={tours} />
          ) : (
            <CourierTourCard 
              tour={tours.find(t => t.courierId === selectedCourierId)}
              deliveryRequestSet={deliveryRequestSet}
            />
          )}
        </div>
      </div>
    );
  };
  ```

- [ ] Créer `TourStatistics.jsx`
  ```jsx
  const TourStatistics = ({ tour }) => {
    const durationHours = (tour.totalDurationSec / 3600).toFixed(2);
    const distanceKm = (tour.totalDistance / 1000).toFixed(2);
    const exceedsLimit = tour.totalDurationSec > 4 * 3600;
    
    return (
      <div className="grid grid-cols-2 gap-4 p-4 bg-gray-800 rounded-lg">
        <StatCard 
          label="Distance" 
          value={`${distanceKm} km`} 
          icon="📏"
        />
        <StatCard 
          label="Durée" 
          value={`${durationHours} h`}
          icon="⏱️"
          warning={exceedsLimit}
        />
        <StatCard 
          label="Demandes" 
          value={tour.requestCount} 
          icon="📦"
        />
        <StatCard 
          label="Stops" 
          value={tour.stopCount} 
          icon="📍"
        />
      </div>
    );
  };
  ```

- [ ] Créer `GlobalStatistics.jsx`
  ```jsx
  const GlobalStatistics = ({ tours }) => {
    const totalDistance = tours.reduce(
      (sum, t) => sum + t.totalDistance, 0);
    const totalDuration = tours.reduce(
      (sum, t) => sum + t.totalDurationSec, 0);
    const totalRequests = tours.reduce(
      (sum, t) => sum + t.requestCount, 0);
    const avgDuration = totalDuration / tours.length / 3600;
    const maxDuration = Math.max(
      ...tours.map(t => t.totalDurationSec)) / 3600;
    const minDuration = Math.min(
      ...tours.map(t => t.totalDurationSec)) / 3600;
    const balanceScore = ((4 - (maxDuration - minDuration)) / 4 * 100)
      .toFixed(0);
    
    return (
      <div className="space-y-6">
        <h3 className="text-xl font-bold">Statistiques Globales</h3>
        
        <div className="grid grid-cols-3 gap-4">
          <StatCard label="Coursiers" value={tours.length} icon="🚴" />
          <StatCard label="Distance totale" 
                    value={`${(totalDistance/1000).toFixed(1)} km`} 
                    icon="📏" />
          <StatCard label="Demandes" value={totalRequests} icon="📦" />
        </div>
        
        <div className="grid grid-cols-3 gap-4">
          <StatCard label="Durée moy." 
                    value={`${avgDuration.toFixed(2)} h`} 
                    icon="⏱️" />
          <StatCard label="Durée max" 
                    value={`${maxDuration.toFixed(2)} h`} 
                    icon="⬆️"
                    warning={maxDuration > 4} />
          <StatCard label="Durée min" 
                    value={`${minDuration.toFixed(2)} h`} 
                    icon="⬇️" />
        </div>
        
        <div className="bg-gray-800 p-4 rounded-lg">
          <div className="flex justify-between items-center mb-2">
            <span className="font-medium">Score d'équilibrage</span>
            <span className="text-2xl font-bold text-blue-400">
              {balanceScore}%
            </span>
          </div>
          <div className="w-full bg-gray-700 rounded-full h-4">
            <div 
              className="bg-blue-500 h-4 rounded-full transition-all"
              style={{width: `${balanceScore}%`}}
            />
          </div>
          <p className="text-xs text-gray-400 mt-2">
            Basé sur l'écart entre la tournée la plus longue et la plus courte
          </p>
        </div>
        
        {/* Liste des coursiers */}
        <div className="space-y-2">
          <h4 className="font-medium">Répartition par coursier</h4>
          {tours.map(tour => (
            <div key={tour.courierId} 
                 className="flex items-center gap-3 p-3 bg-gray-800 rounded">
              <div className="w-4 h-4 rounded-full" 
                   style={{backgroundColor: getCourierColor(tour.courierId)}} />
              <span className="font-medium">Coursier {tour.courierId}</span>
              <span className="text-gray-400">·</span>
              <span className="text-sm">{tour.requestCount} demandes</span>
              <span className="text-gray-400">·</span>
              <span className="text-sm">
                {(tour.totalDurationSec/3600).toFixed(2)} h
              </span>
              <span className="text-gray-400">·</span>
              <span className="text-sm">
                {(tour.totalDistance/1000).toFixed(2)} km
              </span>
            </div>
          ))}
        </div>
      </div>
    );
  };
  ```

- [ ] Modifier `MapViewer.jsx` pour afficher multi-tours
  ```jsx
  // Ajouter prop selectedCourierId
  const MapViewer = ({ 
    mapData, 
    tourData,  // Maintenant peut être array de tours
    selectedCourierId,  // null = tous, sinon = un seul
    ...
  }) => {
    // Filtrer les tours à afficher
    const toursToDisplay = selectedCourierId === null
      ? tourData  // Afficher tous
      : tourData.filter(t => t.courierId === selectedCourierId);
    
    return (
      <>
        {toursToDisplay.map(tour => (
          <TourPolylines 
            key={tour.courierId}
            tour={tour}
            color={getCourierColor(tour.courierId)}
            opacity={selectedCourierId === null ? 0.6 : 1}
          />
        ))}
      </>
    );
  };
  ```

- [ ] Créer palette de couleurs par coursier
  ```jsx
  const COURIER_COLORS = [
    '#FF6B6B',  // Rouge
    '#4ECDC4',  // Turquoise
    '#45B7D1',  // Bleu
    '#FFA07A',  // Orange
    '#98D8C8',  // Vert menthe
    '#F7DC6F',  // Jaune
    '#BB8FCE',  // Violet
    '#85C1E2',  // Bleu clair
    '#F8B739',  // Orange doré
    '#52B788',  // Vert forêt
  ];
  
  const getCourierColor = (courierId) => {
    return COURIER_COLORS[(courierId - 1) % COURIER_COLORS.length];
  };
  ```

**Tests :**
- [ ] Test UI : onglets changent le contenu affiché
- [ ] Test UI : statistiques globales correctes
- [ ] Test UI : statistiques par coursier correctes
- [ ] Test UI : couleurs distinctes pour chaque coursier
- [ ] Test visuel : carte affiche bien les N tournées
- [ ] Test visuel : sélection d'un coursier isole sa tournée

**Livrable :** Visualisation multi-tours complète et ergonomique

---

### Phase 6 : Tests et Validation (Jour 8-9) ⏱️ 8-12h

#### 6.1 Tests unitaires backend

**Fichiers à créer :**
- `backend/src/test/java/com/pickupdelivery/service/ServiceAlgoMultiCourierTest.java`

**Tâches :**

- [ ] Suite de tests `ServiceAlgoMultiCourierTest`
  ```java
  @Test
  void testCalculateOptimalTours_OneCourier_SameAsBefore() {
      // Vérifier compatibilité arrière
  }
  
  @Test
  void testCalculateOptimalTours_TwoCouriers_ValidSplit() {
      // Vérifier split correct
  }
  
  @Test
  void testCalculateOptimalTours_FiveCouriers_AllUsed() {
      // Vérifier tous coursiers utilisés si besoin
  }
  
  @Test
  void testCalculateOptimalTours_TimeLimitRespected() {
      // Aucune tournée > 4h
      for (Tour tour : tours) {
          assertTrue(tour.getTotalDurationSec() <= 14400);
      }
  }
  
  @Test
  void testCalculateOptimalTours_FIFOOrder() {
      // Vérifier ordre FIFO strict
  }
  
  @Test
  void testCalculateOptimalTours_PairNotSplit() {
      // Pickup et delivery dans même tour
  }
  
  @Test
  void testCalculateOptimalTours_UnassignedDemands() {
      // Gérer demandes non assignées
  }
  
  @Test
  void testCalculateOptimalTours_InvalidCourierCount_Zero() {
      assertThrows(IllegalArgumentException.class, ...);
  }
  
  @Test
  void testCalculateOptimalTours_InvalidCourierCount_Eleven() {
      assertThrows(IllegalArgumentException.class, ...);
  }
  ```

- [ ] Tests de temps
  ```java
  @Test
  void testCalculateTravelTime_15KmPerHour() {
      double distance = 1000; // 1 km
      double time = serviceAlgo.calculateTravelTime(distance);
      assertEquals(240, time, 1); // 1000m / 4.17 ≈ 240s
  }
  
  @Test
  void testComputeRouteDuration_WithServiceTime() {
      // Vérifier que pickup/delivery time est ajouté
  }
  ```

#### 6.2 Tests d'intégration

**Tâches :**

- [ ] Test complet flux utilisateur
  ```java
  @Test
  void testFullWorkflow_UploadMapAndDemands_CalculateMultipleTours() {
      // 1. Upload carte
      // 2. Upload demandes
      // 3. Calculer tournée avec 3 coursiers
      // 4. Vérifier résultat cohérent
  }
  ```

- [ ] Test de performance
  ```java
  @Test
  void testPerformance_50Demands_Under5Seconds() {
      long start = System.currentTimeMillis();
      List<Tour> tours = serviceAlgo.calculateOptimalTours(graph, 5);
      long elapsed = System.currentTimeMillis() - start;
      assertTrue(elapsed < 5000, "Calcul trop lent: " + elapsed + "ms");
  }
  ```

#### 6.3 Tests frontend

**Tâches :**

- [ ] Tests manuels UI
  - [ ] Sélecteur de coursiers réactif
  - [ ] Calcul avec 1 coursier = comportement actuel
  - [ ] Calcul avec 5 coursiers = affichage correct
  - [ ] Changement d'onglet = isolation visuelle
  - [ ] Statistiques cohérentes
  - [ ] Couleurs bien distinctes

- [ ] Tests edge cases UI
  - [ ] Aucune carte → erreur claire
  - [ ] Aucune demande → erreur claire
  - [ ] 10 coursiers pour 5 demandes → OK (certains vides)
  - [ ] Demandes impossibles à assigner → warning visible

**Livrable :** Suite de tests complète et validée

---

### Phase 7 : Documentation et Finalisation (Jour 9-10) ⏱️ 4-6h

#### 7.1 Documentation technique

**Fichiers à créer/modifier :**
- `MULTI_COURIERS_ARCHITECTURE.md`
- `README.md` (mise à jour)
- `API_DOCUMENTATION.md`

**Tâches :**

- [ ] Documenter algorithme FIFO
  - Schéma de flux
  - Pseudo-code commenté
  - Exemples de calcul de temps

- [ ] Documenter API
  - Endpoints avec exemples
  - Structure des réponses
  - Codes d'erreur

- [ ] Documenter composants frontend
  - Props de chaque composant
  - Flux de données
  - Événements

- [ ] Guide utilisateur
  - Comment sélectionner le nombre de coursiers
  - Interprétation des statistiques
  - Cas d'usage typiques

#### 7.2 Refactoring et nettoyage

**Tâches :**

- [ ] Supprimer les `System.out.println` en production → utiliser logger
- [ ] Optimiser imports
- [ ] Vérifier cohérence des noms de variables
- [ ] Ajouter javadoc manquante
- [ ] Formatter le code (backend + frontend)

#### 7.3 Préparation déploiement

**Tâches :**

- [ ] Vérifier variables d'environnement
- [ ] Tester en mode production (build optimized)
- [ ] Créer script de démarrage
- [ ] Vérifier CORS configuré correctement

**Livrable :** Documentation complète et code production-ready

---

## 📊 Récapitulatif des Livrables

| Phase | Durée | Livrable Principal |
|-------|-------|-------------------|
| 1. Calcul de temps | 8-12h | Temps calculé pour tous les trajets/tours |
| 2. Distribution FIFO | 12-16h | Algorithme de distribution multi-coursiers |
| 3. Endpoints API | 6-8h | API REST multi-coursiers fonctionnelle |
| 4. Sélecteur UI | 6-8h | Composant de sélection de coursiers |
| 5. Visualisation | 12-16h | Interface multi-tours avec onglets et stats |
| 6. Tests | 8-12h | Suite de tests complète (>90% couverture) |
| 7. Documentation | 4-6h | Documentation technique et utilisateur |
| **TOTAL** | **56-78h** | **Application multi-coursiers complète** |

---

## ⚠️ Risques et Mitigation

### Risques techniques

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|-----------|
| Algorithme FIFO trop lent | Moyenne | Élevé | Optimiser avec structures de données efficaces |
| Calcul de temps incorrect | Faible | Élevé | Tests unitaires exhaustifs avec cas connus |
| Bug dans distribution paires | Moyenne | Critique | Validation stricte à chaque étape |
| Frontend lourd avec 10 tours | Moyenne | Moyen | Virtualisation si nécessaire |
| Dépassement mémoire grandes cartes | Faible | Élevé | Limiter taille des cartes acceptées |

### Risques organisationnels

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|-----------|
| Dépassement délai | Moyenne | Moyen | Prioriser phases 1-3 (fonctionnel minimal) |
| Incompréhension contraintes | Faible | Critique | Valider avec exemples concrets dès phase 1 |
| Régression fonctionnalité 1 coursier | Faible | Élevé | Tests de non-régression automatisés |

---

## 🎯 Critères de Succès

### Critères fonctionnels

- ✅ Calcul de temps implémenté et cohérent
- ✅ Support de 1 à 10 coursiers
- ✅ Distribution FIFO stricte respectée
- ✅ Contrainte 4h respectée (aucune tournée > 4h)
- ✅ Paires pickup/delivery jamais séparées
- ✅ Interface utilisateur intuitive et ergonomique
- ✅ Visualisation claire des N tournées
- ✅ Statistiques globales et par coursier affichées

### Critères de qualité

- ✅ Couverture de tests > 90%
- ✅ Performance : calcul de 50 demandes en < 5 secondes
- ✅ Documentation complète (technique + utilisateur)
- ✅ Code production-ready (pas de console.log, logging propre)
- ✅ Compatibilité arrière (1 coursier = comportement actuel)
- ✅ Gestion d'erreurs exhaustive

### Critères de validation

- ✅ Tests unitaires : 100% passants
- ✅ Tests d'intégration : 100% passants
- ✅ Tests manuels UI : tous les scénarios validés
- ✅ Revue de code effectuée
- ✅ Validation utilisateur finale

---

## 📅 Échéancier Suggéré

### Planning sur 2 semaines (10 jours ouvrés)

```
Semaine 1
─────────
Lundi     : Phase 1 - Calcul de temps (backend)
Mardi     : Phase 2 - Distribution FIFO (début)
Mercredi  : Phase 2 - Distribution FIFO (fin)
Jeudi     : Phase 3 - Endpoints API
Vendredi  : Phase 4 - Sélecteur UI

Semaine 2
─────────
Lundi     : Phase 5 - Visualisation multi-tours (début)
Mardi     : Phase 5 - Visualisation multi-tours (fin)
Mercredi  : Phase 6 - Tests unitaires
Jeudi     : Phase 6 - Tests intégration + performance
Vendredi  : Phase 7 - Documentation + déploiement
```

### Jalons (Milestones)

- **J2 soir :** Calcul de temps fonctionnel ✓
- **J4 soir :** Distribution FIFO multi-coursiers ✓
- **J5 soir :** API complète testée ✓
- **J7 soir :** UI complète avec visualisation ✓
- **J9 soir :** Tous tests validés ✓
- **J10 soir :** Documentation et livraison finale ✓

---

## 🚀 Évolutions Futures (Hors Scope Initial)

### Phase 8 : Réassignation Manuelle (À planifier)

- Drag-and-drop de demandes entre coursiers
- Recalcul automatique après réassignation
- Validation des contraintes en temps réel
- Endpoint `POST /api/tours/reassign`

### Phase 9 : Optimisations Avancées

- Optimisation inter-coursiers (équilibrage automatique)
- Algorithmes génétiques pour améliorer la répartition
- Machine learning pour prédire durées réelles

### Phase 10 : Fonctionnalités Business

- Contraintes de fenêtres horaires
- Contraintes de capacité véhicule
- Priorités de livraison
- Persistance en base de données
- Historique des tournées
- Export PDF des feuilles de route

### Phase 11 : Temps Réel

- Suivi GPS des coursiers
- Mise à jour dynamique des tournées
- Notifications push
- Dashboard de monitoring

---

## 📝 Notes Importantes

### Points d'Attention Critiques

1. **ORDRE ALGORITHME** : Ne JAMAIS distribuer avant optimisation globale
2. **CONTRAINTE 4H** : Vérifier systématiquement, lever warnings clairs
3. **PAIRES INDIVISIBLES** : Tests automatisés pour garantir cohérence
4. **FIFO STRICT** : Pas d'optimisation d'équilibrage en phase initiale
5. **COMPATIBILITÉ** : 1 coursier doit donner exactement le même résultat qu'avant

### Dépendances Techniques

- Java 17+
- Spring Boot 3.x
- React 18+
- Vite 4+
- Leaflet pour la carte
- Maven pour le build

### Contacts et Ressources

- **Repository :** mlemseffer/PickupAndDelivery
- **Branche :** zeliecoupey
- **Documentation existante :** README.md, GUIDE_UTILISATION.md

---

## ✅ Checklist Finale de Livraison

### Backend
- [ ] Calcul de temps implémenté et testé
- [ ] Distribution FIFO fonctionnelle
- [ ] Contrainte 4h respectée
- [ ] Validation 1-10 coursiers
- [ ] Endpoints API documentés
- [ ] Tests unitaires > 90% couverture
- [ ] Logging propre (pas de System.out)
- [ ] Javadoc complète

### Frontend
- [ ] CourierCountSelector opérationnel
- [ ] TourTabs avec navigation
- [ ] Statistiques globales affichées
- [ ] Statistiques par coursier affichées
- [ ] Visualisation multi-tours sur carte
- [ ] Couleurs distinctes par coursier
- [ ] Gestion d'erreurs UI
- [ ] Responsive design

### Tests
- [ ] Tests unitaires backend passants
- [ ] Tests d'intégration passants
- [ ] Tests de performance validés
- [ ] Tests UI manuels effectués
- [ ] Tests edge cases couverts
- [ ] Tests de non-régression (1 coursier)

### Documentation
- [ ] README.md mis à jour
- [ ] Architecture documentée
- [ ] API documentée
- [ ] Guide utilisateur créé
- [ ] Code commenté
- [ ] Schémas de flux ajoutés

### Déploiement
- [ ] Build de production fonctionne
- [ ] Variables d'environnement configurées
- [ ] CORS configuré
- [ ] Script de démarrage créé
- [ ] Tests en environnement de production

---

## 🎓 Conclusion

Ce planning détaillé fournit une feuille de route complète pour étendre l'application Pickup & Delivery d'un système mono-coursier à un système multi-coursiers (1-10) avec distribution automatique FIFO et contrainte temporelle de 4 heures.

**Durée estimée :** 56-78 heures (2 semaines à temps plein)

**Complexité :** Moyenne-Élevée

**Risque :** Faible si les phases sont respectées dans l'ordre

L'approche séquentielle (calcul temps → distribution FIFO → API → UI → tests) garantit une progression solide et minimise les risques de régression. La clé du succès réside dans le respect strict de l'ordre algorithmique : **optimisation globale d'abord, distribution FIFO ensuite**.

---

**Document généré le :** 6 décembre 2025  
**Version :** 1.0  
**Statut :** En attente de validation et démarrage

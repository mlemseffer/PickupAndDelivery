# 📋 Résumé de l'Architecture REST - Pickup & Delivery

## 🎯 Vue d'Ensemble

Cette application utilise une **architecture REST moderne** avec une **séparation stricte** entre :
- **Backend** : Spring Boot avec architecture MVC
- **Frontend** : React avec architecture par composants

## 🏛️ Architecture Détaillée

### Backend - Spring Boot (Port 8080)

```
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND SPRING BOOT                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  📍 COUCHE CONTRÔLEUR (REST API)                               │
│  ├─ MapController                                              │
│  │  ├─ POST   /api/maps/upload       → Upload carte XML       │
│  │  ├─ GET    /api/maps/current      → Récupère carte        │
│  │  ├─ GET    /api/maps/status       → Statut carte          │
│  │  └─ DELETE /api/maps/current      → Supprime carte        │
│  │                                                             │
│  ├─ DeliveryController                                         │
│  │  ├─ POST   /api/deliveries/upload → Upload demandes XML   │
│  │  ├─ GET    /api/deliveries        → Liste demandes        │
│  │  ├─ POST   /api/deliveries        → Ajoute demande        │
│  │  └─ DELETE /api/deliveries        → Supprime demandes     │
│  │                                                             │
│  └─ TourController                                             │
│     └─ POST   /api/tours/calculate    → Calcule tournée       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ⚙️ COUCHE SERVICE (Logique Métier)                           │
│  ├─ MapService                                                 │
│  │  ├─ parseMapFromXML()      : Parse fichier XML carte      │
│  │  ├─ getCurrentMap()        : Récupère carte en mémoire    │
│  │  └─ clearMap()             : Réinitialise carte           │
│  │                                                             │
│  ├─ DeliveryService                                            │
│  │  ├─ parseDeliveryRequestsFromXML() : Parse demandes       │
│  │  ├─ addDeliveryRequest()           : Ajoute demande       │
│  │  └─ getCurrentRequests()           : Liste demandes       │
│  │                                                             │
│  └─ TourService                                                │
│     ├─ calculateOptimalTour()  : Algorithme optimisation      │
│     └─ calculateDistance()     : Calcul de distance           │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  📦 COUCHE MODÈLE (Domaine Métier)                            │
│  ├─ Node          : Nœud du graphe (id, lat, lng)            │
│  ├─ Segment       : Tronçon de route                          │
│  ├─ CityMap       : Carte complète (nodes, segments)          │
│  ├─ DeliveryRequest : Demande de livraison                    │
│  └─ Tour          : Tournée optimisée                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Frontend - React (Port 5173)

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND REACT                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  🎨 COMPOSANTS REACT (Vue)                                     │
│  ├─ Front.jsx              : Composant principal + routing     │
│  ├─ Header.jsx             : En-tête de l'application          │
│  ├─ Navigation.jsx         : Barre de navigation               │
│  ├─ MapUploader.jsx        : Upload de fichier carte           │
│  ├─ MapViewer.jsx          : Affichage carte Leaflet           │
│  ├─ DeliveryManager.jsx    : Gestion livraisons (à venir)     │
│  └─ TourViewer.jsx         : Affichage tournée (à venir)      │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  🔌 COUCHE SERVICE (Communication HTTP)                        │
│  └─ apiService.js                                              │
│     ├─ uploadMap(file)           → POST /api/maps/upload      │
│     ├─ getCurrentMap()           → GET  /api/maps/current     │
│     ├─ clearMap()                → DELETE /api/maps/current   │
│     ├─ uploadDeliveryRequests()  → POST /api/deliveries/upload│
│     ├─ getDeliveryRequests()     → GET  /api/deliveries       │
│     └─ calculateTour()           → POST /api/tours/calculate  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Flux de Communication

### Exemple 1 : Upload d'une Carte

```
┌─────────┐         ┌──────────────┐         ┌─────────────┐         ┌──────────┐
│ User    │         │ MapUploader  │         │ apiService  │         │ Backend  │
│         │         │  Component   │         │             │         │          │
└────┬────┘         └──────┬───────┘         └──────┬──────┘         └────┬─────┘
     │                     │                        │                     │
     │  Sélectionne XML   │                        │                     │
     ├────────────────────>│                        │                     │
     │                     │                        │                     │
     │                     │  uploadMap(file)       │                     │
     │                     ├───────────────────────>│                     │
     │                     │                        │                     │
     │                     │                        │ POST /api/maps/upload
     │                     │                        ├────────────────────>│
     │                     │                        │                     │
     │                     │                        │  MapController      │
     │                     │                        │      ↓              │
     │                     │                        │  MapService.parse() │
     │                     │                        │      ↓              │
     │                     │                        │  CityMap created    │
     │                     │                        │                     │
     │                     │                        │  JSON Response      │
     │                     │                        │<────────────────────│
     │                     │  {success: true, ...}  │                     │
     │                     │<───────────────────────│                     │
     │                     │                        │                     │
     │   Carte affichée   │                        │                     │
     │<────────────────────│                        │                     │
     │                     │                        │                     │
```

### Exemple 2 : Récupération de Carte

```
Component → apiService.getCurrentMap()
              ↓
          GET /api/maps/current
              ↓
          MapController.getCurrentMap()
              ↓
          MapService.getCurrentMap()
              ↓
          Return CityMap object
              ↓
          JSON { nodes: [...], segments: [...] }
              ↓
          Component reçoit les données
              ↓
          MapViewer affiche sur Leaflet
```

## 📊 Responsabilités par Couche

### Backend

| Couche | Responsabilité | Technologie |
|--------|---------------|-------------|
| **Contrôleur** | - Recevoir requêtes HTTP<br>- Valider paramètres<br>- Retourner JSON | `@RestController`<br>`@RequestMapping` |
| **Service** | - Logique métier<br>- Algorithmes<br>- Traitement données | `@Service` |
| **Modèle** | - Représentation domaine<br>- Entités métier | POJOs avec Lombok |
| **DTO** | - Transfer de données<br>- Réponses API | `ApiResponse<T>` |

### Frontend

| Couche | Responsabilité | Technologie |
|--------|---------------|-------------|
| **Composants** | - Interface utilisateur<br>- Gestion événements<br>- Affichage données | React JSX |
| **Service** | - Appels HTTP<br>- Communication backend | Fetch API |
| **État** | - Gestion état global<br>- State management | React Hooks (`useState`) |

## ✅ Avantages de cette Architecture

### 1. **Séparation Backend/Frontend**
- ✅ Développement parallèle possible
- ✅ Technologies indépendantes
- ✅ Scalabilité séparée

### 2. **Architecture en Couches**
- ✅ Code organisé et maintenable
- ✅ Tests unitaires facilités
- ✅ Réutilisabilité des composants

### 3. **API REST**
- ✅ Standard de l'industrie
- ✅ Communication HTTP/JSON universelle
- ✅ Documentation facile (Swagger possible)

### 4. **Évolutivité**
```
Nouvelle fonctionnalité ?
├─ Backend
│  ├─ 1. Créer le modèle
│  ├─ 2. Créer le service
│  ├─ 3. Créer le contrôleur
│  └─ 4. Écrire les tests
└─ Frontend
   ├─ 1. Ajouter méthode dans apiService
   ├─ 2. Créer le composant
   └─ 3. Intégrer dans l'UI

Impact sur le reste du code : MINIMAL ✅
```

### 5. **Testabilité**
```
Backend Tests:
├─ Unit Tests (Services) ✅
├─ Integration Tests (Controllers) ✅
└─ E2E Tests (API complète) ✅

Frontend Tests:
├─ Component Tests ✅
├─ Integration Tests (apiService) ✅
└─ E2E Tests (User flows) ✅
```

## 🛠️ Technologies Utilisées

### Backend Stack
```
Spring Boot 3.2
├─ Spring MVC          → Architecture MVC/REST
├─ Spring Web          → Serveur HTTP
├─ Spring Validation   → Validation données
└─ Lombok              → Réduction boilerplate

Java 17
Maven
```

### Frontend Stack
```
React 19
├─ React Hooks         → State management
├─ React Leaflet       → Cartographie
└─ Lucide React        → Icônes

Vite                   → Build tool
JavaScript ES6+
```

## 📈 Métriques de Qualité

```
✅ Couplage faible       : Backend ←──JSON──→ Frontend
✅ Cohésion forte        : Chaque classe = 1 responsabilité
✅ Code modulaire        : Composants/Services réutilisables
✅ Tests automatisés     : Unit + Integration tests
✅ Documentation         : README + ARCHITECTURE + QUICKSTART
✅ Standards respectés   : REST, MVC, React best practices
```

## 🎓 Apprentissages et Bonnes Pratiques

1. **Séparation des préoccupations** : Chaque couche a un rôle précis
2. **Principe SOLID** : Single Responsibility, Open/Closed, etc.
3. **API REST** : GET pour lecture, POST pour création, DELETE pour suppression
4. **State Management React** : Utilisation des Hooks pour gérer l'état
5. **Service Layer Pattern** : Logique métier isolée des contrôleurs
6. **DTO Pattern** : Objets dédiés pour le transfert de données
7. **Error Handling** : Gestion centralisée des erreurs avec GlobalExceptionHandler

---

**Cette architecture garantit une application maintenable, évolutive et testable ! 🚀**

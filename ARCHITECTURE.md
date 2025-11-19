# Architecture REST - Pickup & Delivery

## 📐 Diagramme d'Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                            │
│                                                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    Composants React                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │   │
│  │  │  Header  │  │Navigation│  │   Map    │  │ Delivery │  │   │
│  │  │          │  │          │  │  Viewer  │  │  Manager │  │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │   │
│  │                                                            │   │
│  │  ┌──────────────────────────────────────────────────┐    │   │
│  │  │            apiService.js                         │    │   │
│  │  │  - uploadMap()                                   │    │   │
│  │  │  - getCurrentMap()                               │    │   │
│  │  │  - uploadDeliveryRequests()                      │    │   │
│  │  │  - calculateTour()                               │    │   │
│  │  └──────────────────────────────────────────────────┘    │   │
│  └────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/JSON (REST)
                              │
┌─────────────────────────────▼─────────────────────────────────────┐
│                      BACKEND (Spring Boot)                         │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                  Couche Contrôleur                          │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │  │
│  │  │     Map      │  │  Delivery    │  │     Tour     │    │  │
│  │  │  Controller  │  │  Controller  │  │  Controller  │    │  │
│  │  │              │  │              │  │              │    │  │
│  │  │ @RestController│ @RestController│ @RestController│    │  │
│  │  │ @RequestMapping│ @RequestMapping│ @RequestMapping│    │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌────────────────────────────▼───────────────────────────────┐  │
│  │                    Couche Service                           │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │  │
│  │  │     Map      │  │  Delivery    │  │     Tour     │    │  │
│  │  │   Service    │  │   Service    │  │   Service    │    │  │
│  │  │              │  │              │  │              │    │  │
│  │  │   @Service   │  │   @Service   │  │   @Service   │    │  │
│  │  │ Logique Métier│ Logique Métier│ Logique Métier│    │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌────────────────────────────▼───────────────────────────────┐  │
│  │                   Couche Modèle                             │  │
│  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐          │  │
│  │  │  Node  │  │Segment │  │CityMap │  │Delivery│          │  │
│  │  │        │  │        │  │        │  │Request │          │  │
│  │  └────────┘  └────────┘  └────────┘  └────────┘          │  │
│  │  Entités du domaine métier                                │  │
│  └────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

## 🔄 Flux de Données

### Exemple : Chargement d'une Carte

```
1. User Action (Frontend)
   └─► Click "Charger une carte"
       │
2. Component (MapUploader)
   └─► handleFileUpload(file)
       │
3. API Service
   └─► apiService.uploadMap(file)
       │
4. HTTP Request
   └─► POST /api/maps/upload
       │
5. Controller (Backend)
   └─► MapController.uploadMap(file)
       │
6. Service Layer
   └─► MapService.parseMapFromXML(file)
       │
7. Domain Model
   └─► CityMap { nodes[], segments[] }
       │
8. HTTP Response
   └─► JSON { success: true, data: {...} }
       │
9. Component Update
   └─► setMapData(response.data)
       │
10. UI Render
    └─► MapViewer affiche la carte
```

## 🎯 Principes d'Architecture

### 1. Séparation des Responsabilités (SoC)

**Backend :**
- **Contrôleurs** : Gestion des requêtes HTTP, validation, sérialisation JSON
- **Services** : Logique métier, algorithmes, règles de gestion
- **Modèles** : Représentation des données du domaine

**Frontend :**
- **Composants** : Affichage UI, gestion des événements utilisateur
- **Services** : Communication HTTP avec le backend
- **Pas de logique métier** dans le frontend

### 2. Architecture REST

- Communication via **HTTP/JSON**
- Endpoints **RESTful** (GET, POST, PUT, DELETE)
- **Stateless** : Chaque requête est indépendante
- Réponses standardisées avec `ApiResponse<T>`

### 3. Découplage

```
Frontend ←────JSON────→ Backend
   │                      │
   │                      │
 React                 Spring
Components              MVC
   │                      │
   │                      │
Pas de                Pas de
dépendance            dépendance
au backend            au frontend
```

### 4. Testabilité

Chaque couche peut être testée indépendamment :

```
┌─────────────────┐
│  Unit Tests     │  ← Test des Services
└─────────────────┘
┌─────────────────┐
│Integration Tests│  ← Test des Contrôleurs + Services
└─────────────────┘
┌─────────────────┐
│  E2E Tests      │  ← Test Frontend + Backend
└─────────────────┘
```

## 📦 Dépendances

### Backend → Frontend
**AUCUNE** ✅
Le backend ne connaît pas le frontend.

### Frontend → Backend
Via **HTTP uniquement** ✅
- Pas d'import de classes Java dans React
- Communication uniquement via JSON

## 🚀 Évolutivité

### Ajout d'une nouvelle fonctionnalité

**Backend :**
1. Créer le modèle dans `model/`
2. Créer le service dans `service/`
3. Créer le contrôleur dans `controller/`
4. Écrire les tests

**Frontend :**
1. Ajouter la méthode dans `apiService.js`
2. Créer le composant React
3. Intégrer le composant dans l'application

**Impact :** Minimal, chaque couche reste isolée

## 📊 Avantages de cette Architecture

| Aspect | Avantage |
|--------|----------|
| **Maintenabilité** | Code organisé, facile à comprendre et modifier |
| **Évolutivité** | Ajout de fonctionnalités sans refonte majeure |
| **Testabilité** | Tests unitaires et d'intégration simplifiés |
| **Réutilisabilité** | Composants et services réutilisables |
| **Performance** | Backend et frontend scalables indépendamment |
| **Flexibilité** | Possibilité de changer le frontend sans toucher au backend |
| **Collaboration** | Équipes frontend/backend peuvent travailler en parallèle |

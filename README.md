# 🚴 Application Pickup & Delivery

> **Application de gestion et d'optimisation de tournées de livraison à vélo**
> 
> Architecture REST moderne avec **Spring Boot** (Backend) et **React** (Frontend)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.0-blue.svg)](https://react.dev/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📑 Table des Matières

1. [Description du Projet](#-description-du-projet)
2. [Démarrage Rapide](#-démarrage-rapide)
3. [Architecture](#-architecture)
4. [Structure du Projet](#-structure-du-projet)
5. [API REST](#-api-rest)
6. [Technologies](#-technologies)
7. [Développement](#-développement)

---

## 🎯 Description du Projet

Application web de gestion de tournées de livraison à vélo permettant :
- 📍 Chargement de plans de ville (intersections et tronçons)
- 📦 Gestion des demandes de livraison
- 🗺️ Visualisation interactive sur carte Leaflet
- 🚴 Calcul de tournées optimisées

**Projet développé pour le cours d'Agilité - 4IF INSA Lyon**

---

## 🚀 Démarrage Rapide

### Prérequis

**Backend :**
- ☕ Java 17+ : [Télécharger](https://adoptium.net/)
- 📦 Maven 3.6+ : [Installer](https://maven.apache.org/install.html)

**Frontend :**
- 🟢 Node.js 18+ : [Télécharger](https://nodejs.org/)
- 📦 npm (inclus avec Node.js)

### Installation et Lancement

#### Option 1 : Script de Démarrage Automatique (Windows)

Exécutez simplement le script :
```bash
.\start.bat
```

Ou avec PowerShell :
```powershell
.\start.ps1
```

#### Option 2 : Lancement Manuel

**Terminal 1 - Backend :**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
✅ Backend disponible sur `http://localhost:8080`

**Terminal 2 - Frontend :**
```bash
cd Site
npm install
npm run dev
```
✅ Frontend disponible sur `http://localhost:5173`

### Vérification

Testez l'API backend :
```bash
curl http://localhost:8080/api/maps/status
```

Ouvrez le frontend : `http://localhost:5173`

---

## 🏗️ Architecture

### Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (React)                        │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Components: Header, Navigation, MapViewer         │     │
│  │  Services: apiService.js (HTTP Client)             │     │
│  └────────────────────────────────────────────────────┘     │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/JSON (REST)
┌──────────────────────▼──────────────────────────────────────┐
│                   BACKEND (Spring Boot)                     │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Controllers: MapController, DeliveryController    │     │
│  │  Services: MapService, DeliveryService, TourService│     │
│  │  XmlParsers: MapXmlParser, DeliveryRequestXmlParser│     │
│  │  Models: Node, Segment, CityMap, DeliveryRequest   │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Backend (Spring MVC)

```
backend/
├── controller/          # Endpoints REST (HTTP → JSON)
│   ├── MapController
│   ├── DeliveryController
│   └── TourController
│
├── service/            # Logique métier
│   ├── MapService
│   ├── DeliveryService
│   └── TourService
│
├── xmlparser/          # Parsing XML
│   ├── MapXmlParser
│   └── DeliveryRequestXmlParser
│
├── model/              # Entités du domaine
│   ├── Node, Segment, CityMap
│   ├── DeliveryRequest
│   └── Tour
│
└── dto/                # Data Transfer Objects
    ├── ApiResponse<T>
    └── MapUploadResponse
```

**Responsabilités par couche :**
- **Controllers** : Exposent les endpoints REST, gèrent HTTP
- **Services** : Contiennent la logique métier et algorithmes
- **XmlParsers** : Parsent les fichiers XML en objets métier
- **Models** : Représentent les entités du domaine
- **DTOs** : Encapsulent les réponses JSON

### Architecture Frontend (React)

```
Site/
├── components/         # Composants React
│   ├── Header.jsx
│   ├── Navigation.jsx
│   ├── MapUploader.jsx
│   └── MapViewer.jsx
│
└── services/           # Communication backend
    └── apiService.js
```

**Principe :**
- Composants autonomes et réutilisables
- Communication backend via `apiService.js`
- Pas de logique métier dans le frontend

### Flux de Données

```
User Action → Component → apiService → HTTP Request
                                            ↓
                                      Controller
                                            ↓
                                        Service
                                            ↓
                                      XmlParser/Model
                                            ↓
                                      HTTP Response
                                            ↓
Component Update ← JSON Data ← apiService ←
```

### Principes d'Architecture

✅ **Séparation des responsabilités** : Chaque couche a un rôle précis  
✅ **REST API** : Communication HTTP/JSON standardisée  
✅ **Découplage** : Frontend et Backend indépendants  
✅ **Testabilité** : Chaque couche testable séparément

---

## 📁 Structure du Projet

### Backend - Spring Boot

```
backend/
├── pom.xml                                    # Configuration Maven
├── src/main/java/com/pickupdelivery/
│   ├── PickupDeliveryApplication.java         # Point d'entrée
│   │
│   ├── controller/                            # REST API
│   │   ├── MapController.java                 # Endpoints cartes
│   │   ├── DeliveryController.java            # Endpoints livraisons
│   │   └── TourController.java                # Endpoints tournées
│   │
│   ├── service/                               # Logique métier
│   │   ├── MapService.java
│   │   ├── DeliveryService.java
│   │   └── TourService.java
│   │
│   ├── xmlparser/                             # Parsing XML
│   │   ├── MapXmlParser.java
│   │   └── DeliveryRequestXmlParser.java
│   │
│   ├── model/                                 # Domaine métier
│   │   ├── Node.java                          # Intersection
│   │   ├── Segment.java                       # Tronçon de rue
│   │   ├── CityMap.java                       # Plan complet
│   │   ├── DeliveryRequest.java               # Demande de livraison
│   │   └── Tour.java                          # Tournée calculée
│   │
│   ├── dto/                                   # Data Transfer Objects
│   │   ├── ApiResponse.java
│   │   └── MapUploadResponse.java
│   │
│   ├── config/                                # Configuration
│   │   └── WebConfig.java                     # CORS, Web
│   │
│   └── exception/                             # Gestion erreurs
│       └── GlobalExceptionHandler.java
│
└── src/test/java/                             # Tests
    ├── controller/MapControllerTest.java
    └── service/MapServiceTest.java
```

### Frontend - React + Vite

```
Site/
├── package.json                               # Dépendances npm
├── vite.config.js                             # Configuration Vite
├── .env                                       # Variables d'environnement
├── index.html                                 # Point d'entrée HTML
├── main.jsx                                   # Point d'entrée React
├── Front.jsx                                  # Composant principal
├── leaflet-custom.css                         # Styles carte
│
└── src/
    ├── components/                            # Composants React
    │   ├── Header.jsx                         # En-tête
    │   ├── Navigation.jsx                     # Barre de navigation
    │   ├── MapUploader.jsx                    # Upload XML
    │   └── MapViewer.jsx                      # Affichage carte
    │
    └── services/                              # Services HTTP
        └── apiService.js                      # Client API REST
```

### Fichiers XML de Test

```
fichiersXMLPickupDelivery/
├── Plans (Cartes)
│   ├── petitPlan.xml                          # ~100 nœuds
│   ├── moyenPlan.xml                          # ~500 nœuds
│   └── grandPlan.xml                          # ~1000+ nœuds
│
└── Demandes de Livraison
    ├── demandePetit1.xml                      # 1 livraison
    ├── demandePetit2.xml                      # 2 livraisons
    ├── demandeMoyen3.xml                      # 3 livraisons
    ├── demandeMoyen5.xml                      # 5 livraisons
    ├── demandeGrand7.xml                      # 7 livraisons
    └── demandeGrand9.xml                      # 9 livraisons
```

---

## 📡 API REST

### Cartes

| Méthode | Endpoint | Description | Corps de la requête |
|---------|----------|-------------|---------------------|
| POST | `/api/maps/upload` | Upload fichier XML de carte | `MultipartFile` |
| GET | `/api/maps/current` | Récupère la carte chargée | - |
| GET | `/api/maps/status` | Vérifie si carte chargée | - |
| DELETE | `/api/maps/current` | Supprime la carte | - |

### Livraisons

| Méthode | Endpoint | Description | Corps de la requête |
|---------|----------|-------------|---------------------|
| POST | `/api/deliveries/upload` | Upload fichier XML demandes | `MultipartFile` |
| GET | `/api/deliveries` | Liste toutes les demandes | - |
| POST | `/api/deliveries` | Ajoute une demande | `DeliveryRequest` JSON |
| DELETE | `/api/deliveries` | Supprime toutes les demandes | - |

### Tournées

| Méthode | Endpoint | Description | Paramètres |
|---------|----------|-------------|------------|
| POST | `/api/tours/calculate` | Calcule tournée optimisée | `warehouseAddress` (String) |

### Format de Réponse

Toutes les API retournent un objet `ApiResponse<T>` :

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Formats XML

**Carte (Plan) :**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<reseau>
    <noeud id="1" latitude="45.75" longitude="4.85"/>
    <troncon origine="1" destination="2" longueur="100.5" nomRue="Rue Example"/>
</reseau>
```

**Demandes de Livraison :**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<demandeDeLivraisons>
    <entrepot adresse="1"/>
    <livraison adresseEnlevement="2" adresseLivraison="3" 
               dureeEnlevement="180" dureeLivraison="240"/>
</demandeDeLivraisons>
```

---

## 💻 Technologies

### Backend
- **Spring Boot 3.2** - Framework Java
- **Spring MVC** - Architecture REST
- **Lombok** - Réduction code boilerplate
- **Maven** - Gestion dépendances
- **JUnit & Mockito** - Tests

### Frontend
- **React 19** - Bibliothèque UI
- **Vite 7** - Build tool rapide
- **Leaflet 1.9** - Cartographie interactive
- **Lucide React** - Icônes modernes

### DevOps
- **Git** - Contrôle de version
- **Maven** - Build backend
- **npm** - Build frontend

---

## 🛠️ Développement

### Tests

**Backend :**
```bash
cd backend
mvn test                          # Tous les tests
mvn test -Dtest=MapServiceTest    # Test spécifique
```

**Frontend :**
```bash
cd Site
npm test
```

### Hot Reload

- **Backend** : Spring Boot DevTools recharge automatiquement
- **Frontend** : Vite recharge à chaque modification

### Build Production

**Backend :**
```bash
cd backend
mvn clean package
java -jar target/pickup-delivery-backend-1.0.0.jar
```

**Frontend :**
```bash
cd Site
npm run build
# Fichiers dans dist/
```

### Ports Utilisés

| Service | Port | URL |
|---------|------|-----|
| Backend API | 8080 | http://localhost:8080 |
| Frontend Dev | 5173 | http://localhost:5173 |

### Configuration

**Backend - `application.properties` :**
```properties
server.port=8080
spring.servlet.multipart.max-file-size=10MB
```

**Frontend - `.env` :**
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 🔧 Résolution de Problèmes

### Port 8080 déjà utilisé

**Windows :**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Ou modifier le port :**
```properties
# application.properties
server.port=8081
```

### Frontend ne se connecte pas au backend

Vérifiez :
1. Backend démarré sur port 8080
2. Fichier `.env` correctement configuré
3. CORS activé dans `WebConfig.java`

### Erreur parsing XML

Vérifiez :
1. Fichier XML bien formé
2. Attributs correspondent aux noms attendus
3. Encodage UTF-8

---

## 📊 Avantages de l'Architecture

| Aspect | Avantage |
|--------|----------|
| **Maintenabilité** | Code organisé par couches, facile à modifier |
| **Évolutivité** | Ajout de fonctionnalités sans refonte majeure |
| **Testabilité** | Tests unitaires et d'intégration simplifiés |
| **Réutilisabilité** | Composants et services réutilisables |
| **Performance** | Backend et frontend scalables indépendamment |
| **Flexibilité** | Changement frontend possible sans toucher backend |
| **Collaboration** | Équipes frontend/backend travaillent en parallèle |

---

## 🤝 Contribution

Pour contribuer au projet :
1. Fork le repository
2. Créez une branche (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

---

## 📄 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

## 👥 Équipe

**Projet Agilité - 4IF INSA Lyon**

---

**Bon développement ! 🚀**

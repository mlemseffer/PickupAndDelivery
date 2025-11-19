# 🚴 Application Pickup & Delivery

> **Application de gestion et d'optimisation de tournées de livraison à vélo**
> 
> Architecture REST moderne avec **Spring Boot** (Backend) et **React** (Frontend)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.0-blue.svg)](https://react.dev/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📚 Navigation Documentation

**🆕 Nouveau ? Commencez ici :**
- 📖 **[INDEX.md](INDEX.md)** - Guide de navigation dans toute la documentation
- 🚀 **[QUICKSTART.md](QUICKSTART.md)** - Démarrer l'application en 5 minutes
- ✅ **[SUMMARY.md](SUMMARY.md)** - Récapitulatif complet du projet

**Pour les développeurs :**
- 🏗️ **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architecture détaillée avec diagrammes
- 📋 **[ARCHITECTURE_SUMMARY.md](ARCHITECTURE_SUMMARY.md)** - Résumé architecture
- 📁 **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - Structure complète des fichiers

**Pour les présentations :**
- 🎓 **[ARCHITECTURE_PRESENTATION.md](ARCHITECTURE_PRESENTATION.md)** - Présentation des choix techniques

---

## 🎯 Description du Projet

## 🏗️ Architecture

### Backend (Spring Boot)

Architecture **REST** avec **Spring MVC** :

```
backend/
├── src/main/java/com/pickupdelivery/
│   ├── PickupDeliveryApplication.java     # Point d'entrée
│   ├── controller/                        # Couche Contrôleur (REST API)
│   │   ├── MapController.java
│   │   ├── DeliveryController.java
│   │   └── TourController.java
│   ├── service/                           # Couche Service (Logique métier)
│   │   ├── MapService.java
│   │   ├── DeliveryService.java
│   │   └── TourService.java
│   ├── model/                             # Couche Modèle (Domaine métier)
│   │   ├── Node.java
│   │   ├── Segment.java
│   │   ├── CityMap.java
│   │   ├── DeliveryRequest.java
│   │   └── Tour.java
│   ├── dto/                               # Data Transfer Objects
│   │   ├── ApiResponse.java
│   │   └── MapUploadResponse.java
│   ├── config/                            # Configuration
│   │   └── WebConfig.java
│   └── exception/                         # Gestion des exceptions
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    └── application.properties
```

**Responsabilités par couche :**

- **Contrôleurs** : Exposent les endpoints REST, valident les requêtes HTTP, retournent des JSON
- **Services** : Contiennent la logique métier et les algorithmes
- **Modèles** : Représentent les entités du domaine métier
- **DTOs** : Encapsulent les données échangées avec le frontend

### Frontend (React)

Architecture par **composants indépendants** :

```
Site/
├── src/
│   ├── components/                        # Composants React réutilisables
│   │   ├── Header.jsx
│   │   ├── Navigation.jsx
│   │   ├── MapUploader.jsx
│   │   └── MapViewer.jsx
│   └── services/                          # Services d'appel API
│       └── apiService.js                  # Communication HTTP avec le backend
├── Front.jsx                              # Composant principal
├── main.jsx                               # Point d'entrée
└── package.json
```

**Principe de fonctionnement :**
- Les composants React sont **autonomes** et **découplés**
- Toute communication avec le backend passe par `apiService.js`
- Les composants consomment les **JSON retournés** par les contrôleurs Spring
- Aucune logique métier dans le frontend, seulement de la logique de présentation

## 🔄 Flux de Communication

```
┌─────────────┐         HTTP/JSON          ┌──────────────┐
│   Frontend  │ ◄────────────────────────► │   Backend    │
│   (React)   │      REST API Calls        │ (Spring Boot)│
└─────────────┘                            └──────────────┘
      │                                            │
      │                                            │
   Components                                  Controllers
      │                                            │
      └─► apiService ─────────────────────────► MapController
                           GET/POST                   │
                           JSON                       │
                                                  MapService
                                                      │
                                                   Model
```

## 🚀 Démarrage

### Backend (Spring Boot)

#### Prérequis
- Java 17+
- Maven 3.6+

#### Commandes
```bash
cd backend

# Compilation
mvn clean install

# Lancement
mvn spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`

### Frontend (React + Vite)

#### Prérequis
- Node.js 18+
- npm ou yarn

#### Commandes
```bash
cd Site

# Installation des dépendances
npm install

# Lancement en mode développement
npm run dev
```

Le frontend démarre sur `http://localhost:5173`

## 📡 API REST Endpoints

### Cartes

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/maps/upload` | Upload un fichier XML de carte |
| GET | `/api/maps/current` | Récupère la carte chargée |
| GET | `/api/maps/status` | Vérifie si une carte est chargée |
| DELETE | `/api/maps/current` | Supprime la carte courante |

### Livraisons

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/deliveries/upload` | Upload un fichier XML de demandes |
| GET | `/api/deliveries` | Récupère toutes les demandes |
| POST | `/api/deliveries` | Ajoute une demande de livraison |
| DELETE | `/api/deliveries` | Supprime toutes les demandes |

### Tournées

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/tours/calculate` | Calcule une tournée optimisée |

## 🎯 Avantages de l'Architecture

### Séparation des responsabilités
- **Backend** : Logique métier, traitement des données, algorithmes
- **Frontend** : Interface utilisateur, expérience utilisateur

### Évolutivité
- Ajout facile de nouveaux endpoints REST
- Ajout de nouveaux composants React sans impact sur le backend
- Possibilité de scaler backend et frontend indépendamment

### Maintenabilité
- Code organisé par couches clairement définies
- Chaque classe/composant a une responsabilité unique
- Facilite les tests unitaires et d'intégration

### Testabilité
- **Backend** : Tests unitaires des services, tests d'intégration des contrôleurs
- **Frontend** : Tests unitaires des composants, tests d'intégration de l'API

## 🧪 Tests

### Backend
```bash
cd backend
mvn test
```

### Frontend
```bash
cd Site
npm test
```

## 📝 Technologies Utilisées

### Backend
- **Spring Boot 3.2** - Framework Java
- **Spring MVC** - Architecture MVC/REST
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

### Frontend
- **React 19** - Bibliothèque UI
- **Vite** - Build tool rapide
- **Leaflet** - Cartographie interactive
- **Lucide React** - Icônes modernes

## 👥 Équipe

Projet développé pour le cours d'Agilité - 4IF INSA Lyon

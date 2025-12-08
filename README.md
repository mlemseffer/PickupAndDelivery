# 🚴 Pickup & Delivery - Gestion de Tournées de Livraison à Vélo

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)
![React](https://img.shields.io/badge/React-19.2-61DAFB.svg)
![License](https://img.shields.io/badge/license-INSA%20Lyon-red.svg)

**Application web moderne pour optimiser les tournées de livraison à vélo**

[Démarrage Rapide](#-démarrage-rapide) • [Documentation](#-documentation) • [Architecture](#-architecture) • [Fonctionnalités](#-fonctionnalités)

</div>

---

## 📋 Table des Matières

- [À Propos](#-à-propos)
- [Fonctionnalités](#-fonctionnalités)
- [Technologies](#️-technologies)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Démarrage Rapide](#-démarrage-rapide)
- [Architecture](#-architecture)
- [Structure du Projet](#-structure-du-projet)
- [API REST](#-api-rest)
- [Interface Utilisateur](#-interface-utilisateur)
- [Tests](#-tests)
- [Contribution](#-contribution)
- [Résolution des Problèmes](#-résolution-des-problèmes)
- [Licence](#-licence)

---

## 🎯 À Propos

**Pickup & Delivery** est une application web full-stack développée pour optimiser la gestion des tournées de livraison à vélo en milieu urbain. Elle permet de :

- 📍 Charger et visualiser des cartes urbaines interactives
- 📦 Gérer des demandes de livraison avec points de pickup et delivery
- 🚴 Calculer des tournées optimisées pour plusieurs coursiers
- ⏱️ Respecter une contrainte de temps maximale (4 heures par tournée)
- 📊 Visualiser en temps réel les itinéraires sur une carte OpenStreetMap
- 💾 Sauvegarder et restaurer des tournées

### 🎓 Contexte Académique

Projet développé dans le cadre du cours d'**Agilité - 4IF H34** à l'**INSA Lyon**.

---

## ✨ Fonctionnalités

### 🗺️ Gestion de Cartes
- ✅ Import de cartes au format XML
- ✅ Visualisation interactive avec Leaflet/OpenStreetMap
- ✅ Affichage des intersections et tronçons
- ✅ Zoom, déplacement et mode plein écran
- ✅ Sélection visuelle de nœuds sur la carte

### 📦 Gestion des Demandes
- ✅ Import de demandes depuis fichiers XML
- ✅ Ajout manuel de demandes (pickup/delivery)
- ✅ Visualisation des demandes avec marqueurs colorés
- ✅ Modification et suppression de demandes
- ✅ Gestion de l'entrepôt (warehouse)

### 🚴 Calcul de Tournées
- ✅ Algorithme d'optimisation TSP (Traveling Salesman Problem)
- ✅ Support multi-coursiers (1 à N coursiers)
- ✅ Respect de la contrainte de 4 heures maximum
- ✅ Gestion des demandes non assignées
- ✅ Recalcul automatique après modifications

### 🎨 Interface Multi-Onglets
- ✅ **Vue Accueil** : Page d'introduction
- ✅ **Vue Carte** : Visualisation et gestion des tournées
- ✅ **Vue Demandes** : Liste des demandes de livraison
- ✅ **Vue Tournées** : Demandes non assignées et statistiques

### 🛠️ Édition de Tournées
- ✅ Mode édition interactif
- ✅ Réassignation de demandes entre coursiers
- ✅ Désassignation de demandes (deviennent non assignées)
- ✅ Validation ou annulation des modifications
- ✅ Aperçu en temps réel des changements

### 💾 Sauvegarde et Restauration
- ✅ Export des itinéraires en format texte (.txt)
- ✅ Export des tournées complètes en JSON
- ✅ Import de tournées depuis JSON
- ✅ Historique et versioning

---

## 🛠️ Technologies

### Backend
```
☕ Java 17
🌱 Spring Boot 3.2
🏗️ Spring MVC (REST API)
🔧 Maven 3.9+
📦 Lombok (annotations)
✅ JUnit 5 (tests unitaires)
🧪 Mockito (mocking)
```

### Frontend
```
⚛️ React 19.2
⚡ Vite 6.0 (build tool)
🗺️ Leaflet & React-Leaflet
🎨 Tailwind CSS 3.4
🎭 Lucide React (icônes)
🌐 Fetch API (HTTP client)
```

### Outils de Développement
```
📝 Visual Studio Code
☕ IntelliJ IDEA (optionnel)
📦 npm / pnpm
🔄 Git
```

---

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Obligatoire
- ☕ **Java JDK 17+** ([Télécharger](https://www.oracle.com/java/technologies/downloads/))
- 📦 **Node.js 18+** ([Télécharger](https://nodejs.org/))
- 🔧 **Maven 3.9+** ([Télécharger](https://maven.apache.org/download.cgi))

### Vérification des Versions

```bash
# Vérifier Java
java -version
# Devrait afficher: openjdk version "17.x.x" ou supérieur

# Vérifier Node.js
node -v
# Devrait afficher: v18.x.x ou supérieur

# Vérifier Maven
mvn -v
# Devrait afficher: Apache Maven 3.9.x ou supérieur
```

---

## 📥 Installation

### 1️⃣ Cloner le Projet

```bash
git clone https://github.com/mlemseffer/PickupAndDelivery.git
cd PickupAndDelivery
```

### 2️⃣ Installation Backend

```bash
cd backend
mvn clean install
```

Cette commande va :
- 📥 Télécharger toutes les dépendances Maven
- 🔨 Compiler le code Java
- ✅ Exécuter les tests unitaires
- 📦 Créer le fichier JAR exécutable

### 3️⃣ Installation Frontend

```bash
cd ../frontend
npm install
```

Cette commande va :
- 📥 Télécharger toutes les dépendances npm
- 🔧 Préparer l'environnement de développement

---

## 🚀 Démarrage Rapide

### Option 1 : Démarrage Automatique (Recommandé)

#### Windows
```bash
# Double-cliquer sur :
start.bat

# Ou en ligne de commande :
.\start.bat
```

#### Linux/macOS
```bash
# Rendre le script exécutable (première fois uniquement)
chmod +x start.sh

# Lancer l'application
./start.sh
```

### Option 2 : Démarrage Manuel

#### Terminal 1 - Backend
```bash
cd backend
mvn spring-boot:run
```
✅ Le backend démarre sur **http://localhost:8080**

#### Terminal 2 - Frontend
```bash
cd frontend
npm run dev
```
✅ Le frontend démarre sur **http://localhost:5173**

### 3️⃣ Accéder à l'Application

Ouvrez votre navigateur et accédez à :
```
http://localhost:5173
```

🎉 **C'est prêt !** Vous pouvez maintenant utiliser l'application.

---

## 🏗️ Architecture

### Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                     NAVIGATEUR WEB                          │
│                  (http://localhost:5173)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/JSON
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   FRONTEND (React + Vite)                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Composants React                                    │   │
│  │  • Navigation                                        │   │
│  │  • MapViewer (Leaflet)                              │   │
│  │  • TourTabs                                         │   │
│  │  • DeliveryRequestUploader                          │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Services                                            │   │
│  │  • apiService.js (API Client)                       │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────────┘
                       │ REST API
                       │ (Fetch HTTP/JSON)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              BACKEND (Spring Boot REST API)                 │
│                  (http://localhost:8080)                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers (REST Endpoints)                        │   │
│  │  • MapController        → /api/maps                 │   │
│  │  • DeliveryController   → /api/deliveries           │   │
│  │  • TourController       → /api/tours                │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Services (Business Logic)                           │   │
│  │  • MapService                                        │   │
│  │  • DeliveryService                                   │   │
│  │  • TourService                                       │   │
│  │  • TourOptimizationService                          │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Models (Domain Objects)                             │   │
│  │  • Map, Node, Segment                               │   │
│  │  • DeliveryRequest, DeliveryRequestSet              │   │
│  │  • Tour, TourSegment, Stop                          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Pattern Architecture

L'application suit une **architecture en couches** avec séparation claire des responsabilités :

#### Backend (Spring Boot)
```
Controller Layer    → Gestion des requêtes HTTP REST
      ↓
Service Layer       → Logique métier et orchestration
      ↓
Model Layer         → Objets du domaine métier
```

#### Frontend (React)
```
Components          → Interface utilisateur
      ↓
Services            → Communication avec l'API
      ↓
State Management    → Gestion de l'état (React hooks)
```

---

## 📂 Structure du Projet

```
PickupAndDelivery/
│
├── 📁 backend/                         # Backend Spring Boot
│   ├── 📁 src/main/java/com/pickupdelivery/
│   │   ├── 📁 controller/              # REST Controllers
│   │   │   ├── MapController.java
│   │   │   ├── DeliveryController.java
│   │   │   └── TourController.java
│   │   ├── 📁 service/                 # Business Logic
│   │   │   ├── MapService.java
│   │   │   ├── DeliveryService.java
│   │   │   ├── TourService.java
│   │   │   └── TourOptimizationService.java
│   │   ├── 📁 model/                   # Domain Models
│   │   │   ├── Map.java
│   │   │   ├── DeliveryRequest.java
│   │   │   └── Tour.java
│   │   ├── 📁 dto/                     # Data Transfer Objects
│   │   │   └── ApiResponse.java
│   │   └── 📁 util/                    # Utilities
│   │       └── XMLParser.java
│   ├── 📁 src/test/                    # Tests unitaires
│   └── pom.xml                         # Maven configuration
│
├── 📁 frontend/                        # Frontend React
│   ├── 📁 src/
│   │   ├── 📁 components/              # Composants React
│   │   │   ├── Navigation.jsx
│   │   │   ├── MapViewer.jsx
│   │   │   ├── TourTabs.jsx
│   │   │   ├── TourTable.jsx
│   │   │   ├── DeliveryRequestUploader.jsx
│   │   │   ├── ManualDeliveryForm.jsx
│   │   │   ├── TourActions.jsx
│   │   │   └── CustomAlert.jsx
│   │   ├── 📁 services/                # API Services
│   │   │   └── apiService.js
│   │   └── Front.jsx                   # Composant principal
│   ├── index.html
│   ├── main.jsx
│   ├── package.json
│   └── vite.config.js
│
├── 📁 fichiersXMLPickupDelivery/       # Fichiers XML de test
│   ├── petitPlan.xml
│   ├── moyenPlan.xml
│   ├── grandPlan.xml
│   ├── demandePetit1.xml
│   └── ...
│
├── 📄 README.md                        # Ce fichier
├── 📄 BANNER.txt                       # Bannière ASCII
├── 🚀 start.bat                        # Script Windows
└── 🚀 start.sh                         # Script Linux/macOS
```

---

## 🌐 API REST

### Endpoints Principaux

#### 📍 Gestion des Cartes

```http
POST   /api/maps/upload
GET    /api/maps/current
DELETE /api/maps/clear
```

**Exemple : Charger une carte**
```bash
curl -X POST http://localhost:8080/api/maps/upload \
  -F "file=@petitPlan.xml"
```

#### 📦 Gestion des Demandes

```http
POST   /api/deliveries/upload         # Upload XML demandes
POST   /api/deliveries/add             # Ajouter une demande
GET    /api/deliveries/current         # Obtenir demandes actuelles
DELETE /api/deliveries/{id}            # Supprimer une demande
DELETE /api/deliveries                 # Vider toutes les demandes
POST   /api/deliveries/warehouse       # Définir l'entrepôt
```

**Exemple : Ajouter une demande manuellement**
```bash
curl -X POST http://localhost:8080/api/deliveries/add \
  -H "Content-Type: application/json" \
  -d '{
    "pickupAddress": "123",
    "deliveryAddress": "456",
    "pickupDuration": 300,
    "deliveryDuration": 300
  }'
```

#### 🚴 Calcul de Tournées

```http
POST   /api/tours/calculate            # Calculer tournée
POST   /api/tours/recalculate          # Recalculer avec assignments
PUT    /api/tours/assignments          # Modifier assignments
```

**Exemple : Calculer une tournée pour 2 coursiers**
```bash
curl -X POST http://localhost:8080/api/tours/calculate?courierCount=2
```

### Format des Réponses

Toutes les réponses suivent ce format standardisé :

```json
{
  "success": true,
  "message": "Opération réussie",
  "data": { ... }
}
```

En cas d'erreur :
```json
{
  "success": false,
  "message": "Description de l'erreur",
  "data": null
}
```

---

## 🖥️ Interface Utilisateur

### Navigation Principale

L'interface est organisée en **4 onglets principaux** :

#### 🏠 Accueil
- Page d'introduction
- Instructions de démarrage

#### 🗺️ Carte (Vue Principale)
- **Carte interactive** : Visualisation OpenStreetMap avec Leaflet
- **Panneau de contrôle** :
  - Sélecteur de nombre de coursiers
  - Boutons d'action (Ajouter, Calculer, Modifier, Sauvegarder)
- **Affichage des tournées** : Onglets multi-coursiers avec couleurs distinctes
- **Mode édition** : Modification interactive des assignments

#### 📦 Demandes
- Liste complète des demandes de livraison
- Détails de chaque demande (pickup, delivery, durées)

#### 🚴 Tournées
- **Demandes non assignées** : Liste des demandes qui n'ont pas pu être assignées
- **Statistiques** : Métriques globales des tournées

### Fonctionnalités de la Carte

#### Interactions
- 🖱️ **Zoom** : Molette de la souris ou boutons +/-
- 🤚 **Déplacement** : Cliquer-glisser
- 🖼️ **Plein écran** : Bouton d'extension
- 📍 **Sélection de nœuds** : Mode sélection pour l'ajout manuel

#### Affichage
- 🔵 **Tronçons bleus** : Segments de rue de la carte
- 🟢 **Tronçons verts** : Segments de rue en mode sélection
- 🎨 **Polylignes colorées** : Itinéraires des coursiers
- 📍 **Marqueurs** : Points de pickup (🟢) et delivery (🔴)

### Mode Édition de Tournées

Le mode édition permet de modifier les tournées calculées :

1. **Cliquer sur "Modifier Tournée"** après avoir calculé une tournée
2. **Réassigner des demandes** :
   - Sélectionner un coursier dans le menu déroulant
   - Ou désassigner (retirer de la tournée)
3. **Aperçu en temps réel** :
   - Les demandes désassignées apparaissent immédiatement dans "Non assignées"
   - Les changements sont visibles en temps réel
4. **Valider ou Annuler** :
   - ✅ **Valider** : Recalcule les tournées avec les nouveaux assignments
   - ❌ **Annuler** : Annule toutes les modifications

---

## 🧪 Tests

### Tests Backend (JUnit)

```bash
cd backend
mvn test
```

Tests disponibles :
- ✅ MapServiceTest
- ✅ DeliveryServiceTest
- ✅ TourServiceTest
- ✅ XMLParserTest
- ✅ Integration Tests

### Couverture de Tests

```bash
mvn test jacoco:report
```

Le rapport de couverture sera généré dans :
```
backend/target/site/jacoco/index.html
```

---

## 🤝 Contribution

### Comment Contribuer

1. **Fork** le projet
2. **Créer une branche** : `git checkout -b feature/ma-nouvelle-fonctionnalite`
3. **Commit** vos changements : `git commit -am 'Ajout d'une nouvelle fonctionnalité'`
4. **Push** vers la branche : `git push origin feature/ma-nouvelle-fonctionnalite`
5. **Ouvrir une Pull Request**

### Convention de Code

#### Java (Backend)
- ✅ Suivre les conventions Java standard
- ✅ Utiliser les annotations Lombok
- ✅ Documenter avec JavaDoc
- ✅ Écrire des tests unitaires

#### JavaScript/React (Frontend)
- ✅ Utiliser ES6+ moderne
- ✅ Suivre les conventions React (hooks, composants fonctionnels)
- ✅ Documenter avec JSDoc
- ✅ Utiliser Tailwind CSS pour le styling

---

## 🐛 Résolution des Problèmes

### Backend ne démarre pas

**Problème** : `Port 8080 already in use`
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/macOS
lsof -i :8080
kill -9 <PID>
```

**Problème** : Erreurs de compilation Maven
```bash
# Nettoyer et réinstaller
mvn clean install -U
```

### Frontend ne démarre pas

**Problème** : `Port 5173 already in use`
```bash
# Modifier le port dans vite.config.js
server: {
  port: 5174
}
```

**Problème** : Erreurs de dépendances npm
```bash
# Supprimer node_modules et réinstaller
rm -rf node_modules package-lock.json
npm install
```

### Erreurs CORS

Si vous rencontrez des erreurs CORS, vérifiez la configuration dans :
```java
// backend/src/main/java/com/pickupdelivery/controller/*Controller.java
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
```

---

## 📝 Licence

Ce projet est développé dans un cadre académique pour l'**INSA Lyon**.

**Utilisation** : Projet éducatif - INSA Lyon - 4IF H34

---

## 👥 Équipe

- **Développeurs** : Équipe Projet Agilité 4IF
- **Établissement** : INSA Lyon
- **Cours** : Agilité - 4IF H34
- **Année** : 2024-2025

---

## 📞 Support

Pour toute question ou problème :

1. 📖 Consultez d'abord la documentation
2. 🐛 Vérifiez la section résolution de problèmes
3. 💬 Ouvrez une issue sur GitHub
4. 📧 Contactez l'équipe pédagogique

---

## 🎯 Roadmap

### ✅ Version 1.0 (Actuelle)
- ✅ Architecture REST complète
- ✅ Interface utilisateur moderne
- ✅ Calcul de tournées multi-coursiers
- ✅ Mode édition interactif
- ✅ Sauvegarde/Restauration

### 🚧 Améliorations Futures
- 🔄 Optimisation de l'algorithme TSP
- 📊 Dashboard avec statistiques avancées
- 🔐 Authentification utilisateur
- 💾 Persistance en base de données
- 🌍 Support multilingue
- 📱 Application mobile
- 🔔 Notifications en temps réel
- 📈 Analytics et reporting

---

## 🙏 Remerciements

- 🎓 **INSA Lyon** pour le cadre pédagogique
- 🗺️ **OpenStreetMap** pour les données cartographiques
- ⚛️ **React** et **Spring Boot** pour les frameworks
- 🌱 La communauté open-source

---

<div align="center">

**⭐ Si ce projet vous est utile, n'hésitez pas à le mettre en favoris !**

Made with ❤️ at INSA Lyon

</div>

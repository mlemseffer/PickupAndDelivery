# 📁 Structure Complète du Projet

## Vue d'Ensemble

```
PickupAndDelivery/
├── 📦 backend/                          # Backend Spring Boot
├── 🎨 Site/                             # Frontend React
├── 📄 fichiersXMLPickupDelivery/        # Fichiers de test XML
├── 📘 README.md                         # Documentation principale
├── 🏗️ ARCHITECTURE.md                  # Architecture détaillée
├── 📋 ARCHITECTURE_SUMMARY.md           # Résumé architecture
├── 🚀 QUICKSTART.md                     # Guide démarrage rapide
├── ▶️ start.ps1                         # Script démarrage PowerShell
└── ▶️ start.bat                         # Script démarrage Windows
```

## Backend - Spring Boot

```
backend/
├── 📄 pom.xml                          # Configuration Maven
├── 📄 .gitignore                       # Fichiers à ignorer
│
├── src/main/
│   ├── java/com/pickupdelivery/
│   │   │
│   │   ├── 🚀 PickupDeliveryApplication.java    # Point d'entrée Spring Boot
│   │   │
│   │   ├── 📡 controller/                       # Couche Contrôleur (REST API)
│   │   │   ├── MapController.java               # Endpoints cartes
│   │   │   ├── DeliveryController.java          # Endpoints livraisons
│   │   │   └── TourController.java              # Endpoints tournées
│   │   │
│   │   ├── ⚙️ service/                          # Couche Service (Logique métier)
│   │   │   ├── MapService.java                  # Service gestion cartes
│   │   │   ├── DeliveryService.java             # Service gestion livraisons
│   │   │   └── TourService.java                 # Service calcul tournées
│   │   │
│   │   ├── 📦 model/                            # Couche Modèle (Domaine)
│   │   │   ├── Node.java                        # Nœud du graphe
│   │   │   ├── Segment.java                     # Segment de route
│   │   │   ├── CityMap.java                     # Carte complète
│   │   │   ├── DeliveryRequest.java             # Demande de livraison
│   │   │   └── Tour.java                        # Tournée optimisée
│   │   │
│   │   ├── 📨 dto/                              # Data Transfer Objects
│   │   │   ├── ApiResponse.java                 # Réponse API standardisée
│   │   │   └── MapUploadResponse.java           # Réponse upload carte
│   │   │
│   │   ├── ⚙️ config/                           # Configuration
│   │   │   └── WebConfig.java                   # Config CORS et Web
│   │   │
│   │   └── ❌ exception/                        # Gestion des exceptions
│   │       └── GlobalExceptionHandler.java      # Handler global erreurs
│   │
│   └── resources/
│       └── 📄 application.properties            # Configuration application
│
└── src/test/java/com/pickupdelivery/
    ├── controller/
    │   └── MapControllerTest.java               # Tests contrôleur
    └── service/
        └── MapServiceTest.java                  # Tests service
```

### Rôles des Classes Backend

#### Contrôleurs (REST API)
- **MapController** : Gère upload/récupération/suppression de cartes
- **DeliveryController** : Gère les demandes de livraison
- **TourController** : Calcule les tournées optimisées

#### Services (Business Logic)
- **MapService** : Parse XML, stocke carte en mémoire
- **DeliveryService** : Gère liste des demandes de livraison
- **TourService** : Implémente algorithmes d'optimisation

#### Modèles (Domain)
- **Node** : Représente un point géographique (id, lat, lng)
- **Segment** : Représente une route entre 2 nœuds
- **CityMap** : Contient tous les nœuds et segments
- **DeliveryRequest** : Détails d'une livraison (pickup, delivery, durée)
- **Tour** : Tournée calculée avec route optimisée

## Frontend - React

```
Site/
├── 📄 package.json                     # Dépendances npm
├── 📄 vite.config.js                   # Configuration Vite
├── 📄 .env                             # Variables d'environnement
├── 📄 .gitignore                       # Fichiers à ignorer
├── 📄 index.html                       # Point d'entrée HTML
├── 📄 leaflet-custom.css               # Styles Leaflet
│
├── 🚀 main.jsx                         # Point d'entrée React
├── 🎨 Front.jsx                        # Composant principal
│
└── src/
    ├── 🧩 components/                  # Composants React
    │   ├── Header.jsx                  # En-tête application
    │   ├── Navigation.jsx              # Barre de navigation
    │   ├── MapUploader.jsx             # Upload fichier carte
    │   └── MapViewer.jsx               # Affichage carte Leaflet
    │
    └── 🔌 services/                    # Services API
        └── apiService.js               # Communication HTTP avec backend
```

### Rôles des Composants Frontend

#### Composants React
- **Front.jsx** : Composant racine, gestion état global, routing
- **Header.jsx** : Affiche logo et titre de l'application
- **Navigation.jsx** : Barre de navigation avec icônes cliquables
- **MapUploader.jsx** : Interface upload fichier XML carte
- **MapViewer.jsx** : Affiche carte interactive avec Leaflet

#### Services
- **apiService.js** : 
  - Centralise tous les appels HTTP au backend
  - Gère la communication REST
  - Retourne des Promises avec les données JSON

## Fichiers XML de Test

```
fichiersXMLPickupDelivery/
├── 🗺️ Plans (Cartes)
│   ├── petitPlan.xml                   # Petite carte (~100 nœuds)
│   ├── moyenPlan.xml                   # Carte moyenne (~500 nœuds)
│   └── grandPlan.xml                   # Grande carte (~1000+ nœuds)
│
└── 📦 Demandes de Livraison
    ├── demandePetit1.xml               # 1 livraison
    ├── demandePetit2.xml               # 2 livraisons
    ├── demandeMoyen3.xml               # 3 livraisons
    ├── demandeMoyen5.xml               # 5 livraisons
    ├── demandeGrand7.xml               # 7 livraisons
    └── demandeGrand9.xml               # 9 livraisons
```

## Documentation

```
📚 Documentation/
├── 📘 README.md                        # Documentation principale
│   ├─ Description projet
│   ├─ Architecture générale
│   ├─ Technologies utilisées
│   ├─ Guide d'installation
│   └─ Endpoints API
│
├── 🏗️ ARCHITECTURE.md                 # Architecture détaillée
│   ├─ Diagrammes
│   ├─ Flux de données
│   ├─ Principes d'architecture
│   └─ Dépendances
│
├── 📋 ARCHITECTURE_SUMMARY.md          # Résumé architecture
│   ├─ Vue d'ensemble
│   ├─ Couches backend/frontend
│   ├─ Flux de communication
│   └─ Avantages
│
└── 🚀 QUICKSTART.md                    # Guide démarrage rapide
    ├─ Prérequis
    ├─ Installation
    ├─ Lancement
    └─ Utilisation
```

## Scripts de Démarrage

### start.ps1 (PowerShell)
```powershell
# Démarre backend Spring Boot en arrière-plan
# Attend 15 secondes
# Démarre frontend React
```

### start.bat (Command Prompt)
```batch
# Version CMD pour Windows
# Même fonctionnalité que start.ps1
```

## Configuration

### Backend - application.properties
```properties
# Port serveur
server.port=8080

# Configuration CORS
spring.web.cors.allowed-origins=http://localhost:5173

# Configuration upload fichiers
spring.servlet.multipart.max-file-size=10MB
```

### Frontend - .env
```env
# URL de l'API backend
VITE_API_BASE_URL=http://localhost:8080/api
```

### Frontend - vite.config.js
```javascript
// Configuration Vite
// Proxy vers le backend
// Port de développement: 5173
```

## Technologies par Fichier

### Backend
| Fichier | Annotations Spring | Dépendances |
|---------|-------------------|-------------|
| `*Controller.java` | `@RestController`, `@RequestMapping`, `@CrossOrigin` | Spring Web |
| `*Service.java` | `@Service` | Spring Core |
| `*.java` (Model) | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` | Lombok |
| `WebConfig.java` | `@Configuration` | Spring MVC |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice`, `@ExceptionHandler` | Spring Web |

### Frontend
| Fichier | Hooks React | Librairies |
|---------|------------|------------|
| `Front.jsx` | `useState` | React |
| `MapUploader.jsx` | `useState`, `useRef` | React |
| `MapViewer.jsx` | - | React, Leaflet |
| `apiService.js` | - | Fetch API |

## Points d'Entrée

### Backend
```java
// PickupDeliveryApplication.java
@SpringBootApplication
public class PickupDeliveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(PickupDeliveryApplication.class, args);
    }
}
```

### Frontend
```javascript
// main.jsx
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <PickupDeliveryUI />
  </React.StrictMode>
)
```

## Ports Utilisés

| Service | Port | URL |
|---------|------|-----|
| Backend API | 8080 | http://localhost:8080 |
| Frontend Dev | 5173 | http://localhost:5173 |

## Dépendances

### Backend (pom.xml)
```xml
- spring-boot-starter-web        # REST API
- spring-boot-starter-validation # Validation
- spring-boot-devtools           # Hot reload
- lombok                         # Code generation
- spring-boot-starter-test       # Tests
```

### Frontend (package.json)
```json
- react ^19.2.0                  # Framework UI
- react-dom ^19.2.0              # DOM rendering
- leaflet ^1.9.4                 # Cartographie
- react-leaflet ^5.0.0           # Leaflet + React
- lucide-react ^0.554.0          # Icônes
- vite ^7.2.2                    # Build tool
```

## Commandes Utiles

### Backend
```bash
mvn clean install    # Compiler
mvn test             # Tests
mvn spring-boot:run  # Lancer
```

### Frontend
```bash
npm install          # Installer dépendances
npm run dev          # Mode développement
npm run build        # Build production
npm test             # Tests
```

---

**Structure complète et organisée pour un développement efficace ! 🎯**

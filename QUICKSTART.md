# 🚀 Guide de Démarrage Rapide

## Prérequis

### Backend
- ☕ **Java 17+** : [Télécharger Java](https://adoptium.net/)
- 📦 **Maven 3.6+** : [Installer Maven](https://maven.apache.org/install.html)

### Frontend
- 🟢 **Node.js 18+** : [Télécharger Node.js](https://nodejs.org/)
- 📦 **npm** (inclus avec Node.js)

## Installation

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd PickupAndDelivery
```

### 2. Configuration du Backend

```bash
cd backend

# Compiler le projet
mvn clean install

# Optionnel : Exécuter les tests
mvn test
```

### 3. Configuration du Frontend

```bash
cd Site

# Installer les dépendances
npm install
```

## Lancement de l'Application

### Méthode 1 : Lancement Manuel (2 terminaux)

#### Terminal 1 - Backend
```bash
cd backend
mvn spring-boot:run
```

✅ Le backend démarre sur `http://localhost:8080`

#### Terminal 2 - Frontend
```bash
cd Site
npm run dev
```

✅ Le frontend démarre sur `http://localhost:5173`

### Méthode 2 : Scripts PowerShell (Windows)

Créez un fichier `start.ps1` à la racine :

```powershell
# Démarrage du backend en arrière-plan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; mvn spring-boot:run"

# Attente de 10 secondes pour que le backend démarre
Start-Sleep -Seconds 10

# Démarrage du frontend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Site; npm run dev"

Write-Host "✅ Application démarrée!"
Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
```

Exécutez :
```bash
.\start.ps1
```

## Vérification

### Backend
Testez l'API :
```bash
curl http://localhost:8080/api/maps/status
```

Réponse attendue :
```json
{
  "success": true,
  "message": "Operation successful",
  "data": false
}
```

### Frontend
Ouvrez votre navigateur : `http://localhost:5173`

Vous devriez voir la page d'accueil de l'application.

## Utilisation

### 1. Charger une Carte

1. Cliquez sur l'icône 📍 (localisation) dans la barre de navigation
2. Sélectionnez un fichier XML de carte (ex: `fichiersXMLPickupDelivery/petitPlan.xml`)
3. La carte s'affiche sur la vue Leaflet

### 2. Ajouter des Demandes de Livraison

1. Cliquez sur l'icône 🚴 (vélo) dans la barre de navigation
2. Chargez un fichier XML de demandes (ex: `fichiersXMLPickupDelivery/demandePetit1.xml`)
3. Les demandes sont envoyées au backend

### 3. Calculer une Tournée

1. Cliquez sur l'icône 🛣️ (route) dans la barre de navigation
2. Le système calcule une tournée optimisée
3. La tournée s'affiche sur la carte

## Endpoints API Disponibles

### Cartes
- **POST** `/api/maps/upload` - Upload une carte XML
- **GET** `/api/maps/current` - Récupère la carte actuelle
- **DELETE** `/api/maps/current` - Supprime la carte

### Livraisons
- **POST** `/api/deliveries/upload` - Upload des demandes XML
- **GET** `/api/deliveries` - Liste toutes les demandes
- **POST** `/api/deliveries` - Ajoute une demande
- **DELETE** `/api/deliveries` - Supprime toutes les demandes

### Tournées
- **POST** `/api/tours/calculate?warehouseAddress=<id>` - Calcule une tournée

## Structure des Fichiers XML

### Fichier Carte (Plan)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<reseau>
    <noeud id="1" latitude="45.75" longitude="4.85"/>
    <noeud id="2" latitude="45.76" longitude="4.86"/>
    <troncon origine="1" destination="2" longueur="100.5" nomRue="Rue Example"/>
</reseau>
```

### Fichier Demandes de Livraison
```xml
<?xml version="1.0" encoding="UTF-8"?>
<demandeDeLivraisons>
    <entrepot adresse="1"/>
    <livraison adresseEnlevement="2" adresseLivraison="3" 
               dureeEnlevement="180" dureeLivraison="240"/>
</demandeDeLivraisons>
```

## Résolution de Problèmes

### Le backend ne démarre pas

**Erreur :** `Port 8080 already in use`

**Solution :**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Ou modifier le port dans application.properties
server.port=8081
```

### Le frontend ne se connecte pas au backend

**Vérifiez :**
1. Le backend est bien démarré sur le port 8080
2. Le fichier `.env` contient `VITE_API_BASE_URL=http://localhost:8080/api`
3. CORS est activé dans `WebConfig.java`

### Erreur de parsing XML

**Vérifiez :**
1. Le fichier XML est bien formé
2. Les attributs correspondent aux noms attendus (voir structure ci-dessus)
3. Le fichier n'est pas corrompu

## Développement

### Hot Reload

#### Backend
Le backend redémarre automatiquement avec Spring Boot DevTools.

#### Frontend
Vite recharge automatiquement les changements.

### Tests

#### Backend
```bash
cd backend
mvn test
```

#### Frontend
```bash
cd Site
npm test
```

## Production

### Build Backend
```bash
cd backend
mvn clean package
java -jar target/pickup-delivery-backend-1.0.0.jar
```

### Build Frontend
```bash
cd Site
npm run build
# Les fichiers de production sont dans dist/
```

## Support

Pour toute question ou problème :
1. Consultez le `README.md` et `ARCHITECTURE.md`
2. Vérifiez les logs du backend et frontend
3. Contactez l'équipe de développement

---

**Bon développement ! 🚀**

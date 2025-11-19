# 🚀 GUIDE DE LANCEMENT RAPIDE

## ✅ Méthode Simple (2 Terminaux)

### Terminal 1 - Backend Spring Boot

```bash
cd backend
mvn spring-boot:run -DskipTests
```

**Attendre ce message :**
```
Started PickupDeliveryApplication in X.XXX seconds
```

✅ Backend prêt sur : **http://localhost:8080**

---

### Terminal 2 - Frontend React

**Première fois uniquement :**
```bash
cd Site
npm install
```

**Ensuite, à chaque fois :**
```bash
cd Site
npm run dev
```

✅ Frontend prêt sur : **http://localhost:5173**

---

## 🌐 Accéder à l'Application

1. Ouvrez votre navigateur
2. Allez sur : **http://localhost:5173**
3. L'application est prête ! 🎉

---

## 📝 Utilisation

### 1. Charger une Carte
1. Cliquez sur l'icône 📍 (Map Pin) dans la barre de navigation
2. Cliquez sur "Sélectionner un fichier XML"
3. Choisissez un fichier dans `fichiersXMLPickupDelivery/` (ex: `petitPlan.xml`)
4. La carte s'affiche avec tous les nœuds !

### 2. Charger des Livraisons
1. Cliquez sur l'icône 🚴 (Bike) dans la barre de navigation
2. Uploadez un fichier de demandes (ex: `demandePetit1.xml`)

### 3. Calculer une Tournée
1. Cliquez sur l'icône 🛣️ (Route) dans la barre de navigation
2. Le calcul se lance (fonctionnalité à compléter)

---

## ⚡ Commandes Utiles

### Backend
```bash
# Compiler
mvn clean install

# Lancer (sans tests)
mvn spring-boot:run -DskipTests

# Lancer (avec tests)
mvn spring-boot:run

# Exécuter uniquement les tests
mvn test
```

### Frontend
```bash
# Installer dépendances
npm install

# Mode développement
npm run dev

# Build production
npm run build

# Prévisualiser build
npm run preview
```

---

## 🔧 Résolution de Problèmes

### ❌ "Port 8080 already in use"

**Solution :**
```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus (remplacez <PID> par le numéro)
taskkill /PID <PID> /F
```

### ❌ "Cannot find module"

**Solution :**
```bash
cd Site
rm -rf node_modules
npm install
```

### ❌ Backend ne démarre pas

**Vérifiez :**
1. Java 17+ installé : `java -version`
2. Maven installé : `mvn -version`
3. Port 8080 libre

### ❌ Frontend ne se connecte pas

**Vérifiez :**
1. Backend est bien démarré sur le port 8080
2. Fichier `.env` existe dans `Site/` avec :
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```

---

## 📊 Vérifier que tout fonctionne

### Test Backend
Ouvrez un navigateur ou utilisez curl :
```bash
curl http://localhost:8080/api/maps/status
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": false
}
```

### Test Frontend
1. Ouvrez http://localhost:5173
2. Vous devez voir la page d'accueil
3. Message : "Bienvenue sur votre plateforme..."

---

## 🎯 Ordre Recommandé

```
1. ✅ Lancer Backend (Terminal 1)
   └─ Attendre "Started PickupDeliveryApplication"

2. ✅ Lancer Frontend (Terminal 2)
   └─ Attendre "Local: http://localhost:5173"

3. ✅ Ouvrir Navigateur
   └─ http://localhost:5173

4. ✅ Tester l'application
   └─ Charger une carte XML
```

---

## 💡 Astuces

- **Backend :** Une fois démarré, il reste actif jusqu'à arrêt manuel (Ctrl+C)
- **Frontend :** Hot reload automatique des changements de code
- **Fichiers XML :** Tous les fichiers de test sont dans `fichiersXMLPickupDelivery/`

---

Bonne utilisation ! 🚀

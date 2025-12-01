#!/bin/bash

# Script de test pour le Graph
# Assure-toi que l'application Spring Boot est lancée sur le port 8080

echo "================================================================================"
echo "🧪 TEST DU GRAPH - Pickup and Delivery"
echo "================================================================================"
echo ""

BASE_URL="http://localhost:8080"
XML_DIR="../fichiersXMLPickupDelivery"

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les résultats
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        exit 1
    fi
}

# 1. Charger la carte
echo -e "${BLUE}📂 Étape 1 : Chargement de la carte (petitPlan.xml)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST -F "file=@${XML_DIR}/petitPlan.xml" ${BASE_URL}/api/map/upload)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Carte chargée avec succès"
    BODY=$(echo "$RESPONSE" | sed '$d')
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    print_result 1 "Échec du chargement de la carte (HTTP $HTTP_CODE)"
fi
echo ""

# 2. Charger les demandes
echo -e "${BLUE}📂 Étape 2 : Chargement des demandes (demandePetit1.xml)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST -F "file=@${XML_DIR}/demandePetit1.xml" ${BASE_URL}/api/delivery/upload)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Demandes chargées avec succès"
    BODY=$(echo "$RESPONSE" | sed '$d')
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    print_result 1 "Échec du chargement des demandes (HTTP $HTTP_CODE)"
fi
echo ""

# 3. Tester le Graph - Résumé
echo -e "${BLUE}🔄 Étape 3 : Génération du Graph (résumé)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" ${BASE_URL}/api/test/graph/summary)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Graph généré avec succès"
    BODY=$(echo "$RESPONSE" | sed '$d')
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    print_result 1 "Échec de la génération du Graph (HTTP $HTTP_CODE)"
fi
echo ""

# 4. Tester le Graph - Détails complets
echo -e "${BLUE}📊 Étape 4 : Analyse détaillée du Graph${NC}"
RESPONSE=$(curl -s ${BASE_URL}/api/test/graph)
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

echo ""
echo "================================================================================"
echo -e "${GREEN}✅ TEST TERMINÉ${NC}"
echo "================================================================================"
echo ""
echo "💡 Résumé :"
echo "   - Nombre de nœuds : $(echo "$RESPONSE" | jq -r '.cityMap.nodesCount' 2>/dev/null || echo 'N/A')"
echo "   - Nombre de stops : $(echo "$RESPONSE" | jq -r '.stops.totalStops' 2>/dev/null || echo 'N/A')"
echo "   - Taille de la matrice : $(echo "$RESPONSE" | jq -r '.graph.matrixSize' 2>/dev/null || echo 'N/A')"
echo "   - Warehouse : $(echo "$RESPONSE" | jq -r '.graph.warehouseNodeId' 2>/dev/null || echo 'N/A')"
echo "   - Temps de construction : $(echo "$RESPONSE" | jq -r '.constructionTimeMs' 2>/dev/null || echo 'N/A') ms"
echo "   - Distance min : $(echo "$RESPONSE" | jq -r '.graph.minDistance' 2>/dev/null || echo 'N/A') m"
echo "   - Distance max : $(echo "$RESPONSE" | jq -r '.graph.maxDistance' 2>/dev/null || echo 'N/A') m"
echo "   - Distance moyenne : $(echo "$RESPONSE" | jq -r '.graph.averageDistance' 2>/dev/null || echo 'N/A') m"
echo "   - Nombre total de trajets : $(echo "$RESPONSE" | jq -r '.graph.totalTrajets' 2>/dev/null || echo 'N/A')"
echo "   - Graph valide : $(echo "$RESPONSE" | jq -r '.isValid' 2>/dev/null || echo 'N/A')"
echo ""

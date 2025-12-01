import React, { useState } from 'react';
import Navigation from './src/components/Navigation';
import MapUploader from './src/components/MapUploader';
import MapViewer from './src/components/MapViewer';
import DeliveryRequestUploader from './src/components/DeliveryRequestUploader';
import ManualDeliveryForm from './src/components/ManualDeliveryForm';
import CourierCountModal from './src/components/CourierCountModal';
import TourTable from './src/components/TourTable';
import TourActions from './src/components/TourActions';
import apiService from './src/services/apiService';
import './leaflet-custom.css';

/**
 * Génère le contenu texte de l'itinéraire
 */
function generateItineraryText(tourData) {
  let content = '=== ITINÉRAIRE DE LIVRAISON ===\n\n';
  content += `Nombre de segments: ${tourData.tour?.length || 0}\n`;
  content += `Distance totale: ${tourData.metrics?.totalDistance?.toFixed(2) || 0} m\n`;
  content += `Nombre de stops: ${tourData.metrics?.stopCount || 0}\n\n`;
  content += '=== TRAJETS ===\n\n';

  if (tourData.tour && Array.isArray(tourData.tour)) {
    tourData.tour.forEach((trajet, index) => {
      content += `${index + 1}. ${trajet.nomRue || 'Segment'}\n`;
      content += `   De: ${trajet.origine || 'N/A'}\n`;
      content += `   À: ${trajet.destination || 'N/A'}\n`;
      content += `   Longueur: ${(trajet.longueur || 0).toFixed(2)} m\n\n`;
    });
  }

  return content;
}

/**
 * Convertit une couleur HSL en format hexadécimal
 */
function hslToHex(h, s, l) {
  s /= 100;
  l /= 100;
  
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs((h / 60) % 2 - 1));
  const m = l - c / 2;
  
  let r = 0, g = 0, b = 0;
  
  if (0 <= h && h < 60) {
    r = c; g = x; b = 0;
  } else if (60 <= h && h < 120) {
    r = x; g = c; b = 0;
  } else if (120 <= h && h < 180) {
    r = 0; g = c; b = x;
  } else if (180 <= h && h < 240) {
    r = 0; g = x; b = c;
  } else if (240 <= h && h < 300) {
    r = x; g = 0; b = c;
  } else if (300 <= h && h < 360) {
    r = c; g = 0; b = x;
  }
  
  // Convertir en valeurs RGB (0-255)
  r = Math.round((r + m) * 255);
  g = Math.round((g + m) * 255);
  b = Math.round((b + m) * 255);
  
  // Convertir en hexadécimal
  const toHex = (n) => {
    const hex = n.toString(16);
    return hex.length === 1 ? '0' + hex : hex;
  };
  
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
}

/**
 * Génère 50 couleurs vives et distinctes en utilisant l'espace HSL
 * et les retourne dans l'ordre: 5e, 10e, 15e, ..., 45e, puis 6e, 11e, ..., 46e, etc.
 */
function generateColorPalette() {
  const totalColors = 50;
  
  // Générer 50 couleurs en HSL avec saturation et luminosité optimales
  const baseColors = [];
  for (let i = 0; i < totalColors; i++) {
    const hue = (360 * i) / totalColors; // Répartition uniforme sur la roue chromatique (0-360°)
    const saturation = 75; // Saturation élevée pour des couleurs vives (75%)
    const lightness = 55; // Luminosité moyenne pour une bonne visibilité (55%)
    
    const hexColor = hslToHex(hue, saturation, lightness);
    baseColors.push(hexColor);
  }
  
  // Réorganiser selon la séquence demandée: prendre de 5 en 5
  // 5e (index 4), 10e (index 9), 15e (index 14), ..., 45e (index 44)
  // puis 6e (index 5), 11e (index 10), ..., 46e (index 45)
  // puis 7e (index 6), 12e (index 11), ..., 47e (index 46)
  // etc.
  const reorderedColors = [];
  for (let offset = 4; offset < totalColors; offset++) {
    for (let i = offset; i < totalColors; i += 5) {
      reorderedColors.push(baseColors[i]);
    }
  }
  
  // Ajouter les couleurs restantes (indices 0-3)
  for (let i = 0; i < 4; i++) {
    if (i < totalColors) {
      reorderedColors.push(baseColors[i]);
    }
  }
  
  return reorderedColors;
}

// Palette de couleurs générée
const COLOR_PALETTE = generateColorPalette();

/**
 * Obtient une couleur de la palette en utilisant un modulo
 */
function getColorFromPalette(index) {
  return COLOR_PALETTE[index % COLOR_PALETTE.length];
}

/**
 * Composant principal de l'application Pickup & Delivery
 * Gère l'état global et la navigation entre les différentes vues
 * Communique avec le backend via apiService
 */
export default function PickupDeliveryUI() {
  const [activeTab, setActiveTab] = useState('home');
  const [showMessage, setShowMessage] = useState(true);
  const [showMapUpload, setShowMapUpload] = useState(false);
  const [showDeliveryUpload, setShowDeliveryUpload] = useState(false);
  const [showManualForm, setShowManualForm] = useState(false);
  const [showCourierModal, setShowCourierModal] = useState(false);
  const [mapData, setMapData] = useState(null);
  const [deliveryRequestSet, setDeliveryRequestSet] = useState(null);
  const [courierCount, setCourierCount] = useState(1);
  const [tourData, setTourData] = useState(null);
  const [isCalculatingTour, setIsCalculatingTour] = useState(false);
  
  // États pour la sélection sur la carte
  const [isMapSelectionActive, setIsMapSelectionActive] = useState(false);
  const [mapSelectionType, setMapSelectionType] = useState(null); // 'pickup' ou 'delivery'
  const [selectedNodeId, setSelectedNodeId] = useState(null);
  const [savedFormData, setSavedFormData] = useState(null); // Pour sauvegarder les données du formulaire

  // État pour savoir si on est en mode ajout manuel (formulaire ouvert ou sélection active)
  const isAddingManually = showManualForm || isMapSelectionActive;

  // Gestion du changement d'onglet
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    if (tab === 'map') {
      if (!mapData) {
        setShowMapUpload(true);
      }
      setShowMessage(false);
    } else {
      setShowMapUpload(false);
    }
  };

  // Gestion du chargement de la carte depuis le backend
  const handleMapLoaded = (map) => {
    setMapData(map);
    setShowMapUpload(false);
    setShowMessage(false);
    setActiveTab('map');
  };

  // Gestion de l'annulation du chargement
  const handleCancelUpload = () => {
    setShowMapUpload(false);
    if (!mapData) {
      setActiveTab('home');
    }
  };

  // Gestion de la suppression de la carte
  const handleClearMap = async () => {
    try {
      await apiService.clearMap();
      setMapData(null);
      setDeliveryRequestSet(null);
      setTourData(null);
      setShowMapUpload(true);
    } catch (error) {
      console.error('Erreur lors de la suppression de la carte:', error);
    }
  };

  // Gestion de la mise à jour des demandes (suppression, etc.)
  const handleDeliveryRequestSetUpdated = (updatedSet) => {
    console.log('handleDeliveryRequestSetUpdated reçoit:', updatedSet);
    
    // Réassigner les couleurs dans le bon ordre après modification
    if (updatedSet?.demands) {
      const demandsWithColors = updatedSet.demands.map((demand, index) => ({
        ...demand,
        color: getColorFromPalette(index)
      }));
      
      setDeliveryRequestSet({
        ...updatedSet,
        demands: demandsWithColors
      });
    } else {
      setDeliveryRequestSet(updatedSet);
    }
  };

  // Gestion du chargement des demandes de livraison
  const handleDeliveryRequestsLoaded = (requestSet) => {
    // Assigner des couleurs à chaque demande en utilisant la palette
    const demandsWithColors = requestSet.demands.map((demand, index) => ({
      ...demand,
      color: getColorFromPalette(index)
    }));
    
    setDeliveryRequestSet({
      ...requestSet,
      demands: demandsWithColors
    });
    setTourData(null); // Réinitialiser la tournée si on charge de nouvelles demandes
    setShowDeliveryUpload(false);
};


  // Gestion de l'annulation du chargement des demandes
  const handleCancelDeliveryUpload = () => {
    setShowDeliveryUpload(false);
  };

  // Gestion du calcul de la tournée
  const handleCalculateTour = async () => {
    if (!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0) {
      alert('Veuillez d\'abord charger des demandes de livraison');
      return;
    }

    setIsCalculatingTour(true);
    
    try {
      console.log(`🚀 Calcul de la tournée pour ${courierCount} livreur(s)...`);
      const result = await apiService.calculateTour(courierCount);
      
      console.log('📦 Résultat complet:', result);
      
      if (result.success && result.data && result.data.length > 0) {
        const tour = result.data[0]; // Premier tour (pour 1 livreur)
        console.log('✅ Tournée calculée avec succès:', tour);
        console.log('🔍 Structure du tour:', {
          keys: Object.keys(tour),
          trajets: tour.trajets,
          stops: tour.stops,
          totalDistance: tour.totalDistance
        });
        
        // Créer un objet tourData pour le MapViewer
        const tourData = {
          tour: tour.trajets || tour.segments || tour.path || [],  // Liste des trajets
          metrics: {
            stopCount: tour.stops?.length || 0,
            totalDistance: tour.totalDistance || 0,
            segmentCount: (tour.trajets || tour.segments || tour.path || []).length
          }
        };
        
        console.log('📊 tourData créé:', tourData);
        console.log('📊 tour.length:', tourData.tour.length);
        
        setTourData(tourData);
        alert(`✅ Tournée calculée avec succès !\n\n` +
              `📍 Stops: ${tourData.metrics.stopCount}\n` +
              `📏 Distance: ${tourData.metrics.totalDistance.toFixed(2)} m\n` +
              `🛣️  Segments: ${tourData.metrics.segmentCount}`);
      } else {
        console.error('❌ Réponse invalide:', result);
        alert(`Erreur: ${result.message || 'Réponse invalide du serveur'}`);
      }
    } catch (error) {
      console.error('💥 Erreur lors du calcul de la tournée:', error);
      alert(`Erreur: ${error.message}`);
    } finally {
      setIsCalculatingTour(false);
    }
  };

  // Gestion du clic sur "Ajouter Pickup&Delivery" (ajout manuel)
  const handleAddDeliveryManually = () => {
    if (!mapData) {
      alert('Veuillez d\'abord charger une carte');
      return;
    }
    setShowManualForm(true);
  };

  // Gestion de l'ajout manuel d'une demande
  const handleManualDemandAdd = async (demand) => {
    try {
      // Enregistre la demande dans le backend
      await apiService.addDeliveryRequest({
        pickupAddress: demand.pickupNodeId,
        deliveryAddress: demand.deliveryNodeId,
        pickupDuration: demand.pickupDurationSec,
        deliveryDuration: demand.deliveryDurationSec
      });
      // Rafraîchit la liste depuis le backend pour récupérer l'id et l'état à jour
      const requestSet = await apiService.getCurrentRequestSet();
      
      // Réassigner les couleurs dans le bon ordre
      if (requestSet?.demands) {
        const demandsWithColors = requestSet.demands.map((demand, index) => ({
          ...demand,
          color: getColorFromPalette(index)
        }));
        
        setDeliveryRequestSet({
          ...requestSet,
          demands: demandsWithColors
        });
      } else {
        setDeliveryRequestSet(requestSet);
      }
    } catch (err) {
      alert('Erreur lors de l\'ajout manuel : ' + err.message);
    }
    setShowManualForm(false);
    setSelectedNodeId(null);
    setMapSelectionType(null);
    setSavedFormData(null);
  };

  // Gestion du démarrage de la sélection sur la carte
  const handleStartMapSelection = (type, formData) => {
    setSavedFormData(formData); // Sauvegarder les données du formulaire
    setMapSelectionType(type);
    setIsMapSelectionActive(true);
    setShowManualForm(false); // Fermer le formulaire
  };

  // Gestion du clic sur un segment de la carte
  const handleMapSegmentClick = (nodeId) => {
    if (isMapSelectionActive) {
      setSelectedNodeId(nodeId);
      setIsMapSelectionActive(false);
      setShowManualForm(true); // Rouvrir le formulaire
    }
  };

  return (
    <div className="h-screen bg-gray-800 text-white flex flex-col overflow-hidden">
      {/* Navigation Bar avec titre intégré */}
      <div className={isMapSelectionActive ? 'pointer-events-none opacity-50' : ''}>
        <Navigation 
          activeTab={activeTab}
          onTabChange={handleTabChange}
          showMapMessage={showMessage}
          hasMap={mapData !== null}
          onLoadDeliveryRequests={() => setShowDeliveryUpload(true)}
        />
      </div>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-h-0 overflow-hidden">
        {/* Home View */}
        {activeTab === 'home' && !showMapUpload && (
          <div className="p-8 mt-20">
            <h2 className="text-3xl font-bold text-center">
              Bienvenue sur votre plateforme de gestion de tournées de livraison à vélo !
            </h2>
            <p className="text-center text-gray-300 mt-4">
              Cliquez sur l'icône de localisation pour charger une carte.
            </p>
          </div>
        )}

        {/* Map Upload View */}
        {showMapUpload && !mapData && (
          <MapUploader 
            onMapLoaded={handleMapLoaded}
            onCancel={handleCancelUpload}
          />
        )}

        {/* Delivery Upload View (XML) */}
        {showDeliveryUpload && mapData && (
          <DeliveryRequestUploader 
            onRequestsLoaded={handleDeliveryRequestsLoaded}
            onCancel={handleCancelDeliveryUpload}
          />
        )}

        {/* Manual Delivery Form */}
        {showManualForm && mapData && (
          <ManualDeliveryForm 
            onAdd={handleManualDemandAdd}
            onCancel={() => {
              setShowManualForm(false);
              setSelectedNodeId(null);
              setMapSelectionType(null);
              setSavedFormData(null);
            }}
            availableNodes={mapData.nodes}
            onStartMapSelection={handleStartMapSelection}
            selectedNodeId={selectedNodeId}
            mapSelectionType={mapSelectionType}
            savedFormData={savedFormData}
          />
        )}

        {/* Courier Count Modal */}
        <CourierCountModal 
          isOpen={showCourierModal}
          onClose={() => setShowCourierModal(false)}
          onConfirm={(count) => {
            setCourierCount(count);
            console.log(`Nombre de livreurs défini à: ${count}`);
          }}
          currentCount={courierCount}
        />

        {/* Map View */}
        {mapData && activeTab === 'map' && !showDeliveryUpload && (
          <div className="flex-1 flex flex-col overflow-hidden p-4 gap-4 min-h-0">
            {/* Ligne principale : Carte + Panneau d'informations */}
            <div className="flex-1 flex gap-4 min-h-0">
              {/* Carte sur la gauche - plus grande */}
              <div className="w-2/3 flex flex-col bg-gray-700 rounded-lg overflow-hidden">
                <MapViewer 
                  mapData={mapData}
                  onClearMap={handleClearMap}
                  deliveryRequestSet={deliveryRequestSet}
                  onDeliveryRequestSetUpdated={handleDeliveryRequestSetUpdated}
                  tourData={tourData}
                  onSegmentClick={handleMapSegmentClick}
                  isMapSelectionActive={isMapSelectionActive}
                  isAddingManually={isAddingManually}
                />
              </div>
              
              {/* Panneau droit avec informations et boutons */}
              <div className={`flex-1 flex flex-col gap-4 min-h-0 ${isMapSelectionActive ? 'pointer-events-none opacity-50' : ''}`}>
                {/* Tableau de tournée */}
                <div className="bg-gray-700 rounded-lg p-6 flex flex-col flex-1 min-h-0 overflow-hidden">
                  <h3 className="text-xl font-semibold mb-4 flex-shrink-0">
                    {tourData ? 'Tournée Calculée' : 'Informations'}
                  </h3>
                  <div className="flex-1 overflow-auto min-h-0">
                    {tourData ? (
                      <TourTable 
                        tourData={tourData}
                        deliveryRequestSet={deliveryRequestSet}
                      />
                    ) : (
                      <div className="text-gray-400 text-center py-8">
                        Chargez des demandes et calculez une tournée
                      </div>
                    )}
                  </div>
                </div>
                
                {/* Boutons d'action */}
                <div className="bg-gray-700 rounded-lg p-4 flex-shrink-0">
                  {!tourData ? (
                    // Boutons avant calcul de tournée
                    <div className="flex gap-3 justify-center">
                      {/* Bouton Nombre de livreurs */}
                      <button 
                        onClick={() => setShowCourierModal(true)}
                        disabled={!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0}
                        className="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                 text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                        title="Choisir le nombre de livreurs"
                      >
                        Nombre de livreurs {deliveryRequestSet?.demands?.length > 0 && `(${courierCount})`}
                      </button>
                      
                      {/* Bouton Ajouter Pickup&Delivery (manuel) */}
                      <button 
                        onClick={handleAddDeliveryManually}
                        disabled={!deliveryRequestSet}
                        className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                        title={!deliveryRequestSet ? "Chargez d'abord des demandes de livraison" : "Ajouter manuellement une demande de livraison"}
                      >
                        Ajouter Pickup&Delivery
                      </button>
                      
                      {/* Bouton Calculer tournée */}
                      <button 
                        onClick={handleCalculateTour}
                        disabled={!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0 || isCalculatingTour}
                        className="flex-1 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                 text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                        title="Calculer la tournée optimale"
                      >
                        {isCalculatingTour ? 'Calcul en cours...' : 'Calculer tournée'}
                      </button>
                    </div>
                  ) : (
                    // Boutons après calcul de tournée (4 boutons sur 2 lignes)
                    <div className="flex flex-col gap-3">
                      {/* Première ligne : Ajouter et Calculer tournée */}
                      <div className="flex gap-3">
                        <button 
                          onClick={handleAddDeliveryManually}
                          className="flex-1 bg-green-600 hover:bg-green-700 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Ajouter une nouvelle demande de livraison"
                        >
                          ➕ Ajouter Pickup&Delivery
                        </button>

                        <button
                          onClick={handleCalculateTour}
                          disabled={!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0 || isCalculatingTour}
                          className="flex-1 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Calculer la tournée optimale"
                        >
                          {isCalculatingTour ? 'Calcul en cours...' : '🧮 Calculer tournée'}
                        </button>
                      </div>
                      
                      {/* Deuxième ligne : Sauvegarder */}
                      <div className="flex gap-3">
                        <button
                          onClick={() => {
                            const content = generateItineraryText(tourData);
                            const blob = new Blob([content], { type: 'text/plain' });
                            const url = URL.createObjectURL(blob);
                            const link = document.createElement('a');
                            link.href = url;
                            link.download = `itineraire_${new Date().toISOString().split('T')[0]}.txt`;
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                            URL.revokeObjectURL(url);
                          }}
                          disabled={!tourData}
                          className="flex-1 bg-teal-600 hover:bg-teal-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Sauvegarder l'itinéraire en fichier texte"
                        >
                          📄 Sauvegarder itinéraire
                        </button>

                        <button
                          onClick={() => {
                            const tourJson = JSON.stringify(tourData, null, 2);
                            const blob = new Blob([tourJson], { type: 'application/json' });
                            const url = URL.createObjectURL(blob);
                            const link = document.createElement('a');
                            link.href = url;
                            link.download = `tournee_${new Date().toISOString().split('T')[0]}.json`;
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                            URL.revokeObjectURL(url);
                          }}
                          disabled={!tourData}
                          className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Sauvegarder la tournée complète (JSON)"
                        >
                          💾 Sauvegarder Tournée
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Deliveries View - À implémenter */}
        {activeTab === 'deliveries' && (
          <div className="p-8 mt-20">
            <h2 className="text-2xl font-bold text-center">
              Gestion des demandes de livraison
            </h2>
            <p className="text-center text-gray-300 mt-4">
              Cette section sera disponible prochainement.
            </p>
          </div>
        )}

        {/* Tours View - À implémenter */}
        {activeTab === 'tours' && (
          <div className="p-8 mt-20">
            <h2 className="text-2xl font-bold text-center">
              Calcul de tournées optimisées
            </h2>
            <p className="text-center text-gray-300 mt-4">
              Cette section sera disponible prochainement.
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
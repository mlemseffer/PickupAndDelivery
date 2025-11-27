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

  // Gestion du chargement des demandes de livraison
  const handleDeliveryRequestsLoaded = (requestSet) => {
    setDeliveryRequestSet(requestSet);
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
  const handleManualDemandAdd = (demand) => {
    // Créer ou mettre à jour le DeliveryRequestSet
    const newDemand = {
      id: Date.now().toString(),
      ...demand,
      courierId: null,
      // Assigner une couleur aléatoire
      color: '#' + Math.floor(Math.random()*16777215).toString(16)
    };

    if (!deliveryRequestSet) {
      // Créer un nouveau set avec juste cette demande
      setDeliveryRequestSet({
        warehouse: null,
        demands: [newDemand]
      });
    } else {
      // Ajouter à la liste existante
      setDeliveryRequestSet({
        ...deliveryRequestSet,
        demands: [...deliveryRequestSet.demands, newDemand]
      });
    }

    setShowManualForm(false);
  };

  return (
    <div className="h-screen bg-gray-800 text-white flex flex-col overflow-hidden">
      {/* Navigation Bar avec titre intégré */}
      <Navigation 
        activeTab={activeTab}
        onTabChange={handleTabChange}
        showMapMessage={showMessage}
        hasMap={mapData !== null}
        onLoadDeliveryRequests={() => setShowDeliveryUpload(true)}
      />

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
            onCancel={() => setShowManualForm(false)}
            availableNodes={mapData.nodes}
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
                  tourData={tourData}
                />
              </div>
              
              {/* Panneau droit avec informations et boutons */}
              <div className="flex-1 flex flex-col gap-4 min-h-0">
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
                        className="flex-1 bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                        title="Ajouter manuellement une demande de livraison"
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
                    // Boutons après calcul de tournée
                    <TourActions 
                      tourData={tourData}
                      onModify={() => {
                        // TODO: Implémenter la modification de tournée
                        alert('Fonctionnalité de modification à implémenter');
                      }}
                      onSaveItinerary={() => {
                        console.log('Itinéraire sauvegardé');
                      }}
                      onSaveTour={() => {
                        console.log('Tournée sauvegardée');
                      }}
                    />
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
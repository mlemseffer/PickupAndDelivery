import React, { useState } from 'react';
import Navigation from './src/components/Navigation';
import MapUploader from './src/components/MapUploader';
import MapViewer from './src/components/MapViewer';
import DeliveryRequestUploader from './src/components/DeliveryRequestUploader';
import ManualDeliveryForm from './src/components/ManualDeliveryForm';
import CourierCountModal from './src/components/CourierCountModal';
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
      setShowMapUpload(true);
    } catch (error) {
      console.error('Erreur lors de la suppression de la carte:', error);
    }
  };

  // Gestion de la mise à jour des demandes (suppression, etc.)
  const handleDeliveryRequestSetUpdated = (updatedSet) => {
    console.log('handleDeliveryRequestSetUpdated reçoit:', updatedSet);
    setDeliveryRequestSet(updatedSet);
  };

  // Gestion du chargement des demandes de livraison
  const handleDeliveryRequestsLoaded =(requestSet) => {
     // Utilise directement le requestSet reçu (avec tous les ids)
    console.log('handleDeliveryRequestsLoaded reçoit:', requestSet);
    setDeliveryRequestSet(requestSet);
    setShowDeliveryUpload(false);
};


  // Gestion de l'annulation du chargement des demandes
  const handleCancelDeliveryUpload = () => {
    setShowDeliveryUpload(false);
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
      setDeliveryRequestSet(requestSet);
    } catch (err) {
      alert('Erreur lors de l’ajout manuel : ' + err.message);
    }
    setShowManualForm(false);
  };

  return (
    <div className="h-screen bg-gray-800 text-white flex flex-col">
      {/* Navigation Bar avec titre intégré */}
      <Navigation 
        activeTab={activeTab}
        onTabChange={handleTabChange}
        showMapMessage={showMessage}
        hasMap={mapData !== null}
        onLoadDeliveryRequests={() => setShowDeliveryUpload(true)}
      />

      {/* Main Content */}
      <main className="flex-1 flex flex-col">
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
          <div className="flex-1 flex flex-col gap-4 overflow-hidden">
            {/* Ligne principale : Carte + Panneau d'informations */}
            <div className="flex-1 flex gap-4 min-h-0 p-4 pt-2">
              {/* Carte sur la gauche - plus grande */}
              <div className="w-2/3 flex flex-col bg-gray-700 rounded-lg overflow-hidden">
                <MapViewer 
                  mapData={mapData}
                  onClearMap={handleClearMap}
                  deliveryRequestSet={deliveryRequestSet}
                  onDeliveryRequestSetUpdated={handleDeliveryRequestSetUpdated}
                />
              </div>
              
              {/* Panneau droit avec informations et boutons */}
              <div className="flex-1 flex flex-col gap-4">
                {/* Espace pour les tableaux */}
                <div className="flex-1 bg-gray-700 rounded-lg p-6">
                  <h3 className="text-xl font-semibold mb-4">Informations</h3>
                </div>
                
                {/* Boutons d'action en bas à droite */}
                <div className="bg-gray-700 rounded-lg p-4">
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
                    <button className="flex-1 bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg">
                      Calculer tournée
                    </button>
                  </div>
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
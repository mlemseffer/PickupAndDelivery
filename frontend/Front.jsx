import React, { useState } from 'react';
import Header from './src/components/Header';
import Navigation from './src/components/Navigation';
import MapUploader from './src/components/MapUploader';
import MapViewer from './src/components/MapViewer';
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
  const [mapData, setMapData] = useState(null);

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
      setShowMapUpload(true);
    } catch (error) {
      console.error('Erreur lors de la suppression de la carte:', error);
    }
  };

  return (
    <div className="h-screen bg-gray-800 text-white flex flex-col">
      {/* Header */}
      <Header />

      {/* Navigation Bar */}
      <Navigation 
        activeTab={activeTab}
        onTabChange={handleTabChange}
        showMapMessage={showMessage}
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

        {/* Map View */}
        {mapData && activeTab === 'map' && (
          <div className="flex-1 flex flex-col p-4 gap-4 overflow-hidden">
            {/* Ligne du haut : Carte + Panneau d'informations */}
            <div className="flex-1 flex gap-4 min-h-0">
              {/* Carte sur la gauche */}
              <div className="w-3/5 flex flex-col bg-gray-700 rounded-lg overflow-hidden">
                <MapViewer 
                  mapData={mapData}
                  onClearMap={handleClearMap}
                />
              </div>
              
              {/* Espace réservé pour les tableaux et boutons à droite */}
              <div className="flex-1 bg-gray-700 rounded-lg p-6">
                <h3 className="text-xl font-semibold mb-4">Informations</h3>
                <p className="text-gray-400">
                  Les tableaux et boutons s'afficheront ici.
                </p>
              </div>
            </div>
            
            {/* Zone du bas : espace réservé pour les futurs boutons */}
            <div className="h-32 bg-gray-700 rounded-lg p-4 flex items-center justify-center">
              <p className="text-gray-400">Espace réservé pour les boutons d'action</p>
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
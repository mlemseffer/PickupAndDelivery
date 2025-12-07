import React, { useState } from 'react';
import GlobalStatistics from './GlobalStatistics';
import CourierTourCard from './CourierTourCard';
import { getCourierColor } from '../utils/courierColors';

/**
 * Composant pour naviguer entre les différents coursiers et la vue globale
 * 
 * @param {Object} props
 * @param {Array} props.tours - Liste de toutes les tournées
 * @param {Object} props.deliveryRequestSet - Ensemble des demandes de livraison
 * @param {Function} props.onTourSelect - Callback appelé lors de la sélection d'un coursier (pour la carte)
 */
export default function TourTabs({ tours, deliveryRequestSet, onTourSelect }) {
  const [selectedCourierId, setSelectedCourierId] = useState(null); // null = vue globale

  if (!tours || tours.length === 0) {
    return (
      <div className="text-gray-400 text-center py-8">
        Aucune tournée calculée
      </div>
    );
  }

  const handleTabClick = (courierId) => {
    setSelectedCourierId(courierId);
    
    // Notifier le parent pour mettre à jour la carte
    if (onTourSelect) {
      if (courierId === null) {
        onTourSelect(null); // Vue globale : afficher tous les tours
      } else {
        const selectedTour = tours.find(t => t.courierId === courierId);
        onTourSelect(selectedTour);
      }
    }
  };

  return (
    <div className="tour-tabs flex flex-col h-full">
      {/* Onglets en haut */}
      <div className="flex border-b border-gray-600 mb-4 overflow-x-auto flex-shrink-0">
        {/* Onglet Vue Globale */}
        <button
          onClick={() => handleTabClick(null)}
          className={`px-4 py-3 font-medium whitespace-nowrap transition-colors flex-shrink-0 ${
            selectedCourierId === null
              ? 'border-b-2 border-blue-500 text-blue-400'
              : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
          }`}
        >
          📊 Vue globale
        </button>
        
        {/* Onglets par coursier */}
        {tours.map(tour => (
          <button
            key={tour.courierId}
            onClick={() => handleTabClick(tour.courierId)}
            className={`px-4 py-3 font-medium flex items-center gap-2 whitespace-nowrap transition-colors flex-shrink-0 ${
              selectedCourierId === tour.courierId
                ? 'border-b-2 border-blue-500 text-blue-400'
                : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
            }`}
          >
            <span 
              className="w-3 h-3 rounded-full" 
              style={{ backgroundColor: getCourierColor(tour.courierId) }} 
            />
            Coursier {tour.courierId}
            {tour.totalDurationSec > 4 * 3600 && (
              <span className="text-red-400 text-xs ml-1">⚠️</span>
            )}
          </button>
        ))}
      </div>
      
      {/* Contenu de l'onglet */}
      <div className="tour-tab-content flex-1 overflow-y-auto">
        {selectedCourierId === null ? (
          <GlobalStatistics tours={tours} />
        ) : (
          <CourierTourCard 
            tour={tours.find(t => t.courierId === selectedCourierId)}
            deliveryRequestSet={deliveryRequestSet}
          />
        )}
      </div>
    </div>
  );
}

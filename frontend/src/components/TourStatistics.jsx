import React from 'react';
import StatCard from './StatCard';

/**
 * Composant pour afficher les statistiques d'une seule tournée
 * 
 * @param {Object} props
 * @param {Object} props.tour - Données de la tournée
 */
export default function TourStatistics({ tour }) {
  if (!tour) {
    return (
      <div className="text-gray-400 text-center py-4">
        Aucune tournée sélectionnée
      </div>
    );
  }

  // Calculs
  const durationHours = (tour.totalDurationSec / 3600).toFixed(2);
  const distanceKm = (tour.totalDistance / 1000).toFixed(2);
  const exceedsLimit = tour.totalDurationSec > 4 * 3600; // > 4 heures
  const requestCount = tour.requestCount || 0;
  const stopCount = tour.stops?.length || 0;

  return (
    <div className="grid grid-cols-2 gap-4">
      <StatCard 
        label="Distance" 
        value={`${distanceKm} km`} 
        icon="📏"
      />
      <StatCard 
        label="Durée" 
        value={`${durationHours} h`}
        icon="⏱️"
        warning={exceedsLimit}
        warningMessage={exceedsLimit ? "Dépasse la limite de 4h" : null}
      />
      <StatCard 
        label="Demandes" 
        value={requestCount} 
        icon="📦"
      />
      <StatCard 
        label="Stops" 
        value={stopCount} 
        icon="📍"
      />
    </div>
  );
}

import React from 'react';

/**
 * Composant pour afficher le tableau récapitulatif de la tournée
 * avec logos, ordre, type (E/P/D) et heures
 */
export default function TourTable({ tourData, deliveryRequestSet }) {
  if (!tourData || !tourData.tour || tourData.tour.length === 0) {
    return (
      <div className="text-gray-400 text-center py-8">
        Aucune tournée calculée
      </div>
    );
  }

  // Créer la liste des stops avec leurs informations
  const stops = [];
  let currentTimeMinutes = 0; // Temps cumulé en minutes depuis le départ
  
  // Parser l'heure de départ de l'entrepôt
  if (deliveryRequestSet?.warehouse?.departureTime) {
    const [hours, minutes] = deliveryRequestSet.warehouse.departureTime.split(':').map(Number);
    currentTimeMinutes = hours * 60 + minutes;
  } else {
    currentTimeMinutes = 8 * 60; 
  }
  
  // Ajouter l'entrepôt au début
  if (deliveryRequestSet?.warehouse) {
    stops.push({
      order: 1,
      type: 'E',
      icon: '🏢',
      time: formatTime(currentTimeMinutes),
      nodeId: deliveryRequestSet.warehouse.nodeId,
      demandId: null,
      color: '#9CA3AF',
      durationSec: 0
    });
  }

  // TODO: Analyser les trajets pour extraire les pickups et deliveries dans l'ordre réel
  // Pour l'instant, on affiche basé sur les demandes
  if (deliveryRequestSet?.demands) {
    deliveryRequestSet.demands.forEach((demand, index) => {
      // Pickup avec sa vraie durée
      const pickupDurationMin = Math.round((demand.pickupDurationSec || 0) / 60);
      stops.push({
        order: stops.length + 1,
        type: 'P',
        icon: '📦',
        time: formatTimeRange(currentTimeMinutes, pickupDurationMin),
        nodeId: demand.pickupNodeId,
        demandId: demand.id,
        color: demand.color || '#3B82F6',
        durationSec: demand.pickupDurationSec || 0
      });
      currentTimeMinutes += pickupDurationMin;
      
      // Delivery avec sa vraie durée
      const deliveryDurationMin = Math.round((demand.deliveryDurationSec || 0) / 60);
      stops.push({
        order: stops.length + 1,
        type: 'D',
        icon: '📍',
        time: formatTimeRange(currentTimeMinutes, deliveryDurationMin),
        nodeId: demand.deliveryNodeId,
        demandId: demand.id,
        color: demand.color || '#3B82F6',
        durationSec: demand.deliveryDurationSec || 0
      });
      currentTimeMinutes += deliveryDurationMin;
    });
  }

  // Retour à l'entrepôt
  if (deliveryRequestSet?.warehouse) {
    stops.push({
      order: stops.length + 1,
      type: 'E',
      icon: '🏢',
      time: formatTime(currentTimeMinutes),
      nodeId: deliveryRequestSet.warehouse.nodeId,
      demandId: null,
      color: '#9CA3AF',
      durationSec: 0
    });
  }

  return (
    <div>
      <table className="w-full text-sm">
        <thead className="bg-gray-600 sticky top-0 z-10">
          <tr>
            <th className="px-3 py-2 text-left">Logo</th>
            <th className="px-3 py-2 text-center">Ordre</th>
            <th className="px-3 py-2 text-center">Type</th>
            <th className="px-3 py-2 text-left">Heure</th>
          </tr>
        </thead>
        <tbody>
          {stops.map((stop, index) => (
            <tr 
              key={`stop-${index}`}
              className={`border-b border-gray-600 hover:bg-gray-600 transition-colors ${
                index % 2 === 0 ? 'bg-gray-700' : 'bg-gray-750'
              }`}
            >
              {/* Logo */}
              <td className="px-3 py-3 text-center text-2xl">
                <div 
                  style={{ 
                    display: 'inline-block',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    backgroundColor: stop.type === 'E' ? '#6B7280' : stop.color + '20',
                    border: `2px solid ${stop.color}`
                  }}
                >
                  {stop.icon}
                </div>
              </td>
              
              {/* Ordre */}
              <td className="px-3 py-3 text-center font-semibold text-lg">
                {stop.order}
              </td>
              
              {/* Type */}
              <td className="px-3 py-3 text-center">
                <span className={`px-3 py-1 rounded-full font-semibold ${
                  stop.type === 'E' ? 'bg-gray-600' :
                  stop.type === 'P' ? 'bg-blue-600' :
                  'bg-red-600'
                }`}>
                  {stop.type}
                </span>
              </td>
              
              {/* Heure */}
              <td className="px-3 py-3 font-mono">
                {stop.time}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/**
 * Formate une heure en minutes en format HHhMM
 */
function formatTime(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${hours}h${minutes.toString().padStart(2, '0')}`;
}

/**
 * Formate une plage horaire avec l'heure de début et de fin
 * @param {number} startMinutes - Heure de début en minutes depuis minuit
 * @param {number} durationMinutes - Durée de l'activité en minutes
 */
function formatTimeRange(startMinutes, durationMinutes) {
  const startHours = Math.floor(startMinutes / 60);
  const startMins = startMinutes % 60;
  
  const endMinutes = startMinutes + durationMinutes;
  const endHours = Math.floor(endMinutes / 60);
  const endMins = endMinutes % 60;
  
  return `${startHours}h${startMins.toString().padStart(2, '0')}-${endHours}h${endMins.toString().padStart(2, '0')}`;
}

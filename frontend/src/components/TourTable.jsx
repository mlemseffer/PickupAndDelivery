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
  const COURIER_SPEED_KMH = 15; // Vitesse constante du livreur en km/h
  const COURIER_SPEED_M_PER_MIN = (COURIER_SPEED_KMH * 1000) / 60; // Conversion en mètres par minute
  
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

  // Parser les trajets pour respecter l'ordre de passage réel
  if (tourData?.tour && tourData.tour.length > 0) {
    tourData.tour.forEach((trajet, index) => {
      console.log(`📍 Trajet ${index + 1}:`, trajet);
      console.log(`  - longueurTotale: ${trajet.longueurTotale} m`);
      console.log(`  - segments: ${trajet.segments?.length || 0}`);
      
      // Calculer la longueur totale du trajet en additionnant tous les segments
      let totalDistance = 0;
      if (trajet.segments && trajet.segments.length > 0) {
        totalDistance = trajet.segments.reduce((sum, segment) => sum + (segment.length || 0), 0);
        console.log(`  - distance calculée depuis segments: ${totalDistance} m`);
      } else if (trajet.longueurTotale) {
        totalDistance = trajet.longueurTotale;
      }
      
      // Ajouter le temps de déplacement vers ce stop
      if (totalDistance > 0) {
        const travelTimeMinutes = totalDistance / COURIER_SPEED_M_PER_MIN;
        console.log(`  - temps de trajet: ${travelTimeMinutes.toFixed(2)} min`);
        currentTimeMinutes += travelTimeMinutes;
      }
      
      // Identifier le type de stop (pickup ou delivery)
      const stopNode = trajet.stopArrivee?.idNode;
      const stopType = trajet.stopArrivee?.typeStop;
      console.log(`  - stopType: ${stopType}, stopNode: ${stopNode}`);
      
      if (stopType === 'PICKUP') {
        // Trouver la demande correspondante
        const demand = deliveryRequestSet?.demands?.find(d => d.pickupNodeId === stopNode);
        if (demand) {
          const pickupDurationMin = (demand.pickupDurationSec || 0) / 60;
          stops.push({
            order: stops.length + 1,
            type: 'P',
            icon: '📦',
            time: formatTimeRange(currentTimeMinutes, pickupDurationMin),
            nodeId: stopNode,
            demandId: demand.id,
            color: demand.color || '#3B82F6',
            durationSec: demand.pickupDurationSec || 0
          });
          currentTimeMinutes += pickupDurationMin;
        }
      } else if (stopType === 'DELIVERY') {
        // Trouver la demande correspondante
        const demand = deliveryRequestSet?.demands?.find(d => d.deliveryNodeId === stopNode);
        if (demand) {
          const deliveryDurationMin = (demand.deliveryDurationSec || 0) / 60;
          stops.push({
            order: stops.length + 1,
            type: 'D',
            icon: '📍',
            time: formatTimeRange(currentTimeMinutes, deliveryDurationMin),
            nodeId: stopNode,
            demandId: demand.id,
            color: demand.color || '#3B82F6',
            durationSec: demand.deliveryDurationSec || 0
          });
          currentTimeMinutes += deliveryDurationMin;
        }
      } else if (stopType === 'WAREHOUSE' && index === tourData.tour.length - 1) {
        // Retour à l'entrepôt (dernier trajet)
        stops.push({
          order: stops.length + 1,
          type: 'E',
          icon: '🏢',
          time: formatTime(currentTimeMinutes),
          nodeId: stopNode,
          demandId: null,
          color: '#9CA3AF',
          durationSec: 0
        });
      }
    });
  }  return (
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
  const roundedMinutes = Math.round(totalMinutes);
  const hours = Math.floor(roundedMinutes / 60);
  const minutes = roundedMinutes % 60;
  return `${hours}h${minutes.toString().padStart(2, '0')}`;
}

/**
 * Formate une plage horaire avec l'heure de début et de fin
 * @param {number} startMinutes - Heure de début en minutes depuis minuit
 * @param {number} durationMinutes - Durée de l'activité en minutes
 */
function formatTimeRange(startMinutes, durationMinutes) {
  const roundedStartMinutes = Math.round(startMinutes);
  const startHours = Math.floor(roundedStartMinutes / 60);
  const startMins = roundedStartMinutes % 60;
  
  const roundedEndMinutes = Math.round(startMinutes + durationMinutes);
  const endHours = Math.floor(roundedEndMinutes / 60);
  const endMins = roundedEndMinutes % 60;
  
  return `${startHours}h${startMins.toString().padStart(2, '0')}-${endHours}h${endMins.toString().padStart(2, '0')}`;
}

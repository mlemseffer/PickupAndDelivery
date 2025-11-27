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
  
  // Ajouter l'entrepôt au début
  if (deliveryRequestSet?.warehouse) {
    stops.push({
      order: 1,
      type: 'E',
      icon: '🏢',
      time: deliveryRequestSet.warehouse.departureTime || '8h00',
      nodeId: deliveryRequestSet.warehouse.nodeId,
      demandId: null,
      color: '#9CA3AF'
    });
  }

  // TODO: Analyser les trajets pour extraire les pickups et deliveries dans l'ordre
  // Pour l'instant, on affiche un exemple basé sur les demandes
  if (deliveryRequestSet?.demands) {
    deliveryRequestSet.demands.forEach((demand, index) => {
      // Pickup
      stops.push({
        order: stops.length + 1,
        type: 'P',
        icon: '📦',
        time: calculateTime(stops.length, 5), // Estimation
        nodeId: demand.pickupNodeId,
        demandId: demand.id,
        color: demand.color || '#3B82F6'
      });
      
      // Delivery
      stops.push({
        order: stops.length + 1,
        type: 'D',
        icon: '📍',
        time: calculateTime(stops.length, 5), // Estimation
        nodeId: demand.deliveryNodeId,
        demandId: demand.id,
        color: demand.color || '#3B82F6'
      });
    });
  }

  // Retour à l'entrepôt
  if (deliveryRequestSet?.warehouse) {
    stops.push({
      order: stops.length + 1,
      type: 'E',
      icon: '🏢',
      time: calculateTime(stops.length, 5),
      nodeId: deliveryRequestSet.warehouse.nodeId,
      demandId: null,
      color: '#9CA3AF'
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
 * Calcule une heure estimée basée sur l'ordre et la durée moyenne
 */
function calculateTime(order, avgMinutesPerStop) {
  const startHour = 8; // 8h00
  const startMinute = 0;
  
  const totalMinutes = order * avgMinutesPerStop;
  const hours = startHour + Math.floor(totalMinutes / 60);
  const minutes = (startMinute + totalMinutes) % 60;
  
  // Format: 8h05-8h10
  const endHours = hours + Math.floor((minutes + avgMinutesPerStop) / 60);
  const endMinutes = (minutes + avgMinutesPerStop) % 60;
  
  return `${hours}h${minutes.toString().padStart(2, '0')}-${endHours}h${endMinutes.toString().padStart(2, '0')}`;
}

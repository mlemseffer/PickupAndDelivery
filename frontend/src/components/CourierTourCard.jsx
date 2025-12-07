import React from 'react';
import TourStatistics from './TourStatistics';
import TourTable from './TourTable';
import { getCourierColor } from '../utils/courierColors';

/**
 * Composant pour afficher les détails d'un coursier individuel
 * Combine les statistiques et le tableau détaillé
 * 
 * @param {Object} props
 * @param {Object} props.tour - Données de la tournée du coursier
 * @param {Object} props.deliveryRequestSet - Ensemble des demandes de livraison
 */
export default function CourierTourCard({ tour, deliveryRequestSet }) {
  if (!tour) {
    return (
      <div className="text-gray-400 text-center py-8">
        Sélectionnez un coursier pour voir les détails
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header avec couleur du coursier */}
      <div className="flex items-center gap-3">
        <div 
          className="w-6 h-6 rounded-full flex-shrink-0" 
          style={{ backgroundColor: getCourierColor(tour.courierId) }}
        />
        <h3 className="text-2xl font-bold">Coursier {tour.courierId}</h3>
      </div>
      
      {/* Statistiques */}
      <div>
        <h4 className="text-lg font-semibold mb-3 text-gray-300">Statistiques</h4>
        <TourStatistics tour={tour} />
      </div>
      
      {/* Itinéraire détaillé */}
      <div>
        <h4 className="text-lg font-semibold mb-3 text-gray-300">Itinéraire Détaillé</h4>
        <div className="bg-gray-800 rounded-lg p-4 max-h-96 overflow-y-auto">
          <TourTable 
            tourData={{
              tour: tour.trajets || [],
              metrics: {
                stopCount: tour.stops?.length || 0,
                totalDistance: tour.totalDistance || 0,
                segmentCount: tour.trajets?.length || 0
              }
            }}
            deliveryRequestSet={deliveryRequestSet}
          />
        </div>
      </div>
    </div>
  );
}

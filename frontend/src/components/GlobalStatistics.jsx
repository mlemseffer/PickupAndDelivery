import React from 'react';
import StatCard from './StatCard';
import { getCourierColor } from '../utils/courierColors';

/**
 * Composant pour afficher les statistiques globales de tous les coursiers
 * 
 * @param {Object} props
 * @param {Array} props.tours - Liste de toutes les tournées
 */
export default function GlobalStatistics({ tours }) {
  if (!tours || tours.length === 0) {
    return (
      <div className="text-gray-400 text-center py-8">
        Aucune tournée calculée
      </div>
    );
  }

  // Calculs agrégés
  const totalDistance = tours.reduce((sum, t) => sum + (t.totalDistance || 0), 0);
  const totalDuration = tours.reduce((sum, t) => sum + (t.totalDurationSec || 0), 0);
  const totalRequests = tours.reduce((sum, t) => sum + (t.requestCount || 0), 0);
  
  // Calculs comparatifs
  const avgDuration = tours.length > 0 ? totalDuration / tours.length / 3600 : 0;
  const maxDuration = tours.length > 0 ? Math.max(...tours.map(t => t.totalDurationSec || 0)) / 3600 : 0;
  const minDuration = tours.length > 0 ? Math.min(...tours.map(t => t.totalDurationSec || 0)) / 3600 : 0;
  
  // Score d'équilibrage (0-100%) : plus les tournées sont équilibrées, plus le score est élevé
  // Formule : 100% - (écart / 4h * 100)
  const durationRange = maxDuration - minDuration;
  const balanceScore = Math.max(0, Math.min(100, ((4 - durationRange) / 4 * 100))).toFixed(0);

  return (
    <div className="space-y-6">
      <h3 className="text-xl font-bold">Statistiques Globales</h3>
      
      {/* Stats générales */}
      <div className="grid grid-cols-3 gap-4">
        <StatCard 
          label="Coursiers" 
          value={tours.length} 
          icon="🚴" 
        />
        <StatCard 
          label="Distance totale" 
          value={`${(totalDistance / 1000).toFixed(1)} km`} 
          icon="📏" 
        />
        <StatCard 
          label="Demandes" 
          value={totalRequests} 
          icon="📦" 
        />
      </div>
      
      {/* Stats de durée */}
      <div className="grid grid-cols-3 gap-4">
        <StatCard 
          label="Durée moy." 
          value={`${avgDuration.toFixed(2)} h`} 
          icon="⏱️" 
        />
        <StatCard 
          label="Durée max" 
          value={`${maxDuration.toFixed(2)} h`} 
          icon="⬆️"
          warning={maxDuration > 4}
          warningMessage={maxDuration > 4 ? "Dépasse 4h" : null}
        />
        <StatCard 
          label="Durée min" 
          value={`${minDuration.toFixed(2)} h`} 
          icon="⬇️" 
        />
      </div>
      
      {/* Score d'équilibrage */}
      <div className="bg-gray-800 p-4 rounded-lg border border-gray-700">
        <div className="flex justify-between items-center mb-2">
          <span className="font-medium text-gray-300">Score d'équilibrage</span>
          <span className="text-2xl font-bold text-blue-400">
            {balanceScore}%
          </span>
        </div>
        <div className="w-full bg-gray-700 rounded-full h-4">
          <div 
            className={`h-4 rounded-full transition-all ${
              balanceScore > 70 ? 'bg-green-500' : 
              balanceScore > 40 ? 'bg-yellow-500' : 
              'bg-red-500'
            }`}
            style={{ width: `${balanceScore}%` }}
          />
        </div>
        <p className="text-xs text-gray-400 mt-2">
          Mesure l'équilibre entre la tournée la plus longue et la plus courte
        </p>
      </div>
      
      {/* Liste des coursiers */}
      <div className="space-y-2">
        <h4 className="font-medium text-gray-300">Répartition par coursier</h4>
        {tours.map(tour => (
          <div 
            key={tour.courierId} 
            className="flex items-center gap-3 p-3 bg-gray-800 rounded border border-gray-700 hover:border-gray-600 transition-colors"
          >
            <div 
              className="w-4 h-4 rounded-full flex-shrink-0" 
              style={{ backgroundColor: getCourierColor(tour.courierId) }} 
            />
            <span className="font-medium text-white">Coursier {tour.courierId}</span>
            <span className="text-gray-500">·</span>
            <span className="text-sm text-gray-400">{tour.requestCount || 0} demandes</span>
            <span className="text-gray-500">·</span>
            <span className="text-sm text-gray-400">
              {((tour.totalDurationSec || 0) / 3600).toFixed(2)} h
            </span>
            <span className="text-gray-500">·</span>
            <span className="text-sm text-gray-400">
              {((tour.totalDistance || 0) / 1000).toFixed(2)} km
            </span>
            {tour.totalDurationSec > 4 * 3600 && (
              <>
                <span className="text-gray-500">·</span>
                <span className="text-xs text-red-400 font-medium">⚠️ {'>'} 4h</span>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

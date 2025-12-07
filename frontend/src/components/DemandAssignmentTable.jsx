import React from 'react';
import { ArrowLeftRight, Trash2 } from 'lucide-react';

/**
 * Tableau réutilisable pour afficher les demandes et actions de gestion
 */
export default function DemandAssignmentTable({
  title,
  demands = [],
  assignments = {},
  courierOptions = [],
  emptyMessage = 'Aucune demande',
  onReassign,
  onRemove,
  isBusy = false,
}) {
  const courierLabel = (courierId) => {
    if (courierId === null || courierId === undefined) return 'Non assigné';
    const found = courierOptions.find((c) => c.value === courierId);
    return found ? found.label : `Coursier ${courierId}`;
  };

  return (
    <div className="space-y-4">
      {title && <h3 className="text-lg font-semibold text-white">{title}</h3>}

      {demands.length === 0 ? (
        <div className="text-gray-400 text-sm">{emptyMessage}</div>
      ) : (
        <div className="space-y-3">
          {demands.map((demand) => {
            const assignedCourier = assignments[demand.id] ?? null;
            return (
              <div
                key={demand.id}
                className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 bg-gray-800 border border-gray-700 rounded-lg p-4"
              >
                <div className="flex items-start gap-3">
                  <div
                    className="w-3 h-3 rounded-full mt-1.5 flex-shrink-0 border border-white/40"
                    style={{ backgroundColor: demand.color || '#3B82F6' }}
                    title="Couleur de la demande"
                  />
                  <div>
                    <p className="text-sm text-gray-300">
                      <span className="font-semibold text-white">Pickup:</span> {demand.pickupNodeId}
                    </p>
                    <p className="text-sm text-gray-300">
                      <span className="font-semibold text-white">Livraison:</span> {demand.deliveryNodeId}
                    </p>
                    <p className="text-sm text-gray-400">
                      ⏱️ Pickup: {(demand.pickupDurationSec || 0)}s · Livraison: {(demand.deliveryDurationSec || 0)}s
                    </p>
                    <p className="text-sm mt-1">
                      <span className="px-2 py-1 rounded-full text-xs font-semibold bg-gray-700 text-gray-200">
                        {courierLabel(assignedCourier)}
                      </span>
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => onReassign && onReassign(demand, assignedCourier)}
                    disabled={isBusy}
                    className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-3 py-2 rounded-lg text-sm font-semibold transition-colors"
                  >
                    <ArrowLeftRight size={16} />
                    Réassigner
                  </button>
                  <button
                    onClick={() => onRemove && onRemove(demand)}
                    disabled={isBusy}
                    className="flex items-center gap-2 bg-red-600 hover:bg-red-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-3 py-2 rounded-lg text-sm font-semibold transition-colors"
                  >
                    <Trash2 size={16} />
                    Supprimer
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}


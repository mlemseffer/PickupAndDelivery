import React, { useState, useEffect } from 'react';

export default function AssignDemandModal({ isOpen, onClose, availableDemands = [], courierLabel = '', onConfirm }) {
  const [selectedDemandId, setSelectedDemandId] = useState('');

  useEffect(() => {
    setSelectedDemandId('');
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black opacity-60" onClick={onClose} />
      <div className="relative bg-gray-900 text-white rounded-lg shadow-xl w-11/12 max-w-lg p-4 z-10">
        <h3 className="text-lg font-semibold mb-2">Ajouter une demande</h3>
        <p className="text-sm text-gray-400 mb-3">
          Sélectionnez une demande non assignée à affecter à {courierLabel || 'ce coursier'}.
        </p>

        <div className="space-y-2 max-h-64 overflow-y-auto mb-4">
          {availableDemands.length === 0 ? (
            <div className="text-gray-400 text-sm">Aucune demande non assignée.</div>
          ) : (
            availableDemands.map((demand) => (
              <label
                key={demand.id}
                className={`flex items-start gap-3 p-3 rounded-lg border ${
                  selectedDemandId === demand.id ? 'border-indigo-500 bg-gray-800' : 'border-gray-700 bg-gray-850'
                } cursor-pointer`}
              >
                <input
                  type="radio"
                  name="demand"
                  value={demand.id}
                  checked={selectedDemandId === demand.id}
                  onChange={() => setSelectedDemandId(demand.id)}
                  className="mt-1"
                />
                <div>
                  <p className="text-sm text-gray-200">
                    <span className="font-semibold text-white">Pickup:</span> {demand.pickupNodeId}
                  </p>
                  <p className="text-sm text-gray-200">
                    <span className="font-semibold text-white">Livraison:</span> {demand.deliveryNodeId}
                  </p>
                  <p className="text-xs text-gray-400">
                    ⏱️ Pickup: {(demand.pickupDurationSec || 0)}s · Livraison: {(demand.deliveryDurationSec || 0)}s
                  </p>
                </div>
              </label>
            ))
          )}
        </div>

        <div className="flex justify-end gap-2">
          <button className="bg-gray-700 hover:bg-gray-600 text-white px-3 py-1.5 rounded" onClick={onClose}>
            Annuler
          </button>
          <button
            className="bg-green-600 hover:bg-green-700 text-white px-3 py-1.5 rounded disabled:bg-gray-600 disabled:cursor-not-allowed"
            disabled={!selectedDemandId}
            onClick={() => onConfirm && onConfirm(selectedDemandId)}
          >
            Ajouter
          </button>
        </div>
      </div>
    </div>
  );
}


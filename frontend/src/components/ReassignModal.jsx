import React, { useState, useEffect } from 'react';

export default function ReassignModal({ isOpen, onClose, courierOptions = [], initialCourierId = null, onConfirm }) {
  const [selected, setSelected] = useState(initialCourierId ?? '');

  useEffect(() => {
    setSelected(initialCourierId ?? '');
  }, [initialCourierId]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black opacity-60" onClick={onClose} />
      <div className="relative bg-gray-900 text-white rounded-lg shadow-xl w-11/12 max-w-md p-4 z-10">
        <h3 className="text-lg font-semibold mb-2">Réassigner la demande</h3>
        <p className="text-sm text-gray-400 mb-3">Choisissez le coursier cible ou &quot;Non assigné&quot;.</p>

        <select
          className="w-full bg-gray-800 text-white border border-gray-700 rounded px-3 py-2 mb-4"
          value={selected}
          onChange={(e) => setSelected(e.target.value)}
        >
          <option value="">Non assigné</option>
          {courierOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>

        <div className="flex justify-end gap-2">
          <button className="bg-gray-700 hover:bg-gray-600 text-white px-3 py-1.5 rounded" onClick={onClose}>
            Annuler
          </button>
          <button
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded"
            onClick={() => onConfirm(selected || null)}
          >
            Confirmer
          </button>
        </div>
      </div>
    </div>
  );
}


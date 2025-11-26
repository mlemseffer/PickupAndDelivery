import React, { useState } from 'react';
import { X, Users, Check } from 'lucide-react';

/**
 * Modal pour choisir le nombre de livreurs
 * Permet à l'opérateur de définir combien de livreurs seront utilisés pour les demandes
 */
export default function CourierCountModal({ isOpen, onClose, onConfirm, currentCount = 1 }) {
  const [courierCount, setCourierCount] = useState(currentCount);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // S'assurer que courierCount est un nombre
    const count = typeof courierCount === 'string' ? parseInt(courierCount) || 1 : courierCount;
    
    // Validation
    if (count < 1) {
      setError('Le nombre de livreurs doit être au moins 1');
      return;
    }
    
    if (count > 10) {
      setError('Le nombre maximum de livreurs est 10');
      return;
    }

    onConfirm(count);
    onClose();
  };

  const handleIncrement = () => {
    if (courierCount < 10) {
      setCourierCount(prev => prev + 1);
      setError('');
    }
  };

  const handleDecrement = () => {
    if (courierCount > 1) {
      setCourierCount(prev => prev - 1);
      setError('');
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-lg shadow-2xl w-full max-w-md p-6 text-white">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <Users className="w-6 h-6 text-blue-400" />
            <h2 className="text-2xl font-bold">Nombre de Livreurs</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Description */}
        <p className="text-gray-300 mb-6">
          Choisissez le nombre de livreurs disponibles pour effectuer les demandes de livraison.
        </p>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Counter */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-gray-300 mb-3">
              Nombre de livreurs
            </label>
            <div className="flex items-center justify-center gap-4">
              {/* Bouton - */}
              <button
                type="button"
                onClick={handleDecrement}
                disabled={courierCount <= 1}
                className="w-12 h-12 bg-gray-700 hover:bg-gray-600 disabled:bg-gray-800 disabled:text-gray-600 
                         rounded-lg text-2xl font-bold transition-colors flex items-center justify-center"
              >
                −
              </button>

              {/* Affichage du nombre */}
              <div className="flex-1 max-w-[120px]">
                <input
                  type="number"
                  min="1"
                  max="10"
                  value={courierCount}
                  onChange={(e) => {
                    const value = e.target.value;
                    // Permettre la saisie vide temporairement
                    if (value === '') {
                      setCourierCount('');
                      return;
                    }
                    const numValue = parseInt(value);
                    if (!isNaN(numValue)) {
                      setCourierCount(Math.max(1, Math.min(10, numValue)));
                      setError('');
                    }
                  }}
                  onBlur={(e) => {
                    // Si le champ est vide au blur, remettre 1
                    if (e.target.value === '' || courierCount === '') {
                      setCourierCount(1);
                    }
                  }}
                  className="w-full bg-gray-700 text-white text-center text-3xl font-bold 
                           rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Bouton + */}
              <button
                type="button"
                onClick={handleIncrement}
                disabled={courierCount >= 10}
                className="w-12 h-12 bg-gray-700 hover:bg-gray-600 disabled:bg-gray-800 disabled:text-gray-600 
                         rounded-lg text-2xl font-bold transition-colors flex items-center justify-center"
              >
                +
              </button>
            </div>

            {/* Range slider */}
            <div className="mt-4">
              <input
                type="range"
                min="1"
                max="10"
                value={courierCount}
                onChange={(e) => {
                  setCourierCount(parseInt(e.target.value));
                  setError('');
                }}
                className="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer 
                         [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-4 
                         [&::-webkit-slider-thumb]:h-4 [&::-webkit-slider-thumb]:rounded-full 
                         [&::-webkit-slider-thumb]:bg-blue-500 [&::-webkit-slider-thumb]:cursor-pointer"
              />
              <div className="flex justify-between text-xs text-gray-400 mt-1">
                <span>1</span>
                <span>5</span>
                <span>10</span>
              </div>
            </div>
          </div>

          {/* Message d'erreur */}
          {error && (
            <div className="mb-4 p-3 bg-red-900/50 border border-red-500 rounded-lg text-red-200 text-sm">
              {error}
            </div>
          )}

          {/* Info */}
          <div className="mb-6 p-4 bg-blue-900/30 border border-blue-500/50 rounded-lg">
            <p className="text-sm text-blue-200">
              <strong>ℹ️ Info:</strong> Les demandes seront réparties entre {courierCount} livreur{courierCount > 1 ? 's' : ''}.
            </p>
          </div>

          {/* Buttons */}
          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-700 hover:bg-gray-600 text-white px-6 py-3 
                       rounded-lg font-semibold transition-colors"
            >
              Annuler
            </button>
            <button
              type="submit"
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 
                       rounded-lg font-semibold transition-colors flex items-center justify-center gap-2"
            >
              <Check className="w-5 h-5" />
              Confirmer
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

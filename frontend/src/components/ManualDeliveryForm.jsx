import React, { useState } from 'react';
import { X, Package, MapPin } from 'lucide-react';

/**
 * Composant pour ajouter manuellement une demande de livraison
 */
export default function ManualDeliveryForm({ 
  onAdd, 
  onCancel, 
  availableNodes,
  onStartMapSelection, // Callback pour activer le mode sélection sur la carte
  selectedNodeId, // ID du nœud sélectionné depuis la carte
  mapSelectionType, // 'pickup' ou 'delivery'
  savedFormData // Données sauvegardées du formulaire
}) {
  const [pickupNodeId, setPickupNodeId] = useState('');
  const [deliveryNodeId, setDeliveryNodeId] = useState('');
  const [pickupDuration, setPickupDuration] = useState(300); // 5 minutes par défaut
  const [deliveryDuration, setDeliveryDuration] = useState(300);

  // Restaurer les données sauvegardées au montage du composant
  React.useEffect(() => {
    if (savedFormData) {
      setPickupNodeId(savedFormData.pickupNodeId || '');
      setDeliveryNodeId(savedFormData.deliveryNodeId || '');
      setPickupDuration(savedFormData.pickupDuration || 300);
      setDeliveryDuration(savedFormData.deliveryDuration || 300);
    }
  }, [savedFormData]);

  // Mettre à jour l'input quand un nœud est sélectionné sur la carte
  React.useEffect(() => {
    if (selectedNodeId && mapSelectionType === 'pickup') {
      setPickupNodeId(selectedNodeId);
    } else if (selectedNodeId && mapSelectionType === 'delivery') {
      setDeliveryNodeId(selectedNodeId);
    }
  }, [selectedNodeId, mapSelectionType]);

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!pickupNodeId || !deliveryNodeId) {
      alert('Veuillez renseigner les deux adresses');
      return;
    }

    if (pickupNodeId === deliveryNodeId) {
      alert('L\'adresse de pickup et de delivery doivent être différentes');
      return;
    }

    const demand = {
      pickupNodeId,
      deliveryNodeId,
      pickupDurationSec: parseInt(pickupDuration),
      deliveryDurationSec: parseInt(deliveryDuration)
    };

    onAdd(demand);
  };

  const handlePickupMapSelection = () => {
    // Sauvegarder les données actuelles du formulaire
    const formData = {
      pickupNodeId,
      deliveryNodeId,
      pickupDuration,
      deliveryDuration
    };
    onStartMapSelection('pickup', formData);
  };

  const handleDeliveryMapSelection = () => {
    // Sauvegarder les données actuelles du formulaire
    const formData = {
      pickupNodeId,
      deliveryNodeId,
      pickupDuration,
      deliveryDuration
    };
    onStartMapSelection('delivery', formData);
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-700 rounded-lg p-6 max-w-2xl w-full">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Ajouter une demande de livraison</h2>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Pickup Address */}
          <div>
            <label className="flex items-center gap-2 text-lg font-semibold mb-2">
              <Package size={20} className="text-green-400" />
              Adresse de Pickup (enlèvement)
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={pickupNodeId}
                onChange={(e) => setPickupNodeId(e.target.value)}
                placeholder="ID du nœud (ex: 25610888)"
                className="flex-1 bg-gray-600 border border-gray-500 rounded px-4 py-3 text-white focus:outline-none focus:border-blue-400"
                required
              />
              <button
                type="button"
                onClick={handlePickupMapSelection}
                className={`px-4 py-3 rounded font-semibold transition-colors ${
                  mapSelectionType === 'pickup' 
                    ? 'bg-green-600 text-white' 
                    : 'bg-gray-600 hover:bg-gray-500 text-white'
                }`}
                title="Cliquer sur la carte pour sélectionner"
              >
                📍 Carte
              </button>
            </div>
            <p className="text-sm text-gray-400 mt-1">
              Entrez l'ID d'un nœud ou cliquez sur "Carte" puis sur un segment
            </p>
          </div>

          {/* Pickup Duration */}
          <div>
            <label className="block text-sm font-semibold mb-2">
              Durée de pickup (secondes)
            </label>
            <input
              type="number"
              value={pickupDuration}
              onChange={(e) => setPickupDuration(e.target.value)}
              min="0"
              step="60"
              className="w-full bg-gray-600 border border-gray-500 rounded px-4 py-2 text-white focus:outline-none focus:border-blue-400"
              required
            />
            <p className="text-sm text-gray-400 mt-1">
              Temps nécessaire pour charger le colis (en secondes)
            </p>
          </div>

          {/* Delivery Address */}
          <div>
            <label className="flex items-center gap-2 text-lg font-semibold mb-2">
              <MapPin size={20} className="text-blue-400" />
              Adresse de Delivery (livraison)
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={deliveryNodeId}
                onChange={(e) => setDeliveryNodeId(e.target.value)}
                placeholder="ID du nœud (ex: 27359745)"
                className="flex-1 bg-gray-600 border border-gray-500 rounded px-4 py-3 text-white focus:outline-none focus:border-blue-400"
                required
              />
              <button
                type="button"
                onClick={handleDeliveryMapSelection}
                className={`px-4 py-3 rounded font-semibold transition-colors ${
                  mapSelectionType === 'delivery' 
                    ? 'bg-blue-600 text-white' 
                    : 'bg-gray-600 hover:bg-gray-500 text-white'
                }`}
                title="Cliquer sur la carte pour sélectionner"
              >
                📍 Carte
              </button>
            </div>
            <p className="text-sm text-gray-400 mt-1">
              Entrez l'ID d'un nœud ou cliquez sur "Carte" puis sur un segment
            </p>
          </div>

          {/* Delivery Duration */}
          <div>
            <label className="block text-sm font-semibold mb-2">
              Durée de delivery (secondes)
            </label>
            <input
              type="number"
              value={deliveryDuration}
              onChange={(e) => setDeliveryDuration(e.target.value)}
              min="0"
              step="60"
              className="w-full bg-gray-600 border border-gray-500 rounded px-4 py-2 text-white focus:outline-none focus:border-blue-400"
              required
            />
            <p className="text-sm text-gray-400 mt-1">
              Temps nécessaire pour déposer le colis (en secondes)
            </p>
          </div>

          {/* Buttons */}
          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              className="flex-1 bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
            >
              Ajouter la demande
            </button>
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-gray-600 hover:bg-gray-500 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
            >
              Annuler
            </button>
          </div>
        </form>

        {/* Info box */}
        <div className="mt-6 bg-blue-900/30 border border-blue-500/50 rounded p-4 text-sm">
          <p className="text-blue-200">
            <strong>💡 Astuce :</strong> Utilisez les boutons "📍 Carte" pour sélectionner les adresses directement en cliquant sur la carte. 
            Lorsque vous cliquez sur un segment, le nœud le plus proche est automatiquement sélectionné.
          </p>
          {mapSelectionType === 'pickup' && (
            <p className="text-green-400 mt-2 font-semibold">
              ✅ Mode sélection PICKUP actif - Cliquez sur un segment de la carte
            </p>
          )}
          {mapSelectionType === 'delivery' && (
            <p className="text-blue-400 mt-2 font-semibold">
              ✅ Mode sélection DELIVERY actif - Cliquez sur un segment de la carte
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

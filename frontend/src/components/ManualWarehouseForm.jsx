import React, { useEffect, useState } from 'react';
import { X, MapPin } from 'lucide-react';
import Icon from './Icon';

/**
 * Formulaire pour définir l'entrepôt manuellement (nœud + heure de départ).
 * Permet également de sélectionner le nœud directement sur la carte.
 */
export default function ManualWarehouseForm({
  onSave,
  onCancel,
  availableNodes = [],
  onStartMapSelection,
  selectedNodeId,
  savedFormData,
  initialWarehouse,
  isSaving = false,
}) {
  const [nodeId, setNodeId] = useState(initialWarehouse?.nodeId || '');

  // Restaurer les données sauvegardées (quand on passe en mode sélection carte puis on revient)
  useEffect(() => {
    if (savedFormData) {
      setNodeId(savedFormData.nodeId || '');
    }
  }, [savedFormData]);

  // Réagir à la sélection sur la carte
  useEffect(() => {
    if (selectedNodeId) {
      setNodeId(selectedNodeId);
    }
  }, [selectedNodeId]);

  // Pré-remplir si l'entrepôt courant change
  useEffect(() => {
    if (!initialWarehouse) return;
    // Ne pas écraser une sélection récente (carte ou saisie) si présente
    if (savedFormData?.nodeId) return;
    if (selectedNodeId) return;
    setNodeId(initialWarehouse.nodeId || '');
  }, [initialWarehouse, savedFormData?.nodeId, selectedNodeId]);

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!nodeId) {
      alert('Veuillez saisir un nœud pour l’entrepôt');
      return;
    }

    onSave({
      nodeId: nodeId.toString(),
    });
  };

  const handleMapSelect = () => {
    onStartMapSelection?.('warehouse', {
      nodeId,
    });
  };

  const nodeExists = nodeId
    ? availableNodes.some((n) => String(n.id) === String(nodeId))
    : true;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-700 rounded-lg p-6 max-w-xl w-full">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Ajouter / Modifier l’entrepôt</h2>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Nœud de l'entrepôt */}
          <div>
            <label className="flex items-center gap-2 text-lg font-semibold mb-2">
              <MapPin size={20} className="text-yellow-400" />
              Nœud de l’entrepôt
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={nodeId}
                onChange={(e) => setNodeId(e.target.value)}
                placeholder="ID du nœud (ex: 123456)"
                className="flex-1 bg-gray-600 border border-gray-500 rounded px-4 py-3 text-white focus:outline-none focus:border-blue-400"
                required
              />
              <button
                type="button"
                onClick={handleMapSelect}
                className="px-4 py-3 rounded font-semibold transition-colors bg-gray-600 hover:bg-gray-500 text-white"
                title="Cliquer sur la carte pour sélectionner"
              >
                <span className="inline-flex items-center gap-2">
                  <Icon name="location" className="text-white" />
                  Carte
                </span>
              </button>
            </div>
            <div className="flex items-center gap-2 text-sm mt-2">
              <span className={`font-semibold ${nodeExists ? 'text-green-300' : 'text-red-300'}`}>
                {nodeId
                  ? nodeExists
                    ? 'Nœud présent dans la carte'
                    : 'Nœud introuvable dans la carte'
                  : 'Saisissez ou sélectionnez un nœud'}
              </span>
            </div>
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={isSaving}
              className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-semibold transition-colors"
            >
              {isSaving ? 'Enregistrement...' : 'Enregistrer l’entrepôt (08:00)'}
            </button>
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-gray-600 hover:bg-gray-500 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
              disabled={isSaving}
            >
              Annuler
            </button>
          </div>

          <div className="mt-4 bg-blue-900/30 border border-blue-500/50 rounded p-4 text-sm text-blue-200">
            <p className="flex items-center gap-2">
              <Icon name="lightbulb" className="text-blue-200" />
              Conseil : utilisez le bouton "Carte" pour sélectionner le nœud de l’entrepôt directement sur la carte. L’heure de départ est fixée à 08:00.
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}


import React, { useState } from 'react';
import { X, Upload } from 'lucide-react';

/**
 * Composant modal pour restaurer une tournée depuis un fichier JSON
 */
export default function RestoreTourModal({ isOpen, onClose, onRestore }) {
  const [error, setError] = useState(null);
  const [fileName, setFileName] = useState(null);

  const handleFileSelect = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setFileName(file.name);
    setError(null);

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const content = e.target?.result;
        if (!content) throw new Error('Fichier vide');

        const jsonData = JSON.parse(content);
        
        // Valider la structure du JSON
        if (!jsonData.tour || !Array.isArray(jsonData.tour)) {
          throw new Error('Format invalide : "tour" doit être un tableau');
        }

        // Transformer les données pour correspondre au format attendu
        const transformedTourData = {
          tour: jsonData.tour.map(trajet => ({
            segments: trajet.segments || []
          })),
          metrics: {
            stopCount: jsonData.tour.reduce((count, trajet) => {
              count += (trajet.stopDepart ? 1 : 0) + (trajet.stopArrivee ? 1 : 0);
              return count;
            }, 0),
            totalDistance: jsonData.tour.reduce((total, trajet) => total + (trajet.distance || 0), 0),
            segmentCount: jsonData.tour.reduce((count, trajet) => count + (trajet.segments?.length || 0), 0)
          }
        };

        // Extraire les demandes de livraison depuis les stops
        const demands = [];
        const demandesMap = new Map(); // Map pour grouper pickup et delivery par ID demande

        // Collecter tous les stops (PICKUP et DELIVERY)
        jsonData.tour.forEach(trajet => {
          if (trajet.stopDepart?.idDemande) {
            if (!demandesMap.has(trajet.stopDepart.idDemande)) {
              demandesMap.set(trajet.stopDepart.idDemande, {});
            }
            const demand = demandesMap.get(trajet.stopDepart.idDemande);
            if (trajet.stopDepart.typeStop === 'PICKUP') {
              demand.pickupNodeId = trajet.stopDepart.idNode;
              demand.id = trajet.stopDepart.idDemande;
            }
          }

          if (trajet.stopArrivee?.idDemande) {
            if (!demandesMap.has(trajet.stopArrivee.idDemande)) {
              demandesMap.set(trajet.stopArrivee.idDemande, {});
            }
            const demand = demandesMap.get(trajet.stopArrivee.idDemande);
            if (trajet.stopArrivee.typeStop === 'PICKUP') {
              demand.pickupNodeId = trajet.stopArrivee.idNode;
              demand.id = trajet.stopArrivee.idDemande;
            } else if (trajet.stopArrivee.typeStop === 'DELIVERY') {
              demand.deliveryNodeId = trajet.stopArrivee.idNode;
              demand.id = trajet.stopArrivee.idDemande;
            }
          }
        });

        // Convertir la map en array et filtrer les demandes complètes
        demandesMap.forEach((demand, id) => {
          if (demand.pickupNodeId && demand.deliveryNodeId) {
            demands.push({
              id: demand.id,
              pickupNodeId: demand.pickupNodeId,
              deliveryNodeId: demand.deliveryNodeId,
              pickupDurationSec: 300,
              deliveryDurationSec: 300
            });
          }
        });

        // Appeler le callback avec les données transformées ET les demandes
        onRestore(transformedTourData, demands);
        handleClose();
      } catch (err) {
        setError(`Erreur : ${err.message}`);
      }
    };

    reader.onerror = () => {
      setError('Erreur lors de la lecture du fichier');
    };

    reader.readAsText(file);
  };

  const handleClose = () => {
    setError(null);
    setFileName(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-700 rounded-lg p-8 max-w-md w-full">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Restaurer une tournée</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        <div className="space-y-4">
          {/* Upload Zone */}
          <div className="border-2 border-dashed border-gray-500 rounded-lg p-8 text-center hover:border-blue-400 transition-colors cursor-pointer"
            onClick={() => document.getElementById('file-input').click()}
          >
            <Upload size={32} className="mx-auto mb-3 text-gray-400" />
            <p className="text-gray-300 font-semibold mb-1">
              Cliquez pour sélectionner un fichier
            </p>
            <p className="text-sm text-gray-400">
              ou glissez-déposez un fichier JSON
            </p>
            {fileName && (
              <p className="text-green-400 text-sm mt-3 font-semibold">
                ✓ {fileName}
              </p>
            )}
          </div>

          {/* Input caché */}
          <input
            id="file-input"
            type="file"
            accept=".json"
            onChange={handleFileSelect}
            className="hidden"
          />

          {/* Message d'erreur */}
          {error && (
            <div className="bg-red-900/30 border border-red-500 rounded p-4 text-red-300 text-sm">
              {error}
            </div>
          )}

          {/* Info box */}
          <div className="bg-blue-900/30 border border-blue-500/50 rounded p-4 text-sm text-blue-200">
            <p className="font-semibold mb-2">📋 Format attendu :</p>
            <p className="text-xs text-blue-300">
              Le fichier JSON doit contenir un objet avec une clé "tour" contenant un tableau de trajets avec segments.
            </p>
          </div>

          {/* Boutons */}
          <div className="flex gap-3 pt-4">
            <button
              onClick={handleClose}
              className="flex-1 bg-gray-600 hover:bg-gray-500 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
            >
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

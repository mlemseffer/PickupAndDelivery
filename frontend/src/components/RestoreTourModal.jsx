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

        // Accepter soit le nouveau format (tours + demands) soit l'ancien (tour)
        let tours = [];
        if (Array.isArray(jsonData?.tours)) {
          tours = jsonData.tours;
        } else if (Array.isArray(jsonData?.tour)) {
          tours = [{
            trajets: jsonData.tour,
            stops: jsonData.stops || [],
          }];
        } else if (Array.isArray(jsonData)) {
          tours = jsonData;
        }

        if (!tours.length) {
          throw new Error('Format invalide : aucune tournée trouvée dans le fichier');
        }

        // Récupérer les demandes : priorité aux données explicites, sinon dériver des stops
        const deriveDemandsFromTours = (toursArray) => {
          const demands = [];
          const demandesMap = new Map();

          toursArray.forEach((tour) => {
            const trajets = tour?.trajets || tour?.tour || [];
            if (Array.isArray(trajets)) {
              trajets.forEach((trajet) => {
                [trajet.stopDepart, trajet.stopArrivee].forEach((stop) => {
                  if (stop?.idDemande) {
                    if (!demandesMap.has(stop.idDemande)) {
                      demandesMap.set(stop.idDemande, { id: stop.idDemande });
                    }
                    const demand = demandesMap.get(stop.idDemande);
                    if (stop.typeStop === 'PICKUP') {
                      demand.pickupNodeId = stop.idNode;
                    } else if (stop.typeStop === 'DELIVERY') {
                      demand.deliveryNodeId = stop.idNode;
                    }
                  }
                });
              });
            }

            // Inspecter aussi la liste des stops si présente
            (tour?.stops || []).forEach((stop) => {
              if (stop?.idDemande) {
                if (!demandesMap.has(stop.idDemande)) {
                  demandesMap.set(stop.idDemande, { id: stop.idDemande });
                }
                const demand = demandesMap.get(stop.idDemande);
                if (stop.typeStop === 'PICKUP') {
                  demand.pickupNodeId = stop.idNode;
                } else if (stop.typeStop === 'DELIVERY') {
                  demand.deliveryNodeId = stop.idNode;
                }
              }
            });
          });

          demandesMap.forEach((demand) => {
            if (demand.pickupNodeId && demand.deliveryNodeId) {
              demands.push({
                id: demand.id,
                pickupNodeId: demand.pickupNodeId,
                deliveryNodeId: demand.deliveryNodeId,
                pickupDurationSec: demand.pickupDurationSec || 300,
                deliveryDurationSec: demand.deliveryDurationSec || 300,
              });
            }
          });

          return demands;
        };

        let demands = Array.isArray(jsonData.demands) ? jsonData.demands : [];
        if (!demands.length) {
          demands = deriveDemandsFromTours(tours);
        }

        if (!demands.length) {
          throw new Error('Impossible de déterminer les demandes à partir du fichier');
        }

        const computeMetrics = (toursArray) => {
          return toursArray.reduce(
            (acc, tour) => {
              acc.stopCount += tour?.stops?.length || 0;
              acc.totalDistance += tour?.totalDistance || 0;
              const trajets = tour?.trajets || tour?.tour || [];
              acc.segmentCount += Array.isArray(trajets)
                ? trajets.reduce((c, t) => c + (t?.segments?.length || 0), 0)
                : 0;
              return acc;
            },
            { stopCount: 0, totalDistance: 0, segmentCount: 0 }
          );
        };

        const payload = {
          tours,
          demands,
          warehouse: jsonData.warehouse || null,
          courierCount: jsonData.courierCount || tours.length || 1,
          metrics: jsonData.metrics || computeMetrics(tours),
          savedAt: jsonData.savedAt,
          version: jsonData.version || 'v1',
        };

        // Appeler le callback avec les données normalisées + les demandes
        onRestore(payload, demands);
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

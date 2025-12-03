import React, { useState } from 'react';
import { Edit, FileText, Save } from 'lucide-react';
import SaveModal from './SaveModal';

/**
 * Composant pour les boutons d'action de la tournée
 * - Modifier Tournée
 * - Sauvegarder itinéraire (.txt)
 * - Sauvegarder Tournée
 */
export default function TourActions({ tourData, onSaveItinerary, onSaveTour, deliveryRequestSet }) {
  const [showItinModal, setShowItinModal] = useState(false);
  const [itinDefaultName, setItinDefaultName] = useState('');
  const [showJsonModal, setShowJsonModal] = useState(false);
  const [jsonDefaultName, setJsonDefaultName] = useState('');

  const openItinModal = () => {
    if (!tourData || !tourData.tour) {
      alert('Aucune tournée à sauvegarder');
      return;
    }
    const defaultName = `itineraire_${new Date().toISOString().split('T')[0]}`;
    setItinDefaultName(defaultName);
    setShowItinModal(true);
  };

  const openJsonModal = () => {
    if (!tourData) {
      alert('Aucune tournée à sauvegarder');
      return;
    }
    const defaultName = `tournee_${new Date().toISOString().split('T')[0]}`;
    setJsonDefaultName(defaultName);
    setShowJsonModal(true);
  };

  const performItinSave = (filename) => {
    if (!filename) return;
    // Générer le contenu du fichier texte en s'inspirant de TourTable.jsx
    let content = '=== ITINÉRAIRE DE LIVRAISON ===\n\n';

    const segmentCount = Array.isArray(tourData?.tour) ? tourData.tour.length : 0;
    content += `Nombre de segments: ${segmentCount}\n`;
    const totalDistance = tourData?.metrics && typeof tourData.metrics.totalDistance === 'number'
      ? Number(tourData.metrics.totalDistance).toFixed(2)
      : '0.00';
    content += `Distance totale: ${totalDistance} m\n`;
    content += `Nombre de stops: ${tourData?.metrics?.stopCount || 0}\n\n`;

    content += '=== DÉTAIL DES TRAJETS ET STOPS ===\n\n';

    // Time calculation constants (copied from TourTable logic)
    const COURIER_SPEED_KMH = 15;
    const COURIER_SPEED_M_PER_MIN = (COURIER_SPEED_KMH * 1000) / 60;

    // Déterminer l'heure de départ (en minutes depuis minuit)
    let currentTimeMinutes = 8 * 60; // fallback 08:00
    if (deliveryRequestSet?.warehouse?.departureTime) {
      const parts = deliveryRequestSet.warehouse.departureTime.split(':').map(Number);
      if (parts.length === 2) currentTimeMinutes = parts[0] * 60 + parts[1];
    }

    // Helper formatters (reuse small versions from TourTable)
    const formatTime = (totalMinutes) => {
      const rounded = Math.round(totalMinutes);
      const hours = Math.floor(rounded / 60);
      const minutes = rounded % 60;
      return `${hours}h${minutes.toString().padStart(2, '0')}`;
    };
    const formatTimeRange = (startMinutes, durationMinutes) => {
      const s = Math.round(startMinutes);
      const e = Math.round(startMinutes + durationMinutes);
      const sh = Math.floor(s / 60); const sm = s % 60;
      const eh = Math.floor(e / 60); const em = e % 60;
      return `${sh}h${sm.toString().padStart(2, '0')}-${eh}h${em.toString().padStart(2, '0')}`;
    };

    // If warehouse exists, print it as start
    if (deliveryRequestSet?.warehouse) {
      content += `Départ Entrepôt (${deliveryRequestSet.warehouse.nodeId || 'N/A'}) - départ: ${formatTime(currentTimeMinutes)}\n\n`;
    }

    if (Array.isArray(tourData?.tour)) {
      tourData.tour.forEach((trajet, index) => {
        // compute total distance for this trajet
        let totalDistanceTrajet = 0;
        if (Array.isArray(trajet.segments) && trajet.segments.length > 0) {
          totalDistanceTrajet = trajet.segments.reduce((sum, s) => sum + (s.length || s.longueur || 0), 0);
        } else if (trajet.longueurTotale) {
          totalDistanceTrajet = trajet.longueurTotale;
        } else if (trajet.distance) {
          totalDistanceTrajet = trajet.distance;
        }

        // travel time
        const travelTimeMinutes = totalDistanceTrajet > 0 ? totalDistanceTrajet / COURIER_SPEED_M_PER_MIN : 0;
        // list streets involved
        let streets = [];
        if (Array.isArray(trajet.segments) && trajet.segments.length > 0) {
          streets = trajet.segments.map(s => s.name || s.nomRue || s.street || s.streetName || 'Rue inconnue');
        }

        // arrival stop info
        const stopNode = trajet.stopArrivee?.idNode || trajet.stopArrivee || 'N/A';
        const stopType = trajet.stopArrivee?.typeStop || trajet.stopArrivee?.type || null;

        // advance current time by travel time
        if (travelTimeMinutes > 0) {
          currentTimeMinutes += travelTimeMinutes;
        }

        content += `Trajet ${index + 1}: De ${trajet.stopDepart?.idNode || 'N/A'} à ${stopNode} — ${totalDistanceTrajet.toFixed(2)} m — temps trajet ~ ${travelTimeMinutes.toFixed(2)} min\n`;
        if (streets.length > 0) {
          content += `\tRues à parcourir: \n${[...new Set(streets)].join('\n\t')}\n`;
        }

        // If it's a pickup or delivery, find the demand to get durations
        if (stopType === 'PICKUP') {
          const demand = deliveryRequestSet?.demands?.find(d => d.pickupNodeId === stopNode);
          const pickupDurMin = demand ? (demand.pickupDurationSec || 0) / 60 : 0;
          content += `  → PICKUP (node ${stopNode}) — heure estimée: ${formatTimeRange(currentTimeMinutes, pickupDurMin)} — durée: ${pickupDurMin.toFixed(2)} min\n\n`;
          currentTimeMinutes += pickupDurMin;
        } else if (stopType === 'DELIVERY') {
          const demand = deliveryRequestSet?.demands?.find(d => d.deliveryNodeId === stopNode);
          const deliveryDurMin = demand ? (demand.deliveryDurationSec || 0) / 60 : 0;
          content += `  → DELIVERY (node ${stopNode}) — heure estimée: ${formatTimeRange(currentTimeMinutes, deliveryDurMin)} — durée: ${deliveryDurMin.toFixed(2)} min\n\n`;
          currentTimeMinutes += deliveryDurMin;
        } else if (stopType === 'WAREHOUSE' && index === tourData.tour.length - 1) {
          content += `  → Retour Entrepôt (${stopNode}) — arrivée approximative: ${formatTime(currentTimeMinutes)}\n\n`;
        } else {
          // unknown stop type - just print arrival time
          content += `  → Arrivée approximative: ${formatTime(currentTimeMinutes)}\n\n`;
        }
      });
    }

    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${filename}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    setShowItinModal(false);
    if (onSaveItinerary) onSaveItinerary();
  };

  const performJsonSave = (filename) => {
    if (!filename) return;
    const tourJson = JSON.stringify(tourData, null, 2);
    const blob = new Blob([tourJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `${filename}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    setShowJsonModal(false);
    if (onSaveTour) onSaveTour();
  };

  return (
    <>
      <div className="flex gap-2 justify-center">

        {/* Sauvegarder itinéraire (.txt) */}
        <button
          onClick={openItinModal}
          disabled={!tourData}
          className="flex-1 bg-teal-600 hover:bg-teal-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                   flex items-center justify-center gap-2"
          title="Sauvegarder l'itinéraire en fichier texte"
        >
          <FileText size={18} />
          Sauvegarder itinéraire (.txt)
        </button>

        {/* Sauvegarder Tournée */}
        <button
          onClick={openJsonModal}
          disabled={!tourData}
          className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                   flex items-center justify-center gap-2"
          title="Sauvegarder la tournée complète (JSON)"
        >
          <Save size={18} />
          Sauvegarder Tournée (.json)
        </button>
      </div>

      <SaveModal
        isOpen={showItinModal}
        title="Sauvegarder l'itinéraire"
        description="Entrez le nom du fichier texte :"
        defaultName={itinDefaultName}
        placeholder="nom_du_fichier"
        confirmLabel="Sauvegarder"
        onCancel={() => setShowItinModal(false)}
        onConfirm={performItinSave}
      />

      <SaveModal
        isOpen={showJsonModal}
        title="Sauvegarder la tournée"
        description="Entrez le nom du fichier JSON :"
        defaultName={jsonDefaultName}
        placeholder="nom_du_fichier"
        confirmLabel="Sauvegarder"
        onCancel={() => setShowJsonModal(false)}
        onConfirm={performJsonSave}
      />
    </>
  );
}

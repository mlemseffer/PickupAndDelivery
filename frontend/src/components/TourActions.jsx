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
    const hasItinerary = Array.isArray(tourData)
      ? tourData.some((t) => Array.isArray(t?.trajets) || Array.isArray(t?.tour))
      : Array.isArray(tourData?.tour) || Array.isArray(tourData?.trajets);

    if (!hasItinerary) {
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
    const toursToExport = (() => {
      if (!tourData) return [];
      if (Array.isArray(tourData)) {
        return tourData.map((tour, idx) => ({
          courierId: tour.courierId ?? idx + 1,
          trajets: tour.trajets || tour.tour || [],
          stops: tour.stops || [],
          totalDistance:
            typeof tour.totalDistance === 'number'
              ? tour.totalDistance
              : tour.metrics?.totalDistance ?? 0,
          totalDurationSec:
            typeof tour.totalDurationSec === 'number'
              ? tour.totalDurationSec
              : tour.metrics?.totalDurationSec ?? 0,
        }));
      }
      if (Array.isArray(tourData?.trajets) || Array.isArray(tourData?.tour)) {
        const trajets = tourData.trajets || tourData.tour || [];
        return [
          {
            courierId: tourData.courierId ?? 1,
            trajets,
            stops: tourData.stops || [],
            totalDistance:
              tourData.totalDistance ??
              tourData.metrics?.totalDistance ??
              0,
            totalDurationSec:
              tourData.totalDurationSec ??
              tourData.metrics?.totalDurationSec ??
              0,
          },
        ];
      }
      return [];
    })();

    if (!toursToExport.length) {
      alert('Aucune tournée à sauvegarder');
      return;
    }

    // Prépare des index pour retrouver rapidement les demandes associées aux stops
    const demands = Array.isArray(deliveryRequestSet?.demands)
      ? deliveryRequestSet.demands
      : [];
    const demandsByPickupNode = new Map();
    const demandsByDeliveryNode = new Map();
    const demandsById = new Map();
    demands.forEach((d) => {
      if (d.pickupNodeId) demandsByPickupNode.set(d.pickupNodeId, d);
      if (d.deliveryNodeId) demandsByDeliveryNode.set(d.deliveryNodeId, d);
      if (d.id) demandsById.set(d.id, d);
    });

    const COURIER_SPEED_KMH = 15;
    const COURIER_SPEED_M_PER_MIN = (COURIER_SPEED_KMH * 1000) / 60;

    const formatTime = (totalMinutes) => {
      const rounded = Math.round(totalMinutes);
      const hours = Math.floor(rounded / 60);
      const minutes = rounded % 60;
      return `${hours}h${minutes.toString().padStart(2, '0')}`;
    };

    const formatTimeRange = (startMinutes, durationMinutes) => {
      const s = Math.round(startMinutes);
      const e = Math.round(startMinutes + durationMinutes);
      const sh = Math.floor(s / 60);
      const sm = s % 60;
      const eh = Math.floor(e / 60);
      const em = e % 60;
      return `${sh}h${sm.toString().padStart(2, '0')}-${eh}h${em.toString().padStart(2, '0')}`;
    };

    const computeTrajetDistance = (trajet) => {
      if (!trajet) return 0;
      if (Array.isArray(trajet.segments) && trajet.segments.length > 0) {
        return trajet.segments.reduce(
          (sum, s) => sum + (s.length ?? s.longueur ?? 0),
          0
        );
      }
      if (typeof trajet.longueurTotale === 'number') {
        return trajet.longueurTotale;
      }
      if (typeof trajet.distance === 'number') {
        return trajet.distance;
      }
      if (typeof trajet.longueur === 'number') {
        return trajet.longueur;
      }
      return 0;
    };

    const sumTrajetsDistance = (trajets) => {
      if (!Array.isArray(trajets)) return 0;
      return trajets.reduce((acc, t) => acc + computeTrajetDistance(t), 0);
    };

    const getDemandForStop = (stopNode, demandId, stopType) => {
      if (stopType === 'PICKUP') {
        return (
          demandsByPickupNode.get(stopNode) ||
          (demandId ? demandsById.get(demandId) : null)
        );
      }
      if (stopType === 'DELIVERY') {
        return (
          demandsByDeliveryNode.get(stopNode) ||
          (demandId ? demandsById.get(demandId) : null)
        );
      }
      return demandId ? demandsById.get(demandId) : null;
    };

    const totalDistanceAll = toursToExport.reduce(
      (sum, tour) => sum + (tour.totalDistance || sumTrajetsDistance(tour.trajets)),
      0
    );
    const totalStopsAll = toursToExport.reduce((sum, tour) => {
      if (Array.isArray(tour.stops) && tour.stops.length > 0) {
        return sum + tour.stops.length;
      }
      if (Array.isArray(tour.trajets) && tour.trajets.length > 0) {
        return sum + tour.trajets.length + 1;
      }
      return sum;
    }, 0);
    const totalSegmentsAll = toursToExport.reduce(
      (sum, tour) => sum + (Array.isArray(tour.trajets) ? tour.trajets.length : 0),
      0
    );

    let content = '=== ITINÉRAIRES DE LIVRAISON ===\n\n';
    content += `Nombre de coursiers: ${toursToExport.length}\n`;
    content += `Distance totale: ${totalDistanceAll.toFixed(2)} m\n`;
    content += `Nombre de stops: ${totalStopsAll}\n`;
    content += `Nombre de segments: ${totalSegmentsAll}\n\n`;

    toursToExport.forEach((tour, courierIndex) => {
      const trajets = Array.isArray(tour.trajets) ? tour.trajets : [];
      const baseDistance = tour.totalDistance || sumTrajetsDistance(trajets);
      const baseStopCount =
        (Array.isArray(tour.stops) && tour.stops.length) ||
        (trajets.length ? trajets.length + 1 : 0);

      content += `${'='.repeat(70)}\n`;
      content += `COURSIER ${tour.courierId ?? courierIndex + 1}\n`;
      content += `${'='.repeat(70)}\n`;
      content += `Distance totale: ${baseDistance.toFixed(2)} m\n`;
      content += `Durée totale estimée: ${
        tour.totalDurationSec ? (tour.totalDurationSec / 3600).toFixed(2) + ' h' : 'N/A'
      }\n`;
      content += `Nombre de stops: ${baseStopCount}\n`;
      content += `Nombre de segments: ${trajets.length}\n\n`;
      content += '=== DÉTAIL DES TRAJETS ET STOPS ===\n\n';

      // Déterminer l'heure de départ (en minutes depuis minuit)
      let currentTimeMinutes = 8 * 60; // fallback 08:00
      if (deliveryRequestSet?.warehouse?.departureTime) {
        const parts = deliveryRequestSet.warehouse.departureTime.split(':').map(Number);
        if (parts.length === 2) currentTimeMinutes = parts[0] * 60 + parts[1];
      }

      if (deliveryRequestSet?.warehouse) {
        content += `Départ Entrepôt (${deliveryRequestSet.warehouse.nodeId || 'N/A'}) - départ: ${formatTime(currentTimeMinutes)}\n\n`;
      }

      trajets.forEach((trajet, index) => {
        const totalDistanceTrajet = computeTrajetDistance(trajet);

        const travelTimeMinutes =
          totalDistanceTrajet > 0
            ? totalDistanceTrajet / COURIER_SPEED_M_PER_MIN
            : trajet?.durationSec
              ? trajet.durationSec / 60
              : 0;

        const streets = Array.isArray(trajet.segments)
          ? trajet.segments
              .map((s) => s.name || s.nomRue || s.street || s.streetName)
              .filter(Boolean)
          : [];

        const stopNode =
          trajet.stopArrivee?.idNode ||
          trajet.stopArrivee?.nodeId ||
          trajet.stopArrivee ||
          'N/A';
        const stopType =
          trajet.stopArrivee?.typeStop ||
          trajet.stopArrivee?.type ||
          trajet.stopArrivee?.stopType ||
          null;
        const stopDemandId =
          trajet.stopArrivee?.idDemande ||
          trajet.stopArrivee?.demandId ||
          null;

        if (travelTimeMinutes > 0) {
          currentTimeMinutes += travelTimeMinutes;
        }

        content += `Trajet ${index + 1}: De ${trajet.stopDepart?.idNode || 'N/A'} à ${stopNode} — ${totalDistanceTrajet.toFixed(2)} m — temps trajet ~ ${travelTimeMinutes.toFixed(2)} min\n`;
        if (streets.length > 0) {
          content += `Rues à parcourir: \n\t${[...new Set(streets)].join('\n\t')}\n`;
        }

        const demand = getDemandForStop(stopNode, stopDemandId, stopType);

        if (stopType === 'PICKUP') {
          const pickupDurMin = demand ? (demand.pickupDurationSec || 0) / 60 : 0;
          content += `  → PICKUP${demand?.id ? ` (demande ${demand.id})` : ''} — heure estimée: ${formatTimeRange(currentTimeMinutes, pickupDurMin)} — durée: ${pickupDurMin.toFixed(2)} min\n\n`;
          currentTimeMinutes += pickupDurMin;
        } else if (stopType === 'DELIVERY') {
          const deliveryDurMin = demand ? (demand.deliveryDurationSec || 0) / 60 : 0;
          content += `  → DELIVERY${demand?.id ? ` (demande ${demand.id})` : ''} — heure estimée: ${formatTimeRange(currentTimeMinutes, deliveryDurMin)} — durée: ${deliveryDurMin.toFixed(2)} min\n\n`;
          currentTimeMinutes += deliveryDurMin;
        } else if (stopType === 'WAREHOUSE' && index === trajets.length - 1) {
          content += `  → Retour Entrepôt (${stopNode}) — arrivée approximative: ${formatTime(currentTimeMinutes)}\n\n`;
        } else {
          content += `  → Arrivée approximative: ${formatTime(currentTimeMinutes)}\n\n`;
        }
      });
    });

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
    
    console.log('[TourActions] Sauvegarde de la tournée:', tourData);

    // Normaliser le format (mono ou multi-coursier) pour sauvegarder tout ce qui est utile à la restauration
    const normalizeToursForSave = () => {
      const baseTours = (() => {
        if (Array.isArray(tourData)) {
          return tourData.map((tour) => ({
            ...tour,
            trajets: tour.trajets || tour.tour || tour, // compatibilité éventuelle
          }));
        }
        if (tourData?.trajets || tourData?.tour || tourData?.stops) {
          return [{
            ...tourData,
            trajets: tourData.trajets || tourData.tour || [],
            stops: tourData.stops || [],
          }];
        }
        return [];
      })();

      // Single source of truth: courierId comes from the tour itself (fallback = index)
      return baseTours.map((tour, idx) => {
        const normalizedCourierId = Number.isFinite(Number(tour?.courierId))
          ? Number(tour.courierId)
          : idx + 1;

        return {
          ...tour,
          courierId: normalizedCourierId,
          trajets: tour?.trajets || tour?.tour || tour?.segments || tour?.path || [],
          stops: tour?.stops || [],
        };
      });
    };

    const toursToSave = normalizeToursForSave();
    if (!toursToSave.length) {
      alert('Aucune tournée à sauvegarder');
      return;
    }

    // Inclure les demandes pour pouvoir les réinjecter au backend lors d'une restauration
    const demandsToSave = (deliveryRequestSet?.demands || []).map((d) => ({
      id: d.id,
      pickupNodeId: d.pickupNodeId,
      deliveryNodeId: d.deliveryNodeId,
      pickupDurationSec: d.pickupDurationSec,
      deliveryDurationSec: d.deliveryDurationSec,
    }));

    const payload = {
      version: 'v1',
      savedAt: new Date().toISOString(),
      courierCount: Math.max(toursToSave.length, 1),
      warehouse: deliveryRequestSet?.warehouse || null,
      demands: demandsToSave,
      tours: toursToSave,
    };

    // Ajout d'un champ legacy pour les anciens JSON basés sur une seule tournée
    if (!Array.isArray(tourData) && Array.isArray(tourData?.tour)) {
      payload.tour = tourData.tour;
      if (tourData.metrics) {
        payload.metrics = tourData.metrics;
      }
    }

    const tourJson = JSON.stringify(payload, null, 2);
    const blob = new Blob([tourJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `${filename}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    console.log('[TourActions] Tournée sauvegardée avec', toursToSave.length, 'tournée(s)');

    setShowJsonModal(false);
    if (onSaveTour) onSaveTour();
  };

  return (
    <>
      <div className="flex gap-3">

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

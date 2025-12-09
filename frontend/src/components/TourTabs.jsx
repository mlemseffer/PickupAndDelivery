import React, { useEffect, useMemo, useState } from 'react';
import GlobalStatistics from './GlobalStatistics';
import { getCourierColor } from '../utils/courierColors';
import DemandAssignmentTable from './DemandAssignmentTable';
import ReassignModal from './ReassignModal';
import AssignDemandModal from './AssignDemandModal';
import TourStatistics from './TourStatistics';
import TourTable from './TourTable';

const MAX_COURIERS = 10;

/**
 * Composant pour naviguer entre les différents coursiers et la vue globale
 * 
 * @param {Object} props
 * @param {Array} props.tours - Liste de toutes les tournées
 * @param {Object} props.deliveryRequestSet - Ensemble des demandes de livraison
 * @param {Function} props.onTourSelect - Callback appelé lors de la sélection d'un coursier (pour la carte)
 * @param {Object} props.demandAssignments - Mapping demandId -> courierId|null
 * @param {Array} props.unassignedDemands - Liste des demandes non assignées (backend)
 * @param {Function} props.onReassignDemand - (demandId, targetCourierId|null) => Promise
 * @param {Function} props.onRemoveDemand - (demandId) => Promise
 * @param {Boolean} props.isBusy - désactive les actions en cours
 * @param {Boolean} props.isEditing - active le mode édition
 * @param {Function} props.onValidateEdit - callback validation
 * @param {Function} props.onCancelEdit - callback annulation
 */
export default function TourTabs({
  tours,
  deliveryRequestSet,
  onTourSelect,
  demandAssignments = {},
  unassignedDemands = [],
  onReassignDemand,
  onRemoveDemand,
  isBusy = false,
  isEditing = false,
  onValidateEdit,
  onCancelEdit,
}) {
  const [selectedCourierId, setSelectedCourierId] = useState(null); // null = vue globale
  const [showReassignModal, setShowReassignModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [activeDemand, setActiveDemand] = useState(null);
  const [extraCouriers, setExtraCouriers] = useState([]);

  const normalizedTours =
    tours && tours.length > 0
      ? tours
      : [
          {
            courierId: 1,
            trajets: [],
            stops: [],
            totalDistance: 0,
            totalDurationSec: 0,
            requestCount: 0,
          },
        ];

  const handleTabClick = (courierId) => {
    setSelectedCourierId(courierId);
    
    // Notifier le parent pour mettre à jour la carte
    if (onTourSelect) {
      if (courierId === null) {
        onTourSelect(null); // Vue globale : afficher tous les tours
      } else {
        const selectedTour = displayTours.find(t => t.courierId === courierId);
        onTourSelect(selectedTour);
      }
    }
  };

  const allDemands = deliveryRequestSet?.demands || [];

  const uniqueTours = useMemo(() => {
    const seen = new Set();
    return normalizedTours.filter((tour) => {
      const id = tour?.courierId;
      if (id === null || id === undefined) return false;
      if (seen.has(id)) return false;
      seen.add(id);
      return true;
    });
  }, [normalizedTours]);

  const baseCouriers = uniqueTours.map((tour) => tour.courierId);

  const filteredExtraCouriers = useMemo(
    () => extraCouriers.filter((id) => !baseCouriers.includes(id)),
    [extraCouriers, baseCouriers]
  );

  const currentCourierCount = useMemo(
    () => new Set([...baseCouriers, ...filteredExtraCouriers]).size,
    [baseCouriers, filteredExtraCouriers]
  );

  const isAtCourierLimit = currentCourierCount >= MAX_COURIERS;

  // Nettoyer les doublons quand les tournées changent (évite la duplication d'un coursier existant)
  useEffect(() => {
    if (filteredExtraCouriers.length !== extraCouriers.length) {
      setExtraCouriers(filteredExtraCouriers);
    }
  }, [filteredExtraCouriers, extraCouriers]);

  const displayTours = useMemo(() => {
    const placeholders = filteredExtraCouriers.map((id) => ({
      courierId: id,
      trajets: [],
      stops: [],
      totalDistance: 0,
      totalDurationSec: 0,
      requestCount: 0,
    }));
    return [...uniqueTours, ...placeholders];
  }, [uniqueTours, filteredExtraCouriers]);

  const courierOptions = useMemo(
    () =>
      [...baseCouriers, ...filteredExtraCouriers].map((id) => ({
        value: id,
        label: `Coursier ${id}`,
      })),
    [baseCouriers, filteredExtraCouriers]
  );

  const derivedUnassigned = useMemo(() => {
    if (!allDemands.length) return [];
    const setAssigned = new Set(Object.keys(demandAssignments || {}).filter((k) => demandAssignments[k] !== null && demandAssignments[k] !== undefined));
    // backend-provided unassignedDemands may be more reliable; merge both
    const union = new Map();
    unassignedDemands.forEach((d) => union.set(d.id, d));
    allDemands.forEach((d) => {
      if (!setAssigned.has(d.id)) {
        union.set(d.id, d);
      }
    });
    return Array.from(union.values());
  }, [allDemands, demandAssignments, unassignedDemands]);

  const demandsForCourier = (courierId) =>
    allDemands.filter((d) => (demandAssignments?.[d.id] ?? null) === courierId);

  const handleAddCourier = () => {
    if (isAtCourierLimit) return;

    const maxId = [...baseCouriers, ...extraCouriers].reduce(
      (m, v) => (Number(v) > m ? Number(v) : m),
      0
    );
    const newId = maxId + 1 || 1;
    setExtraCouriers((prev) => [...prev, newId]);
  };

  const handleRequestReassign = (demand) => {
    setActiveDemand(demand);
    setShowReassignModal(true);
  };

  const handleRequestAssign = () => {
    setShowAssignModal(true);
  };

  const handleConfirmReassign = async (targetCourierId) => {
    if (!activeDemand || !onReassignDemand) {
      setShowReassignModal(false);
      return;
    }
    await onReassignDemand(activeDemand.id, targetCourierId);
    setShowReassignModal(false);
    setActiveDemand(null);
  };

  const handleConfirmAssign = async (demandId) => {
    if (!onReassignDemand) {
      setShowAssignModal(false);
      return;
    }
    await onReassignDemand(demandId, selectedCourierId);
    setShowAssignModal(false);
  };

  const handleRemove = async (demand) => {
    if (!onRemoveDemand) return;
    await onRemoveDemand(demand.id);
  };

  return (
    <div id="assignments-panel" className="tour-tabs flex flex-col h-full min-w-0">
      {isEditing && (
        <div className="flex items-center justify-between gap-2 mb-3">
          <button
            onClick={handleAddCourier}
            className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded text-sm font-semibold disabled:bg-gray-600 disabled:cursor-not-allowed"
            disabled={isBusy || isAtCourierLimit}
            title={isAtCourierLimit ? 'Limite atteinte: maximum 10 coursiers' : undefined}
          >
            Ajouter coursier
          </button>
          <div className="flex items-center gap-2">
            <button
              onClick={onCancelEdit}
              className="bg-gray-700 hover:bg-gray-600 text-white px-3 py-1.5 rounded text-sm font-semibold"
            >
              Annuler
            </button>
            <button
              onClick={onValidateEdit}
              className="bg-green-600 hover:bg-green-700 text-white px-3 py-1.5 rounded text-sm font-semibold disabled:bg-gray-600 disabled:cursor-not-allowed"
              disabled={isBusy}
            >
              Valider
            </button>
          </div>
        </div>
      )}
      {/* Onglets en haut */}
      <div className="flex border-b border-gray-600 mb-4 overflow-x-auto flex-shrink-0 min-w-0 gap-1">
        {/* Onglet Vue Globale */}
        <button
          onClick={() => handleTabClick(null)}
          className={`px-4 py-3 font-medium whitespace-nowrap transition-colors flex-shrink-0 ${
            selectedCourierId === null
              ? 'border-b-2 border-blue-500 text-blue-400'
              : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
          }`}
        >
          📊 Vue globale
        </button>
        
        {/* Onglets par coursier */}
        {displayTours.map(tour => (
          <button
            key={tour.courierId}
            onClick={() => handleTabClick(tour.courierId)}
            className={`px-4 py-3 font-medium flex items-center gap-2 whitespace-nowrap transition-colors flex-shrink-0 ${
              selectedCourierId === tour.courierId
                ? 'border-b-2 border-blue-500 text-blue-400'
                : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
            }`}
          >
            <span 
              className="w-3 h-3 rounded-full" 
              style={{ backgroundColor: getCourierColor(tour.courierId) }} 
            />
            Coursier {tour.courierId}
            {tour.totalDurationSec > 4 * 3600 && (
              <span className="text-red-400 text-xs ml-1">⚠️</span>
            )}
          </button>
        ))}
      </div>
      
      {/* Contenu de l'onglet */}
      <div className="tour-tab-content flex-1 overflow-y-auto">
        {selectedCourierId === null ? (
          <div className="space-y-6">
            <GlobalStatistics tours={displayTours} />
            {isEditing && (
              <DemandAssignmentTable
                title="Toutes les demandes"
                demands={allDemands}
                assignments={demandAssignments}
                courierOptions={courierOptions}
                onReassign={handleRequestReassign}
                onRemove={handleRemove}
                isBusy={isBusy}
                emptyMessage="Aucune demande chargée"
              />
            )}
          </div>
        ) : (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span
                  className="w-3 h-3 rounded-full"
                  style={{ backgroundColor: getCourierColor(selectedCourierId) }}
                />
                <h3 className="text-lg font-semibold text-white">Coursier {selectedCourierId}</h3>
              </div>
              {isEditing && (
                <button
                  onClick={handleRequestAssign}
                  disabled={isBusy || derivedUnassigned.length === 0}
                  className="bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-3 py-2 rounded-lg text-sm font-semibold transition-colors"
                  title={derivedUnassigned.length === 0 ? "Aucune demande non assignée" : "Ajouter une demande à ce coursier"}
                >
                  Ajouter une demande
                </button>
              )}
            </div>

            {!isEditing && (
              <>
                <TourStatistics tour={displayTours.find((t) => t.courierId === selectedCourierId)} />
                <div className="bg-gray-800 rounded-lg p-3">
                  <TourTable
                    tourData={{
                      tour: displayTours.find((t) => t.courierId === selectedCourierId)?.trajets || [],
                      metrics: {
                        stopCount: displayTours.find((t) => t.courierId === selectedCourierId)?.stops?.length || 0,
                        totalDistance: displayTours.find((t) => t.courierId === selectedCourierId)?.totalDistance || 0,
                        segmentCount: displayTours.find((t) => t.courierId === selectedCourierId)?.trajets?.length || 0,
                      },
                    }}
                    deliveryRequestSet={deliveryRequestSet}
                  />
                </div>
              </>
            )}

            {isEditing && (
              <DemandAssignmentTable
                demands={demandsForCourier(selectedCourierId)}
                assignments={demandAssignments}
                courierOptions={courierOptions}
                onReassign={handleRequestReassign}
                onRemove={handleRemove}
                isBusy={isBusy}
                emptyMessage="Aucune demande pour ce coursier"
              />
            )}
          </div>
        )}
      </div>

      <ReassignModal
        isOpen={showReassignModal}
        onClose={() => setShowReassignModal(false)}
        courierOptions={courierOptions}
        initialCourierId={activeDemand ? demandAssignments?.[activeDemand.id] ?? null : null}
        onConfirm={handleConfirmReassign}
      />

      <AssignDemandModal
        isOpen={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        availableDemands={derivedUnassigned}
        courierLabel={`Coursier ${selectedCourierId || ''}`}
        onConfirm={handleConfirmAssign}
      />
    </div>
  );
}

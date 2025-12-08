import React, { useMemo, useState } from 'react';
import Navigation from './src/components/Navigation';
import MapUploader from './src/components/MapUploader';
import MapViewer from './src/components/MapViewer';
import DeliveryRequestUploader from './src/components/DeliveryRequestUploader';
import ManualDeliveryForm from './src/components/ManualDeliveryForm';
import CourierCountModal from './src/components/CourierCountModal';
import TourTable from './src/components/TourTable';
import TourActions from './src/components/TourActions';
import RestoreTourModal from './src/components/RestoreTourModal';
import CourierCountSelector from './src/components/CourierCountSelector';
import TourTabs from './src/components/TourTabs';
import CustomAlert from './src/components/CustomAlert';
import UnassignedDemands from './src/components/UnassignedDemands';
import apiService from './src/services/apiService';
import './leaflet-custom.css';

/**
 * Génère le contenu texte de l'itinéraire
 */
function generateItineraryText(tourData) {
  // Gérer le cas multi-tours
  if (Array.isArray(tourData)) {
    let content = '=== ITINÉRAIRES DE LIVRAISON MULTI-COURSIERS ===\n\n';
    content += `Nombre de coursiers: ${tourData.length}\n\n`;
    
    tourData.forEach((tour, courierIndex) => {
      content += `\n${'='.repeat(60)}\n`;
      content += `COURSIER ${courierIndex + 1}\n`;
      content += `${'='.repeat(60)}\n\n`;
      content += `Distance totale: ${((tour.totalDistance || 0) / 1000).toFixed(2)} km\n`;
      content += `Durée totale: ${((tour.totalDuration || 0) / 3600).toFixed(2)} h\n`;
      content += `Nombre de stops: ${tour.stops?.length || 0}\n`;
      content += `Nombre de segments: ${tour.trajets?.length || 0}\n\n`;
      content += '--- TRAJETS ---\n\n';
      
      if (tour.trajets && Array.isArray(tour.trajets)) {
        tour.trajets.forEach((trajet, index) => {
          content += `${index + 1}. ${trajet.nomRue || 'Segment'}\n`;
          content += `   De: ${trajet.origine || 'N/A'}\n`;
          content += `   À: ${trajet.destination || 'N/A'}\n`;
          content += `   Longueur: ${((trajet.longueur || 0) / 1000).toFixed(3)} km\n\n`;
        });
      }
    });
    
    return content;
  }
  
  // Cas mono-tour (ancien format)
  let content = '=== ITINÉRAIRE DE LIVRAISON ===\n\n';
  content += `Nombre de segments: ${tourData.tour?.length || 0}\n`;
  content += `Distance totale: ${tourData.metrics?.totalDistance?.toFixed(2) || 0} m\n`;
  content += `Nombre de stops: ${tourData.metrics?.stopCount || 0}\n\n`;
  content += '=== TRAJETS ===\n\n';

  if (tourData.tour && Array.isArray(tourData.tour)) {
    tourData.tour.forEach((trajet, index) => {
      content += `${index + 1}. ${trajet.nomRue || 'Segment'}\n`;
      content += `   De: ${trajet.origine || 'N/A'}\n`;
      content += `   À: ${trajet.destination || 'N/A'}\n`;
      content += `   Longueur: ${(trajet.longueur || 0).toFixed(2)} m\n\n`;
    });
  }

  return content;
}

/**
 * Convertit une couleur HSL en format hexadécimal
 */
function hslToHex(h, s, l) {
  s /= 100;
  l /= 100;
  
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs((h / 60) % 2 - 1));
  const m = l - c / 2;
  
  let r = 0, g = 0, b = 0;
  
  if (0 <= h && h < 60) {
    r = c; g = x; b = 0;
  } else if (60 <= h && h < 120) {
    r = x; g = c; b = 0;
  } else if (120 <= h && h < 180) {
    r = 0; g = c; b = x;
  } else if (180 <= h && h < 240) {
    r = 0; g = x; b = c;
  } else if (240 <= h && h < 300) {
    r = x; g = 0; b = c;
  } else if (300 <= h && h < 360) {
    r = c; g = 0; b = x;
  }
  
  // Convertir en valeurs RGB (0-255)
  r = Math.round((r + m) * 255);
  g = Math.round((g + m) * 255);
  b = Math.round((b + m) * 255);
  
  // Convertir en hexadécimal
  const toHex = (n) => {
    const hex = n.toString(16);
    return hex.length === 1 ? '0' + hex : hex;
  };
  
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
}

/**
 * Génère 50 couleurs vives et distinctes en utilisant l'espace HSL
 * et les retourne dans l'ordre: 5e, 10e, 15e, ..., 45e, puis 6e, 11e, ..., 46e, etc.
 */
function generateColorPalette() {
  const totalColors = 50;
  
  // Générer 50 couleurs en HSL avec saturation et luminosité optimales
  const baseColors = [];
  for (let i = 0; i < totalColors; i++) {
    const hue = (360 * i) / totalColors; // Répartition uniforme sur la roue chromatique (0-360°)
    const saturation = 75; // Saturation élevée pour des couleurs vives (75%)
    const lightness = 55; // Luminosité moyenne pour une bonne visibilité (55%)
    
    const hexColor = hslToHex(hue, saturation, lightness);
    baseColors.push(hexColor);
  }
  
  // Réorganiser selon la séquence demandée: prendre de 5 en 5
  // 5e (index 4), 10e (index 9), 15e (index 14), ..., 45e (index 44)
  // puis 6e (index 5), 11e (index 10), ..., 46e (index 45)
  // puis 7e (index 6), 12e (index 11), ..., 47e (index 46)
  // etc.
  const reorderedColors = [];
  for (let offset = 4; offset < totalColors; offset++) {
    for (let i = offset; i < totalColors; i += 5) {
      reorderedColors.push(baseColors[i]);
    }
  }
  
  // Ajouter les couleurs restantes (indices 0-3)
  for (let i = 0; i < 4; i++) {
    if (i < totalColors) {
      reorderedColors.push(baseColors[i]);
    }
  }
  
  return reorderedColors;
}

// Palette de couleurs générée
const COLOR_PALETTE = generateColorPalette();

/**
 * Obtient une couleur de la palette en utilisant un modulo
 */
function getColorFromPalette(index) {
  return COLOR_PALETTE[index % COLOR_PALETTE.length];
}

/**
 * Composant principal de l'application Pickup & Delivery
 * Gère l'état global et la navigation entre les différentes vues
 * Communique avec le backend via apiService
 */
export default function PickupDeliveryUI() {
  const [activeTab, setActiveTab] = useState('home');
  const [showMessage, setShowMessage] = useState(true);
  const [showMapUpload, setShowMapUpload] = useState(false);
  const [showDeliveryUpload, setShowDeliveryUpload] = useState(false);
  const [showManualForm, setShowManualForm] = useState(false);
  const [showCourierModal, setShowCourierModal] = useState(false);
  const [showRestoreTourModal, setShowRestoreTourModal] = useState(false);
  const [mapData, setMapData] = useState(null);
  const [deliveryRequestSet, setDeliveryRequestSet] = useState(null);
  const [courierCount, setCourierCount] = useState(1);
  const [tourData, setTourData] = useState(null); // Maintenant peut être un array de tours
  const [unassignedDemands, setUnassignedDemands] = useState([]); // Demandes non assignées (contrainte 4h)
  const [selectedCourierId, setSelectedCourierId] = useState(null); // null = tous les coursiers
  const [isCalculatingTour, setIsCalculatingTour] = useState(false);
  const [isEditingAssignments, setIsEditingAssignments] = useState(false);
  const [stagedAssignments, setStagedAssignments] = useState(null); // demandId -> courierId|null
  const [stagedRemovals, setStagedRemovals] = useState([]);
  // Save modal state moved to `TourActions` to centralize save logic
  
  // États pour la sélection sur la carte
  const [isMapSelectionActive, setIsMapSelectionActive] = useState(false);
  const [mapSelectionType, setMapSelectionType] = useState(null); // 'pickup' ou 'delivery'
  const [selectedNodeId, setSelectedNodeId] = useState(null);
  const [savedFormData, setSavedFormData] = useState(null); // Pour sauvegarder les données du formulaire

  // États pour CustomAlert
  const [alertConfig, setAlertConfig] = useState(null);

  // État pour savoir si on est en mode ajout manuel (formulaire ouvert ou sélection active)
  const isAddingManually = showManualForm || isMapSelectionActive;

  // Fonction helper pour afficher une alerte personnalisée
  const showAlert = (type, title, message, autoClose = false) => {
    setAlertConfig({ type, title, message, autoClose });
  };

  const closeAlert = () => {
    setAlertConfig(null);
  };

  // Gestion du changement d'onglet
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    if (tab === 'map') {
      if (!mapData) {
        setShowMapUpload(true);
      }
      setShowMessage(false);
    } else {
      setShowMapUpload(false);
    }
  };

  // Gestion du chargement de la carte depuis le backend
  const handleMapLoaded = (map) => {
    setMapData(map);
    setShowMapUpload(false);
    setShowMessage(false);
    setActiveTab('map');
  };

  // Gestion de l'annulation du chargement
  const handleCancelUpload = () => {
    setShowMapUpload(false);
    if (!mapData) {
      setActiveTab('home');
    }
  };

  // Gestion de la suppression de la carte
  const handleClearMap = async () => {
    try {
      await apiService.clearMap();
      setMapData(null);
      setDeliveryRequestSet(null);
      setTourData(null);
      setShowMapUpload(true);
    } catch (error) {
      console.error('Erreur lors de la suppression de la carte:', error);
    }
  };

  // Gestion de la mise à jour des demandes (suppression, etc.)
  const handleDeliveryRequestSetUpdated = async (updatedSet) => {
    console.log('handleDeliveryRequestSetUpdated reçoit:', updatedSet);
    
    // Réassigner les couleurs dans le bon ordre après modification
    if (updatedSet?.demands && updatedSet.demands.length > 0) {
      const demandsWithColors = updatedSet.demands.map((demand, index) => ({
        ...demand,
        color: getColorFromPalette(index)
      }));
      
      setDeliveryRequestSet({
        ...updatedSet,
        demands: demandsWithColors
      });

      // ✅ Recalculer automatiquement si une tournée était déjà calculée
      if (tourData) {
        console.log('🔄 Recalcul automatique de la tournée après modification des demandes...');
        console.log('📊 Nombre de demandes après modification:', demandsWithColors.length);
        setIsCalculatingTour(true);
        
        try {
          const result = await apiService.calculateTour(courierCount);
          console.log('📦 Résultat du recalcul:', result);
          
          if (result.success && result.data && result.data.length > 0) {
            const tour = result.data[0];
            console.log('✅ Tour recalculé:', tour);
            console.log('📍 Stops dans le nouveau tour:', tour.stops?.length || 0);
            
            const newTourData = {
              tour: tour.trajets || tour.segments || tour.path || [],
              metrics: {
                stopCount: tour.stops?.length || 0,
                totalDistance: tour.totalDistance || 0,
                segmentCount: (tour.trajets || tour.segments || tour.path || []).length
              }
            };
            
            console.log('📊 Nouveau tourData créé:', newTourData);
            console.log('🛣️  Nombre de trajets:', newTourData.tour.length);
            setTourData(newTourData);
            console.log('✅ Tournée recalculée automatiquement avec', newTourData.tour.length, 'trajets');
          } else {
            console.warn('⚠️ Pas de données valides dans le résultat du recalcul');
            setTourData(null);
          }
        } catch (error) {
          console.error('❌ Erreur lors du recalcul automatique:', error);
          setTourData(null);
        } finally {
          setIsCalculatingTour(false);
        }
      }
    } else {
      // Si plus aucune demande, réinitialiser la tournée ET le deliveryRequestSet
      console.log('⚠️ Aucune demande restante, réinitialisation de la tournée');
      setDeliveryRequestSet(updatedSet || null);
      setTourData(null);
    }
  };

  const buildDemandAssignments = (tours, requestSet) => {
    const demands = requestSet?.demands || [];
    const mapping = {};
    demands.forEach((d) => {
      mapping[d.id] = null;
    });

    if (!Array.isArray(tours) || tours.length === 0) return mapping;

    const nodeBelongsToDemand = (nodeId, demand) =>
      demand.pickupNodeId === nodeId || demand.deliveryNodeId === nodeId;

    tours.forEach((tour) => {
      const courierId = tour.courierId;
      // Collect node ids from trajets stopArrivee and stops if present
      const nodes = new Set();
      (tour.trajets || tour.tour || []).forEach((trajet) => {
        if (trajet?.stopArrivee?.idNode) nodes.add(trajet.stopArrivee.idNode);
        if (trajet?.stopDepart?.idNode) nodes.add(trajet.stopDepart.idNode);
      });
      (tour.stops || []).forEach((stop) => {
        if (stop?.idNode) nodes.add(stop.idNode);
      });

      demands.forEach((d) => {
        if (nodeBelongsToDemand(d.pickupNodeId, d) && nodes.has(d.pickupNodeId)) {
          mapping[d.id] = courierId;
        }
        if (nodeBelongsToDemand(d.deliveryNodeId, d) && nodes.has(d.deliveryNodeId)) {
          mapping[d.id] = courierId;
        }
      });
    });

    return mapping;
  };

  const demandAssignments = useMemo(
    () => buildDemandAssignments(tourData, deliveryRequestSet),
    [tourData, deliveryRequestSet]
  );

  const effectiveAssignments = isEditingAssignments && stagedAssignments ? stagedAssignments : demandAssignments;

  const filteredDeliveryRequestSet = useMemo(() => {
    if (!deliveryRequestSet) return deliveryRequestSet;
    if (!isEditingAssignments || !Array.isArray(stagedRemovals) || stagedRemovals.length === 0) return deliveryRequestSet;
    return {
      ...deliveryRequestSet,
      demands: (deliveryRequestSet.demands || []).filter((d) => !stagedRemovals.includes(d.id)),
    };
  }, [deliveryRequestSet, isEditingAssignments, stagedRemovals]);

  const recalculateToursSilent = async () => {
    setIsCalculatingTour(true);
    try {
      const result = await apiService.calculateTour(courierCount);
      if (result.success) {
        const response = result.data;
        const tours = response.tours || [];
        const unassigned = response.unassignedDemands || [];
        setTourData(tours);
        setUnassignedDemands(unassigned);
      } else {
        showAlert('error', '❌ Erreur', result.message || 'Réponse invalide du serveur');
      }
    } catch (error) {
      console.error('💥 Erreur lors du recalcul de la tournée:', error);
      showAlert('error', '❌ Erreur', error.message);
    } finally {
      setIsCalculatingTour(false);
    }
  };

  const handleRemoveDemandById = async (demandId) => {
    if (!demandId) return;
    if (isEditingAssignments) {
      setStagedRemovals((prev) => Array.from(new Set([...(prev || []), demandId])));
      setStagedAssignments((prev) => {
        const next = { ...(prev || effectiveAssignments) };
        delete next[demandId];
        return next;
      });
      return;
    }

    if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette demande ?')) return;
    try {
      await apiService.removeDemand(demandId);
      const updatedDemands = (deliveryRequestSet?.demands || []).filter((d) => d.id !== demandId);
      const updatedRequestSet = {
        warehouse: deliveryRequestSet?.warehouse || null,
        demands: updatedDemands,
      };
      await handleDeliveryRequestSetUpdated(updatedRequestSet);
      await recalculateToursSilent();
    } catch (err) {
      showAlert('error', '❌ Erreur', err.message);
    }
  };

  const handleReassignDemand = async (demandId, targetCourierId) => {
    if (!deliveryRequestSet?.demands) return;
    if (isEditingAssignments) {
      setStagedAssignments((prev) => {
        const base = prev || effectiveAssignments || {};
        return { ...base, [demandId]: targetCourierId === '' ? null : targetCourierId };
      });
      return;
    }

    try {
      setIsCalculatingTour(true);
      await apiService.updateCourierAssignment({
        demandId,
        newCourierId: targetCourierId !== null && targetCourierId !== undefined ? String(targetCourierId) : null,
        oldCourierId: (demandAssignments?.[demandId] ?? selectedCourierId ?? null) !== null
          ? String(demandAssignments?.[demandId] ?? selectedCourierId)
          : null,
        deliveryIndex: null,
      });
      await recalculateToursSilent();
    } catch (err) {
      showAlert('error', '❌ Erreur', err.message);
    } finally {
      setIsCalculatingTour(false);
    }
  };

  // Gestion du chargement des demandes de livraison
  const handleDeliveryRequestsLoaded = (requestSet) => {
    // Assigner des couleurs à chaque demande en utilisant la palette
    const demandsWithColors = requestSet.demands.map((demand, index) => ({
      ...demand,
      color: getColorFromPalette(index)
    }));
    
    setDeliveryRequestSet({
      ...requestSet,
      demands: demandsWithColors
    });
    setTourData(null); // Réinitialiser la tournée si on charge de nouvelles demandes
    setShowDeliveryUpload(false);
};


  // Gestion de l'annulation du chargement des demandes
  const handleCancelDeliveryUpload = () => {
    setShowDeliveryUpload(false);
  };

  // Gestion du calcul de la tournée
  const handleCalculateTour = async () => {
    if (!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0) {
      showAlert('warning', '⚠️ Attention', 'Veuillez d\'abord charger des demandes de livraison');
      return;
    }

    setIsCalculatingTour(true);
    
    try {
      console.log(`🚀 Calcul de la tournée pour ${courierCount} livreur(s)...`);
      const result = await apiService.calculateTour(courierCount);
      
      console.log('📦 Résultat complet:', result);
      
      if (result.success) {
        // Nouvelle structure de réponse avec TourCalculationResponse
        const response = result.data;
        const tours = response.tours || [];
        const unassignedDemands = response.unassignedDemands || [];
        
        // Cas où aucune tournée n'a été créée (toutes les demandes rejetées)
        if (tours.length === 0) {
          alert('⚠️ ATTENTION: Aucune tournée n\'a pu être calculée !\n\n' +
                `Avec ${courierCount} coursier(s), la contrainte des 4h est trop restrictive.\n` +
                'Toutes les demandes ont été rejetées.\n\n' +
                '💡 Solution: Augmentez le nombre de coursiers.');
          return;
        }
        
        // Stocker les tournées et demandes non assignées
        console.log('✅ Tournées calculées avec succès:', tours);
        console.log('⚠️  Demandes non assignées:', unassignedDemands);
        
        // 🔍 DEBUG: Vérifier les IDs des coursiers
        console.log('🔍 CourierIds reçus:', tours.map(t => t.courierId));
        const courierIds = tours.map(t => t.courierId);
        const uniqueIds = new Set(courierIds);
        if (courierIds.length !== uniqueIds.size) {
          console.warn('⚠️ ATTENTION: Doublons de courierIds détectés!', courierIds);
        }
        
        setTourData(tours); // Array de tours
        setUnassignedDemands(unassignedDemands); // Demandes non assignées
        
        // Calculer les statistiques globales pour l'alerte récapitulative
        const totalDistance = tours.reduce(
          (sum, tour) => sum + (tour.totalDistance || 0),
          0
        );
        const totalStops = tours.reduce(
          (sum, tour) => sum + (tour.stops?.length || 0),
          0
        );
        const totalSegments = tours.reduce(
          (sum, tour) =>
            sum + ((tour.trajets || tour.segments || tour.path || []).length),
          0
        );

        alert(
          `✅ Tournée calculée avec succès !\n\n` +
          `👥 Coursiers: ${tours.length}\n` +
          `📍 Stops: ${totalStops}\n` +
          `📏 Distance: ${Number(totalDistance || 0).toFixed(2)} m\n` +
          `🛣️  Segments: ${totalSegments}`
        );
      } else {
        console.error('❌ Réponse invalide:', result);
        showAlert('error', '❌ Erreur', result.message || 'Réponse invalide du serveur');
      }
    } catch (error) {
      console.error('💥 Erreur lors du calcul de la tournée:', error);
      showAlert('error', '❌ Erreur', error.message);
    } finally {
      setIsCalculatingTour(false);
    }
  };

  
  

  

  // Gestion du clic sur "Ajouter Pickup&Delivery" (ajout manuel)
  const handleAddDeliveryManually = () => {
    if (!mapData) {
      showAlert('warning', '⚠️ Attention', 'Veuillez d\'abord charger une carte');
      return;
    }
    setShowManualForm(true);
  };

  // Gestion de l'ajout manuel d'une demande
  const handleManualDemandAdd = async (demand) => {
    try {
      // Ajouter la demande au backend
      const response = await apiService.addDeliveryRequest({
        pickupAddress: demand.pickupNodeId,
        deliveryAddress: demand.deliveryNodeId,
        pickupDuration: demand.pickupDurationSec,
        deliveryDuration: demand.deliveryDurationSec
      });

      // Extraire l'ID correctement selon la structure de réponse
      const addedDemandId = response.data?.id || response.id;

      const newDemand = {
        id: addedDemandId || `demand_${Date.now()}`,
        pickupNodeId: demand.pickupNodeId,
        deliveryNodeId: demand.deliveryNodeId,
        pickupDurationSec: demand.pickupDurationSec,
        deliveryDurationSec: demand.deliveryDurationSec
      };

      // Ajouter à la liste existante
      const updatedDemands = [...(deliveryRequestSet?.demands || []), newDemand];
      const demandsWithColors = updatedDemands.map((d, index) => ({
        ...d,
        color: getColorFromPalette(index)
      }));

      const updatedRequestSet = {
        warehouse: deliveryRequestSet?.warehouse || null,
        demands: demandsWithColors
      };

      // Appeler le callback pour mettre à jour le state et recalculer si besoin
      handleDeliveryRequestSetUpdated(updatedRequestSet);
    } catch (err) {
      showAlert('error', '❌ Erreur', 'Erreur lors de l\'ajout manuel : ' + err.message);
    }
    setShowManualForm(false);
    setSelectedNodeId(null);
    setMapSelectionType(null);
    setSavedFormData(null);
  };

  // Gestion du démarrage de la sélection sur la carte
  const handleStartMapSelection = (type, formData) => {
    setSavedFormData(formData); // Sauvegarder les données du formulaire
    setMapSelectionType(type);
    setIsMapSelectionActive(true);
    setShowManualForm(false); // Fermer le formulaire
  };

  // Gestion du clic sur un segment de la carte
  const handleMapSegmentClick = (nodeId) => {
    if (isMapSelectionActive) {
      setSelectedNodeId(nodeId);
      setIsMapSelectionActive(false);
      setShowManualForm(true); // Rouvrir le formulaire
    }
  };

  // Gestion de la restauration d'une tournée depuis un fichier JSON
  const handleRestoreTour = async (restorePayload, legacyDemands = []) => {
    if (!mapData) {
      alert('Veuillez d\'abord charger une carte');
      return;
    }

    // Normaliser les données du fichier (nouveau format ou ancien)
    const toursFromFile = Array.isArray(restorePayload?.tours)
      ? restorePayload.tours
      : Array.isArray(restorePayload)
        ? restorePayload
        : Array.isArray(restorePayload?.tour)
          ? [{ trajets: restorePayload.tour, stops: restorePayload.stops || [] }]
          : [];

    const demandsFromFile = Array.isArray(restorePayload?.demands) && restorePayload.demands.length > 0
      ? restorePayload.demands
      : Array.isArray(legacyDemands)
        ? legacyDemands
        : [];

    if (!demandsFromFile.length) {
      alert('Aucune demande trouvée dans le fichier à restaurer');
      return;
    }

    const deriveWarehouseNode = () => {
      if (restorePayload?.warehouse?.nodeId) return restorePayload.warehouse.nodeId;

      for (const tour of toursFromFile || []) {
        if (tour?.stops?.length) {
          const firstStop = tour.stops[0];
          if (firstStop?.typeStop === 'WAREHOUSE' && firstStop.idNode) {
            return firstStop.idNode;
          }
        }

        const trajets = tour?.trajets || tour?.tour || [];
        if (Array.isArray(trajets) && trajets.length > 0) {
          const firstTrajet = trajets[0];
          if (firstTrajet?.stopDepart?.idNode) return firstTrajet.stopDepart.idNode;
          if (firstTrajet?.segments?.length && firstTrajet.segments[0]?.origin) {
            return firstTrajet.segments[0].origin;
          }
        }
      }

      return mapData?.nodes?.[0]?.id || null;
    };

    try {
      console.log('🔄 Restauration de tournée avec', demandsFromFile.length, 'demandes');

      // Ajouter les demandes au backend et récupérer les IDs générés
      const addedDemandsWithIds = [];
      
      for (const demand of demandsFromFile) {
        const response = await apiService.addDeliveryRequest({
          pickupAddress: demand.pickupNodeId,
          deliveryAddress: demand.deliveryNodeId,
          pickupDuration: demand.pickupDurationSec ?? 300,
          deliveryDuration: demand.deliveryDurationSec ?? 300,
        });
        
        // Récupérer l'ID retourné par le backend
        const backendId = response.data?.id || response.id;
        
        addedDemandsWithIds.push({
          ...demand,
          id: backendId || demand.id, // Utiliser l'ID du backend, sinon l'ancien ID
        });
      }

      console.log('✅ Toutes les demandes ont été ajoutées au backend');

      const warehouseNodeId = deriveWarehouseNode();
      const warehouse = warehouseNodeId
        ? {
            nodeId: warehouseNodeId,
            departureTime: restorePayload?.warehouse?.departureTime || '08:00',
          }
        : null;

      const demandsWithColors = addedDemandsWithIds.map((demand, index) => ({
        ...demand,
        color: getColorFromPalette(index),
      }));
      
      setDeliveryRequestSet({
        warehouse,
        demands: demandsWithColors,
      });

      console.log('✅ DeliveryRequestSet défini avec IDs du backend');
      
      // 🔄 Recalculer la tournée automatiquement pour avoir la bonne structure
      setIsCalculatingTour(true);
      let recalculatedTours = null;
      let recalculatedUnassigned = [];

      try {
        const couriersToUse = restorePayload?.courierCount || courierCount;
        const result = await apiService.calculateTour(couriersToUse);
        
        if (result.success && result.data && Array.isArray(result.data.tours)) {
          recalculatedTours = result.data.tours || [];
          recalculatedUnassigned = result.data.unassignedDemands || [];
          console.log('✅ Tournée recalculée après restauration');
        }
      } catch (error) {
        console.error('❌ Erreur lors du recalcul:', error);
      } finally {
        setIsCalculatingTour(false);
      }

      const toursToApply = (Array.isArray(recalculatedTours) && recalculatedTours.length > 0)
        ? recalculatedTours
        : (Array.isArray(toursFromFile) && toursFromFile.length > 0 ? toursFromFile : null);

      setTourData(toursToApply);
      setUnassignedDemands((Array.isArray(recalculatedTours) && recalculatedTours.length > 0) ? recalculatedUnassigned : []);

      setActiveTab('map');
      
      // Choisir les métriques pour l'alerte finale
      const toursForMetrics = Array.isArray(toursToApply) && toursToApply.length > 0
        ? toursToApply
        : toursFromFile;

      const metrics = toursForMetrics && toursForMetrics.length > 0
        ? {
            stopCount: toursForMetrics.reduce((sum, t) => sum + (t.stops?.length || 0), 0),
            totalDistance: toursForMetrics.reduce((sum, t) => sum + (t.totalDistance || 0), 0),
            segmentCount: toursForMetrics.reduce((sum, t) => sum + ((t.trajets || t.segments || t.path || []).length), 0),
          }
        : restorePayload?.metrics;

      alert(`Tournée restaurée avec succès !\n\n` +
            `📍 Stops: ${metrics?.stopCount || 0}\n` +
            `📏 Distance: ${Number(metrics?.totalDistance || 0).toFixed(2)} m\n` +
            `🛣️  Segments: ${metrics?.segmentCount || 0}\n` +
            `📦 Demandes: ${addedDemandsWithIds.length}`);
    } catch (error) {
      console.error('❌ Erreur lors de la restauration de la tournée:', error);
      alert(`Erreur lors de la restauration : ${error.message}`);
    }
  };

  return (
    <div className="h-screen bg-gray-800 text-white flex flex-col overflow-hidden">
      {/* Navigation Bar avec titre intégré */}
      <div className={isMapSelectionActive ? 'pointer-events-none opacity-50' : ''}>
        <Navigation 
          activeTab={activeTab}
          onTabChange={handleTabChange}
          showMapMessage={showMessage}
          hasMap={mapData !== null}
          onLoadDeliveryRequests={() => setShowDeliveryUpload(true)}
          onRestoreTour={() => setShowRestoreTourModal(true)}
        />
      </div>

      {/* Restore Tour Modal */}
      <RestoreTourModal
        isOpen={showRestoreTourModal}
        onClose={() => setShowRestoreTourModal(false)}
        onRestore={handleRestoreTour}
      />

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-h-0 overflow-hidden">
        {/* Home View */}
        {activeTab === 'home' && !showMapUpload && (
          <div className="p-8 mt-20">
            <h2 className="text-3xl font-bold text-center">
              Bienvenue sur votre plateforme de gestion de tournées de livraison à vélo !
            </h2>
            <p className="text-center text-gray-300 mt-4">
              Cliquez sur l'icône de localisation pour charger une carte.
            </p>
          </div>
        )}

        {/* Map Upload View */}
        {showMapUpload && !mapData && (

          <MapUploader 
            onMapLoaded={handleMapLoaded}
            onCancel={handleCancelUpload}
          />
        )}

        {/* Delivery Upload View (XML) */}
        {showDeliveryUpload && mapData && (
          <DeliveryRequestUploader 
            onRequestsLoaded={handleDeliveryRequestsLoaded}
            onCancel={handleCancelDeliveryUpload}
          />
        )}

        {/* Manual Delivery Form */}
        {showManualForm && mapData && (
          <ManualDeliveryForm 
            onAdd={handleManualDemandAdd}
            onCancel={() => {
              setShowManualForm(false);
              setSelectedNodeId(null);
              setMapSelectionType(null);
              setSavedFormData(null);
            }}
            availableNodes={mapData.nodes}
            onStartMapSelection={handleStartMapSelection}
            selectedNodeId={selectedNodeId}
            mapSelectionType={mapSelectionType}
            savedFormData={savedFormData}
          />
        )}

        {/* Courier Count Modal */}
        <CourierCountModal 
          isOpen={showCourierModal}
          onClose={() => setShowCourierModal(false)}
          onConfirm={(count) => {
            setCourierCount(count);
            console.log(`Nombre de livreurs défini à: ${count}`);
          }}
          currentCount={courierCount}
        />

        {/* Map View */}
        {mapData && activeTab === 'map' && !showDeliveryUpload && (
          <div className="flex-1 flex flex-col overflow-hidden p-4 gap-4 min-h-0">
            {/* Ligne principale : Carte + Panneau d'informations */}
            <div className="flex-1 flex gap-4 min-h-0">
              {/* Carte sur la gauche - plus grande */}
              <div className="w-2/3 flex flex-col bg-gray-700 rounded-lg overflow-hidden">
                <MapViewer 
                  mapData={mapData}
                  onClearMap={handleClearMap}
                  deliveryRequestSet={deliveryRequestSet}
                  onDeliveryRequestSetUpdated={handleDeliveryRequestSetUpdated}
                  tourData={tourData}
                  selectedCourierId={selectedCourierId}
                  onSegmentClick={handleMapSegmentClick}
                  isMapSelectionActive={isMapSelectionActive}
                  isAddingManually={isAddingManually}
                />
              </div>
              
              {/* Panneau droit avec informations et boutons */}
              <div className={`flex-1 flex flex-col gap-4 min-h-0 ${isMapSelectionActive ? 'pointer-events-none opacity-50' : ''}`}>
                {/* Tableau de tournée ou onglets multi-tours */}
                <div className="bg-gray-700 rounded-lg p-6 flex flex-col flex-1 min-h-0 overflow-hidden">
                  <h3 className="text-xl font-semibold mb-4 flex-shrink-0">
                    {tourData ? (Array.isArray(tourData) && tourData.length > 1 ? 'Tournées Multi-Coursiers' : 'Tournée Calculée') : 'Informations'}
                  </h3>
                  <div className="flex-1 overflow-auto min-h-0">
                    {tourData ? (
                      Array.isArray(tourData) ? (
                        <TourTabs
                          tours={tourData}
                          deliveryRequestSet={filteredDeliveryRequestSet}
                          onTourSelect={(tour) => setSelectedCourierId(tour?.courierId || null)}
                          demandAssignments={effectiveAssignments}
                          unassignedDemands={unassignedDemands}
                          onReassignDemand={handleReassignDemand}
                          onRemoveDemand={handleRemoveDemandById}
                          isBusy={isCalculatingTour}
                          isEditing={isEditingAssignments}
                          onValidateEdit={async () => {
                            try {
                              setIsCalculatingTour(true);

                              // 1) Supprimer réellement les demandes marquées côté backend et frontend
                              let remainingDemands = deliveryRequestSet?.demands || [];
                              if (Array.isArray(stagedRemovals) && stagedRemovals.length > 0) {
                                await Promise.all(stagedRemovals.map((id) => apiService.removeDemand(id)));
                                remainingDemands = remainingDemands.filter((d) => !stagedRemovals.includes(d.id));

                                // Réappliquer les couleurs localement après suppression
                                const demandsWithColors = remainingDemands.map((demand, index) => ({
                                  ...demand,
                                  color: getColorFromPalette(index),
                                }));

                                setDeliveryRequestSet((prev) => ({
                                  ...(prev || {}),
                                  warehouse: deliveryRequestSet?.warehouse || prev?.warehouse || null,
                                  demands: demandsWithColors,
                                }));
                              }

                              // 2) Construire les assignments uniquement avec les demandes restantes
                              const assignments = (remainingDemands || []).map((d) => ({
                                demandId: d.id,
                                courierId:
                                  stagedAssignments && stagedAssignments[d.id] !== undefined
                                    ? stagedAssignments[d.id]
                                    : demandAssignments?.[d.id] ?? null,
                              }));

                              // 3) Recalcul complet via nouvel endpoint
                              const result = await apiService.recalculateAssignments(assignments);
                              if (result?.success && result.data) {
                                const resp = result.data;
                                const incomingTours = resp.tours || [];
                                const toursWithDefault =
                                  incomingTours.length > 0
                                    ? incomingTours
                                    : [
                                        {
                                          courierId: 1,
                                          trajets: [],
                                          stops: [],
                                          totalDistance: 0,
                                          totalDurationSec: 0,
                                          requestCount: 0
                                        }
                                      ];
                                setTourData(toursWithDefault);
                                setUnassignedDemands(resp.unassignedDemands || []);
                              } else {
                                throw new Error(result?.message || 'Réponse invalide du serveur');
                              }
                            } catch (err) {
                              showAlert('error', '❌ Erreur', err.message);
                            } finally {
                              setIsCalculatingTour(false);
                              setIsEditingAssignments(false);
                              setStagedAssignments(null);
                              setStagedRemovals([]);
                            }
                          }}
                          onCancelEdit={() => {
                            setIsEditingAssignments(false);
                            setStagedAssignments(null);
                            setStagedRemovals([]);
                          }}
                        />
                      ) : (
                        <TourTable 
                          tourData={Array.isArray(tourData) ? { tour: tourData[0].trajets, metrics: { stopCount: tourData[0].stops?.length || 0, totalDistance: tourData[0].totalDistance || 0, segmentCount: tourData[0].trajets?.length || 0 }} : tourData}
                          deliveryRequestSet={deliveryRequestSet}
                        />
                      )
                    ) : (
                      <div className="text-gray-400 text-center py-8">
                        Chargez des demandes et calculez une tournée
                      </div>
                    )}
                  </div>
                </div>
                
                {/* Boutons d'action */}
                <div className="bg-gray-700 rounded-lg p-4 flex-shrink-0">
                  {!tourData ? (
                    // Avant calcul de tournée : Sélecteur + Boutons
                    <div className="flex flex-col gap-4">
                      {/* Sélecteur de coursiers - Affiché seulement si des demandes sont chargées */}
                      {deliveryRequestSet && deliveryRequestSet.demands && deliveryRequestSet.demands.length > 0 && (
                        <CourierCountSelector
                          value={courierCount}
                          onChange={setCourierCount}
                          disabled={isCalculatingTour}
                        />
                      )}
                      
                      {/* Boutons d'action */}
                      <div className="flex gap-3">
                        {/* Bouton Ajouter Pickup&Delivery (manuel) */}
                        <button 
                          onClick={handleAddDeliveryManually}
                          disabled={!deliveryRequestSet}
                          className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                          title={!deliveryRequestSet ? "Chargez d'abord des demandes de livraison" : "Ajouter manuellement une demande de livraison"}
                        >
                          Ajouter Pickup&Delivery
                        </button>
                        
                        {/* Bouton Calculer tournée */}
                        <button 
                          onClick={handleCalculateTour}
                          disabled={!deliveryRequestSet || !deliveryRequestSet.demands || deliveryRequestSet.demands.length === 0 || isCalculatingTour}
                          className="flex-1 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                                   text-white px-6 py-3 rounded-lg font-semibold transition-colors shadow-lg"
                          title="Calculer la tournée optimale"
                        >
                          {isCalculatingTour ? 'Calcul en cours...' : 'Calculer tournée'}
                        </button>
                      </div>
                    </div>
                  ) : (
                    // Boutons après calcul de tournée (4 boutons sur 2 lignes)
                    <div className="flex flex-col gap-3">
                      {/* Première ligne : Ajouter et Modifier tournée */}
                      <div className="flex gap-3">
                        <button 
                          onClick={handleAddDeliveryManually}
                          className="flex-1 bg-green-600 hover:bg-green-700 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Ajouter une nouvelle demande de livraison"
                        >
                          ➕ Ajouter Pickup&Delivery
                        </button>

                        <button
                          onClick={() => {
                            setIsEditingAssignments(true);
                            setStagedAssignments({ ...demandAssignments });
                            setStagedRemovals([]);
                            const panel = document.getElementById('assignments-panel');
                            if (panel) {
                              panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            }
                          }}
                          className="flex-1 bg-orange-600 hover:bg-orange-700 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                                   flex items-center justify-center gap-2"
                          title="Modifier la tournée calculée"
                        >
                          ✏️ Modifier Tournée
                        </button>
                      </div>
                      
                      {/* Deuxième ligne : Sauvegarder itinéraire et Sauvegarder tournée */}
                      <TourActions
                        tourData={tourData}
                        deliveryRequestSet={deliveryRequestSet}
                        onSaveItinerary={() => console.log('Itinéraire sauvegardée')}
                        onSaveTour={() => console.log('Tournée sauvegardée')}
                      />
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Deliveries View - À implémenter */}
        {activeTab === 'deliveries' && (
          <div className="p-8 mt-20">
            <h2 className="text-2xl font-bold text-center">
              Gestion des demandes de livraison
            </h2>
            <p className="text-center text-gray-300 mt-4">
              Cette section sera disponible prochainement.
            </p>
          </div>
        )}

        {/* Tours View - À implémenter */}
        {activeTab === 'tours' && (
          <div className="p-8 mt-20">
            <h2 className="text-2xl font-bold mb-6">
              📋 Demandes non traitées
            </h2>
            <UnassignedDemands 
              unassignedDemands={unassignedDemands}
              deliveryRequestSet={deliveryRequestSet}
              courierCount={courierCount}
            />
          </div>
        )}
        {/* Save modals are centralized inside TourActions */}

      </main>

      {/* CustomAlert */}
      {alertConfig && (
        <CustomAlert
          type={alertConfig.type}
          title={alertConfig.title}
          message={alertConfig.message}
          autoClose={alertConfig.autoClose}
          onClose={closeAlert}
        />
      )}
    </div>
  );
}
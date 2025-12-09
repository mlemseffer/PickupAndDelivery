import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Polyline, Popup, useMap } from 'react-leaflet';
import { Maximize2, Minimize2, Eye, EyeOff } from 'lucide-react';
import DeliveryMarkers from './DeliveryMarkers';
import TourSegments from './TourSegments';
import MultiTourPolylines from './MultiTourPolylines';
import Icon from './Icon';
import 'leaflet/dist/leaflet.css';

/**
 * Composant pour forcer le redimensionnement et le centrage de la carte
 */
function MapResizer({ center }) {
  const map = useMap();
  
  useEffect(() => {
    // Petit délai pour s'assurer que la carte est complètement montée
    const timer = setTimeout(() => {
      map.invalidateSize();
      if (center && center.length === 2) {
        map.setView(center, map.getZoom(), { animate: false });
      }
    }, 50);
    
    return () => clearTimeout(timer);
  }, [map, center]);
  
  return null;
}

function ZoomWatcher({ onZoomChange }) {
  const map = useMap();

  useEffect(() => {
    if (!map) {
      return;
    }

    const handleZoom = () => onZoomChange(map.getZoom());
    handleZoom();

    map.on('zoomend', handleZoom);
    return () => {
      map.off('zoomend', handleZoom);
    };
  }, [map, onZoomChange]);

  return null;
}

/**
 * Composant pour gérer le zoom et n'afficher les segments que si le zoom est suffisant
 */
function SegmentRenderer({ segments, nodesById, isMapSelectionActive, onSegmentClick, maxSegments = 500 }) {
  const map = useMap();
  const [zoom, setZoom] = useState(map.getZoom());
  
  useEffect(() => {
    const handleZoom = () => {
      setZoom(map.getZoom());
    };
    
    map.on('zoomend', handleZoom);
    return () => {
      map.off('zoomend', handleZoom);
    };
  }, [map]);
  
  // Protection : vérifier que segments est un tableau
  if (!Array.isArray(segments) || segments.length === 0) {
    console.log('[SegmentRenderer] Pas de segments à afficher');
    return null;
  }
  
  // Afficher tous les segments sans limite
  const segmentsToRender = segments;
  
  console.log(`[SegmentRenderer] Affichage de ${segmentsToRender.length} segments`);
  
  return (
    <>
      {segmentsToRender.map((segment, index) => {
        if (!segment || !segment.origin || !segment.destination) {
          return null;
        }
        
        const originNode = nodesById[segment.origin];
        const destinationNode = nodesById[segment.destination];
        
        // Si les deux nœuds existent, dessiner le tronçon
        if (originNode && destinationNode) {
          const handleSegmentClick = () => {
            if (isMapSelectionActive && onSegmentClick) {
              onSegmentClick(segment.origin);
            }
          };

          return (
            <Polyline
              key={`segment-${index}`}
              positions={[
                [originNode.latitude, originNode.longitude],
                [destinationNode.latitude, destinationNode.longitude]
              ]}
              color={isMapSelectionActive ? "#10b981" : "#3b82f6"}
              weight={isMapSelectionActive ? 5 : 3}
              opacity={isMapSelectionActive ? 1 : 0.7}
              eventHandlers={{
                click: handleSegmentClick
              }}
              className={isMapSelectionActive ? "cursor-pointer" : ""}
            />
          );
        }
        return null;
      })}
    </>
  );
}

/**
 * Composant pour afficher la carte avec Leaflet
 */

export default function MapViewer({ 
  mapData, 
  onClearMap, 
  deliveryRequestSet, 
  tourData,
  selectedCourierId,
  onDeliveryRequestSetUpdated,
  onSegmentClick,
  isMapSelectionActive,
  isAddingManually 
}) {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showSegments, setShowSegments] = useState(true);
  const [mapZoom, setMapZoom] = useState(13);
  const mapContainerRef = useRef(null);

  // Debug : vérifier les données reçues
  useEffect(() => {
    if (mapData) {
      console.log('[MapViewer] Données reçues:', {
        nodes: mapData.nodes?.length || 0,
        segments: mapData.segments?.length || 0
      });
    }
  }, [mapData]);

  // Extraire la liste des demandes de livraison
  const deliveries = deliveryRequestSet?.demands || [];
  


  // Gérer le plein écran
  const toggleFullscreen = () => {
    if (!isFullscreen) {
      // Entrer en plein écran
      if (mapContainerRef.current.requestFullscreen) {
        mapContainerRef.current.requestFullscreen();
      } else if (mapContainerRef.current.webkitRequestFullscreen) {
        mapContainerRef.current.webkitRequestFullscreen();
      } else if (mapContainerRef.current.msRequestFullscreen) {
        mapContainerRef.current.msRequestFullscreen();
      }
    } else {
      // Sortir du plein écran
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  };

  // Écouter les changements d'état du plein écran
  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('msfullscreenchange', handleFullscreenChange);

    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
      document.removeEventListener('webkitfullscreenchange', handleFullscreenChange);
      document.removeEventListener('msfullscreenchange', handleFullscreenChange);
    };
  }, []);
  
  // Calculer et mémoriser le centre de la carte basé sur les nœuds
  const mapCenter = React.useMemo(() => {
    if (!mapData || !mapData.nodes || mapData.nodes.length === 0) {
      console.log('[MapViewer] Pas de nœuds, utilisation du centre par défaut');
      return [45.75, 4.85];
    }
    
    try {
      // Filtrer les nœuds avec des coordonnées valides
      const validNodes = mapData.nodes.filter(node => 
        node && 
        typeof node.latitude === 'number' && 
        typeof node.longitude === 'number' &&
        !isNaN(node.latitude) && 
        !isNaN(node.longitude)
      );
      
      if (validNodes.length === 0) {
        console.warn('[MapViewer] Aucun nœud valide trouvé');
        return [45.75, 4.85];
      }
      
      const avgLat = validNodes.reduce((sum, node) => sum + node.latitude, 0) / validNodes.length;
      const avgLng = validNodes.reduce((sum, node) => sum + node.longitude, 0) / validNodes.length;
      
      console.log('[MapViewer] Centre calculé:', [avgLat, avgLng]);
      return [avgLat, avgLng];
    } catch (error) {
      console.error('[MapViewer] Erreur calcul centre:', error);
      return [45.75, 4.85];
    }
  }, [mapData?.nodes]);

  // Créer un index des nœuds par ID pour un accès rapide
  const nodesById = React.useMemo(() => {
    if (!mapData?.nodes) return {};
    
    try {
      const index = mapData.nodes.reduce((acc, node) => {
        if (node && node.id) {
          acc[node.id] = node;
        }
        return acc;
      }, {});
      
      console.log('[MapViewer] Index nœuds créé:', Object.keys(index).length, 'nœuds');
      return index;
    } catch (error) {
      console.error('[MapViewer] Erreur création index:', error);
      return {};
    }
  }, [mapData?.nodes]);

  // Protection : si pas de données de carte, afficher un message
  if (!mapData || !mapData.nodes || mapData.nodes.length === 0) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-700">
        <div className="text-center text-gray-300">
          <p className="text-xl mb-2 flex items-center justify-center gap-2">
            <Icon name="location" />
            Aucune carte chargée
          </p>
          <p className="text-sm">Chargez un fichier XML pour commencer</p>
        </div>
      </div>
    );
  }

  return (
    <div ref={mapContainerRef} className="flex-1 flex flex-col bg-gray-700">
      {/* Bannière de mode sélection */}
      {isMapSelectionActive && (
        <div className="bg-green-600 text-white p-3 text-center font-semibold animate-pulse">
          <span className="flex items-center justify-center gap-2">
            <Icon name="location" />
            Mode sélection actif - Cliquez sur un segment de la carte pour sélectionner un nœud
          </span>
        </div>
      )}
      

      
      <div className="p-3 bg-gray-600 border-b border-gray-500">
        <div className="flex justify-between items-center">
          <div>
            <h3 className="text-sm font-semibold">
              {mapData.nodes?.length || 0} intersections, {mapData.segments?.length || 0} tronçons
            </h3>
            {tourData && tourData.metrics && (
              <p className="text-xs text-green-400 mt-1">
                <span className="inline-flex items-center gap-1">
                  <Icon name="bike" className="text-green-400" />
                  Tournée: {tourData.metrics.stopCount} stops, {tourData.metrics.totalDistance.toFixed(2)} m
                </span>
              </p>
            )}
            {isMapSelectionActive && (
              <p className="text-xs text-green-300 mt-1 font-semibold">
                <span className="inline-flex items-center gap-1">
                  <Icon name="sparkles" className="text-green-300" />
                  Cliquez sur un segment vert pour le sélectionner
                </span>
              </p>
            )}
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setShowSegments(!showSegments)}
              className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded text-sm transition-colors flex items-center gap-1"
              title={showSegments ? "Masquer les segments" : "Afficher les segments"}
            >
              {showSegments ? <Eye size={16} /> : <EyeOff size={16} />}
              {showSegments ? "Segments" : "Segments"}
            </button>
            <button
              onClick={toggleFullscreen}
              className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors flex items-center gap-1"
              title={isFullscreen ? "Quitter le plein écran" : "Plein écran"}
            >
              {isFullscreen ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
              {isFullscreen ? "Réduire" : "Plein écran"}
            </button>
            <button
              onClick={onClearMap}
              disabled={isAddingManually}
              className="bg-red-600 hover:bg-red-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-3 py-1 rounded text-sm transition-colors"
              title={isAddingManually ? "Terminez d'abord l'ajout en cours" : "Nouvelle carte"}
            >
              Nouvelle carte
            </button>
          </div>
        </div>
      </div>
      
      <div className="flex-1 relative">
        <MapContainer
          key={`map-${mapData?.nodes?.length || 0}`}
          center={mapCenter}
          zoom={13}
          style={{ height: '100%', width: '100%', position: 'absolute', top: 0, left: 0 }}
          scrollWheelZoom={true}
          preferCanvas={true}
        >
          <MapResizer center={mapCenter} />
          <ZoomWatcher onZoomChange={setMapZoom} />
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          
          {/* Affichage des tronçons (segments/rues) avec optimisation */}
          {showSegments && mapData.segments && (
            <SegmentRenderer 
              segments={mapData.segments}
              nodesById={nodesById}
              isMapSelectionActive={isMapSelectionActive}
              onSegmentClick={onSegmentClick}
            />
          )}

          {/* Affichage des demandes de livraison */}
          {deliveryRequestSet && (
            <DeliveryMarkers 
              requestSet={deliveryRequestSet} 
              nodesById={nodesById}
              mapZoom={mapZoom}
            />
          )}

          {/* Affichage de la tournée ou des tournées multi-coursiers */}
          {tourData && (
            Array.isArray(tourData) && tourData.length > 1 ? (
              // Multi-tours avec couleurs
              <MultiTourPolylines 
                tours={tourData}
                nodesById={nodesById}
                selectedCourierId={selectedCourierId}
              />
            ) : (
              // Single tour (ancien format)
              <TourSegments 
                tourData={Array.isArray(tourData) ? { tour: tourData[0].trajets, metrics: { stopCount: tourData[0].stops?.length || 0, totalDistance: tourData[0].totalDistance || 0 }} : tourData}
                nodesById={nodesById}
                mapZoom={mapZoom}
              />
            )
          )}
        </MapContainer>
      </div>
    </div>
  );
}

import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Polyline, Popup, useMap } from 'react-leaflet';
import { Maximize2, Minimize2 } from 'lucide-react';
import DeliveryMarkers from './DeliveryMarkers';
import TourSegments from './TourSegments';
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

/**
 * Composant pour afficher la carte avec Leaflet
 */

export default function MapViewer({ 
  mapData, 
  onClearMap, 
  deliveryRequestSet, 
  tourData, 
  onDeliveryRequestSetUpdated,
  onSegmentClick,
  isMapSelectionActive,
  isAddingManually 
}) {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentTour, setCurrentTour] = useState(null);
  const mapContainerRef = useRef(null);

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
      return [45.75, 4.85];
    }
    
    const avgLat = mapData.nodes.reduce((sum, node) => sum + node.latitude, 0) / mapData.nodes.length;
    const avgLng = mapData.nodes.reduce((sum, node) => sum + node.longitude, 0) / mapData.nodes.length;
    
    return [avgLat, avgLng];
  }, [mapData?.nodes]);

  // Créer un index des nœuds par ID pour un accès rapide
  const nodesById = React.useMemo(() => {
    if (!mapData?.nodes) return {};
    return mapData.nodes.reduce((acc, node) => {
      acc[node.id] = node;
      return acc;
    }, {});
  }, [mapData?.nodes]);

  return (
    <div ref={mapContainerRef} className="flex-1 flex flex-col bg-gray-700">
      {/* Bannière de mode sélection */}
      {isMapSelectionActive && (
        <div className="bg-green-600 text-white p-3 text-center font-semibold animate-pulse">
          📍 Mode sélection actif - Cliquez sur un segment de la carte pour sélectionner un nœud
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
                🚴 Tournée: {tourData.metrics.stopCount} stops, {tourData.metrics.totalDistance.toFixed(2)} m
              </p>
            )}
            {isMapSelectionActive && (
              <p className="text-xs text-green-300 mt-1 font-semibold">
                ✨ Cliquez sur un segment vert pour le sélectionner
              </p>
            )}
          </div>
          <div className="flex gap-2">
            <button
              onClick={toggleFullscreen}
              className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors flex items-center gap-1"
              title={isFullscreen ? "Quitter le plein écran" : "Plein écran"}
            >
              {isFullscreen ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
              {isFullscreen ? "Réduire" : "Plein écran"}
            </button>
            <div className={isAddingManually ? 'pointer-events-none opacity-50' : ''}>
              <ModifyTourButton 
                tourData={currentTour}
                mapData={mapData}
                deliveries={deliveries}
                warehouse={deliveryRequestSet?.warehouse}
                onTourUpdated={setCurrentTour}
                onDeliveryRequestSetUpdated={onDeliveryRequestSetUpdated}
              />
            </div>
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
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          
          {/* Affichage des tronçons (segments/rues) */}
          {mapData.segments && mapData.segments.map((segment, index) => {
            const originNode = nodesById[segment.origin];
            const destinationNode = nodesById[segment.destination];
            
            // Si les deux nœuds existent, dessiner le tronçon
            if (originNode && destinationNode) {
              const handleSegmentClick = () => {
                if (isMapSelectionActive && onSegmentClick) {
                  // Calculer le nœud le plus proche du centre du segment
                  // Pour simplifier, on prend le nœud d'origine
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

          {/* Affichage des demandes de livraison */}
          {deliveryRequestSet && (
            <DeliveryMarkers 
              requestSet={deliveryRequestSet} 
              nodesById={nodesById}
            />
          )}

          {/* Affichage de la tournée calculée (segments jaunes numérotés) */}
          {tourData && (
            <TourSegments 
              tourData={tourData}
              nodesById={nodesById}
            />
          )}
        </MapContainer>
      </div>
    </div>
  );
}

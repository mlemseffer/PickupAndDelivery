import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Polyline, Popup, useMap } from 'react-leaflet';
import { Maximize2, Minimize2 } from 'lucide-react';
import DeliveryMarkers from './DeliveryMarkers';
import ModifyTourButton from './ModifyTourButton';
import 'leaflet/dist/leaflet.css';

/**
 * Composant pour forcer le redimensionnement de la carte
 */
function MapResizer() {
  const map = useMap();
  
  useEffect(() => {
    // Forcer le redimensionnement après un court délai
    const timer = setTimeout(() => {
      map.invalidateSize();
    }, 100);
    
    return () => clearTimeout(timer);
  }, [map]);
  
  return null;
}

/**
 * Composant pour afficher la carte avec Leaflet
 */
export default function MapViewer({ mapData, onClearMap, deliveryRequestSet, onDeliveryRequestSetUpdated }) {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentTour, setCurrentTour] = useState(null);
  const mapContainerRef = useRef(null);

  // Extraire la liste des demandes de livraison
  const deliveries = deliveryRequestSet?.demands || [];
  
  // Debug logs
  console.log('MapViewer reçoit deliveryRequestSet:', deliveryRequestSet);
  console.log('MapViewer deliveries:', deliveries);

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
  
  // Calculer le centre de la carte basé sur les nœuds
  const getMapCenter = () => {
    if (!mapData || !mapData.nodes || mapData.nodes.length === 0) {
      return [45.75, 4.85];
    }
    
    const avgLat = mapData.nodes.reduce((sum, node) => sum + node.latitude, 0) / mapData.nodes.length;
    const avgLng = mapData.nodes.reduce((sum, node) => sum + node.longitude, 0) / mapData.nodes.length;
    
    return [avgLat, avgLng];
  };

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
      <div className="p-3 bg-gray-600 border-b border-gray-500">
        <div className="flex justify-between items-center">
          <h3 className="text-sm font-semibold">
            {mapData.nodes?.length || 0} intersections, {mapData.segments?.length || 0} tronçons
          </h3>
          <div className="flex gap-2">
            <button
              onClick={toggleFullscreen}
              className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors flex items-center gap-1"
              title={isFullscreen ? "Quitter le plein écran" : "Plein écran"}
            >
              {isFullscreen ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
              {isFullscreen ? "Réduire" : "Plein écran"}
            </button>
            <ModifyTourButton 
              tourData={currentTour}
              mapData={mapData}
              deliveries={deliveries}
              onTourUpdated={setCurrentTour}
              onDeliveryRequestSetUpdated={onDeliveryRequestSetUpdated}
            />
            <button
              onClick={onClearMap}
              className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm transition-colors"
            >
              Nouvelle carte
            </button>
          </div>
        </div>
      </div>
      
      <div className="flex-1 relative">
        <MapContainer
          key={`map-${mapData?.nodes?.length || 0}`}
          center={getMapCenter()}
          zoom={13}
          style={{ height: '100%', width: '100%', position: 'absolute', top: 0, left: 0 }}
          scrollWheelZoom={true}
          whenReady={() => {
            // Force un redimensionnement quand la carte est prête
            setTimeout(() => {
              window.dispatchEvent(new Event('resize'));
            }, 100);
          }}
        >
          <MapResizer />
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
              return (
                <Polyline
                  key={`segment-${index}`}
                  positions={[
                    [originNode.latitude, originNode.longitude],
                    [destinationNode.latitude, destinationNode.longitude]
                  ]}
                  color="#3b82f6"
                  weight={3}
                  opacity={0.7}
                >
                  <Popup>
                    <div>
                      <strong>Rue:</strong> {segment.name}<br />
                      <strong>Longueur:</strong> {segment.length.toFixed(2)} m<br />
                      <strong>De:</strong> {segment.origin}<br />
                      <strong>À:</strong> {segment.destination}
                    </div>
                  </Popup>
                </Polyline>
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
        </MapContainer>
      </div>
    </div>
  );
}

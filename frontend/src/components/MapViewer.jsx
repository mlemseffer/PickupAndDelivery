import React, { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Polyline, Popup, useMap } from 'react-leaflet';
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
export default function MapViewer({ mapData, onClearMap }) {
  
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
    <>
      <div className="p-3 bg-gray-600 border-b border-gray-500">
        <div className="flex justify-between items-center">
          <h3 className="text-sm font-semibold">
            {mapData.nodes?.length || 0} intersections, {mapData.segments?.length || 0} tronçons
          </h3>
          <button
            onClick={onClearMap}
            className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm transition-colors"
          >
            Nouvelle carte
          </button>
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
        </MapContainer>
      </div>
    </>
  );
}

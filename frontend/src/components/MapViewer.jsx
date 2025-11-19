import React from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

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

  return (
    <>
      <div className="p-3 bg-gray-600 border-b border-gray-500">
        <div className="flex justify-between items-center">
          <h3 className="text-sm font-semibold">
            {mapData.nodes?.length || 0} nœuds, {mapData.segments?.length || 0} segments
          </h3>
          <button
            onClick={onClearMap}
            className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm transition-colors"
          >
            Nouvelle carte
          </button>
        </div>
      </div>
      
      <div className="flex-1">
        <MapContainer
          center={getMapCenter()}
          zoom={13}
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          
          {mapData.nodes && mapData.nodes.map((node) => (
            <CircleMarker
              key={node.id}
              center={[node.latitude, node.longitude]}
              radius={4}
              fillColor="#fbbf24"
              color="#f59e0b"
              weight={1}
              opacity={0.8}
              fillOpacity={0.6}
            >
              <Popup>
                <div>
                  <strong>Nœud ID:</strong> {node.id}<br />
                  <strong>Latitude:</strong> {node.latitude}<br />
                  <strong>Longitude:</strong> {node.longitude}
                </div>
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>
    </>
  );
}

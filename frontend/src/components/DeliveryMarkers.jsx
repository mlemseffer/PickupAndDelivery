import React from 'react';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';

/**
 * Icône SVG pour l'entrepôt (warehouse) - Logo gris
 */
const warehouseIcon = new L.DivIcon({
  html: `
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" fill="#4A5568" stroke="#000" stroke-width="1.5"/>
      <path d="M9 22V12h6v10" fill="#718096" stroke="#000" stroke-width="1.5"/>
      <rect x="6" y="6" width="3" height="2" fill="#2D3748"/>
      <rect x="15" y="6" width="3" height="2" fill="#2D3748"/>
      <circle cx="12" cy="16" r="1.5" fill="#2D3748"/>
    </svg>
  `,
  className: 'warehouse-icon',
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40]
});

/**
 * Créer une icône SVG pour le pickup (paquet/package) avec couleur
 */
const createPickupIcon = (color) => new L.DivIcon({
  html: `
    <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" 
            fill="${color}" stroke="#000" stroke-width="1.8"/>
      <polyline points="3.27 6.96 12 12.01 20.73 6.96" stroke="#000" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
      <line x1="12" y1="22.08" x2="12" y2="12" stroke="#000" stroke-width="1.8" stroke-linecap="round"/>
    </svg>
  `,
  className: 'pickup-icon',
  iconSize: [36, 36],
  iconAnchor: [18, 36],
  popupAnchor: [0, -36]
});

/**
 * Créer une icône SVG pour la delivery (pin de localisation) avec couleur
 */
const createDeliveryIcon = (color) => new L.DivIcon({
  html: `
    <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" 
            fill="${color}" stroke="#000" stroke-width="1.8"/>
      <circle cx="12" cy="10" r="3" fill="#fff" stroke="#000" stroke-width="1.5"/>
    </svg>
  `,
  className: 'delivery-icon',
  iconSize: [36, 36],
  iconAnchor: [18, 36],
  popupAnchor: [0, -36]
});

/**
 * Composant pour afficher les marqueurs de demandes de livraison sur la carte
 */
export default function DeliveryMarkers({ requestSet, nodesById }) {
  if (!requestSet || !requestSet.warehouse || !requestSet.demands) {
    console.warn('DeliveryMarkers: requestSet incomplet', { requestSet, nodesById });
    return null;
  }

  const { warehouse, demands } = requestSet;


  // Récupérer le nœud de l'entrepôt
  const warehouseNode = nodesById[warehouse.nodeId];

  return (
    <>
      {/* Marqueur de l'entrepôt - Logo gris */}
      {warehouseNode && (
        <Marker
          position={[warehouseNode.latitude, warehouseNode.longitude]}
          icon={warehouseIcon}
        >
          <Popup>
            <div style={{ color: '#1a202c' }}>
              <strong className="text-lg">🏢 Entrepôt</strong><br />
              <strong style={{ color: '#ffffff' }}>Heure de départ:</strong> <strong style={{ color: '#ffffff' }}>{warehouse.departureTime}</strong><br />
              <strong style={{ color: '#ffffff' }}>Nœud:</strong> <strong style={{ color: '#ffffff' }}>{warehouse.nodeId}</strong>
            </div>
          </Popup>
        </Marker>
      )}

      {/* Marqueurs des demandes - Logos colorés */}
      {demands.map((demand, index) => {
        const pickupNode = nodesById[demand.pickupNodeId];
        const deliveryNode = nodesById[demand.deliveryNodeId];
        const color = demand.color || '#FF6B6B';

        return (
          <React.Fragment key={demand.id || index}>
            {/* Marqueur Pickup - Logo paquet coloré */}
            {pickupNode && (
              <Marker
                position={[pickupNode.latitude, pickupNode.longitude]}
                icon={createPickupIcon(color)}
              >
                <Popup>
                  <div style={{ color: '#1a202c' }}>
                    <strong className="text-lg" style={{ color }}>
                      📦 Pickup #{index + 1}
                    </strong><br />
                    <strong style={{ color: '#ffffff' }}>Durée:</strong> <strong style={{ color: '#ffffff' }}>{demand.pickupDurationSec} sec</strong><br />
                    <strong style={{ color: '#ffffff' }}>Nœud:</strong> <strong style={{ color: '#ffffff' }}>{demand.pickupNodeId}</strong>
                  </div>
                </Popup>
              </Marker>
            )}

            {/* Marqueur Delivery - Logo pin coloré */}
            {deliveryNode && (
              <Marker
                position={[deliveryNode.latitude, deliveryNode.longitude]}
                icon={createDeliveryIcon(color)}
              >
                <Popup>
                  <div style={{ color: '#1a202c' }}>
                    <strong className="text-lg" style={{ color }}>
                      📍 Delivery #{index + 1}
                    </strong><br />
                    <strong style={{ color: '#ffffff' }}>Durée:</strong> <strong style={{ color: '#ffffff' }}>{demand.deliveryDurationSec} sec</strong><br />
                    <strong style={{ color: '#ffffff' }}>Nœud:</strong> <strong style={{ color: '#ffffff' }}>{demand.deliveryNodeId}</strong>
                  </div>
                </Popup>
              </Marker>
            )}
          </React.Fragment>
        );
      })}
    </>
  );
}

import React from 'react';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';

const computeScaleFromZoom = (zoom) => {
  const baseZoom = 13;
  const step = 0.12;
  const minScale = 0.6;
  const maxScale = 1.8;

  if (typeof zoom !== 'number') {
    return 1;
  }

  return Math.min(maxScale, Math.max(minScale, 1 + (zoom - baseZoom) * step));
};

/**
 * Icône SVG pour l'entrepôt (warehouse) - Logo gris
 */
const createWarehouseIcon = (scale) => {
  const size = Math.round(40 * scale);
  const anchorX = size / 2;
  const anchorY = size;
  const popupOffsetY = -size;

  return new L.DivIcon({
    html: `
      <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" fill="#4A5568" stroke="#000" stroke-width="1.5"/>
        <path d="M9 22V12h6v10" fill="#718096" stroke="#000" stroke-width="1.5"/>
        <rect x="6" y="6" width="3" height="2" fill="#2D3748"/>
        <rect x="15" y="6" width="3" height="2" fill="#2D3748"/>
        <circle cx="12" cy="16" r="1.5" fill="#2D3748"/>
      </svg>
    `,
    className: 'warehouse-icon',
    iconSize: [size, size],
    iconAnchor: [anchorX, anchorY],
    popupAnchor: [0, popupOffsetY]
  });
};

/**
 * Créer une icône SVG pour le pickup (paquet/package) avec couleur
 */
const createPickupIcon = (color, scale) => {
  const size = Math.round(36 * scale);
  const anchorX = size / 2;
  const anchorY = size;
  const popupOffsetY = -size;

  return new L.DivIcon({
    html: `
      <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" 
              fill="${color}" stroke="#000" stroke-width="1.8"/>
        <polyline points="3.27 6.96 12 12.01 20.73 6.96" stroke="#000" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        <line x1="12" y1="22.08" x2="12" y2="12" stroke="#000" stroke-width="1.8" stroke-linecap="round"/>
      </svg>
    `,
    className: 'pickup-icon',
    iconSize: [size, size],
    iconAnchor: [anchorX, anchorY],
    popupAnchor: [0, popupOffsetY]
  });
};

/**
 * Créer une icône SVG pour la delivery (pin de localisation) avec couleur
 */
const createDeliveryIcon = (color, scale) => {
  const size = Math.round(36 * scale);
  const anchorX = size / 2;
  const anchorY = size;
  const popupOffsetY = -size;

  return new L.DivIcon({
    html: `
      <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" 
              fill="${color}" stroke="#000" stroke-width="1.8"/>
        <circle cx="12" cy="10" r="3" fill="#fff" stroke="#000" stroke-width="1.5"/>
      </svg>
    `,
    className: 'delivery-icon',
    iconSize: [size, size],
    iconAnchor: [anchorX, anchorY],
    popupAnchor: [0, popupOffsetY]
  });
};

/**
 * Composant pour afficher les marqueurs de demandes de livraison sur la carte
 */
export default function DeliveryMarkers({ requestSet, nodesById, mapZoom = 13 }) {
  if (!requestSet || !requestSet.warehouse || !requestSet.demands) {
    console.warn('DeliveryMarkers: requestSet incomplet', { requestSet, nodesById });
    return null;
  }

  const { warehouse, demands } = requestSet;
  const iconScale = computeScaleFromZoom(mapZoom);
  const warehouseIcon = React.useMemo(() => createWarehouseIcon(iconScale), [iconScale]);


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
                icon={createPickupIcon(color, iconScale)}
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
                icon={createDeliveryIcon(color, iconScale)}
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

import React from 'react';
import { Polyline, Popup, CircleMarker } from 'react-leaflet';
import L from 'leaflet';
import Icon from './Icon';

/**
 * Composant pour afficher la tournée calculée sur la carte
 * Dessine les trajets avec une couleur distinctive et affiche les numéros d'ordre
 */
export default function TourPolylines({ tourData, nodesById }) {
  if (!tourData || !tourData.tour || !tourData.tour.length) {
    return null;
  }

  const tour = tourData.tour;

  // Créer un mapping pour l'ordre des stops
  const stopOrder = new Map();
  let orderIndex = 0;

  tour.forEach((trajet) => {
    // Le premier segment de chaque trajet donne le point de départ
    if (trajet.listeSegment && trajet.listeSegment.length > 0) {
      const firstSegment = trajet.listeSegment[0];
      const originNodeId = firstSegment.origine.id;
      
      if (!stopOrder.has(originNodeId)) {
        stopOrder.set(originNodeId, orderIndex++);
      }

      // Le dernier segment donne le point d'arrivée
      const lastSegment = trajet.listeSegment[trajet.listeSegment.length - 1];
      const destNodeId = lastSegment.destination.id;
      
      if (!stopOrder.has(destNodeId)) {
        stopOrder.set(destNodeId, orderIndex++);
      }
    }
  });

  return (
    <>
      {/* Dessiner les trajets de la tournée */}
      {tour.map((trajet, trajetIndex) => {
        if (!trajet.listeSegment || trajet.listeSegment.length === 0) {
          return null;
        }

        // Construire les coordonnées du trajet
        const coordinates = [];
        trajet.listeSegment.forEach((segment) => {
          coordinates.push([segment.origine.latitude, segment.origine.longitude]);
          // Ajouter la destination du dernier segment
          if (segment === trajet.listeSegment[trajet.listeSegment.length - 1]) {
            coordinates.push([segment.destination.latitude, segment.destination.longitude]);
          }
        });

        return (
          <Polyline
            key={`tour-trajet-${trajetIndex}`}
            positions={coordinates}
            color="#FF6B35"
            weight={5}
            opacity={0.8}
          >
            <Popup>
              <div>
                <strong className="inline-flex items-center gap-2">
                  <Icon name="bike" />
                  Trajet {trajetIndex + 1}
                </strong><br />
                <strong>Distance:</strong> {trajet.longueurTotale.toFixed(2)} m<br />
                <strong>Segments:</strong> {trajet.listeSegment.length}
              </div>
            </Popup>
          </Polyline>
        );
      })}

      {/* Afficher les numéros d'ordre sur les stops */}
      {Array.from(stopOrder.entries()).map(([nodeId, order]) => {
        const node = nodesById[nodeId];
        if (!node) return null;

        // Ne pas afficher de numéro pour l'entrepôt (ordre 0 et dernier)
        const isWarehouse = order === 0 || order === stopOrder.size - 1;
        if (isWarehouse) return null;

        return (
          <CircleMarker
            key={`tour-order-${nodeId}`}
            center={[node.latitude, node.longitude]}
            radius={15}
            fillColor="#FF6B35"
            color="#FFFFFF"
            weight={2}
            fillOpacity={0.9}
          >
            <Popup>
              <div>
                <strong className="inline-flex items-center gap-2">
                  <Icon name="bullseye" />
                  Étape {order}
                </strong><br />
                <strong>Nœud:</strong> {nodeId}
              </div>
            </Popup>
            {/* Ajouter un texte avec le numéro */}
            <text
              x="0"
              y="0"
              textAnchor="middle"
              dominantBaseline="central"
              fill="white"
              fontSize="12"
              fontWeight="bold"
            >
              {order}
            </text>
          </CircleMarker>
        );
      })}

      {/* Afficher un marqueur spécial pour l'entrepôt */}
      {stopOrder.size > 0 && Array.from(stopOrder.entries())[0] && (() => {
        const [warehouseNodeId] = Array.from(stopOrder.entries())[0];
        const warehouseNode = nodesById[warehouseNodeId];
        if (!warehouseNode) return null;

        return (
          <CircleMarker
            key="tour-warehouse"
            center={[warehouseNode.latitude, warehouseNode.longitude]}
            radius={20}
            fillColor="#10B981"
            color="#FFFFFF"
            weight={3}
            fillOpacity={0.9}
          >
            <Popup>
              <div>
                <strong className="inline-flex items-center gap-2">
                  <Icon name="warehouse" />
                  Entrepôt
                </strong><br />
                <strong>Nœud:</strong> {warehouseNodeId}<br />
                <em>Point de départ et d'arrivée</em>
              </div>
            </Popup>
          </CircleMarker>
        );
      })()}
    </>
  );
}

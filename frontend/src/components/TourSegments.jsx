import React from 'react';
import { Polyline, Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import Icon from './Icon';

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
 * Composant pour afficher les segments de la tournée en jaune avec numérotation et flèches
 */
export default function TourSegments({ tourData, nodesById, mapZoom = 13 }) {

  
  if (!tourData || !tourData.tour || tourData.tour.length === 0) {
    console.warn('[TourSegments] Pas de données de tournée');
    return null;
  }
  
  const iconScale = computeScaleFromZoom(mapZoom);

  // Aplatir tous les segments de tous les trajets
  let segmentCounter = 0;
  const allSegmentsWithNumbers = [];
  
  tourData.tour.forEach((trajet, trajetIndex) => {
    if (!trajet.segments || trajet.segments.length === 0) {
      console.warn(`[TourSegments] Trajet ${trajetIndex + 1} n'a pas de segments`);
      return;
    }
    
    trajet.segments.forEach((segment) => {
      segmentCounter++;
      allSegmentsWithNumbers.push({
        segment,
        number: segmentCounter,
        trajetIndex
      });
    });
  });

  // Créer une icône de flèche personnalisée (petite et simple)
  const createArrowIcon = (rotation) => {
    const borderWidth = 5 * iconScale;
    const borderHeight = 10 * iconScale;
    const iconWidth = borderWidth * 2;
    const iconHeight = borderHeight;

    return L.divIcon({
      html: `
        <div style="
          width: 0;
          height: 0;
          border-left: ${borderWidth}px solid transparent;
          border-right: ${borderWidth}px solid transparent;
          border-bottom: ${borderHeight}px solid #FCD34D;
          transform: rotate(${rotation}deg);
          filter: drop-shadow(0 0 1px rgba(0,0,0,0.5));
        "></div>
      `,
      className: 'arrow-icon',
      iconSize: [iconWidth, iconHeight],
      iconAnchor: [iconWidth / 2, iconHeight / 2]
    });
  };

  // Calculer l'angle de rotation pour la flèche
  const calculateRotation = (lat1, lng1, lat2, lng2) => {
    const dLng = lng2 - lng1;
    const dLat = lat2 - lat1;
    const angle = Math.atan2(dLng, dLat) * (180 / Math.PI);
    return angle;
  };

  return (
    <>
      {allSegmentsWithNumbers.map(({ segment, number, trajetIndex }) => {
        const originNode = nodesById[segment.origin];
        const destinationNode = nodesById[segment.destination];

        if (!originNode || !destinationNode) {
          console.warn(`Nœuds manquants pour le segment ${number}: ${segment.origin} -> ${segment.destination}`);
          return null;
        }

        const positions = [
          [originNode.latitude, originNode.longitude],
          [destinationNode.latitude, destinationNode.longitude]
        ];

        // Calculer le point milieu pour placer le numéro
        const midLat = (originNode.latitude + destinationNode.latitude) / 2;
        const midLng = (originNode.longitude + destinationNode.longitude) / 2;

        return (
          <React.Fragment key={`tour-segment-${trajetIndex}-${number}`}>
            {/* Ligne jaune pour le segment avec tooltip au clic */}
            <Polyline
              positions={positions}
              color="#FCD34D"
              weight={6}
              opacity={0.9}
              dashArray="0"
              eventHandlers={{
                click: (e) => {
                  e.target.openPopup();
                }
              }}
            >
              <Tooltip direction="center" offset={[0, 0]}>
                <div className="text-sm">
                  <strong><Icon name="number" className="mr-1" />Segment {number}</strong><br />
                  <strong><Icon name="location" className="mr-1" />Rue:</strong> {segment.name}<br />
                  <strong><Icon name="ruler" className="mr-1" />Longueur:</strong> {segment.length.toFixed(2)} m<br />
                  <strong><Icon name="arrowRight" className="mr-1" />De:</strong> {segment.origin}<br />
                  <strong><Icon name="arrowRight" className="mr-1" />À:</strong> {segment.destination}
                </div>
              </Tooltip>
            </Polyline>

            {/* Flèche au milieu du segment */}
            <Marker
              position={[midLat, midLng]}
              icon={createArrowIcon(
                calculateRotation(
                  originNode.latitude,
                  originNode.longitude,
                  destinationNode.latitude,
                  destinationNode.longitude
                )
              )}
              zIndexOffset={2000}
            >
              <Tooltip direction="top" offset={[0, -8]} permanent={false}>
                <div className="text-sm">
                  <strong><Icon name="number" className="mr-1" />Segment {number}</strong><br />
                  <strong><Icon name="location" className="mr-1" />Rue:</strong> {segment.name}<br />
                  <strong><Icon name="ruler" className="mr-1" />Longueur:</strong> {segment.length.toFixed(2)} m
                </div>
              </Tooltip>
            </Marker>
          </React.Fragment>
        );
      })}
    </>
  );
}

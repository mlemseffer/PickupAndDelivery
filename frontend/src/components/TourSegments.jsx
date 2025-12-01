import React from 'react';
import { Polyline, Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';

/**
 * Composant pour afficher les segments de la tournée en jaune avec numérotation et flèches
 */
export default function TourSegments({ tourData, nodesById }) {
  console.log('🔍 TourSegments - tourData:', tourData);
  console.log('🔍 TourSegments - nodesById keys:', Object.keys(nodesById).length);
  
  if (!tourData || !tourData.tour || tourData.tour.length === 0) {
    console.warn('⚠️ TourSegments: Pas de données de tournée');
    return null;
  }
  
  console.log('✅ TourSegments: Affichage de', tourData.tour.length, 'trajets');
  
  // Aplatir tous les segments de tous les trajets
  let segmentCounter = 0;
  const allSegmentsWithNumbers = [];
  
  tourData.tour.forEach((trajet, trajetIndex) => {
    console.log(`📍 Trajet ${trajetIndex + 1}:`, trajet);
    
    if (!trajet.segments || trajet.segments.length === 0) {
      console.warn(`⚠️ Trajet ${trajetIndex + 1} n'a pas de segments`);
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
  
  console.log(`📊 Total de ${segmentCounter} segments à afficher`);

  // Créer une icône de flèche personnalisée (petite et simple)
  const createArrowIcon = (rotation) => {
    return L.divIcon({
      html: `
        <div style="
          width: 0;
          height: 0;
          border-left: 5px solid transparent;
          border-right: 5px solid transparent;
          border-bottom: 10px solid #FCD34D;
          transform: rotate(${rotation}deg);
          filter: drop-shadow(0 0 1px rgba(0,0,0,0.5));
        "></div>
      `,
      className: 'arrow-icon',
      iconSize: [10, 10],
      iconAnchor: [5, 5]
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
                  <strong>🔢 Segment {number}</strong><br />
                  <strong>📍 Rue:</strong> {segment.name}<br />
                  <strong>📏 Longueur:</strong> {segment.length.toFixed(2)} m<br />
                  <strong>➡️ De:</strong> {segment.origin}<br />
                  <strong>➡️ À:</strong> {segment.destination}
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
                  <strong>🔢 Segment {number}</strong><br />
                  <strong>📍 Rue:</strong> {segment.name}<br />
                  <strong>📏 Longueur:</strong> {segment.length.toFixed(2)} m
                </div>
              </Tooltip>
            </Marker>
          </React.Fragment>
        );
      })}
    </>
  );
}

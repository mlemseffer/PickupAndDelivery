import React from 'react';
import { Polyline } from 'react-leaflet';
import TourSegments from './TourSegments';
import { getCourierColor } from '../utils/courierColors';

/**
 * Composant pour afficher plusieurs tournées sur la carte
 * Chaque tournée a sa propre couleur selon le coursier
 * 
 * @param {Object} props
 * @param {Array} props.tours - Liste des tournées à afficher
 * @param {number} props.selectedCourierId - ID du coursier sélectionné (null = tous)
 * @param {Object} props.nodesById - Index des nœuds par ID
 */
export default function MultiTourPolylines({ tours, selectedCourierId, nodesById }) {
  if (!tours || tours.length === 0) {
    return null;
  }

  // Filtrer les tours à afficher
  const toursToDisplay = selectedCourierId === null
    ? tours  // Afficher tous
    : tours.filter(t => t.courierId === selectedCourierId);

  console.log('🗺️ MultiTourPolylines - Affichage de', toursToDisplay.length, 'tour(s)');

  return (
    <>
      {toursToDisplay.map(tour => (
        <TourSegmentsColored
          key={tour.courierId}
          tourData={tour}
          nodesById={nodesById}
          color={getCourierColor(tour.courierId)}
          opacity={selectedCourierId === null ? 0.7 : 1} // Plus transparent en vue globale
        />
      ))}
    </>
  );
}

/**
 * Version colorée de TourSegments
 * Affiche les segments avec une couleur personnalisée
 */
function TourSegmentsColored({ tourData, nodesById, color, opacity }) {
  if (!tourData || !nodesById) {
    return null;
  }

  // Utiliser tourData.trajets (format backend) si disponible, sinon tourData.tour (ancien format)
  const trajets = tourData.trajets || tourData.tour;

  if (!trajets || trajets.length === 0) {
    return null;
  }

  return (
    <>
      {trajets.map((trajet, trajetIndex) => {
        if (!trajet.segments || trajet.segments.length === 0) {
          return null;
        }

        return trajet.segments.map((segment, segmentIndex) => {
          const originNode = nodesById[segment.origin];
          const destNode = nodesById[segment.destination];

          if (!originNode || !destNode) {
            return null;
          }

          const positions = [
            [originNode.latitude, originNode.longitude],
            [destNode.latitude, destNode.longitude]
          ];

          return (
            <React.Fragment key={`${trajetIndex}-${segmentIndex}`}>
              <Polyline
                positions={positions}
                color={color}
                weight={4}
                opacity={opacity}
              />
            </React.Fragment>
          );
        });
      })}
    </>
  );
}

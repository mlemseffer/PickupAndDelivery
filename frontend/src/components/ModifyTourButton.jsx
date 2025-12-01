import React, { useState } from 'react';
import { Edit2 } from 'lucide-react';
import ModifyTourModal from './ModifyTourModal';
import './ModifyTourButton.css';

/**
 * Bouton pour accéder au menu de modification des tournées
 * Ouvre une modal permettant de :
 * - Ajouter une livraison
 * - Supprimer une livraison
 * - Modifier le coursier assigné
 */
export default function ModifyTourButton({ tourData, mapData, deliveries, onTourUpdated, onDeliveryRequestSetUpdated }) {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <button 
        className="modify-tour-btn"
        onClick={() => setShowModal(true)}
        title="Modifier la tournée de livraison"
      >
        <Edit2 size={18} />
        <span>Modifier la tournée</span>
      </button>

      {showModal && (
        <ModifyTourModal 
          tourData={tourData}
          mapData={mapData}
          deliveries={deliveries}
          onClose={() => setShowModal(false)}
          onTourUpdated={onTourUpdated}
          onDeliveryRequestSetUpdated={onDeliveryRequestSetUpdated}
        />
      )}
    </>
  );
}

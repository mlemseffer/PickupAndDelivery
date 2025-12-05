import React, { useState } from 'react';
import { X, Plus, Trash2, User } from 'lucide-react';
import apiService from '../services/apiService';
import './ModifyTourModal.css';

/**
 * Modal pour modifier une tournée
 * Permet :
 * - Ajouter une livraison
 * - Supprimer une livraison
 * - Changer le coursier assigné
 */
export default function ModifyTourModal({ tourData, mapData, deliveries, warehouse, onClose, onTourUpdated, onDeliveryRequestSetUpdated }) {
  const [activeTab, setActiveTab] = useState('remove'); // Défaut: 'remove'
  
  // État pour la suppression
  const [deliveryToRemove, setDeliveryToRemove] = useState(null);
  
  // État pour le changement de coursier
  const [deliveryToChangeIndex, setDeliveryToChangeIndex] = useState(null);
  const [newCourierAssignment, setNewCourierAssignment] = useState('');
  
  // État général
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // Liste des coursiers (à mettre à jour selon votre logique)
  // Si tourData ne contient pas de couriers, on utilise une liste par défaut
  const couriers = (tourData?.couriers) || ['C001', 'C002', 'C003', 'C004'];
  
  // Demandes de livraison passées en prop
  const deliveriesList = deliveries || [];

  // Supprimer une livraison
  /* const handleRemoveDelivery = async (deliveryIndex) => {
    if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette livraison?')) {
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await apiService.removeDemand(deliveryIndex);

      if (response.data && response.data.success) {
        setSuccess('Livraison supprimée avec succès');
        setTimeout(() => onClose(), 1500);
      } else {
        setError(response.data?.message || 'Erreur lors de la suppression');
      }
    } catch (err) {
      setError('Erreur lors de la suppression: ' + err.message);
    } finally {
      setLoading(false);
    }
  };
  
 */
const handleRemoveDelivery = async (deliveryIndex) => {
  const delivery = deliveriesList[deliveryIndex];
  const deliveryId = delivery?.id;

  if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette livraison ?')) {
    return;
  }

  setLoading(true);
  setError(null);
  setSuccess(null);

  try {
    // 1. Appel API avec l'id string
    const response = await apiService.removeDemand(deliveryId);
    console.log('Réponse de removeDemand:', response);

    if (response.success) {
      setSuccess('Livraison supprimée avec succès');
      
      // 2. Créer localement le nouvel état sans cette livraison
      // (car le backend retourne null, donc on le fait en frontend)
      const updatedDemands = deliveriesList.filter((_, idx) => idx !== deliveryIndex);
      
      const updatedRequestSet = {
        warehouse: warehouse || null,
        demands: updatedDemands
      };
      
      // 3. Appelle le callback pour mettre à jour deliveryRequestSet dans Front.jsx
      if (onDeliveryRequestSetUpdated) {
        onDeliveryRequestSetUpdated(updatedRequestSet);
      }
      
      // Ne pas fermer la modal automatiquement pour permettre d'autres suppressions
      // L'utilisateur peut fermer manuellement quand il a terminé
      setTimeout(() => setSuccess(null), 2000); // Effacer le message de succès après 2s
    } else {
      console.error('Erreur réponse backend:', response);
      setError(response.message || 'Erreur lors de la suppression');
    }
  } catch (err) {
    console.error('Exception removeDemand:', err);
    setError('Erreur lors de la suppression: ' + err.message);
  } finally {
    setLoading(false);
  }
};

  // Changer le coursier assigné
  const handleChangeCourier = async () => {
    if (deliveryToChangeIndex === null || !newCourierAssignment) {
      setError('Veuillez sélectionner un coursier');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await apiService.updateCourierAssignment({
        oldCourierId: tourData?.courierId,
        newCourierId: newCourierAssignment,
        deliveryIndex: deliveryToChangeIndex
      });

      if (response.data.success) {
        setSuccess('Livraison réassignée avec succès');
        onTourUpdated(response.data.data.updatedTour);
        setDeliveryToChangeIndex(null);
        setNewCourierAssignment('');
        
        setTimeout(() => onClose(), 2000);
      } else if (response.data.data.requiresCourierChange) {
        setError(`${response.data.error}\n\nVeuillez sélectionner un autre coursier.`);
      } else {
        setError(response.data.error);
      }
    } catch (err) {
      setError('Erreur lors du changement de coursier: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modify-tour-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Modifier la tournée</h2>
          <button className="close-btn" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <div className="modal-tabs">
          <button 
            className={`tab ${activeTab === 'remove' ? 'active' : ''}`}
            onClick={() => setActiveTab('remove')}
          >
            <Trash2 size={18} /> Supprimer
          </button>
          <button 
            className={`tab ${activeTab === 'changeCourier' ? 'active' : ''}`}
            onClick={() => setActiveTab('changeCourier')}
          >
            <User size={18} /> Changer coursier
          </button>
        </div>

        <div className="modal-content">
          {activeTab === 'remove' && (
            <div className="tab-content">
              <p className="info-text">Sélectionnez une livraison à supprimer:</p>
              {deliveriesList && deliveriesList.length > 0 ? (
                <div className="deliveries-list">
                  {deliveriesList.map((delivery, idx) => (
                    <div key={idx} className="delivery-item" style={{borderLeftColor: delivery.color}}>
                      <div className="delivery-info">
                        <p><strong>Pickup:</strong> {delivery.pickupNodeId}</p>
                        <p><strong>Livraison:</strong> {delivery.deliveryNodeId}</p>
                        <p><strong>Durée pickup:</strong> {delivery.pickupDurationSec}s</p>
                        <p><strong>Durée livraison:</strong> {delivery.deliveryDurationSec}s</p>
                      </div>
                      <button 
                        className="btn btn-danger btn-small"
                        onClick={() => handleRemoveDelivery(idx)}
                        disabled={loading}
                      >
                        Supprimer
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="no-data">Aucune livraison à supprimer</p>
              )}
            </div>
          )}

          {/* TAB: Changer de coursier */}
          {activeTab === 'changeCourier' && (
            <div className="tab-content">
              <p className="info-text">Sélectionnez une livraison à réassigner:</p>
              {deliveriesList && deliveriesList.length > 0 ? (
                <div className="deliveries-list">
                  {deliveriesList.map((delivery, idx) => (
                    <div key={idx} className="delivery-item" style={{borderLeftColor: delivery.color}}>
                      <div className="delivery-info">
                        <p><strong>Pickup:</strong> {delivery.pickupNodeId}</p>
                        <p><strong>Livraison:</strong> {delivery.deliveryNodeId}</p>
                      </div>
                      {deliveryToChangeIndex === idx ? (
                        <div className="courier-selector">
                          <select 
                            value={newCourierAssignment} 
                            onChange={(e) => setNewCourierAssignment(e.target.value)}
                            disabled={loading}
                          >
                            <option value="">-- Choisir un coursier --</option>
                            {couriers.map(courier => (
                              <option key={courier} value={courier}>{courier}</option>
                            ))}
                          </select>
                          <button 
                            className="btn btn-success btn-small"
                            onClick={handleChangeCourier}
                            disabled={loading || !newCourierAssignment}
                          >
                            Confirmer
                          </button>
                          <button 
                            className="btn btn-secondary btn-small"
                            onClick={() => {
                              setDeliveryToChangeIndex(null);
                              setNewCourierAssignment('');
                            }}
                            disabled={loading}
                          >
                            Annuler
                          </button>
                        </div>
                      ) : (
                        <button 
                          className="btn btn-info btn-small"
                          onClick={() => setDeliveryToChangeIndex(idx)}
                          disabled={loading}
                        >
                          Réassigner
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="no-data">Aucune livraison dans cette tournée</p>
              )}
            </div>
          )}
        </div>

        {/* Messages de succès/erreur en bas */}
        {error && (
          <div className="alert alert-error alert-bottom">
            {error}
          </div>
        )}

        {success && (
          <div className="alert alert-success alert-bottom">
            {success}
          </div>
        )}
      </div>
    </div>
  );
}

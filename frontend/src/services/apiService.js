// Configuration de l'API Backend
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Service pour interagir avec l'API Backend
 * Toutes les requêtes HTTP passent par ce service
 */
class ApiService {
  
  /**
   * Upload un fichier carte XML vers le backend
   * @param {File} file - Le fichier XML à uploader
   * @returns {Promise} La réponse de l'API
   */
  async uploadMap(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/maps/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Erreur lors de l\'upload de la carte');
    }

    return response.json();
  }

  /**
   * Récupère la carte actuellement chargée
   * @returns {Promise} La carte avec tous ses nœuds et segments
   */
  async getCurrentMap() {
    const response = await fetch(`${API_BASE_URL}/maps/current`);

    if (!response.ok) {
      throw new Error('Aucune carte n\'est chargée');
    }

    return response.json();
  }

  /**
   * Vérifie si une carte est chargée
   * @returns {Promise} Le statut de la carte
   */
  async getMapStatus() {
    const response = await fetch(`${API_BASE_URL}/maps/status`);
    return response.json();
  }

  /**
   * Supprime la carte courante
   * @returns {Promise} La confirmation de suppression
   */
  async clearMap() {
    const response = await fetch(`${API_BASE_URL}/maps/current`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la suppression de la carte');
    }

    return response.json();
  }

  /**
   * Upload un fichier de demandes de livraison XML
   * @param {File} file - Le fichier XML à uploader
   * @returns {Promise} Les demandes de livraison
   */
  async uploadDeliveryRequests(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/deliveries/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Erreur lors de l\'upload des demandes de livraison');
    }

    return response.json();
  }

  /**
   * Récupère toutes les demandes de livraison
   * @returns {Promise} Les demandes de livraison
   */
  async getDeliveryRequests() {
    const response = await fetch(`${API_BASE_URL}/deliveries`);
    return response.json();
  }

  /**
   * Ajoute une nouvelle demande de livraison
   * @param {Object} request - La demande de livraison
   * @returns {Promise} La confirmation d'ajout
   */
  async addDeliveryRequest(request) {
    const response = await fetch(`${API_BASE_URL}/deliveries`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de l\'ajout de la demande de livraison');
    }

    return response.json();
  }

  /**
   * Supprime toutes les demandes de livraison
   * @returns {Promise} La confirmation de suppression
   */
  async clearDeliveryRequests() {
    const response = await fetch(`${API_BASE_URL}/deliveries`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la suppression des demandes');
    }

    return response.json();
  }

  /**
   * Définit/Met à jour l'entrepôt courant
   * @param {Object} warehouse - { nodeId, departureTime }
   */
  async setWarehouse(warehouse) {
    const response = await fetch(`${API_BASE_URL}/deliveries/warehouse`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(warehouse),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Erreur lors de la mise à jour du warehouse');
    }

    return response.json();
  }

  /**
   * Calcule une tournée optimisée pour un nombre de livreurs donné
   * @param {number} courierCount - Nombre de livreurs (actuellement seul 1 est supporté)
   * @returns {Promise} La tournée calculée avec tous les trajets
   */
  async calculateTour(courierCount = 1) {
    const response = await fetch(
      `${API_BASE_URL}/tours/calculate?courierCount=${courierCount}`,
      {
        method: 'POST',
      }
    );

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.error || 'Erreur lors du calcul de la tournée');
    }

    return response.json();
  }

  /**
   * Vérifie le statut de disponibilité pour calculer une tournée
   * @returns {Promise} Le statut (ready, mapLoaded, requestsLoaded, stopCount)
   */
  async getTourStatus() {
    const response = await fetch(`${API_BASE_URL}/tours/status`);
    
    if (!response.ok) {
      throw new Error('Erreur lors de la vérification du statut');
    }

    return response.json();
  }

  /**
   * Charge un ensemble de demandes de livraison depuis un fichier XML
   * @param {File} file - Le fichier XML contenant les demandes
   * @returns {Promise} L'ensemble des demandes avec l'entrepôt et les couleurs
   */
  async loadDeliveryRequests(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/deliveries/load`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Erreur lors du chargement des demandes');
    }

    const result = await response.json();
    console.log('apiService.loadDeliveryRequests - réponse complète:', result);
    console.log('apiService.loadDeliveryRequests - result.data:', result.data);
    return result.data; // Retourne directement les données
  }

  /**
   * Récupère l'ensemble des demandes actuelles
   * @returns {Promise} L'ensemble des demandes avec l'entrepôt
   */
  async getCurrentRequestSet() {
    const response = await fetch(`${API_BASE_URL}/deliveries/current`);
    
    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des demandes');
    }

    const result = await response.json();
    return result.data;
  }

  /**
   * Ajoute une livraison à la tournée d'un coursier
   * @param {Object} request - Contient courierId, addresses et durées
   * @returns {Promise} La tournée modifiée
   */
  async addDeliveryToTour(request) {
    const response = await fetch(`${API_BASE_URL}/tours/add-delivery`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de l\'ajout de la livraison');
    }

    return response.json();
  }

  /**
   * Supprime une livraison d'une tournée
   * @param {Object} request - Contient courierId et deliveryIndex
   * @returns {Promise} La tournée modifiée
   */
  
  async removeDeliveryFromTour(request) {
    const response = await fetch(`${API_BASE_URL}/tours/remove-delivery`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la suppression de la livraison');
    }

    return response.json();
  }

  /**
   * Met à jour le coursier assigné à une livraison
   * @param {Object} request - Contient oldCourierId, newCourierId et deliveryIndex
   * @returns {Promise} La tournée modifiée
   */
  async updateCourierAssignment(request) {
    const response = await fetch(`${API_BASE_URL}/tours/update-courier`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Erreur lors de la mise à jour du coursier');
    }

    return response.json();
  }

  async recalculateAssignments(assignments) {
    // assignments: [{ demandId, courierId|null }]
    const response = await fetch(`${API_BASE_URL}/tours/recalculate-assignments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ assignments }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Erreur lors du recalcul des tournées');
    }

    return response.json();
  }

  /**
   * Récupère la tournée d'un coursier
   * @param {string} courierId - L'ID du coursier
   * @returns {Promise} La tournée du coursier
   */
  async getTourByCourier(courierId) {
    const response = await fetch(`${API_BASE_URL}/tours/${courierId}`);

    if (!response.ok) {
      throw new Error('Erreur lors de la récupération de la tournée');
    }

    return response.json();
  }

  /**
   * Sauvegarde une tournée pour un coursier
   * @param {string} courierId - L'ID du coursier
   * @param {Object} tour - La tournée à sauvegarder
   * @returns {Promise} Confirmation de sauvegarde
   */
  async saveTour(courierId, tour) {
    const response = await fetch(`${API_BASE_URL}/tours/save/${courierId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(tour),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la sauvegarde de la tournée');
    }

    return response.json();
  }

  /**
   * Supprime une demande de livraison par son index
   * @param {number} index - L'index de la demande à supprimer
   * @returns {Promise} L'ensemble des demandes mis à jour
   */
  /* async removeDemand(index) {
    const response = await fetch(`${API_BASE_URL}/deliveries/${index}`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Erreur lors de la suppression de la demande');
    }

    return response.json();
  } */
 async removeDemand(deliveryId) {
  console.log('apiService.removeDemand appelé avec id:', deliveryId);
  const response = await fetch(`${API_BASE_URL}/deliveries/${deliveryId}`, {
    method: 'DELETE',
  });

  console.log('Réponse fetch removeDemand - status:', response.status);

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Erreur lors de la suppression de la livraison');
  }

  return response.json();
}

}

export default new ApiService();

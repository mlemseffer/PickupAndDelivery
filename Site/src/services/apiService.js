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
   * Calcule une tournée optimisée
   * @param {string} warehouseAddress - L'adresse de l'entrepôt
   * @returns {Promise} La tournée optimisée
   */
  async calculateTour(warehouseAddress) {
    const response = await fetch(
      `${API_BASE_URL}/tours/calculate?warehouseAddress=${encodeURIComponent(warehouseAddress)}`,
      {
        method: 'POST',
      }
    );

    if (!response.ok) {
      throw new Error('Erreur lors du calcul de la tournée');
    }

    return response.json();
  }
}

export default new ApiService();

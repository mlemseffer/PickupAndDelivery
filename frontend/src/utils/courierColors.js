/**
 * Palette de couleurs pour les coursiers
 * Couleurs vives et distinctes pour une visualisation claire sur la carte
 */
export const COURIER_COLORS = [
  '#FF6B6B',  // Rouge vif - Coursier 1
  '#4ECDC4',  // Turquoise - Coursier 2
  '#45B7D1',  // Bleu ciel - Coursier 3
  '#FFA07A',  // Orange saumon - Coursier 4
  '#98D8C8',  // Vert menthe - Coursier 5
  '#F7DC6F',  // Jaune doré - Coursier 6
  '#BB8FCE',  // Violet lavande - Coursier 7
  '#85C1E2',  // Bleu clair - Coursier 8
  '#F8B739',  // Orange doré - Coursier 9
  '#52B788',  // Vert forêt - Coursier 10
];

/**
 * Obtient la couleur associée à un coursier
 * @param {number} courierId - ID du coursier (1-10)
 * @returns {string} Code couleur hexadécimal
 */
export const getCourierColor = (courierId) => {
  if (!courierId || courierId < 1) {
    return COURIER_COLORS[0]; // Couleur par défaut
  }
  return COURIER_COLORS[(courierId - 1) % COURIER_COLORS.length];
};

/**
 * Obtient toutes les couleurs utilisées pour un ensemble de tours
 * @param {Array} tours - Liste des tours
 * @returns {Array} Liste des couleurs utilisées
 */
export const getUsedColors = (tours) => {
  if (!tours || tours.length === 0) return [];
  return tours.map(tour => getCourierColor(tour.courierId));
};

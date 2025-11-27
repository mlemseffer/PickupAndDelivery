import React from 'react';
import { Edit, FileText, Save } from 'lucide-react';

/**
 * Composant pour les boutons d'action de la tournée
 * - Modifier Tournée
 * - Sauvegarder itinéraire (.txt)
 * - Sauvegarder Tournée
 */
export default function TourActions({ tourData, onModify, onSaveItinerary, onSaveTour }) {
  const handleSaveItinerary = () => {
    if (!tourData || !tourData.tour) {
      alert('Aucune tournée à sauvegarder');
      return;
    }

    // Générer le contenu du fichier texte
    let content = '=== ITINÉRAIRE DE LIVRAISON ===\n\n';
    content += `Nombre de segments: ${tourData.tour.length}\n`;
    content += `Distance totale: ${tourData.metrics?.totalDistance?.toFixed(2) || 0} m\n`;
    content += `Nombre de stops: ${tourData.metrics?.stopCount || 0}\n\n`;
    content += '=== TRAJETS ===\n\n';

    tourData.tour.forEach((trajet, index) => {
      content += `${index + 1}. ${trajet.nomRue}\n`;
      content += `   De: ${trajet.origine}\n`;
      content += `   À: ${trajet.destination}\n`;
      content += `   Longueur: ${trajet.longueur.toFixed(2)} m\n\n`;
    });

    // Créer et télécharger le fichier
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `itineraire_${new Date().toISOString().split('T')[0]}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    if (onSaveItinerary) onSaveItinerary();
  };

  const handleSaveTour = () => {
    if (!tourData) {
      alert('Aucune tournée à sauvegarder');
      return;
    }

    // Générer le fichier JSON de la tournée
    const tourJson = JSON.stringify(tourData, null, 2);
    const blob = new Blob([tourJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `tournee_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    if (onSaveTour) onSaveTour();
  };

  return (
    <div className="flex gap-2 justify-center">
      {/* Modifier Tournée */}
      <button
        onClick={onModify}
        disabled={!tourData}
        className="flex-1 bg-orange-600 hover:bg-orange-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                 flex items-center justify-center gap-2"
        title="Modifier la tournée calculée"
      >
        <Edit size={18} />
        Modifier Tournée
      </button>

      {/* Sauvegarder itinéraire (.txt) */}
      <button
        onClick={handleSaveItinerary}
        disabled={!tourData}
        className="flex-1 bg-teal-600 hover:bg-teal-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                 flex items-center justify-center gap-2"
        title="Sauvegarder l'itinéraire en fichier texte"
      >
        <FileText size={18} />
        Sauvegarder itinéraire (.txt)
      </button>

      {/* Sauvegarder Tournée */}
      <button
        onClick={handleSaveTour}
        disabled={!tourData}
        className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                 text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                 flex items-center justify-center gap-2"
        title="Sauvegarder la tournée complète (JSON)"
      >
        <Save size={18} />
        Sauvegarder Tournée
      </button>
    </div>
  );
}

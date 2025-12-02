import React, { useState } from 'react';
import { Edit, FileText, Save } from 'lucide-react';
import SaveModal from './SaveModal';

/**
 * Composant pour les boutons d'action de la tournée
 * - Modifier Tournée
 * - Sauvegarder itinéraire (.txt)
 * - Sauvegarder Tournée
 */
export default function TourActions({ tourData, onModify, onSaveItinerary, onSaveTour }) {
  const [showItinModal, setShowItinModal] = useState(false);
  const [itinDefaultName, setItinDefaultName] = useState('');
  const [showJsonModal, setShowJsonModal] = useState(false);
  const [jsonDefaultName, setJsonDefaultName] = useState('');

  const openItinModal = () => {
    if (!tourData || !tourData.tour) {
      alert('Aucune tournée à sauvegarder');
      return;
    }
    const defaultName = `itineraire_${new Date().toISOString().split('T')[0]}`;
    setItinDefaultName(defaultName);
    setShowItinModal(true);
  };

  const openJsonModal = () => {
    if (!tourData) {
      alert('Aucune tournée à sauvegarder');
      return;
    }
    const defaultName = `tournee_${new Date().toISOString().split('T')[0]}`;
    setJsonDefaultName(defaultName);
    setShowJsonModal(true);
  };

  const performItinSave = (filename) => {
    if (!filename) return;
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

    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${filename}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    setShowItinModal(false);
    if (onSaveItinerary) onSaveItinerary();
  };

  const performJsonSave = (filename) => {
    if (!filename) return;
    const tourJson = JSON.stringify(tourData, null, 2);
    const blob = new Blob([tourJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `${filename}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    setShowJsonModal(false);
    if (onSaveTour) onSaveTour();
  };

  return (
    <>
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
          onClick={openItinModal}
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
          onClick={openJsonModal}
          disabled={!tourData}
          className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed 
                   text-white px-4 py-2.5 rounded-lg font-semibold transition-colors shadow-lg
                   flex items-center justify-center gap-2"
          title="Sauvegarder la tournée complète (JSON)"
        >
          <Save size={18} />
          Sauvegarder Tournée (.json)
        </button>
      </div>

      <SaveModal
        isOpen={showItinModal}
        title="Sauvegarder l'itinéraire"
        description="Entrez le nom du fichier texte :"
        defaultName={itinDefaultName}
        placeholder="nom_du_fichier"
        confirmLabel="Sauvegarder"
        onCancel={() => setShowItinModal(false)}
        onConfirm={performItinSave}
      />

      <SaveModal
        isOpen={showJsonModal}
        title="Sauvegarder la tournée"
        description="Entrez le nom du fichier JSON :"
        defaultName={jsonDefaultName}
        placeholder="nom_du_fichier"
        confirmLabel="Sauvegarder"
        onCancel={() => setShowJsonModal(false)}
        onConfirm={performJsonSave}
      />
    </>
  );
}

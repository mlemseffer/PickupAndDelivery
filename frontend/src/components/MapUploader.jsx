import React, { useState, useRef } from 'react';
import { Upload } from 'lucide-react';
import apiService from '../services/apiService';

/**
 * Composant pour uploader et charger une carte depuis le backend
 */
export default function MapUploader({ onMapLoaded, onCancel }) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.xml')) {
      setError('Veuillez sélectionner un fichier XML valide.');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      // Upload du fichier vers le backend
      const uploadResponse = await apiService.uploadMap(file);
      
      if (uploadResponse.success) {
        // Récupère les données de la carte complète
        const mapResponse = await apiService.getCurrentMap();
        
        if (mapResponse.success) {
          onMapLoaded(mapResponse.data);
        }
      } else {
        setError(uploadResponse.message || 'Erreur lors du chargement de la carte');
      }
    } catch (err) {
      setError('Erreur: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="p-8 mt-20">
      <div className="max-w-md mx-auto bg-gray-700 rounded-lg p-6 shadow-lg">
        <h3 className="text-xl font-bold mb-4 text-center">Charger une carte</h3>
        <p className="text-gray-300 mb-4 text-center text-sm">
          Sélectionnez un fichier XML contenant les données de la carte (ex: grandPlan.xml)
        </p>
        
        {error && (
          <div className="mb-4 p-3 bg-red-600 rounded-lg text-white text-sm">
            {error}
          </div>
        )}
        
        <input
          ref={fileInputRef}
          type="file"
          accept=".xml"
          onChange={handleFileUpload}
          className="hidden"
        />
        
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={isLoading}
          className="w-full bg-yellow-400 hover:bg-yellow-500 text-gray-800 font-semibold py-3 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
        >
          <Upload size={20} />
          {isLoading ? 'Chargement...' : 'Sélectionner un fichier XML'}
        </button>
        
        <button
          onClick={onCancel}
          className="w-full mt-3 bg-gray-600 hover:bg-gray-500 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
        >
          Annuler
        </button>
      </div>
    </div>
  );
}

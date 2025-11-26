import React, { useState } from 'react';
import { Upload, X, FileText } from 'lucide-react';
import apiService from '../services/apiService';

/**
 * Composant pour uploader un fichier XML de demandes de livraison
 */
export default function DeliveryRequestUploader({ onRequestsLoaded, onCancel }) {
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Gestion du drag & drop
  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile && droppedFile.name.endsWith('.xml')) {
      setFile(droppedFile);
      setError(null);
    } else {
      setError('Veuillez déposer un fichier XML valide');
    }
  };

  // Gestion de la sélection de fichier
  const handleFileSelect = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setError(null);
    }
  };

  // Upload du fichier
  const handleUpload = async () => {
    if (!file) {
      setError('Veuillez sélectionner un fichier');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const requestSet = await apiService.loadDeliveryRequests(file);
      onRequestsLoaded(requestSet);
    } catch (err) {
      setError(err.message || 'Erreur lors du chargement des demandes');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center p-8">
      <div className="bg-gray-700 rounded-lg p-8 max-w-2xl w-full">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Charger des demandes de livraison</h2>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        {/* Zone de drop */}
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
            isDragging
              ? 'border-blue-400 bg-blue-900/20'
              : 'border-gray-500 hover:border-gray-400'
          }`}
        >
          <Upload size={48} className="mx-auto mb-4 text-gray-400" />
          <p className="text-lg mb-2">
            Glissez-déposez un fichier XML ici
          </p>
          <p className="text-gray-400 mb-4">ou</p>
          <label className="inline-block bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded cursor-pointer transition-colors">
            Choisir un fichier
            <input
              type="file"
              accept=".xml"
              onChange={handleFileSelect}
              className="hidden"
            />
          </label>
        </div>

        {/* Fichier sélectionné */}
        {file && (
          <div className="mt-4 flex items-center justify-between bg-gray-600 rounded p-4">
            <div className="flex items-center gap-3">
              <FileText size={24} className="text-blue-400" />
              <div>
                <p className="font-semibold">{file.name}</p>
                <p className="text-sm text-gray-400">
                  {(file.size / 1024).toFixed(2)} KB
                </p>
              </div>
            </div>
            <button
              onClick={() => setFile(null)}
              className="text-gray-400 hover:text-white transition-colors"
            >
              <X size={20} />
            </button>
          </div>
        )}

        {/* Message d'erreur */}
        {error && (
          <div className="mt-4 bg-red-900/30 border border-red-500 rounded p-4 text-red-200">
            <div className="whitespace-pre-line">{error}</div>
          </div>
        )}

        {/* Boutons d'action */}
        <div className="flex gap-4 mt-6">
          <button
            onClick={handleUpload}
            disabled={!file || isLoading}
            className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-6 py-3 rounded font-semibold transition-colors"
          >
            {isLoading ? 'Chargement...' : 'Charger les demandes'}
          </button>
          <button
            onClick={onCancel}
            className="flex-1 bg-gray-600 hover:bg-gray-500 text-white px-6 py-3 rounded font-semibold transition-colors"
          >
            Annuler
          </button>
        </div>

        {/* Informations */}
        <div className="mt-6 text-sm text-gray-400">
          <p className="mb-2">
            <strong>Format attendu :</strong> Fichier XML contenant les demandes de livraison
          </p>
          <p>
            <strong>Exemple :</strong> demandeGrand7.xml, demandePetit1.xml
          </p>
        </div>
      </div>
    </div>
  );
}

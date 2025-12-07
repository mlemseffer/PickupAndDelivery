import React from 'react';

/**
 * Composant réutilisable pour afficher une statistique sous forme de carte
 * 
 * @param {Object} props
 * @param {string} props.label - Libellé de la statistique
 * @param {string|number} props.value - Valeur à afficher
 * @param {string} props.icon - Emoji ou icône à afficher
 * @param {boolean} props.warning - Si true, affiche en rouge (alerte)
 * @param {string} props.warningMessage - Message d'avertissement optionnel
 */
export default function StatCard({ label, value, icon, warning = false, warningMessage }) {
  return (
    <div className={`
      p-4 rounded-lg transition-all
      ${warning 
        ? 'bg-red-900/30 border border-red-500 ring-2 ring-red-500/50' 
        : 'bg-gray-800 border border-gray-700'
      }
    `}>
      {/* Header avec label et icône */}
      <div className="flex items-center justify-between mb-2">
        <span className={`text-sm font-medium ${warning ? 'text-red-300' : 'text-gray-400'}`}>
          {label}
        </span>
        <span className="text-2xl" role="img" aria-label={label}>
          {icon}
        </span>
      </div>
      
      {/* Valeur principale */}
      <div className={`text-2xl font-bold ${warning ? 'text-red-400' : 'text-white'}`}>
        {value}
      </div>
      
      {/* Message d'avertissement */}
      {warning && warningMessage && (
        <div className="text-xs text-red-300 mt-2 flex items-center gap-1">
          <span>⚠️</span>
          <span>{warningMessage}</span>
        </div>
      )}
    </div>
  );
}

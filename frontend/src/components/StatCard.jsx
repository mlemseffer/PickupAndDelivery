import React from 'react';
import Icon from './Icon';

/**
 * Composant réutilisable pour afficher une statistique sous forme de carte
 * 
 * @param {Object} props
 * @param {string} props.label - Libellé de la statistique
 * @param {string|number} props.value - Valeur à afficher
 * @param {string} props.icon - Nom d'icône FontAwesome à afficher
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
        {icon && (
          <span className="text-2xl text-gray-200" aria-hidden="true">
            <Icon name={icon} />
          </span>
        )}
      </div>
      
      {/* Valeur principale */}
      <div className={`text-2xl font-bold ${warning ? 'text-red-400' : 'text-white'}`}>
        {value}
      </div>
      
      {/* Message d'avertissement */}
      {warning && warningMessage && (
        <div className="text-xs text-red-300 mt-2 flex items-center gap-1">
          <Icon name="warning" className="text-yellow-400" />
          <span>{warningMessage}</span>
        </div>
      )}
    </div>
  );
}

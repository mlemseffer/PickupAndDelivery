import React from 'react';
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react';

/**
 * Composant d'alerte personnalisé pour remplacer les alerts natives
 * Types: success, error, info, warning
 */
export default function CustomAlert({ type = 'info', title, message, onClose, autoClose = false }) {
  React.useEffect(() => {
    if (autoClose) {
      const timer = setTimeout(() => {
        onClose();
      }, 5000); // Auto-fermer après 5 secondes
      return () => clearTimeout(timer);
    }
  }, [autoClose, onClose]);

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <CheckCircle size={24} className="text-green-400" />;
      case 'error':
        return <XCircle size={24} className="text-red-400" />;
      case 'warning':
        return <AlertTriangle size={24} className="text-yellow-400" />;
      default:
        return <Info size={24} className="text-blue-400" />;
    }
  };

  const getColors = () => {
    switch (type) {
      case 'success':
        return 'bg-green-900 border-green-500';
      case 'error':
        return 'bg-red-900 border-red-500';
      case 'warning':
        return 'bg-yellow-900 border-yellow-500';
      default:
        return 'bg-blue-900 border-blue-500';
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 bg-black bg-opacity-50 animate-fadeIn">
      <div className={`${getColors()} border-2 rounded-lg shadow-2xl max-w-md w-full mx-4 animate-slideDown`}>
        <div className="p-6">
          <div className="flex items-start gap-4">
            <div className="flex-shrink-0">{getIcon()}</div>
            <div className="flex-1">
              {title && <h3 className="text-lg font-semibold text-white mb-2">{title}</h3>}
              <p className="text-gray-200 whitespace-pre-line">{message}</p>
            </div>
            <button
              onClick={onClose}
              className="flex-shrink-0 text-gray-400 hover:text-white transition-colors"
              aria-label="Fermer"
            >
              <X size={20} />
            </button>
          </div>
          <div className="mt-4 flex justify-end">
            <button
              onClick={onClose}
              className={`px-4 py-2 rounded font-semibold transition-colors ${
                type === 'success'
                  ? 'bg-green-600 hover:bg-green-700'
                  : type === 'error'
                  ? 'bg-red-600 hover:bg-red-700'
                  : type === 'warning'
                  ? 'bg-yellow-600 hover:bg-yellow-700'
                  : 'bg-blue-600 hover:bg-blue-700'
              } text-white`}
            >
              OK
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

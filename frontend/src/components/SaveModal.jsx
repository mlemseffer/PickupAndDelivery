import React from 'react';

// Simple dark-themed modal to ask for a filename and confirm
export default function SaveModal({ isOpen, title = 'Sauvegarder', description = 'Entrez le nom du fichier :', defaultName = '', placeholder = 'nom_du_fichier', confirmLabel = 'Sauvegarder', onCancel, onConfirm }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black opacity-60" onClick={onCancel} />
      <div className="relative bg-gray-900 text-white rounded-lg shadow-xl w-11/12 max-w-md p-4 z-10">
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        <p className="text-sm text-gray-400 mb-3">{description}</p>
        <input
          id="save-modal-input"
          className="w-full bg-gray-800 text-white border border-gray-700 rounded px-3 py-2 mb-3 placeholder-gray-500"
          defaultValue={defaultName}
          placeholder={placeholder}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              onConfirm(e.target.value.trim());
            }
            if (e.key === 'Escape') {
              onCancel();
            }
          }}
          autoFocus
        />
        <div className="flex justify-end gap-2">
          <button className="bg-gray-700 hover:bg-gray-600 text-white px-3 py-1.5 rounded" onClick={onCancel}>Annuler</button>
          <button
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded"
            onClick={() => {
              const input = document.getElementById('save-modal-input');
              onConfirm(input ? input.value.trim() : defaultName);
            }}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

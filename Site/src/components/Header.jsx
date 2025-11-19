import React from 'react';

/**
 * Composant Header de l'application
 */
export default function Header() {
  return (
    <header className="bg-gray-600 py-4 px-6 shadow-lg">
      <div className="flex items-center gap-2">
        <span className="text-2xl">🚴</span>
        <div>
          <h1 className="text-xl font-bold">Pickup & Delivery</h1>
          <p className="text-sm text-gray-300">Optimisation des tournées de livraison à vélo</p>
        </div>
      </div>
    </header>
  );
}

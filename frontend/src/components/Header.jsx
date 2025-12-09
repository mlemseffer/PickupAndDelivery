import React from 'react';
import Icon from './Icon';

/**
 * Composant Header de l'application
 */
export default function Header() {
  return (
    <header className="bg-gray-600 py-4 px-6 shadow-lg">
      <div className="flex items-center gap-2">
        <Icon name="bike" className="text-2xl text-white" />
        <div>
          <h1 className="text-xl font-bold">Pickup & Delivery</h1>
          <p className="text-sm text-gray-300">Optimisation des tournées de livraison à vélo</p>
        </div>
      </div>
    </header>
  );
}

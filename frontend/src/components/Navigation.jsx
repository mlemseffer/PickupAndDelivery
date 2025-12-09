import React from 'react';
import { Home, MapPin, Bike, Route } from 'lucide-react';
import Icon from './Icon';

/**
 * Composant de navigation principal
 */
export default function Navigation({ activeTab, onTabChange, showMapMessage, hasMap, onLoadDeliveryRequests, onRestoreTour }) {
  return (
    <nav className="bg-slate-950/80 border-b border-slate-800 backdrop-blur">
      <div className="flex items-center justify-between px-6 py-3">
        {/* Titre de l'application à gauche */}
        <div className="flex items-center gap-3">
          <Icon name="bike" className="text-3xl text-slate-100" />
          <div>
            <h1 className="text-2xl font-semibold tracking-tight leading-tight text-slate-50">
              Pickup & Delivery
            </h1>
            <p className="text-sm text-slate-300 leading-snug">
              Optimisation des tournées de livraison à vélo
            </p>
          </div>
        </div>
        
        {/* Boutons de navigation à droite */}
        <div className="flex">
          {/* Home Icon */}
          <button 
            className={`p-4 rounded-xl transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/70 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900 ${
              activeTab === 'home'
                ? 'bg-slate-800 text-slate-50 shadow-lg shadow-black/30'
                : 'text-slate-300 hover:bg-slate-800/70 hover:text-slate-50'
            }`}
            onClick={() => onTabChange('home')}
          >
            <Home size={28} className="text-current" />
          </button>
        
        {/* Map Pin Icon */}
        <button 
          className={`p-4 rounded-xl relative transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/70 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900 ${
            activeTab === 'map'
              ? 'bg-slate-800 text-slate-50 shadow-lg shadow-black/30'
              : 'text-slate-300 hover:bg-slate-800/70 hover:text-slate-50'
          }`}
          onClick={() => onTabChange('map')}
        >
          <MapPin size={28} className={`${!hasMap && activeTab === 'map' ? 'text-yellow-400 fill-yellow-400' : 'text-current'}`} />
          {showMapMessage && (
            <div className="absolute top-16 left-1/2 -translate-x-1/2 bg-yellow-400 text-gray-800 px-4 py-3 rounded-2xl shadow-lg whitespace-nowrap pointer-events-none">
              <div className="text-sm font-semibold text-center leading-tight">
                Commencez en<br />chargeant une<br />carte
              </div>
              <div className="absolute -top-2 left-1/2 -translate-x-1/2 w-0 h-0 border-l-8 border-r-8 border-b-8 border-l-transparent border-r-transparent border-b-yellow-400"></div>
            </div>
          )}
        </button>
        
        {/* Bike Icon - Charger demandes XML */}
        <button 
          className={`p-4 rounded-xl relative transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/70 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900 ${
            activeTab === 'deliveries'
              ? 'bg-slate-800 text-slate-50 shadow-lg shadow-black/30'
              : 'text-slate-300 hover:bg-slate-800/70 hover:text-slate-50'
          }`}
          onClick={() => hasMap ? onLoadDeliveryRequests() : alert('Veuillez d\'abord charger une carte')}
          title={hasMap ? "Charger un fichier de demandes XML" : "Chargez d'abord une carte"}
        >
          <Bike size={28} className={`${hasMap ? 'text-yellow-400' : 'text-current'}`} />
          {hasMap && (
            <div className="absolute -bottom-1 left-1/2 -translate-x-1/2 bg-yellow-400 text-gray-900 px-2 py-0.5 rounded text-xs font-bold pointer-events-none">
              XML
            </div>
          )}
        </button>
        
        {/* Route Icon - Restaurer tournée */}
        <button 
          className={`p-4 rounded-xl relative transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/70 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900 ${
            activeTab === 'tours'
              ? 'bg-slate-800 text-slate-50 shadow-lg shadow-black/30'
              : 'text-slate-300 hover:bg-slate-800/70 hover:text-slate-50'
          }`}
          onClick={() => hasMap ? onRestoreTour() : alert('Veuillez d\'abord charger une carte')}
          title={hasMap ? "Restaurer une tournée depuis un fichier JSON" : "Chargez d'abord une carte"}
        >
          <Route size={28} className={`${hasMap ? 'text-yellow-400' : 'text-current'}`} />
          {hasMap && (
            <div className="absolute -bottom-1 left-1/2 -translate-x-1/2 bg-yellow-400 text-gray-900 px-2 py-0.5 rounded text-xs font-bold pointer-events-none">
              JSON
            </div>
          )}
        </button>
        </div>
      </div>
    </nav>
  );
}

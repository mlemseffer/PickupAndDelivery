import React from 'react';
import Icon from './Icon';

/**
 * Composant pour afficher les demandes qui n'ont pas pu être assignées
 * à cause de la contrainte des 4 heures par coursier
 */
const UnassignedDemands = ({ unassignedDemands, deliveryRequestSet, courierCount }) => {
  if (!unassignedDemands || unassignedDemands.length === 0) {
    return (
      <div className="p-6 bg-green-50 border border-green-200 rounded-lg">
        <div className="flex items-center gap-3">
          <div className="flex-shrink-0">
            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h3 className="text-lg font-semibold text-green-800">
              <span className="inline-flex items-center gap-2">
                <Icon name="success" className="text-green-700" />
                Toutes les demandes ont été assignées !
              </span>
            </h3>
            <p className="text-sm text-green-600 mt-1">
              Les {courierCount} coursier{courierCount > 1 ? 's' : ''} {courierCount > 1 ? 'ont' : 'a'} pu traiter toutes les demandes dans la contrainte des 4 heures.
            </p>
          </div>
        </div>
      </div>
    );
  }

  const totalDemands = deliveryRequestSet?.demands?.length || 0;
  const assignedDemands = totalDemands - unassignedDemands.length;
  const assignmentRate = totalDemands > 0 ? ((assignedDemands / totalDemands) * 100).toFixed(1) : 0;

  return (
    <div className="space-y-4">
      {/* En-tête avec alerte */}
      <div className="bg-orange-50 border-l-4 border-orange-400 p-4 rounded-r-lg">
        <div className="flex items-start">
          <div className="flex-shrink-0">
            <svg className="w-6 h-6 text-orange-400" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3 flex-1">
            <h3 className="text-lg font-semibold text-orange-800">
              <span className="inline-flex items-center gap-2">
                <Icon name="warning" className="text-orange-500" />
                Demandes non traitées : {unassignedDemands.length} sur {totalDemands}
              </span>
            </h3>
            <div className="mt-2 text-sm text-orange-700">
              <p className="mb-2">
                La contrainte des <strong>4 heures par coursier</strong> a empêché l'assignation de certaines demandes.
              </p>
              <p className="font-medium">
                <span className="inline-flex items-center gap-2">
                  <Icon name="chart" className="text-orange-500" />
                  Taux d'assignation : {assignmentRate}% ({assignedDemands}/{totalDemands} demandes)
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Solution suggérée */}
      <div className="bg-blue-50 border border-blue-200 p-4 rounded-lg">
        <div className="flex items-start gap-3">
          <svg className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
          </svg>
          <div>
            <h4 className="font-semibold text-blue-800 mb-1 flex items-center gap-2">
              <Icon name="lightbulb" className="text-blue-600" />
              Solution recommandée
            </h4>
            <p className="text-sm text-blue-700">
              Augmentez le nombre de coursiers à <strong className="text-blue-900">{courierCount + Math.ceil(unassignedDemands.length / 3)}</strong> ou plus 
              pour traiter toutes les demandes dans la contrainte des 4 heures.
            </p>
          </div>
        </div>
      </div>

      {/* Liste des demandes non assignées */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div className="px-4 py-3 bg-gray-50 border-b border-gray-200">
          <h4 className="font-semibold text-gray-800">
            <span className="inline-flex items-center gap-2">
              <Icon name="clipboard" className="text-gray-600" />
              Liste des demandes non assignées ({unassignedDemands.length})
            </span>
          </h4>
        </div>
        
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  ID Demande
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <span className="inline-flex items-center gap-1">
                    <Icon name="location" className="text-gray-600" />
                    Pickup
                  </span>
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <span className="inline-flex items-center gap-1">
                    <Icon name="box" className="text-gray-600" />
                    Delivery
                  </span>
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <span className="inline-flex items-center gap-1">
                    <Icon name="timer" className="text-gray-600" />
                    Durées
                  </span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {unassignedDemands.map((demand, index) => (
                <tr key={demand.id || index} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 whitespace-nowrap">
                    <div className="flex items-center">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                        {demand.id}
                      </span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="text-sm text-gray-900">
                      <div className="font-medium">Node: {demand.pickupNodeId}</div>
                      {demand.pickupAddress && (
                        <div className="text-gray-500 text-xs mt-0.5">{demand.pickupAddress}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="text-sm text-gray-900">
                      <div className="font-medium">Node: {demand.deliveryNodeId}</div>
                      {demand.deliveryAddress && (
                        <div className="text-gray-500 text-xs mt-0.5">{demand.deliveryAddress}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">
                    <div className="text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <span className="text-xs inline-flex items-center gap-1">
                          <Icon name="timer" className="text-gray-500" />
                          Pickup:
                        </span>
                        <span className="font-medium">{demand.pickupDuration}s</span>
                      </div>
                      <div className="flex items-center gap-1 mt-1">
                        <span className="text-xs inline-flex items-center gap-1">
                          <Icon name="timer" className="text-gray-500" />
                          Delivery:
                        </span>
                        <span className="font-medium">{demand.deliveryDuration}s</span>
                      </div>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Statistiques détaillées */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Demandes assignées</p>
              <p className="text-2xl font-bold text-green-600">{assignedDemands}</p>
            </div>
            <svg className="w-10 h-10 text-green-200" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Demandes non assignées</p>
              <p className="text-2xl font-bold text-orange-600">{unassignedDemands.length}</p>
            </div>
            <svg className="w-10 h-10 text-orange-200" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Taux de réussite</p>
              <p className="text-2xl font-bold text-blue-600">{assignmentRate}%</p>
            </div>
            <svg className="w-10 h-10 text-blue-200" fill="currentColor" viewBox="0 0 20 20">
              <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UnassignedDemands;

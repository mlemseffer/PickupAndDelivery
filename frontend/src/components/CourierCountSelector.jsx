import React from 'react';

/**
 * Sélecteur visuel pour choisir le nombre de coursiers (1-10)
 * 
 * @param {Object} props - Les propriétés du composant
 * @param {number} props.value - Nombre actuel de coursiers sélectionné
 * @param {Function} props.onChange - Callback appelé lors du changement de valeur
 * @param {boolean} props.disabled - Si true, le sélecteur est désactivé
 * 
 * @example
 * <CourierCountSelector
 *   value={courierCount}
 *   onChange={setCourierCount}
 *   disabled={isCalculatingTour}
 * />
 */
export default function CourierCountSelector({ value, onChange, disabled }) {
  const courierOptions = Array.from({ length: 10 }, (_, i) => i + 1);

  return (
    <div className="courier-count-selector">
      {/* Label */}
      <label className="block text-sm font-medium text-gray-300 mb-3">
        Nombre de coursiers
      </label>

      {/* Sélecteur visuel avec boutons 1-10 */}
      <div className="flex gap-2 flex-wrap mb-4">
        {courierOptions.map((count) => (
          <button
            key={count}
            type="button"
            onClick={() => !disabled && onChange(count)}
            disabled={disabled}
            className={`
              px-4 py-2 rounded-lg font-semibold transition-all duration-200
              ${
                value === count
                  ? 'bg-blue-600 text-white scale-110 shadow-lg ring-2 ring-blue-400'
                  : 'bg-gray-600 text-gray-300 hover:bg-gray-500'
              }
              ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:shadow-md'}
              focus:outline-none focus:ring-2 focus:ring-blue-500
            `}
            title={`Sélectionner ${count} coursier${count > 1 ? 's' : ''}`}
          >
            {count}
          </button>
        ))}
      </div>

      {/* Slider alternatif */}
      <div className="mt-4">
        <input
          type="range"
          min="1"
          max="10"
          value={value}
          onChange={(e) => !disabled && onChange(parseInt(e.target.value))}
          disabled={disabled}
          className={`
            w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer
            ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
            [&::-webkit-slider-thumb]:appearance-none 
            [&::-webkit-slider-thumb]:w-4 
            [&::-webkit-slider-thumb]:h-4 
            [&::-webkit-slider-thumb]:rounded-full 
            [&::-webkit-slider-thumb]:bg-blue-500 
            [&::-webkit-slider-thumb]:cursor-pointer
            [&::-webkit-slider-thumb]:transition-all
            [&::-webkit-slider-thumb]:hover:scale-125
            [&::-moz-range-thumb]:w-4 
            [&::-moz-range-thumb]:h-4 
            [&::-moz-range-thumb]:rounded-full 
            [&::-moz-range-thumb]:bg-blue-500 
            [&::-moz-range-thumb]:cursor-pointer
            [&::-moz-range-thumb]:border-0
          `}
          title="Ajuster le nombre de coursiers avec le curseur"
        />
        <div className="flex justify-between text-xs text-gray-400 mt-1 px-1">
          <span>1</span>
          <span>5</span>
          <span>10</span>
        </div>
      </div>

      {/* Indicateur de sélection */}
      <div className="mt-3 text-center">
        <span className="text-2xl font-bold text-blue-400">
          {value} coursier{value > 1 ? 's' : ''}
        </span>
      </div>

      {/* Message informatif */}
      {value > 1 && (
        <div className="mt-3 p-3 bg-blue-900/30 border border-blue-500/50 rounded-lg">
          <p className="text-xs text-blue-200">
            ℹ️ Les demandes seront réparties entre {value} coursiers selon l'ordre FIFO avec une limite de 4h par tournée.
          </p>
        </div>
      )}
    </div>
  );
}

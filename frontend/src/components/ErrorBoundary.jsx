import React from 'react';
import Icon from './Icon';

/**
 * Composant Error Boundary pour capturer les erreurs de rendu React
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('[ErrorBoundary] Erreur capturée:', error, errorInfo);
    this.setState({ error, errorInfo });
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex-1 flex items-center justify-center bg-gray-800">
          <div className="bg-red-900/30 border border-red-500 rounded-lg p-6 max-w-2xl">
            <h2 className="text-2xl font-bold text-red-400 mb-4 flex items-center gap-2">
              <Icon name="error" className="text-red-400" />
              Erreur de rendu
            </h2>
            <p className="text-gray-300 mb-4">
              Une erreur s'est produite lors de l'affichage de la carte.
            </p>
            {this.state.error && (
              <div className="bg-gray-900 p-4 rounded mb-4">
                <p className="text-red-300 font-mono text-sm">
                  {this.state.error.toString()}
                </p>
              </div>
            )}
            <button
              onClick={() => {
                this.setState({ hasError: false, error: null, errorInfo: null });
                window.location.reload();
              }}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
            >
              <span className="inline-flex items-center gap-2">
                <Icon name="rotate" className="text-white" />
                Recharger la page
              </span>
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

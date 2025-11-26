package com.pickupdelivery.exception;

/**
 * Exception personnalisée pour les erreurs liées aux algorithmes de calcul de tournée
 * Permet de distinguer les erreurs métier des erreurs techniques
 */
public class AlgorithmException extends RuntimeException {
    
    /**
     * Type d'erreur rencontrée dans l'algorithme
     */
    public enum ErrorType {
        /** Aucun chemin trouvé entre deux points */
        NO_PATH_FOUND,
        
        /** Contraintes de précédence non respectées */
        PRECEDENCE_VIOLATION,
        
        /** Aucun stop faisable trouvé (bug logique) */
        NO_FEASIBLE_STOP,
        
        /** Warehouse manquant */
        NO_WAREHOUSE,
        
        /** Stop non trouvé dans le graphe */
        STOP_NOT_FOUND,
        
        /** Graphe invalide ou incomplet */
        INVALID_GRAPH
    }
    
    private final ErrorType errorType;
    
    public AlgorithmException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public AlgorithmException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
}

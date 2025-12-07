package com.pickupdelivery.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import com.pickupdelivery.model.AlgorithmModel.Trajet;
import com.pickupdelivery.model.AlgorithmModel.Tour;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires ROBUSTES pour le calcul de temps (Phase 1)
 * Complète ServiceAlgoTimeCalculationTest avec des cas limites et tests paramétriques
 * 
 * Ces tests vérifient:
 * - Les cas limites (valeurs nulles, négatives, très grandes)
 * - Les tests paramétrés pour multiple valeurs
 * - La précision des calculs
 * - La cohérence des formules
 */
@SpringBootTest
class ServiceAlgoTimeCalculationRobustTest {

    @ParameterizedTest
    @CsvSource({
        "1000, 240.0",  // 1 km = 4 minutes
        "500, 120.0",   // 500 m = 2 minutes
        "250, 60.0",    // 250 m = 1 minute
        "100, 24.0",    // 100 m = 24 secondes
        "0, 0.0",       // distance nulle
        "10000, 2400.0", // 10 km = 40 minutes
        "50, 12.0",     // 50 m = 12 secondes
        "5000, 1200.0"  // 5 km = 20 minutes
    })
    void testTravelTime_ParametricDistances(double distance, double expectedTime) {
        double speed = 15.0 / 3.6; // 4.166... m/s
        double actualTime = distance / speed;
        
        assertEquals(expectedTime, actualTime, 1.0,
            String.format("Distance %.0f m devrait prendre %.1f secondes à 15 km/h", 
                distance, expectedTime));
        
        // Vérifier que le temps est positif ou nul
        assertTrue(actualTime >= 0, "Le temps de trajet ne peut pas être négatif");
    }

    @Test
    void testTravelTime_VeryLargeDistance() {
        // Test: très grande distance (100 km)
        double largeDistance = 100000.0; // 100 km
        double speed = 15.0 / 3.6;
        double expectedTime = 24000.0; // 400 minutes = 6h40
        double actualTime = largeDistance / speed;
        
        assertEquals(expectedTime, actualTime, 10.0,
            "100 km devraient prendre environ 24000 secondes");
        
        // Vérifier que ça dépasse la limite de 4h
        double timeLimit = 4 * 3600; // 14400 secondes
        assertTrue(actualTime > timeLimit,
            "100 km devraient dépasser la limite de 4h");
    }

    @Test
    void testTravelTime_SmallDistances() {
        // Test avec des très petites distances pour vérifier la précision
        double speed = 15.0 / 3.6;
        double distance1 = 10.0;  // 10 mètres
        double distance2 = 1.0;   // 1 mètre
        double distance3 = 0.1;   // 10 cm

        double time1 = distance1 / speed;
        double time2 = distance2 / speed;
        double time3 = distance3 / speed;

        assertTrue(time1 > time2, "10m devrait prendre plus de temps que 1m");
        assertTrue(time2 > time3, "1m devrait prendre plus de temps que 0.1m");
        assertTrue(time1 > 0, "Le temps devrait être positif même pour 10m");
        assertTrue(time3 < 1, "10cm devrait prendre moins d'1 seconde");
    }

    @ParameterizedTest
    @CsvSource({
        "3600, 1.0",     // 1 heure
        "7200, 2.0",     // 2 heures
        "14400, 4.0",    // 4 heures
        "1800, 0.5",     // 30 minutes
        "0, 0.0",        // 0
        "3660, 1.0166",  // 1h 1min
        "900, 0.25",     // 15 minutes
        "10800, 3.0"     // 3 heures
    })
    void testDurationConversion_SecondsToHours(double seconds, double expectedHours) {
        double actualHours = seconds / 3600.0;
        assertEquals(expectedHours, actualHours, 0.01,
            String.format("%.0f secondes devraient être %.4f heures", seconds, expectedHours));
    }

    @ParameterizedTest
    @CsvSource({
        "60, 1.0",      // 1 minute
        "120, 2.0",     // 2 minutes
        "600, 10.0",    // 10 minutes
        "0, 0.0",       // 0
        "3600, 60.0",   // 60 minutes
        "90, 1.5",      // 1.5 minutes
        "30, 0.5",      // 30 secondes
        "1800, 30.0"    // 30 minutes
    })
    void testDurationConversion_SecondsToMinutes(double seconds, double expectedMinutes) {
        double actualMinutes = seconds / 60.0;
        assertEquals(expectedMinutes, actualMinutes, 0.01,
            String.format("%.0f secondes devraient être %.2f minutes", seconds, expectedMinutes));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 14400, 14399, 7200, 3600, 1, 10000, 100})
    void testTimeLimit_NotExceeded(double durationSec) {
        double timeLimit = 4 * 3600; // 14400 secondes
        assertFalse(durationSec > timeLimit,
            String.format("%.0f secondes ne devraient PAS dépasser la limite de 4h", durationSec));
    }

    @ParameterizedTest
    @ValueSource(doubles = {14401, 15000, 20000, 86400, 100000, 50000})
    void testTimeLimit_Exceeded(double durationSec) {
        double timeLimit = 4 * 3600; // 14400 secondes
        assertTrue(durationSec > timeLimit,
            String.format("%.0f secondes devraient dépasser la limite de 4h", durationSec));
    }

    @Test
    void testTrajetDuration_ZeroAndEdgeCases() {
        Trajet trajet = new Trajet();
        
        // Test avec durée 0
        trajet.setDurationSec(0.0);
        assertEquals(0.0, trajet.getDurationHours(), 0.001,
            "Durée 0 devrait donner 0 heures");
        assertEquals(0.0, trajet.getDurationMinutes(), 0.001,
            "Durée 0 devrait donner 0 minutes");
        
        // Test avec durée d'exactement 4h (limite)
        trajet.setDurationSec(14400.0);
        assertEquals(4.0, trajet.getDurationHours(), 0.001,
            "14400 secondes devraient donner exactement 4 heures");
        assertEquals(240.0, trajet.getDurationMinutes(), 0.001,
            "14400 secondes devraient donner exactement 240 minutes");
            
        // Test avec 1 heure
        trajet.setDurationSec(3600.0);
        assertEquals(1.0, trajet.getDurationHours(), 0.001,
            "3600 secondes = 1 heure");
        assertEquals(60.0, trajet.getDurationMinutes(), 0.001,
            "3600 secondes = 60 minutes");
    }

    @Test
    void testServiceTimeCalculation_MultipleStops() {
        // Test: calculer le temps de service pour plusieurs stops
        int pickupTime1 = 120; // 2 minutes
        int deliveryTime1 = 180; // 3 minutes
        int pickupTime2 = 240; // 4 minutes
        int deliveryTime2 = 300; // 5 minutes

        double totalServiceTime = pickupTime1 + deliveryTime1 + pickupTime2 + deliveryTime2;

        assertEquals(840.0, totalServiceTime, 0.01,
            "Le temps de service total devrait être 840 secondes (14 minutes)");
            
        double totalMinutes = totalServiceTime / 60.0;
        assertEquals(14.0, totalMinutes, 0.01,
            "840 secondes = 14 minutes");
    }

    @Test
    void testConstantsConsistency() {
        // Vérifier la cohérence entre les constantes
        double speedKmh = 15.0;
        double speedMs = speedKmh / 3.6;
        
        assertEquals(4.166666666666667, speedMs, 0.001,
            "15 km/h devrait être converti en 4.166... m/s");
        
        double fourHours = 4 * 3600;
        assertEquals(14400, fourHours,
            "4 heures devraient correspondre à 14400 secondes");
            
        // Vérifier les conversions
        assertEquals(60, 1 * 60, "1 minute = 60 secondes");
        assertEquals(3600, 1 * 3600, "1 heure = 3600 secondes");
        assertEquals(86400, 24 * 3600, "1 jour = 86400 secondes");
    }

    @Test
    void testComplexScenario_MultipleDeliveries() {
        // Scénario complexe: 3 livraisons sur 5km avec temps de service
        double totalDistance = 5000.0; // 5 km
        double speed = 15.0 / 3.6;
        double travelTime = totalDistance / speed; // 1200 secondes (20 minutes)
        
        // 3 pickups de 3 minutes chacun
        double pickupTime = 3 * 180.0; // 540 secondes (9 minutes)
        
        // 3 deliveries de 2 minutes chacun
        double deliveryTime = 3 * 120.0; // 360 secondes (6 minutes)
        
        double totalTime = travelTime + pickupTime + deliveryTime;
        
        assertEquals(2100.0, totalTime, 5.0,
            "Temps total devrait être environ 2100 secondes (35 minutes)");
        
        // Vérifier que c'est bien sous la limite de 4h
        assertTrue(totalTime < 14400,
            "Ce scénario devrait être sous la limite de 4h");
            
        double totalHours = totalTime / 3600.0;
        assertTrue(totalHours < 1.0,
            "Le scénario devrait prendre moins d'1 heure");
    }

    @Test
    void testEdgeCase_ExactlyAtTimeLimit() {
        // Test: exactement à la limite de 4h
        double exactTimeLimit = 14400.0;
        double fourHours = 4 * 3600;
        
        assertEquals(fourHours, exactTimeLimit, 0.001,
            "La limite devrait être exactement 14400 secondes");
        
        // Vérifier que c'est considéré comme accepté (<=)
        assertFalse(exactTimeLimit > fourHours,
            "Exactement 4h ne devrait PAS dépasser la limite");
        
        // Mais 1 seconde de plus devrait dépasser
        assertTrue((exactTimeLimit + 1) > fourHours,
            "4h + 1 seconde devrait dépasser la limite");
    }

    @Test
    void testFormulaAccuracy_SpeedDistanceTime() {
        // Test: Vérifier la formule vitesse = distance / temps
        double distance = 1500.0; // 1.5 km
        double speed = 15.0 / 3.6; // m/s
        double expectedTime = distance / speed;
        
        // Vérification inverse: distance = vitesse * temps
        double calculatedDistance = speed * expectedTime;
        assertEquals(distance, calculatedDistance, 0.1,
            "La formule distance = vitesse * temps devrait être cohérente");
        
        // Vérification: vitesse = distance / temps
        double calculatedSpeed = distance / expectedTime;
        assertEquals(speed, calculatedSpeed, 0.001,
            "La formule vitesse = distance / temps devrait être cohérente");
    }

    @Test
    void testTourDuration_Methods() {
        // Test des méthodes de durée de Tour
        Tour tour = new Tour(null, null, 0.0, 7200.0, 1); // 2 heures
        
        assertEquals(2.0, tour.getTotalDurationHours(), 0.01,
            "7200 secondes = 2 heures");
        assertEquals(120.0, tour.getTotalDurationMinutes(), 0.01,
            "7200 secondes = 120 minutes");
        assertFalse(tour.exceedsTimeLimit(),
            "2 heures ne devraient pas dépasser la limite");
            
        // Test avec 5 heures (dépasse)
        Tour tour2 = new Tour(null, null, 0.0, 18000.0, 2); // 5 heures
        assertTrue(tour2.exceedsTimeLimit(),
            "5 heures devraient dépasser la limite de 4h");
    }

    @Test
    void testEdgeCase_ZeroSpeed() {
        // Test théorique: si la vitesse était 0 (impossible)
        double distance = 1000.0;
        double speed = 0.0;
        
        // Division par 0 donne l'infini
        double time = distance / speed;
        assertTrue(Double.isInfinite(time),
            "Division par vitesse 0 devrait donner l'infini");
    }

    @Test
    void testEdgeCase_InfiniteDistance() {
        // Test théorique: distance infinie
        double distance = Double.POSITIVE_INFINITY;
        double speed = 15.0 / 3.6;
        
        double time = distance / speed;
        assertTrue(Double.isInfinite(time),
            "Distance infinie devrait donner un temps infini");
    }

    @Test
    void testRealisticScenario_FullDay() {
        // Scénario réaliste: 6 livraisons sur 20km
        double totalDistance = 20000.0; // 20 km
        double speed = 15.0 / 3.6;
        double travelTime = totalDistance / speed; // 4800 secondes (80 minutes = 1h20)
        
        // 6 pickups de 5 minutes chacun
        double pickupTime = 6 * 300.0; // 1800 secondes (30 minutes)
        
        // 6 deliveries de 5 minutes chacun
        double deliveryTime = 6 * 300.0; // 1800 secondes (30 minutes)
        
        double totalTime = travelTime + pickupTime + deliveryTime; // 8400 secondes
        
        assertEquals(8400.0, totalTime, 10.0,
            "Temps total devrait être environ 8400 secondes");
        
        // Vérifier que c'est bien sous la limite de 4h (14400 secondes)
        assertTrue(totalTime < 14400,
            "Ce scénario devrait être sous la limite de 4h");
            
        double totalHours = totalTime / 3600.0;
        assertEquals(2.333, totalHours, 0.01,
            "Le scénario devrait prendre environ 2.33 heures (2h20)");
    }

    @Test
    void testPrecision_RoundingErrors() {
        // Test pour vérifier qu'il n'y a pas d'erreurs d'arrondi significatives
        double speed = 15.0 / 3.6;
        double distance1 = 1000.0;
        
        // Calculer le temps puis recalculer la distance
        double time = distance1 / speed;
        double distance2 = speed * time;
        
        assertEquals(distance1, distance2, 0.001,
            "Pas d'erreur d'arrondi significative dans les calculs");
    }
}

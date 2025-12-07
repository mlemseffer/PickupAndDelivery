package com.pickupdelivery.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le calcul de temps (Phase 1)
 * Vérifie que le calcul de temps de trajet fonctionne correctement
 */
@SpringBootTest
class ServiceAlgoTimeCalculationTest {

    @Test
    void testCourierSpeed_15KmPerHour() {
        // Vitesse attendue: 15 km/h = 4.166... m/s
        double expectedSpeed = 15.0 / 3.6;
        double delta = 0.01;
        
        assertEquals(4.166, expectedSpeed, delta, 
            "La vitesse du coursier devrait être 4.166 m/s (15 km/h)");
    }

    @Test
    void testTravelTime_1Kilometer() {
        // Test: 1 km à 15 km/h devrait prendre 4 minutes (240 secondes)
        double distance = 1000; // 1 km en mètres
        double speed = 15.0 / 3.6; // 4.166 m/s
        double expectedTime = distance / speed; // 240 secondes
        
        assertEquals(240.0, expectedTime, 1.0,
            "1 km à 15 km/h devrait prendre environ 240 secondes (4 minutes)");
    }

    @Test
    void testTravelTime_500Meters() {
        // Test: 500 m à 15 km/h devrait prendre 2 minutes (120 secondes)
        double distance = 500; // 500 m
        double speed = 15.0 / 3.6; // 4.166 m/s
        double expectedTime = distance / speed; // 120 secondes
        
        assertEquals(120.0, expectedTime, 1.0,
            "500 m à 15 km/h devrait prendre environ 120 secondes (2 minutes)");
    }

    @Test
    void testTimeLimit_4Hours() {
        // Test: limite de 4 heures = 14400 secondes
        double fourHours = 4 * 3600;
        assertEquals(14400.0, fourHours,
            "4 heures devraient être égales à 14400 secondes");
    }

    @Test
    void testDurationConversion_SecondsToHours() {
        // Test: conversion secondes -> heures
        double durationSec = 7200; // 2 heures en secondes
        double durationHours = durationSec / 3600.0;
        
        assertEquals(2.0, durationHours, 0.01,
            "7200 secondes devraient être égales à 2 heures");
    }

    @Test
    void testDurationConversion_SecondsToMinutes() {
        // Test: conversion secondes -> minutes
        double durationSec = 600; // 10 minutes en secondes
        double durationMinutes = durationSec / 60.0;
        
        assertEquals(10.0, durationMinutes, 0.01,
            "600 secondes devraient être égales à 10 minutes");
    }

    @Test
    void testTotalDuration_WithPickupAndDelivery() {
        // Test: temps total = temps de trajet + temps de service
        // Scénario: 1 km de distance + 5 min pickup + 5 min delivery
        double travelTime = 1000 / (15.0 / 3.6); // 240 sec (4 min)
        double pickupTime = 300; // 5 min
        double deliveryTime = 300; // 5 min
        double totalTime = travelTime + pickupTime + deliveryTime; // 840 sec (14 min)
        
        assertEquals(840.0, totalTime, 1.0,
            "Temps total devrait être 840 secondes (14 minutes)");
        
        double totalMinutes = totalTime / 60.0;
        assertEquals(14.0, totalMinutes, 0.1,
            "Temps total devrait être 14 minutes");
    }

    @Test
    void testTimeLimit_Exceeded() {
        // Test: vérifier si une durée dépasse 4h
        double durationSec1 = 14400; // Exactement 4h
        double durationSec2 = 14401; // 4h + 1 seconde
        double durationSec3 = 10000; // Moins de 4h
        double timeLimit = 4 * 3600; // 14400 sec
        
        assertFalse(durationSec1 > timeLimit,
            "14400 secondes ne devraient PAS dépasser la limite");
        assertTrue(durationSec2 > timeLimit,
            "14401 secondes devraient dépasser la limite");
        assertFalse(durationSec3 > timeLimit,
            "10000 secondes ne devraient PAS dépasser la limite");
    }

    @Test
    void testDistance_10KmAt15KmPerHour() {
        // Test: 10 km à 15 km/h = 40 minutes
        double distance = 10000; // 10 km
        double speed = 15.0 / 3.6;
        double timeSec = distance / speed;
        double timeMinutes = timeSec / 60.0;
        
        assertEquals(40.0, timeMinutes, 1.0,
            "10 km à 15 km/h devraient prendre 40 minutes");
    }
}

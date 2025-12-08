package com.pickupdelivery.service;

import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests supplémentaires pour ServiceAlgo
 */
@ExtendWith(MockitoExtension.class)
public class ServiceAlgoExtraTest {

    @InjectMocks
    private ServiceAlgo serviceAlgo;

    private CityMap cityMap;
    private StopSet stopSet;

    @BeforeEach
    void setUp() {
        cityMap = new CityMap();
        cityMap.setNodes(new java.util.HashMap<>());
        cityMap.setSegments(new ArrayList<>());
        
        stopSet = mock(StopSet.class);
        when(stopSet.getStops()).thenReturn(new ArrayList<>());
    }

    @Test
    @DisplayName("buildGraph crée un graph vide pour un stopSet vide")
    void testBuildGraphWithEmptyStopSet() {
        Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
        
        assertNotNull(graph);
        assertNotNull(graph.getDistancesMatrix());
    }

    @Test
    @DisplayName("buildGraph gère correctement un stopSet null")
    void testBuildGraphWithNullStopSet() {
        assertThrows(Exception.class, () -> {
            serviceAlgo.buildGraph(null, cityMap);
        });
    }

    @Test
    @DisplayName("buildGraph gère correctement une cityMap null")
    void testBuildGraphWithNullCityMap() {
        assertThrows(Exception.class, () -> {
            serviceAlgo.buildGraph(stopSet, null);
        });
    }

    @Test
    @DisplayName("clearCache vide le cache Dijkstra")
    void testClearCache() {
        // Ce test vérifie que la méthode clearCache ne génère pas d'exception
        assertDoesNotThrow(() -> serviceAlgo.clearCache());
    }
}

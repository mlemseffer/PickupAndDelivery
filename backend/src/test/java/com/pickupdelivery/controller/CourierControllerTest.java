package com.pickupdelivery.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration pour CourierController
 * Teste la validation du nombre de livreurs
 */
@SpringBootTest
@AutoConfigureMockMvc
class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------------------------------------------------
    // Tests de validation du nombre de livreurs
    // ---------------------------------------------------------

    @Test
    void setCourierCount_WithValidCount_ShouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nombre de livreurs défini à 3"))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void setCourierCount_WithMinValidCount_ShouldReturnSuccess() throws Exception {
        // Act & Assert - Test avec le minimum (1)
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nombre de livreurs défini à 1"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void setCourierCount_WithMaxValidCount_ShouldReturnSuccess() throws Exception {
        // Act & Assert - Test avec le maximum (10)
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nombre de livreurs défini à 10"))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    void setCourierCount_WithNegativeCount_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Nombre négatif
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("❌ Le nombre de livreurs doit être entre 1 et 10"));
    }

    @Test
    void setCourierCount_WithZeroCount_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Zéro
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("❌ Le nombre de livreurs doit être entre 1 et 10"));
    }

    @Test
    void setCourierCount_WithTooHighCount_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Trop grand
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("❌ Le nombre de livreurs doit être entre 1 et 10"));
    }

    @Test
    void setCourierCount_WithDecimalNumber_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Nombre décimal (1.3 livreurs)
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "1.3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void setCourierCount_WithFloatNumber_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Nombre à virgule (2.5 livreurs)
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "2.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void setCourierCount_WithInvalidString_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Chaîne de caractères invalide
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void setCourierCount_WithEmptyParameter_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Paramètre vide
        mockMvc.perform(post("/api/couriers/count")
                .param("count", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void setCourierCount_WithMissingParameter_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Paramètre manquant
        mockMvc.perform(post("/api/couriers/count"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setCourierCount_WithVeryLargeNegativeNumber_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Nombre négatif très grand
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "-999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("❌ Le nombre de livreurs doit être entre 1 et 10"));
    }

    @Test
    void setCourierCount_WithVeryLargePositiveNumber_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Nombre positif très grand
        mockMvc.perform(post("/api/couriers/count")
                .param("count", "999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("❌ Le nombre de livreurs doit être entre 1 et 10"));
    }
}

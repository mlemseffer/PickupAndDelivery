package com.pickupdelivery.service;

import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.xmlparser.MapXmlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service pour gérer les cartes de la ville
 * Contient la logique métier pour le traitement des cartes
 */
@Service
public class MapService {

    private CityMap currentMap;
    
    @Autowired
    private MapXmlParser mapXmlParser;

    /**
     * Parse un fichier XML contenant les données de la carte
     * @param file Le fichier XML uploadé
     * @return La carte parsée
     * @throws Exception Si le parsing échoue
     */
    public CityMap parseMapFromXML(MultipartFile file) throws Exception {
        // Déléguer le parsing au MapXmlParser
        CityMap map = mapXmlParser.parseMapFromXML(file);
        this.currentMap = map;
        return map;
    }

    /**
     * Récupère la carte actuellement chargée
     * @return La carte courante ou null si aucune carte n'est chargée
     */
    public CityMap getCurrentMap() {
        return currentMap;
    }

    /**
     * Vérifie si une carte est chargée
     * @return true si une carte est chargée, false sinon
     */
    public boolean hasMap() {
        return currentMap != null;
    }

    /**
     * Réinitialise la carte courante
     */
    public void clearMap() {
        this.currentMap = null;
    }
}

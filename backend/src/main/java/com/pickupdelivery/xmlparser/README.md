# Package XmlParser

Ce package contient les classes responsables du parsing des fichiers XML utilisés dans l'application.

## Architecture

Conformément au diagramme de packages, le package `xmlparser` :
- Est utilisé par le package `service` (dépendance)
- Produit des instances de classes du package `model`
- Est indépendant des packages `controller` et `tsp`

## Classes

### MapXmlParser
Parse les fichiers XML contenant les plans de ville avec leurs intersections et tronçons.

**Format XML attendu :**
```xml
<reseau>
  <noeud id="1" latitude="45.750404" longitude="4.8744674"/>
  <troncon origine="1" destination="2" longueur="145.5" nomRue="Rue de la République"/>
</reseau>
```

**Méthode principale :**
- `parseMapFromXML(MultipartFile file)` : Parse un fichier XML et retourne un objet `CityMap`

### DeliveryRequestXmlParser
Parse les fichiers XML contenant les demandes de livraison.

**Format XML attendu :**
```xml
<demandes>
  <demande adresseEnlevement="123" adresseLivraison="456" dureeEnlevement="300" dureeLivraison="240"/>
</demandes>
```

**Méthode principale :**
- `parseDeliveryRequestsFromXML(MultipartFile file)` : Parse un fichier XML et retourne une liste de `DeliveryRequest`

## Utilisation

Les parsers sont des composants Spring (`@Component`) injectés automatiquement dans les services :

```java
@Service
public class MapService {
    @Autowired
    private MapXmlParser mapXmlParser;
    
    public CityMap parseMapFromXML(MultipartFile file) throws Exception {
        return mapXmlParser.parseMapFromXML(file);
    }
}
```

## Gestion des erreurs

Les parsers peuvent lancer des exceptions :
- `Exception` générique si le parsing échoue
- Les services appelants sont responsables de gérer ces exceptions

## Extension future

Pour ajouter un nouveau type de parsing XML :
1. Créer une nouvelle classe dans ce package
2. Annoter avec `@Component`
3. Implémenter la logique de parsing
4. Injecter dans le service approprié

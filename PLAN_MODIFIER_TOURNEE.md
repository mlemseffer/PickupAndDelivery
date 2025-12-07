## Objectif
Refondre l’expérience “Modifier la tournée” pour gérer affectations, réaffectations et suppressions directement dans le panneau “Tournée calculée / Tournées multi-coursiers”, sans passer par la pop-up actuelle.

## Périmètre fonctionnel
- Vue globale : liste complète des demandes (assignées ou non), avec actions Réassigner et Supprimer.
- Vue par coursier : liste filtrée sur le coursier, actions Réassigner (y compris “Non assigné”), Supprimer, Ajouter une demande (depuis les non assignées).
- Gestion des demandes non assignées (contrainte 4h) : visibles en “Vue globale”, sélectionnables pour assignation.
- Suppression d’une demande : retire la demande et déclenche le recalcul (flux actuel via `handleDeliveryRequestSetUpdated`).
- Ajout (depuis vue coursier) : assigne une demande non assignée à ce coursier via réaffectation.

## UX cible
### Vue globale
- Tableau des demandes : pastille couleur, IDs pickup/livraison, durées, “Assigné à” (Nom coursier ou “Non assigné”), actions `Réassigner` `Supprimer`.
- Bouton “Réassigner” ouvre une modal simple : select coursier cible (ou “Non assigné”), bouton Confirmer/Annuler.
- Bouton “Supprimer” supprime la demande (confirmation légère).

### Vue “Coursier N”
- Tableau filtré : mêmes colonnes mais sans les autres coursiers.
- Bouton “Ajouter demande” : ouvre une modal listant les demandes non assignées (ou toutes avec filtre) -> sélection + Assignation au coursier courant.
- Bouton “Réassigner” par ligne : idem Vue globale.
- Bouton “Supprimer” par ligne : idem Vue globale.

### États & affichage
- Afficher un badge “Non assigné” pour les demandes unassigned.
- Si aucune demande sur un coursier : message vide + bouton “Ajouter demande”.
- Loading/disabled pendant appels API, messages d’erreur/succès non intrusifs.

## Modèle de données / état
- Source unique : `tourData` (array de tournées multi-coursiers) + `deliveryRequestSet.demands`.
- Besoin d’un mapping demande -> coursier :
  - Aujourd’hui l’assignation est implicite via la tournée retournée. Prévoir une fonction utilitaire pour déduire le coursier d’une demande (stops pickup/delivery) ou stocker `courierId` dans chaque demande lors du calcul/rendu.
  - Supporter `courierId = null` pour “Non assigné”.

## API & logique
- Réassignation : utiliser `apiService.updateCourierAssignment({ oldCourierId, newCourierId, deliveryIndex })` ou créer une variante par `demandId` si nécessaire (à valider côté backend).
- Suppression : `apiService.removeDemand(demandId)` puis `onDeliveryRequestSetUpdated` déclenche le recalcul (comportement existant).
- Ajout depuis non assignées : si backend ne gère pas “ajouter dans tournée”, utiliser `updateCourierAssignment` avec `oldCourierId = null`.
- Recalcul : conservé via `handleDeliveryRequestSetUpdated` (Front.jsx), spinner déjà géré par `isCalculatingTour`.

## Composants pressentis
- `TourTabs` : enrichir pour intégrer la nouvelle UI (tableaux + actions) et exposer callbacks.
- Nouveau tableau réutilisable (ex. `DemandAssignmentTable`) pour Vue globale et Vue coursier.
- Modals légères : `ReassignModal`, `SelectDemandModal` (pour “Ajouter demande”).
- Gestion d’état local dans `Front.jsx` ou `TourTabs` pour selected demand / modal open.

## Risques / points à clarifier
- Identifiant sûr pour une demande (backend retourne bien un `id` string ?), nécessaire pour réassignation/suppression.
- Comment inférer le coursier d’une demande dans `tourData` multi-coursiers (mapping à implémenter).
- API `updateCourierAssignment` attend `deliveryIndex` et `oldCourierId` d’une tournée mono ? Peut nécessiter adaptation backend ou un endpoint multi-coursiers.
- Cohérence des couleurs : garder la couleur de la demande en cas de réassignation.

## Plan d’exécution (pro)
1) Cartographier données d’affectation  
   - Étendre/ajouter un mapper `demandsWithCourier` (demandId -> courierId|null).  
   - Vérifier le format des tours multi-coursiers (stops/trajets) pour retrouver les demandes.
2) Conception UI/état  
   - Définir props/état pour `TourTabs` : liste demandes, mapping coursier, actions assign/remove, sélection de coursier courant.  
   - Choisir emplacement des modals (dans `Front.jsx` ou `TourTabs`).
3) Implémentation Vue globale  
   - Créer le tableau de demandes avec actions Réassigner/Supprimer.  
   - Modal de réassignation simple (select coursier + Non assigné).  
   - Brancher sur API/handlers et rafraîchir `tourData`/`deliveryRequestSet`.
4) Implémentation Vue coursier  
   - Tableau filtré par coursier.  
   - Bouton “Ajouter demande” + modal de sélection des non assignées -> assignation.  
   - Réutiliser réassignation/suppression.
5) Gestion des non assignées & recalcul  
   - S’assurer que suppression/assignation déclenchent le recalcul existant et mettent à jour les tableaux.  
   - États de chargement et erreurs.
6) QA rapide  
   - Scénarios : multi-coursiers, suppression d’une demande, réassignation vers un autre coursier, réassignation vers “Non assigné”, ajout d’une demande non assignée à un coursier, recalcul auto OK/KO.  
   - Vérifier affichage couleurs et badges.

## Livrables attendus
- UI mise à jour dans le panneau droit (Vue globale & Vue coursier).  
- Modals de réassignation/ajout fonctionnelles.  
- Mapping demande -> coursier fiable.  
- Gestion des non assignées et recalcul conservée.


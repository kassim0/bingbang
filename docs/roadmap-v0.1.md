# Roadmap — Terminer la V0.1 de bingbang

## Comment utiliser ce document

Cette roadmap part du wiki (`V0.1`, `Gestes métiers`, `What do I develope`, `Future ideas`) et de l'état du code au 14/11/2026, une fois le système de compte terminé.

Contrairement à la roadmap du compte, il n'y a **pas de commit de référence** à comparer : tout est à concevoir. Pour chaque étape :

1. Lis l'objectif, les concepts clés et les **décisions à prendre**. Tranche-les avant de coder (note ta décision dans la case prévue).
2. Écris le code toi-même ; demande de l'aide à Claude sur un concept ou une relecture, pas pour qu'il écrive à ta place.
3. Teste avant de passer à la suite : tests back (JUnit) + e2e (Playwright) + un test manuel réel.
4. Un commit par étape terminée, sur `dev` (ou une branche par phase si tu préfères).

---

## Vue d'ensemble

| Phase | Sujet | Estimation |
|---|---|---|
| 0 | Finir le système de compte (0a → 0c ✅, 0d à faire) | 1–2 j |
| 1 | Règle de création d'une liste | 1–2 h |
| 2 | Commentaires sur les jeux | 1–2 j |
| 3 | Fond d'écran personnel (gamesWallet) | ½–1 j |
| 4 | Partage en lecture seule | 2–3 j |
| 5 | Mettre le wiki à jour | 30 min |
| 6 | Nettoyer les comptes invités orphelins | ½ j |
| Après | Idées de `Future ideas` (types de listes, liste coop) | — |

**Total estimé : 6 à 9 jours**, à ton rythme.

Ordre conseillé : 0 → 1 → 2 → 3 → 4. Les commentaires (phase 2) passent avant le reste parce que c'est la raison d'être de l'app (« For each game in their gamesList, they can add a commentary »). Le partage (phase 4) vient en dernier : c'est le plus gros morceau, et il doit afficher les commentaires et le fond, donc autant qu'ils existent déjà.

---

## Phase 0 — Finir le système de compte

### 0a. Commiter l'inscription d'un invité

**État** : ✅ fait et commité (`496bc59`).

### 0b. Un invité qui se connecte à un compte existant garde ses listes

**État** : ✅ fait et commité

**Problème** : un invité crée des listes, puis se connecte à son compte existant (`/login`). Le front remplace le token : les listes de l'invité sont perdues (toujours en base, mais rattachées à un compte que plus personne ne peut ouvrir).

**Concepts clés** :
- Ici on ne peut **pas** transformer l'invité comme à l'inscription : le compte cible existe déjà. Il faut un vrai **transfert** des listes, puis la suppression de l'invité.
- Requête de mise à jour en masse : `@Modifying` + `@Query("update GamesList gl set ...")`, dans une transaction.
- **Positions** : le compte a déjà des listes en positions 1..n, l'invité aussi en 1..m. Après transfert, il faut placer celles de l'invité à la suite (n+1..n+m), sinon deux listes ont la même position.
- `/login` est public : comme pour `/register`, `@AuthenticationPrincipal` peut être `null`.

**Étapes** :
1. Dans `login`, récupérer le principal (peut être `null`).
2. Si c'est un invité : décaler les positions de ses listes de `findMaxOrder(compte)`, les rattacher au compte, supprimer l'invité. Le tout dans **une seule** transaction.
3. Si l'identifiant est faux : rien ne doit avoir bougé (l'invité garde ses listes et son token).

**Décision à prendre** : un invité sans aucune liste qui se connecte → on le supprime aussi ? (Conseil : oui, sinon les invités orphelins s'accumulent.)
> Ma décision : l'invité est toujours supprimé, même sans liste.

**Tests** :
- Back : invité avec 2 listes + compte avec 1 liste → après login, le compte a 3 listes en positions 1, 2, 3 et l'invité n'existe plus.
- Back : login invité avec mauvais mot de passe → 401, l'invité et ses listes sont intacts.
- E2E : un invité crée une liste, se connecte à un compte existant, retrouve sa liste (et après rechargement).

### 0c. Une réponse tardive de `/guest` ne doit pas écraser un vrai compte

**État** : ✅ fait et commité

**Problème** : au chargement sans token, le front appelle `/guest`. Si l'utilisateur se connecte avant la réponse (réseau lent), la réponse de `/guest` arrive après et remplace son vrai compte par un invité.

**Piste** : à la réception de `/guest`, ne connecter l'invité que si aucun utilisateur n'est connecté entre-temps (`connectedUser()` vide).

**Test** : difficile en e2e (il faut ralentir `/guest` avec `page.route`). C'est un bon exercice : intercepter `/api/auth/guest`, attendre la connexion, puis laisser passer la réponse, et vérifier que le pseudo est toujours affiché. Une fois corrigé, l'attente de l'invité dans `openRegisterForm` (`e2e/helpers.ts`), qui contourne ce bug, pourra être retirée.

### 0d. Points de sécurité de l'audit (auth)

**État** : ❌ à faire. Le merge de `claude/improvements` (#44) a appliqué l'audit **sauf** la partie auth, volontairement laissée de côté. Le détail de chaque point (problème, piste de correction) est dans `docs/AUDIT.md`.

| Point | Sujet |
|---|---|
| `SEC-03` | Pas de déconnexion côté backend, tokens très longs et non révocables |
| `SEC-04` | Secret JWT par défaut présent dans le dépôt |
| `SEC-05` | Création de comptes invités illimitée |
| `SEC-06` | Token stocké dans `localStorage` |
| `SEC-07` | Pas de limite de tentatives sur le login |
| `SEC-08` | Swagger et `/v3/api-docs` publics |
| `SEC-09` | `server.error.include-message=always` |

**Conseil d'ordre** : `SEC-04`, `SEC-08` et `SEC-09` sont de la configuration (rapides). `SEC-03` est le plus formateur (révocation : colonne `token_version` ou table de tokens révoqués : **décision à prendre**). `SEC-05` à `SEC-07` peuvent attendre l'ouverture au public, mais doivent être faits **avant**.
> Ma décision (SEC-03) :

---

## Phase 1 — Règle de création d'une liste

**Objectif (wiki)** : « for creating a gamesList they need at least a name or games ».

**État actuel** : une liste sans nom et sans jeu est acceptée et nommée « Liste N° x ».

**Concepts clés** :
- La règle porte sur **deux champs à la fois** : une annotation simple sur un champ (`@NotBlank`) ne suffit pas. Options : vérification dans le service (simple), ou contrainte de validation au niveau de la classe (plus avancé, à découvrir si tu veux).
- Ne jamais faire confiance au front seul : la règle doit être dans le back (400), le front ne fait qu'éviter l'erreur à l'utilisateur.

**Étapes** :
1. Back : `createGamesList` renvoie 400 si nom vide (ou que des espaces) **et** aucun jeu.
2. Front : le bouton de création est désactivé (ou la popup ne crée rien) tant qu'il n'y a ni nom ni jeu.

**Décision à prendre** : et à la modification ? Retirer tous les jeux d'une liste sans nom la rend « vide et sans nom ». On l'interdit, ou on la laisse (elle a déjà son nom « Liste N° x ») ?
> Ma décision :

**Tests** :
- Back : sans nom ni jeu → 400 ; avec nom seul → 201 ; avec jeux seuls → 201 avec le nom par défaut.
- E2E : la popup ne crée pas de liste vide sans nom.

---

## Phase 2 — Commentaires sur les jeux

**Objectif (wiki)** :
- Un commentaire par jeu dans une liste.
- Il est **conservé même si le jeu est retiré** de la liste ; si le jeu est ajouté à nouveau, le commentaire réapparaît.
- On le supprime en le supprimant lui-même (vider le texte, ou un bouton).
- Un même jeu dans plusieurs listes → un commentaire **différent** par liste, avec la possibilité de transférer un commentaire vers une autre liste.

### 2a. Modèle de données

**Concepts clés** :
- **Le piège** : mettre le commentaire dans `GamesListEntry`. Quand on retire le jeu, l'entrée est supprimée (`deleteGameByGamesListId`), donc le commentaire aussi. Le commentaire doit vivre dans **sa propre table**.
- Une entité `GameComment` reliée à la liste et au jeu (`@ManyToOne` vers `GamesList` et vers `Game`), plus le texte.
- **Unicité** : au plus un commentaire par couple (liste, jeu) → `@Table(uniqueConstraints = ...)`. C'est la base de données qui garantit la règle, pas seulement le code.
- **Suppression d'une liste** : ses commentaires doivent partir avec elle, sinon la clé étrangère bloque `deleteGamesListByIdAndUserId`. À toi de choisir comment (suppression explicite dans le service, ou cascade).
- **Taille du texte** : fixer une limite (`@Size` + `@Column(length = ...)`), sinon n'importe qui peut envoyer un texte de plusieurs Mo.

**Étapes** :
1. Créer l'entité et son repository ; vérifier la table générée par `ddl-auto=update` dans PostgreSQL.
2. Adapter la suppression d'une liste pour supprimer ses commentaires.

### 2b. API

**Concepts clés** :
- **Propriété** : comme pour les listes, un utilisateur ne peut lire ou modifier que les commentaires de **ses** listes (vérifier le propriétaire de la liste, sinon 404).
- « Ajouter » et « modifier » un commentaire peuvent être la même opération (créer s'il n'existe pas, sinon remplacer), souvent faite en `PUT`.
- **Renvoyer les commentaires avec la liste** : le plus simple pour le front est d'ajouter le commentaire à chaque jeu dans `GamesListDto` (champ `comment`, `null` si aucun). Attention au nombre de requêtes SQL : charger les commentaires d'une liste en une requête, pas une par jeu.

**Étapes** :
1. Écrire / modifier le commentaire d'un jeu d'une liste.
2. Supprimer un commentaire (texte vide → suppression, ou route dédiée : à toi de choisir, et de rester cohérent).
3. Exposer les commentaires dans `GamesListDto`.
4. Vérifier : retirer le jeu puis le rajouter → le commentaire est toujours là.

### 2c. Transfert vers une autre liste

**Décision à prendre** : « transférer » = **copier** (le commentaire reste aussi dans la liste d'origine) ou **déplacer** (il quitte la liste d'origine) ? Et si la liste cible a déjà un commentaire sur ce jeu : on l'écrase, on refuse (409), ou on concatène ?
> Ma décision :

**Concepts clés** :
- Les deux listes doivent appartenir à l'utilisateur.
- La liste cible doit-elle contenir le jeu ? D'après le wiki (« what if a same game is in multiple gamesList »), oui ; mais comme un commentaire survit au retrait, techniquement ce n'est pas obligatoire. Décide.

### 2d. Front

**Étapes** :
1. Dans la vue d'une liste (`my-game-list`), un champ de commentaire par jeu.
2. Enregistrer au moment opportun (bouton, ou perte de focus) ; afficher un retour (enregistré / erreur).
3. Proposer le transfert quand le jeu est présent dans une autre liste.

**Tests** :
- Back : écrire, modifier, supprimer un commentaire ; commentaire conservé après retrait puis ré-ajout du jeu ; deux listes avec le même jeu → deux commentaires indépendants ; liste d'un autre utilisateur → 404 ; supprimer une liste supprime ses commentaires ; texte trop long → 400 ; transfert selon ta décision.
- E2E : écrire un commentaire, recharger, il est là ; retirer le jeu, le rajouter, le commentaire réapparaît.

---

## Phase 3 — Fond d'écran personnel (gamesWallet)

**Objectif (wiki)** : « A gamesWallet = GamesList + special background ». La home doit être « a cozy place, his personal zone ».

**Décision à prendre (la plus importante de la phase)** :
- **Fonds prédéfinis** (une liste d'images ou de couleurs proposées par l'app) : simple, sûr, rien à stocker à part un identifiant.
- **Image envoyée par l'utilisateur** : bien plus de travail (stockage des fichiers, taille maximale, type de fichier vérifié côté serveur, suppression des anciens fichiers).
- **URL d'image fournie par l'utilisateur** : simple à stocker, mais l'image peut disparaître, et une URL doit être validée (uniquement `https://`, jamais `javascript:`).

Conseil : commencer par des fonds prédéfinis ; l'envoi d'image pourra venir plus tard.
> Ma décision :

**Concepts clés** :
- Un champ sur `User` (le fond appartient au compte, pas à une liste). Pense à `@ColumnDefault` pour les comptes existants (même problème que pour `guest`).
- Le renvoyer dans `/me` et dans la réponse d'authentification, pour que le front l'applique dès le chargement.
- L'invité a aussi un fond : comme l'inscription **transforme** l'invité, il le garde automatiquement. À la connexion d'un invité (phase 0b), décider : on garde le fond du compte (logique) ou celui de l'invité ?
- Angular : appliquer le fond à partir d'un signal (le fond change → la home change sans recharger).

**Tests** :
- Back : changer le fond ; valeur refusée (identifiant inconnu ou URL non `https`) → 400 ; le fond est renvoyé par `/me`.
- E2E : choisir un fond, recharger, il est toujours appliqué ; un invité qui s'inscrit garde son fond.

---

## Phase 4 — Partage en lecture seule

**Objectif (wiki)** :
- Partager tout son gamesWallet, une seule liste, ou une **sélection** de listes, avec un lien.
- La personne qui ouvre le lien voit la home avec les listes partagées (et le fond), **en lecture seule** : aucune modification possible.

### 4a. Modèle et API

**Concepts clés** :
- Une entité `Share` : un **token aléatoire** (UUID) qui sert d'identifiant dans le lien, le propriétaire, et ce qui est partagé (tout le wallet, ou une liste d'ids de listes → relation `@ManyToMany`).
- **Pourquoi un UUID et pas l'id de la liste** : avec `/share/12`, il suffit d'essayer 13, 14… pour voir les listes des autres. Un UUID ne se devine pas.
- « Tout le wallet » : le lien doit-il montrer aussi les listes créées **après** le partage ? (Conseil : oui, c'est le sens de « tout mon wallet ».) Pour une sélection, seulement les listes choisies.
- Une route **publique** (`permitAll`) `GET /api/share/{token}` : elle ne doit renvoyer que ce qui est nécessaire à l'affichage, **jamais l'email** ni l'id du propriétaire. C'est un DTO dédié, pas `GamesListDto` réutilisé tel quel s'il contient des informations privées.
- Une liste supprimée doit disparaître du partage (et le partage d'une sélection devenue vide ?).
- Token inconnu → 404.

**Étapes** :
1. Créer un partage (propriétaire seulement ; les listes choisies doivent lui appartenir).
2. Lire un partage (public).
3. Lister ses partages et en **révoquer** un (propriétaire seulement).

**Décisions à prendre** :
- Un invité peut-il partager ? (Techniquement rien ne l'empêche ; mais si son navigateur perd le token, il ne pourra plus révoquer le lien.)
- Le lien expire-t-il ?
- Les commentaires sont-ils visibles dans le partage ?
> Mes décisions :

### 4b. Front

**Concepts clés** :
- Le **routeur Angular** : aujourd'hui il n'existe que `/home`. Il faut une route `/share/:token`, lire le paramètre avec `ActivatedRoute`.
- Réutiliser les composants d'affichage existants (`apercu-games-list`) en mode lecture seule : un `@Input()` pour masquer les boutons, plutôt que dupliquer les composants.
- **Ne pas créer d'invité** pour quelqu'un qui ouvre un lien de partage ? Aujourd'hui tout visiteur sans token devient invité au chargement. Décide si c'est souhaitable sur `/share/...` (sinon chaque visiteur d'un lien crée un compte en base).
- SSR : la page de partage sera rendue côté serveur (bon pour l'aperçu du lien), mais les appels HTTP faits pendant le rendu serveur passent par le serveur Express ; vérifie qu'elle marche aussi en mode production (`server.ts`).
- Sélection de plusieurs listes à partager : cases à cocher sur la home, puis bouton « partager ».

**Tests** :
- Back : créer un partage de listes qui ne m'appartiennent pas → 404 ; lecture publique sans token → 200 sans email ; token inconnu → 404 ; partage révoqué → 404 ; partage « tout » inclut une liste créée après ; liste supprimée absente du partage.
- E2E : un utilisateur partage une liste, un **autre navigateur non connecté** (nouveau `browser.newContext()`) ouvre le lien et voit la liste sans aucun bouton de modification ; après révocation, le lien affiche une erreur.

---

## Phase 5 — Mettre le wiki à jour

La page `Gestes métiers` liste encore « delete gamesList » et « account » dans les fonctionnalités à ajouter. Une fois la V0.1 terminée :
- Mettre à jour `Gestes métiers` avec tout ce qui est possible dans l'app.
- Noter dans `V0.1` les décisions prises dans cette roadmap (commentaires, fond, partage).
- Compléter la phrase coupée de `Future ideas` (« idea of strategy, users can have 10 toke… »).

---

## Phase 6 — Nettoyer les comptes invités orphelins

**État** : ❌ à faire. Couvre le point (3) de `SEC-05` dans `docs/AUDIT.md`.

**Problème** : certains invités restent en base alors que plus personne n'a leur token :
- la réponse tardive de `/guest` ignorée depuis la phase 0c (le back a déjà créé l'invité) ;
- un navigateur qui perd son `localStorage` (navigation privée, données effacées, autre appareil) ;
- les visiteurs qui ouvrent la page une fois et ne reviennent jamais.

La table `users` grossit donc sans fin. (Un invité qui se connecte ou s'inscrit, lui, ne pose plus problème : il est supprimé en 0b, transformé en 0a.)

**Concepts clés** :
- **Tâche planifiée** : `@EnableScheduling` sur une classe de configuration, puis une méthode `@Scheduled(cron = "...")` dans un service. Elle tourne seule, sans requête HTTP, donc sans utilisateur connecté.
- **Savoir qu'un invité est abandonné** : on ne peut pas savoir si quelqu'un a encore son token. Il faut un critère basé sur le temps :
  - `createdAt` existe déjà, mais un invité actif depuis des mois serait supprimé avec ses listes ;
  - `updateAt` n'est mis à jour que si la ligne `users` change, pas quand l'invité utilise l'app ;
  - une colonne « dernière activité » (mise à jour par exemple sur `/me`, appelé à chaque chargement de page) est le critère le plus juste.
- **Ordre de suppression** : les listes référencent l'utilisateur (clé étrangère `user_id`), et les entrées (`games_list_entry`) référencent les listes. Une suppression en masse (`@Modifying @Query("delete ...")`) **ne déclenche pas les cascades JPA** : il faut supprimer les entrées, puis les listes, puis les invités, dans une seule transaction.
- **Ne jamais toucher un vrai compte** : la requête doit filtrer sur `guest = true`. C'est le point à tester en premier.

**Décisions à prendre** :
- Quels invités supprimer : seulement ceux **sans aucune liste** (prudent, ne perd jamais de données), ou aussi ceux **inactifs** avec des listes (nettoie vraiment, mais un invité qui revient après X jours a tout perdu) ?
- Le délai X (7 jours ? 30 jours ?) et la fréquence de la tâche (une fois par nuit suffit).
> Ma décision :

**Tests** :
- Back (appeler directement la méthode de nettoyage, pas attendre le cron) : un invité ancien sans liste est supprimé ; un invité récent est gardé ; un **vrai compte** ancien et inactif est gardé ; selon ta décision, un invité ancien avec des listes est gardé, ou supprimé avec ses listes et leurs entrées.

---

## Après la V0.1 — `Future ideas`

À ne pas commencer avant la fin de la V0.1, mais à garder en tête pendant la conception :

- **Types de listes** (wishlist, meilleurs jeux, pires jeux…) : un champ `type` sur `GamesList`. Le **score d'envie** de la wishlist est une donnée par jeu et par liste, exactement comme un commentaire : le modèle de la phase 2 te servira de base.
- **Types cachés à découvrir**, avec une récompense quand on en trouve un : il faudra garder en base quels types chaque compte a découverts.
- **Liste coop** : la seule liste partagée en **écriture**. Elle repose sur le partage de la phase 4, avec en plus des droits (qui peut modifier), des participants identifiés (donc un compte, au moins invité), et un score d'envie par participant. Sujet bien plus complexe (deux personnes qui modifient en même temps), à prévoir pour une V0.2.

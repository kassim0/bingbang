# Roadmap BingBang

Cette roadmap compare ce que décrit le wiki avec ce qui existe réellement dans le code de `dev`
(commit `496bc59`), puis découpe ce qui reste en phases ordonnées.

**Sources du wiki utilisées** : `V0.1`, `Gestes-métiers`, `Future-ideas` (pages d'origine) et leurs versions
plus récentes `v2-Features`, `v2-Decisions`, `v2-Future-ideas`, `v2-Vision`. Quand les deux versions diffèrent,
la `v2` fait foi (elle est plus précise et contient les décisions déjà prises).

---

## 1. Ce qui est fait / ce qui reste

Légende : ✅ fait · 🚧 partiel · ❌ pas commencé · 🔒 relève de l'auth (c'est toi qui le fais)

### Rechercher des jeux

| Règle du wiki | État | Preuve dans le code / remarque |
|---|---|---|
| Chercher un jeu par son nom dans un catalogue externe | ✅ | `GameSearchController`, `IgdbGameProvider`, `RawgGameProvider` ; source active choisie en base (`app_setting`) |
| Utiliser l'app sans créer de compte | ✅ | compte invité automatique (`AuthService.newGuest`, `auth.service.ts`) |

### GamesLists

| Règle du wiki | État | Preuve dans le code / remarque |
|---|---|---|
| Créer autant de listes que voulu | ✅ | `GameService.createGamesList` |
| Une liste a **au moins un nom ou des jeux** | 🚧 | le backend accepte une liste sans nom ni jeu (nom par défaut « Liste N° x ») ; le front, lui, refuse de créer une liste sans jeu, même avec un nom (`home.component.ts:53`) |
| La home montre toutes les listes avec leurs **3 premiers jeux** | ✅ | `apercu-games-list.component.html` |
| Modifier une liste : renommer, ajouter, retirer des jeux | ✅ | `GameService.updateGamesList`, `my-game-list.component` (mais un jeu peut être ajouté en double, AUDIT `ARCH-01`) |
| Supprimer une liste | ✅ | `GameService.deleteGamesList` (sans confirmation, AUDIT `FRONT-02`) |
| Un utilisateur ne voit et ne modifie que **ses** listes (404 sinon) | ✅ | `existsByIdAndUserId`, `deleteGamesListByIdAndUserId`, e2e `games-list-ownership.spec.ts` |

### Commentaires

| Règle du wiki | État | Remarque |
|---|---|---|
| Ajouter un commentaire sur chaque jeu d'une liste | ❌ | aucune table, aucune route |
| Le commentaire est **conservé si le jeu est retiré**, et réapparaît si on le rajoute | ❌ | impose de stocker le commentaire à part de la ligne « jeu dans la liste » |
| Supprimer un commentaire explicitement | ❌ | |
| Un même jeu dans deux listes a **deux commentaires différents** | ❌ | |
| Transférer un commentaire vers une autre liste | ❌ | questions ouvertes dans `v2-Decisions` (copier ou déplacer ? conflit ?) |

### GamesWallet

| Règle du wiki | État | Remarque |
|---|---|---|
| gamesWallet = toutes les listes + un **fond personnel** | ❌ | le fond est le même pour tout le monde (`styles.scss`) |
| Choisir le fond de la home | ❌ | question ouverte : fonds prédéfinis ou image envoyée ? |

### Compte

| Règle du wiki | État | Remarque |
|---|---|---|
| Sans compte : invité automatique, on retrouve son wallet sur le même navigateur | ✅ 🔒 | token invité de 3 ans en `localStorage` |
| Créer un compte et se connecter (wallet sur tous les appareils) | ✅ 🔒 | `AuthController` |
| Un invité qui crée un compte garde ses listes | ✅ 🔒 | `AuthService.updateGuestUser` |
| Un invité qui **se connecte à un compte existant** garde aussi ses listes | ❌ 🔒 | aujourd'hui les listes de l'invité sont perdues (son token est remplacé) |
| Se déconnecter | 🚧 🔒 | côté navigateur seulement ; pas de logout backend (AUDIT `SEC-03`) |

### Partage

| Règle du wiki | État | Remarque |
|---|---|---|
| Partager tout le gamesWallet avec un lien | ❌ | |
| Partager une seule liste avec un lien | ❌ | |
| Sélectionner plusieurs listes et les partager dans un seul lien | ❌ | |
| Les visiteurs d'un lien ne peuvent que **lire** | ❌ | |
| Révoquer un lien de partage | ❌ | |

### Idées futures (`Future-ideas`)

| Idée | État |
|---|---|
| Types de listes (wishList, bestGames, worstGames…) | ❌ |
| Score d'envie (+1) sur les jeux d'une wishList | ❌ |
| Découverte progressive des types + récompense | ❌ |
| Liste coop (modifiable à plusieurs) + score de « craving » + 10 jetons | ❌ |

**En résumé** : la recherche, les listes et les comptes sont faits. Il reste pour la V0.1 : la règle
« nom ou jeux », les **commentaires**, le **fond personnel** et le **partage** (plus deux points d'auth).
Les idées futures ne sont pas commencées.

---

## 2. Phases

Ordre choisi : d'abord ce qui est petit et débloque le reste, puis les fonctionnalités de la V0.1 dans l'ordre
où elles dépendent les unes des autres, puis les idées futures.

```
Phase 0 (fondations) ─► Phase 1 (règle nom/jeux) ─► Phase 2 (commentaires) ─┐
                                                                            ├─► Phase 4 (partage) ─► Phase 6 (coop)
                                              Phase 3 (fond personnel) ─────┘
Phase A (auth, par toi) : indépendante, en parallèle            Phase 5 (types de listes) : après la phase 2
```

### Phase 0 — Fondations techniques

- **Objectif** : rendre le modèle de données assez solide pour porter les commentaires et le partage.
- **Fonctionnalités** (aucune visible, ce sont des corrections de l'audit) :
  - un jeu ne peut être qu'**une seule fois** dans une liste (AUDIT `ARCH-01`) — indispensable, car un
    commentaire appartient à « un jeu dans une liste » ;
  - validation des données reçues (AUDIT `SEC-01`, `SEC-02`) ;
  - mock de recherche réaliste pour que les e2e tournent sans IGDB (AUDIT `ARCH-09`, `TEST-04`).
- **Backend** : `GameService` (ignorer les doublons), contraintes de validation sur les DTO.
- **Frontend** : rien d'obligatoire.
- **Base de données** : à terme, contrainte unique `(games_list_id, game_id)` sur `games_list_entry`.
  **Décision à prendre par toi** : passer à Flyway (AUDIT `ARCH-04`). Tant que `ddl-auto=update` reste en
  place, Hibernate crée les nouvelles tables tout seul, mais ne saura pas migrer des données existantes.
- **Dépendances** : aucune. Ces points sont appliqués sur `claude/improvements` ; `claude/features` n'en
  reprend que le strict nécessaire (pas de doublon dans une liste).
- **Critères de fin** : ajouter deux fois le même jeu ne crée qu'une entrée ; un payload invalide renvoie 400 ;
  tous les tests passent.

### Phase 1 — Terminer la règle « au moins un nom ou des jeux »

- **Objectif** : fermer le seul point 🚧 des GamesLists.
- **Fonctionnalités** :
  - créer une liste avec **seulement un nom** (sans jeu) depuis l'interface ;
  - refuser une liste **sans nom et sans jeu** (400) ;
  - refuser de renommer une liste avec un nom vide.
- **Décision proposée** pour la question ouverte « retirer tous les jeux d'une liste sans nom personnalisé :
  autorisé ? » : **oui**. Une liste a toujours un nom en base (le nom par défaut est enregistré à la création),
  donc la règle reste vraie même une fois vidée.
- **Backend** : validation dans `GameService.createGamesList` et `updateNameGamesList`.
- **Frontend** : la popup de création renvoie son résultat même sans jeu ; la home crée la liste si le nom ou
  les jeux sont présents.
- **Base de données** : aucun changement.
- **Dépendances** : aucune.
- **Critères de fin** : tests backend (nom seul → créé ; ni nom ni jeux → 400 ; renommage vide → 400), test
  e2e « crée une liste avec seulement un nom ».

### Phase 2 — Commentaires

- **Objectif** : pouvoir écrire ce qu'on pense d'un jeu, liste par liste (cœur de la vision : « garder une trace »).
- **Fonctionnalités** :
  1. ajouter / modifier un commentaire sur un jeu d'une liste ;
  2. supprimer un commentaire explicitement ;
  3. le commentaire survit au retrait du jeu et réapparaît si le jeu est rajouté ;
  4. un même jeu dans deux listes a deux commentaires indépendants ;
  5. transférer un commentaire vers une autre liste.
- **Décisions proposées** pour les questions ouvertes (à valider, faciles à changer) :
  - transfert = **copie** (le commentaire reste dans la liste d'origine ; on peut ensuite le supprimer) ;
  - si la liste cible a déjà un commentaire sur ce jeu : **refus (409)**, sauf si l'utilisateur confirme
    le remplacement ;
  - la liste cible **n'a pas besoin de contenir le jeu** (cohérent avec la règle 3 : le commentaire attend le jeu).
- **Backend** :
  - nouvelle entité `GameCommentary` (liste, jeu, texte, date) **séparée** de `GamesListEntry` : c'est ce qui
    permet au commentaire de survivre au retrait du jeu ;
  - routes : `PUT` et `DELETE /api/game/gamesList/{listId}/games/{gameId}/commentary`,
    `POST …/commentary/transfer` ;
  - `GamesListEntryDto` gagne un champ `commentary` ;
  - supprimer une liste supprime ses commentaires ;
  - contrôle d'appartenance identique aux listes (404 si la liste n'est pas à moi).
- **Frontend** : dans la popup d'une liste, une zone de commentaire par jeu (afficher, modifier, supprimer,
  « copier vers… »).
- **Base de données** : table `game_commentary (id, games_list_id, game_id, text, updated_at)` avec contrainte
  unique `(games_list_id, game_id)`.
- **Dépendances** : phase 0 (un jeu une seule fois par liste).
- **Critères de fin** : tests backend pour les 5 règles + appartenance ; test e2e « un commentaire réapparaît
  quand on rajoute le jeu ».

### Phase 3 — Fond personnel du gamesWallet

- **Objectif** : faire de la home « un endroit cosy et personnel » (page Vision).
- **Fonctionnalités** : choisir le fond de sa home parmi une sélection ; le retrouver à chaque visite et sur
  tous ses appareils.
- **Décision proposée** pour la question ouverte : **fonds prédéfinis** (des dégradés CSS, puis des
  illustrations). Raisons : pas de stockage de fichiers, pas de modération, pas de faille liée à l'upload,
  coût nul. L'upload d'image pourra venir plus tard (voir BUSINESS-MODEL : bon candidat pour une offre payante).
- **Backend** : entité `Wallet` (un par utilisateur, créée à la première utilisation) avec la clé du fond ;
  routes `GET /api/wallet` et `PUT /api/wallet/background`. Une entité à part évite de toucher à `User`
  (périmètre auth) et accueillera les futurs réglages du wallet.
- **Frontend** : sélecteur de fond sur la home ; le fond s'applique à la page ; liste des fonds dans un seul
  fichier.
- **Base de données** : table `wallet (id, user_id unique, background)`.
- **Dépendances** : aucune (peut se faire avant ou après la phase 2).
- **Critères de fin** : tests backend (valeur par défaut, changement, clé inconnue → 400, chacun son fond) ;
  test e2e « le fond choisi est toujours là après rechargement ».

### Phase 4 — Partage en lecture seule

- **Objectif** : montrer son wallet ou ses listes à quelqu'un avec un simple lien.
- **Fonctionnalités** :
  1. partager **tout le wallet** ;
  2. partager **une** liste ;
  3. partager **plusieurs** listes choisies, dans un seul lien ;
  4. la vue partagée est **en lecture seule** ;
  5. **révoquer** un lien ; voir la liste de ses liens.
- **Décisions proposées** pour les questions ouvertes :
  - *Un invité peut-il partager ?* **Oui** (l'app doit être utilisable « à 100 % » sans compte). Prévenir
    l'invité que son wallet est lié à ce navigateur.
  - *Un lien expire-t-il ?* **Non**, mais il est révocable à tout moment.
  - *Les commentaires sont-ils visibles ?* **Au choix à la création du lien**, masqués par défaut (un
    commentaire peut être personnel).
  - *Partage de tout le wallet : les listes créées après sont-elles visibles ?* **Oui** : le lien « wallet »
    est dynamique. Un lien « listes choisies » reste figé sur sa sélection.
  - *Le visiteur d'un lien devient-il un invité ?* **Aujourd'hui oui, de fait** : le front crée un invité pour
    tout visiteur sans token, et la route de lecture demande un utilisateur connecté. Voir l'encadré 🔒 ci-dessous.
- **Backend** :
  - entité `ShareLink` (jeton aléatoire non devinable, propriétaire, portée `WALLET` ou `LISTS`, listes
    choisies, option commentaires, date de révocation) ;
  - routes du propriétaire : `POST /api/share`, `GET /api/share`, `DELETE /api/share/{id}` ;
  - route de lecture : `GET /api/share/view/{token}` → 404 si le lien n'existe pas ou est révoqué (comme pour
    les listes : on ne confirme pas l'existence).
- **Frontend** : route `/share/:token` avec une vue sans aucun bouton de modification ; sur la home, un bouton
  « Share » (choix wallet / listes, copie du lien, liste des liens avec révocation).
- **Base de données** : tables `share_link` et `share_link_lists` (jointure lien ↔ listes).
- **Dépendances** : phase 2 (option « commentaires visibles ») et phase 3 (le fond fait partie du wallet partagé).
- **Critères de fin** : tests backend (portées, lecture seule, révocation → 404, lien d'un autre → 404, liste
  supprimée retirée du lien) ; test e2e « un autre utilisateur ouvre le lien et voit la liste sans pouvoir la modifier ».

> 🔒 **Partie auth à faire toi-même pour un partage vraiment public.** Tant que `SecurityConfig` exige un
> utilisateur connecté pour toute route hors liste blanche, la route de lecture passe par le compte invité du
> visiteur : ça fonctionne, mais chaque visiteur crée un compte invité en base (AUDIT `SEC-05`) et le rendu
> serveur (SSR) ne peut pas afficher la page (pas d'aperçu du lien dans une messagerie).
> Pour l'ouvrir : ajouter la route de lecture à la liste des routes publiques de `SecurityConfig` (là où se
> trouvent déjà `/api/games/search` et `/api/auth/login`), ne pas créer d'invité quand l'URL commence par
> `/share/`, puis écrire un test « GET sans token sur un lien valide → 200, sur toute autre route → 401 ».

### Phase A — Compte (🔒 auth : à faire par toi, en parallèle)

- **Objectif** : terminer les règles « Compte » du wiki et corriger les points de sécurité de l'audit.
- **Fonctionnalités** :
  - un invité qui se connecte à un compte existant garde ses listes ;
  - logout côté backend (AUDIT `SEC-03`) ;
  - points `SEC-04` à `SEC-09` de l'audit.
- **Comment aborder la fusion invité → compte existant** (sans le code) : au moment du login, si la requête
  porte aussi un token d'invité valide, tu connais deux utilisateurs : l'invité (token) et le compte (email +
  mot de passe). Dans une seule transaction : rattacher les listes de l'invité au compte (`UPDATE games_list
  SET user_id = …`), recalculer leurs positions à la suite des listes existantes, puis supprimer le compte
  invité. Questions à te poser : que faire si les deux ont une liste du même nom ? Faut-il demander
  confirmation à l'utilisateur avant de fusionner ? Écris d'abord le test (« un invité avec 1 liste se connecte
  à un compte qui en a 2 → le compte en a 3, l'invité n'existe plus »).
- **Base de données** : selon ton choix pour le logout (colonne `token_version` ou table de tokens révoqués).
- **Dépendances** : aucune ; mais à finir **avant** d'ouvrir l'app au public.
- **Critères de fin** : les lignes 🔒 du tableau « Compte » passent à ✅.

### Phase 5 — Types de listes et score d'envie (idées futures)

- **Objectif** : donner un comportement propre à certaines listes, en commençant par la wishList.
- **Fonctionnalités** :
  - une liste a un **type** : `STANDARD` (défaut), `WISHLIST`, `BEST`, `WORST` ;
  - dans une wishList, chaque jeu a un **score d'envie** qu'on augmente d'un clic (+1) ; la liste peut se trier
    par score ;
  - **découverte** : les types ne sont pas tous affichés d'emblée ; on les « débloque » (ex. nommer une liste
    « wishlist » propose le type) et chaque découverte donne une récompense (un fond exclusif de la phase 3).
- **Backend** : champ `type` sur `GamesList` ; champ `envyScore` sur `GamesListEntry` ; route
  `POST …/games/{gameId}/envy` ; table des types découverts par utilisateur.
- **Frontend** : choix du type, bouton « +1 », tri, écran des découvertes.
- **Base de données** : colonnes `games_list.type`, `games_list_entry.envy_score` ; table `discovered_type`.
- **Dépendances** : phase 0 ; phase 3 pour les récompenses.
- **Critères de fin** : une wishList se trie par envie ; le score survit au rechargement ; un type découvert le
  reste.
- **Point d'attention** : « ne pas montrer les types trop évidemment » est un vrai risque d'UX (voir IDEAS,
  « découverte guidée ») — à tester auprès de 3 ou 4 personnes avant de l'écrire.

### Phase 6 — Liste coop (idée future)

- **Objectif** : une liste que plusieurs personnes modifient ensemble (« à quoi on joue ce soir ? »).
- **Fonctionnalités** : inviter des participants par lien ; chaque participant ajoute des jeux et donne un
  score de « craving » ; chaque participant dispose de **10 jetons** à répartir ; classement des jeux.
- **Backend** : table des participants d'une liste et de leurs droits ; table des votes (participant, jeu,
  jetons) ; les contrôles d'accès passent de « propriétaire » à « propriétaire ou participant ».
- **Frontend** : écran d'invitation, affichage des votes, mise à jour quand un autre participant modifie
  (rechargement régulier d'abord, temps réel plus tard).
- **Base de données** : tables `list_member`, `list_vote`.
- **Dépendances** : phase 4 (liens), phase 5 (types), **phase A terminée** (droits d'accès plus fins = sécurité).
- **Critères de fin** : deux comptes modifient la même liste ; un participant ne peut pas dépenser plus de 10
  jetons ; un non-participant reçoit 404.
- **Point d'attention** : c'est la phase la plus coûteuse (droits, conflits de modification). L'idée « 10
  jetons » du wiki est incomplète : finir la règle sur papier avant de coder.

---

## 3. Ce qui est implémenté sur `claude/features`

Voir `docs/PROGRESS.md` pour l'état exact. Les phases sont traitées dans l'ordre 0 (minimum) → 1 → 2 → 3 → 4,
hors auth. Les phases A, 5 et 6 ne sont pas commencées.

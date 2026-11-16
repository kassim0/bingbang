# Audit du code BingBang

Audit réalisé sur la branche `dev` (commit `496bc59`), en lisant tout le code backend (Spring Boot)
et frontend (Angular), les tests et la configuration.

## Comment lire ce document

Chaque point a un identifiant (ex. `SEC-01`) et suit toujours le même format :

- **Où** : `fichier:ligne`
- **Problème** : ce qui ne va pas
- **Impact** : ce que ça provoque concrètement
- **Correction** : ce que je propose
- **Priorité** : haute / moyenne / basse
- **Auth** : `oui` = fait partie de l'authentification/JWT/sécurité → **je ne le modifie pas**, c'est toi qui le
  réécris. Pour ces points, j'ajoute une rubrique **« À faire toi-même »** qui explique la démarche sans donner le code final.

La case `[ ]` devant chaque point sera cochée `[x]` sur la branche `claude/improvements` quand le point y est appliqué.

### Ce que j'appelle « périmètre auth » (fichiers que je ne modifie pas)

- Backend : `security/*`, `controller/AuthController.java`, `service/AuthService.java`, `tools/AuthTools.java`,
  `entity/User.java`, `repository/UserRepository.java`, `dto/payload/LoginRequestDto.java`,
  `dto/payload/RegisterRequestDto.java`, `dto/response/AuthResponseDto.java`, `dto/response/UserDto.java`,
  `config/CorsConfig.java`, les propriétés `jwt.*` et les tests `AuthControllerTest` / `JwtServiceTest`.
- Frontend : `services/auth.service.ts`, `interceptors/auth.interceptor.ts`, `components/auth-dialog/*`,
  `models/user.model.ts`, et la partie connexion/déconnexion de `components/navbar/*`.

### État de départ mesuré (avant toute modification)

| Vérification | Résultat sur `dev` |
|---|---|
| `mvnw test` (backend) | ✅ 62 tests, 0 échec, 1 ignoré (`RawgApiRealTest`, sans clé RAWG) |
| `ng build` (frontend) | ✅ compile, avec un avertissement de budget (bundle 799 kB > 500 kB) |
| `ng test` (tests unitaires frontend) | ❌ **9 échecs sur 10** (voir `TEST-01`) |
| `playwright test` (e2e), backend en profil `mock` | ⚠️ 26 passent, 2 échouent **à cause du mock** (voir `TEST-04`) |

### Vue d'ensemble

| Catégorie | Haute | Moyenne | Basse |
|---|---|---|---|
| Sécurité | 5 | 8 | 4 |
| Architecture | 1 | 6 | 4 |
| Performance | 0 | 3 | 4 |
| Conventions et lisibilité | 0 | 4 | 8 |
| Tests | 1 | 4 | 3 |
| Frontend (UX technique, accessibilité) | 4 | 7 | 4 |
| Build / config | 1 | 6 | 6 |

---

## 1. Sécurité

### - [ ] SEC-01 — Les données d'un jeu envoyées par le navigateur sont enregistrées telles quelles
- **Où** : `backend/.../service/GameService.java:66-74` (`createGame`), `controller/JeuxRestController.java:25,36`
- **Problème** : quand on crée une liste, le navigateur renvoie au backend le nom, le slug, la date et l'URL
  d'image de chaque jeu. Le backend les enregistre dans la table `game` sans rien vérifier. Or cette table est
  **commune à tous les utilisateurs** (un jeu est retrouvé par son `igdbId`/`rawgId`).
- **Impact** : n'importe qui (même un invité) peut appeler l'API à la main et créer le jeu IGDB n° 1020 avec un
  faux nom et une image pointant vers son propre serveur. Tous les utilisateurs qui ajouteront ensuite ce jeu
  verront le faux nom, et leur navigateur chargera l'image de l'attaquant (ce qui lui révèle leur adresse IP).
  C'est un « empoisonnement du catalogue ».
- **Correction** : (1) valider les champs reçus (`@Valid`, tailles maximales, `externalId` et `source`
  obligatoires) ; (2) n'accepter une `imageUrl` que si elle pointe vers les hôtes d'images connus
  (`images.igdb.com`, `media.rawg.io`), sinon l'ignorer. À terme (plus sûr) : ne recevoir que
  `source + externalId` et redemander les informations au fournisseur côté serveur.
- **Priorité** : haute — **Auth** : non

### - [ ] SEC-02 — Aucune validation des payloads de listes (erreurs 500)
- **Où** : `dto/payload/NewGamesListDto.java:5`, `dto/payload/UpdateGamesListDto.java:7`,
  `controller/JeuxRestController.java:25,36`, `service/GameService.java:95,138`
- **Problème** : pas de `@Valid` ni de contraintes. `games: null` provoque une `NullPointerException` (ligne 95),
  un nom de 300 caractères provoque une erreur SQL (colonne `varchar(255)`), et un renommage en chaîne vide `""`
  est accepté (ligne 137 : seul `null` est testé).
- **Impact** : réponses 500 au lieu de 400, listes sans nom, et la règle du wiki « une liste a au moins un nom
  ou des jeux » n'est pas respectée.
- **Correction** : contraintes Bean Validation (`@Size`, `@NotNull`, `@Valid` en cascade) + refuser/ignorer un
  nom vide + limiter le nombre de jeux par requête.
- **Priorité** : haute — **Auth** : non

### - [ ] SEC-03 — Pas de déconnexion côté backend, tokens très longs et non révocables
- **Où** : `frontend/src/app/services/auth.service.ts:57-62`, `backend/.../security/JwtService.java:28-36`,
  `application.properties:25-26`
- **Problème** : « Se déconnecter » efface seulement le token du navigateur. Le backend n'a **aucune route
  de logout** et ne peut pas invalider un token : un token reste valable 1 mois (compte) ou 3 ans (invité),
  quoi qu'il arrive.
- **Impact** : si un token est volé (XSS, ordinateur partagé, copie du `localStorage`), le voleur garde l'accès
  jusqu'à l'expiration. Changer de mot de passe (quand ça existera) ne déconnectera personne.
- **Priorité** : haute — **Auth** : oui
- **À faire toi-même** : il faut que le serveur ait un moyen de dire « ce token ne vaut plus rien ». Trois
  approches, de la plus simple à la plus complète :
  1. **Version de token** : ajouter un compteur `tokenVersion` sur `User`, le mettre dans le token (un « claim »),
     et le comparer dans `JwtAuthFilter`. Le logout incrémente le compteur → tous les anciens tokens sont refusés.
     Simple, et tu charges déjà l'utilisateur à chaque requête.
  2. **Liste noire** : donner un identifiant unique à chaque token (claim `jti`) et stocker en base ceux qui sont
     révoqués, jusqu'à leur date d'expiration.
  3. **Access token court + refresh token** : un token d'accès de 15 minutes, et un « refresh token » stocké en
     base (révocable) qui sert à en redemander un. C'est le standard, mais c'est le plus long à écrire.
  Dans tous les cas : créer `POST /api/auth/logout` (authentifié), l'appeler depuis `AuthService.logout()` du
  front, et écrire un test « après logout, l'ancien token reçoit 401 ».

### - [ ] SEC-04 — Secret JWT par défaut présent dans le dépôt
- **Où** : `backend/src/main/resources/application.properties:24`
- **Problème** : si la variable `JWT_SECRET` n'est pas définie, l'application démarre quand même avec le secret
  écrit dans le fichier, qui est public sur GitHub.
- **Impact** : en production, un oubli de variable = n'importe qui peut fabriquer un token valide pour
  n'importe quel compte.
- **Priorité** : haute — **Auth** : oui
- **À faire toi-même** : supprimer la valeur par défaut dans le fichier principal (`jwt.secret=${JWT_SECRET}`),
  ce qui fait échouer le démarrage si elle manque (c'est voulu : « fail fast »). Garder un secret de dev dans un
  fichier de profil `application-dev.properties` ou dans ton `.env` local (déjà ignoré par git). Bonus : vérifier
  au démarrage que le secret fait au moins 32 octets.

### - [ ] SEC-05 — Création de comptes invités illimitée
- **Où** : `controller/AuthController.java:39-42`, `service/AuthService.java:113-117`,
  `frontend/src/app/services/auth.service.ts:16-18`
- **Problème** : chaque appel à `POST /api/auth/guest` crée une ligne dans `users`, sans limite ni nettoyage.
  Chaque visite sans token, chaque navigation privée et chaque déconnexion en crée une nouvelle.
- **Impact** : la table grossit sans fin ; un script peut créer des millions de comptes (saturation de la base).
- **Priorité** : haute — **Auth** : oui
- **À faire toi-même** : (1) créer l'invité **le plus tard possible** : seulement quand le visiteur fait sa
  première action qui a besoin d'un compte (créer une liste), pas à l'ouverture de la page ; (2) limiter le
  nombre de créations par adresse IP et par minute (un filtre avec un compteur en mémoire, ou la bibliothèque
  Bucket4j) ; (3) une tâche planifiée (`@Scheduled`) qui supprime les invités sans aucune liste créés depuis
  plus de X jours.

### - [ ] SEC-06 — Token stocké dans `localStorage`
- **Où** : `frontend/src/app/services/auth.service.ts:53`, `interceptors/auth.interceptor.ts:9`
- **Problème** : tout script qui s'exécute dans la page peut lire `localStorage`.
- **Impact** : une seule faille XSS (ou une dépendance npm compromise) suffit pour voler le token, valable
  jusqu'à 3 ans (voir `SEC-03`).
- **Priorité** : moyenne — **Auth** : oui
- **À faire toi-même** : l'alternative classique est un cookie `HttpOnly; Secure; SameSite=Lax` posé par le
  backend : JavaScript ne peut pas le lire. En échange il faut réactiver une protection CSRF. Si tu gardes
  `localStorage` (acceptable pour un projet d'apprentissage), réduis la durée des tokens et ajoute un en-tête
  `Content-Security-Policy` pour limiter les scripts autorisés.

### - [ ] SEC-07 — Pas de limite de tentatives sur le login
- **Où** : `controller/AuthController.java:29-32`
- **Problème** : on peut essayer des mots de passe à l'infini.
- **Impact** : attaque par force brute ou par liste de mots de passe volés.
- **Priorité** : moyenne — **Auth** : oui
- **À faire toi-même** : compter les échecs par email et par IP ; après N échecs, répondre `429 Too Many
  Requests` pendant quelques minutes. Commence par un compteur en mémoire (`ConcurrentHashMap`), et teste-le.

### - [ ] SEC-08 — Swagger et `/v3/api-docs` publics
- **Où** : `security/SecurityConfig.java:39`
- **Problème** : la documentation complète de l'API est accessible sans connexion, y compris en production.
- **Impact** : ça facilite le travail d'un attaquant (liste de toutes les routes et de leurs paramètres).
- **Priorité** : moyenne — **Auth** : oui
- **À faire toi-même** : n'ouvrir ces routes que dans un profil de dev (`springdoc.api-docs.enabled=false` par
  défaut, `true` dans `application-dev.properties`).

### - [ ] SEC-09 — `server.error.include-message=always`
- **Où** : `application.properties:29`
- **Problème** : le message de toutes les exceptions est renvoyé au client, pas seulement « Email already in use ».
- **Impact** : fuite possible d'informations internes (messages SQL, noms de classes). De plus le front n'utilise
  pas ce message : il se base sur le code HTTP (`auth-dialog.component.ts:92-104`).
- **Priorité** : moyenne — **Auth** : oui (réglage ajouté pour l'inscription)
- **À faire toi-même** : retirer la propriété, et renvoyer les erreurs métier avec un format maîtrisé grâce à
  un `@RestControllerAdvice` (voir `ARCH-06`). Vérifie ensuite que `AuthControllerTest` passe toujours.

### - [ ] SEC-10 — La recherche est publique, sans limite et sans cache
- **Où** : `security/SecurityConfig.java:38`, `controller/GameSearchController.java:21`
- **Problème** : `/api/games/search` ne demande pas de token, n'a pas de limite de débit, et le paramètre `name`
  n'a pas de taille maximale. Chaque appel consomme le quota IGDB/RAWG.
- **Impact** : un script peut épuiser le quota des API de jeux (et donc casser la recherche pour tout le monde).
- **Correction (hors auth)** : limiter la taille de `name` et mettre les résultats en cache quelques minutes
  (voir `PERF-01`). La limite de débit et le `permitAll` relèvent de la config de sécurité → à toi.
- **Priorité** : moyenne — **Auth** : partiel (taille + cache : non ; `permitAll` et rate limit : oui)

### - [ ] SEC-11 — Pas de délai maximum (timeout) sur les appels IGDB / RAWG / Twitch
- **Où** : `igdb/IgdbApi.java:11`, `igdb/TwitchAuthApi.java:10`, `rawg/RawgApi.java:9`, `application.properties`
- **Problème** : aucun timeout configuré pour Feign (valeurs par défaut : 10 s de connexion, 60 s de lecture).
- **Impact** : si IGDB est lent, chaque recherche bloque un thread du serveur pendant une minute ; quelques
  dizaines de recherches suffisent à rendre l'application indisponible.
- **Correction** : `spring.cloud.openfeign.client.config.default.connect-timeout=2000` et `read-timeout=5000`.
- **Priorité** : moyenne — **Auth** : non

### - [ ] SEC-12 — Identifiants de base de données en dur
- **Où** : `application.properties:11-13`
- **Problème** : URL, utilisateur et mot de passe (`postgres`/`test`) sont écrits dans le fichier.
- **Impact** : impossible de déployer sans modifier le fichier ; risque de commiter un vrai mot de passe un jour.
- **Correction** : `${DB_URL:...}`, `${DB_USERNAME:...}`, `${DB_PASSWORD:...}` avec les valeurs locales par
  défaut. Attention : `frontend/e2e/db.ts` lit ce fichier à la main, il faut l'adapter en même temps.
- **Priorité** : moyenne — **Auth** : non

### - [ ] SEC-13 — CORS : origine en dur et `allowCredentials(true)` inutile
- **Où** : `config/CorsConfig.java:17-20`
- **Problème** : seule `http://localhost:4200` est autorisée, et les cookies sont autorisés alors que l'app
  n'en utilise pas (le token passe par un en-tête). En plus le front passe par un proxy (`/api`), donc CORS
  ne sert pas aujourd'hui.
- **Impact** : confusion ; en production ce réglage sera soit inutile, soit faux.
- **Priorité** : basse — **Auth** : oui (config de sécurité)
- **À faire toi-même** : lire l'origine dans une propriété (`app.cors.allowed-origins`) et retirer
  `allowCredentials` tant que tu n'utilises pas de cookie.

### - [ ] SEC-14 — Course possible à l'inscription (500 au lieu de 409)
- **Où** : `service/AuthService.java:76,83,90,97`
- **Problème** : on vérifie que l'email est libre, puis on enregistre. Deux inscriptions simultanées avec le
  même email passent toutes les deux la vérification ; la seconde échoue sur la contrainte unique de la base.
- **Impact** : une erreur 500 au lieu d'un 409 propre (rare, mais réel).
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : attraper `DataIntegrityViolationException` autour du `save` et la transformer en 409.

### - [ ] SEC-15 — Le sujet du token est l'email
- **Où** : `security/JwtService.java:31`, `security/JwtAuthFilter.java:37-38`
- **Problème** : le token identifie l'utilisateur par son email. Quand un invité devient un vrai compte, son
  email change et son ancien token ne marche plus (d'où le nouveau token renvoyé). Le jour où tu ajouteras
  « changer d'email », tous les tokens de l'utilisateur casseront.
- **Impact** : fragilité future ; l'email (donnée personnelle) circule aussi dans chaque token.
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : mettre l'`id` de l'utilisateur dans le `subject` et charger l'utilisateur par id.

### - [ ] SEC-16 — Limite de 72 « caractères » pour bcrypt
- **Où** : `dto/payload/RegisterRequestDto.java:10`
- **Problème** : bcrypt ignore tout ce qui dépasse 72 **octets**, pas 72 caractères. Un mot de passe avec des
  accents ou des émojis peut dépasser 72 octets avec moins de 72 caractères.
- **Impact** : la fin du mot de passe est ignorée silencieusement (très rare).
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : valider la longueur en octets UTF-8 (un petit validateur personnalisé), ou garder la
  règle actuelle et documenter la limite.

### - [ ] SEC-17 — L'intercepteur ajoute le token à toutes les requêtes
- **Où** : `frontend/src/app/interceptors/auth.interceptor.ts:11`
- **Problème** : le token est ajouté sans regarder l'URL de destination.
- **Impact** : aujourd'hui aucun (toutes les requêtes vont vers `/api`). Le jour où le front appellera une autre
  API (ex. un CDN, une API tierce), le token y sera envoyé.
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : n'ajouter l'en-tête que si `req.url` commence par `/api`.

---

## 2. Architecture

### - [ ] ARCH-01 — Un même jeu peut être ajouté deux fois dans une liste
- **Où** : `service/GameService.java:153-168` (`addGamesInGamesList`), `service/GameService.java:95-101`,
  `entity/GamesListEntry.java` (pas de contrainte unique)
- **Problème** : rien n'empêche d'ajouter un jeu déjà présent. Ensuite, retirer ce jeu supprime **toutes** ses
  occurrences (suppression par `game_id`, `GamesListEntryRepository.java:19`).
- **Impact** : doublons visibles, et bientôt bloquant : un commentaire est attaché à « un jeu dans une liste »,
  ce qui suppose qu'il n'y est qu'une fois. (C'est aussi ce qui fait échouer 2 tests e2e avec le profil `mock`.)
- **Correction** : ignorer côté service les jeux déjà présents dans la liste (et les doublons à la création) ;
  puis ajouter une contrainte unique `(games_list_id, game_id)`.
- **Priorité** : haute — **Auth** : non

### - [ ] ARCH-02 — Routes REST non conventionnelles
- **Où** : `controller/JeuxRestController.java:17-41`, `frontend/src/app/services/game-api.service.ts:17-31`
- **Problème** : les URL contiennent des verbes (`/newGamesList`, `/getGamesLists`, `/updateGamesList`,
  `/deleteGamesList/{id}`), la création se fait en `PUT` et la modification en `POST` (c'est l'inverse de
  l'usage), et la ressource s'appelle `/api/game` alors qu'il s'agit de listes.
- **Impact** : API difficile à deviner et à documenter ; surprenant pour quiconque connaît REST.
- **Correction** : `POST /api/games-lists`, `GET /api/games-lists`, `PATCH /api/games-lists/{id}`,
  `DELETE /api/games-lists/{id}`. Garder les anciennes routes le temps de migrer le front et les tests e2e.
- **Priorité** : moyenne — **Auth** : non

### - [ ] ARCH-03 — La modification renvoie une phrase au lieu de la liste
- **Où** : `service/GameService.java:120-133,184-190`, `controller/JeuxRestController.java:36-45`
- **Problème** : `updateGamesList` renvoie `"Games list updated"` / `"Nothing to update"`. Le front doit donc
  refaire un `GET` de toutes les listes après chaque modification.
- **Impact** : deux requêtes au lieu d'une, et du code front plus compliqué (`subscribe` imbriqués).
- **Correction** : renvoyer le `GamesListDto` à jour ; `204 No Content` pour la suppression.
- **Priorité** : moyenne — **Auth** : non

### - [ ] ARCH-04 — Schéma de base géré par `ddl-auto=update`
- **Où** : `application.properties:17`
- **Problème** : Hibernate modifie la base tout seul au démarrage. Il sait ajouter une colonne, mais pas en
  renommer ni en supprimer une, ni migrer des données, et il n'y a aucun historique.
- **Impact** : risqué dès qu'il y a de vraies données ; les prochaines fonctionnalités (commentaires, partage)
  ajoutent des tables.
- **Correction** : Flyway (fichiers `V1__init.sql`, `V2__...`) + `ddl-auto=validate`. **Non appliqué
  automatiquement** : cela change ta façon de travailler et demande de « baseliner » ta base locale ; je préfère
  que tu le décides (voir ROADMAP, phase 0).
- **Priorité** : moyenne — **Auth** : non

### - [ ] ARCH-05 — Contraintes manquantes sur les tables de listes
- **Où** : `entity/GamesList.java:16-25`
- **Problème** : `user_id` et `name` peuvent être `NULL` ; `id` est un `long` primitif alors que les autres
  entités utilisent `Long` ; la relation `games` est un `@OneToMany` à sens unique avec `@JoinColumn`
  (Hibernate fait un `INSERT` puis un `UPDATE` par ligne) et sans `orphanRemoval`.
- **Impact** : une liste sans propriétaire est possible en base ; requêtes SQL en trop.
- **Correction** : `@ManyToOne(optional = false, fetch = LAZY)` + `@JoinColumn(nullable = false)`,
  `@Column(nullable = false)` sur `name`, `orphanRemoval = true`.
- **Priorité** : moyenne — **Auth** : non

### - [ ] ARCH-06 — Pas de gestion centralisée des erreurs
- **Où** : tout le backend (aucun `@RestControllerAdvice`)
- **Problème** : chaque service lance des `ResponseStatusException` ; les erreurs de validation et les erreurs
  inattendues ont le format par défaut de Spring.
- **Impact** : le front ne peut pas afficher un message fiable ; lié à `SEC-09`.
- **Correction** : un `@RestControllerAdvice` qui renvoie toujours `{status, message}` pour les erreurs de
  validation des routes hors auth.
- **Priorité** : moyenne — **Auth** : non (mais ne pas changer le comportement des routes `/api/auth`)

### - [ ] ARCH-07 — `GameService` fait deux métiers
- **Où** : `service/GameService.java` (192 lignes)
- **Problème** : la classe gère à la fois le catalogue de jeux (`findOrCreateGame`…) et les listes.
  Son nom ne dit pas qu'elle gère les listes ; le contrôleur s'appelle `JeuxRestController`.
- **Impact** : lisibilité ; la classe va grossir avec les commentaires et le partage.
- **Correction** : extraire `GameCatalogService` (trouver/créer un jeu) et renommer en `GamesListService` /
  `GamesListController`.
- **Priorité** : moyenne — **Auth** : non

### - [ ] ARCH-08 — Code mort
- **Où** :
  - `frontend/src/app/app.routes.ts:1-11,19-34` : `AppRoutingModule` jamais utilisé (l'app est « standalone ») ;
  - `backend/.../dto/rawgDto/` : 11 DTO jamais utilisés (`RawgAddedByStatusDto`, `RawgEsrbRatingDto`,
    `RawgGenreDto`, `RawgParentPlateformDto`, `RawgPlateformDetailDto`, `RawgPlateformDto`, `RawgRatingDto`,
    `RawgScreenshotDto`, `RawgStoreDetailDto`, `RawgStoreDto`, `RawgTagDto`) et 9 champs inutiles dans
    `RawgResponseDto.java:16-23` ;
  - `backend/src/main/resources/static/ResponseRawgGames.json` : 320 ko, référencé nulle part, embarqué dans le jar ;
  - `games-search-popup.component.ts:1-9,20,48-49` : imports inutilisés (`MatSnackBar`, `MatDialogTitle`…) et
    `@Input() gameNameSearch` inutile ;
  - `frontend/src/app/models/games.model.ts:23` : `gamesList` n'est jamais renvoyé par le backend ;
  - `app.component.ts:13` : `title = 'frontend'` ;
  - `test-results/.last-run.json` et `openapitools.json` à la racine (doublon de `frontend/openapitools.json`).
- **Impact** : bruit, temps de lecture, jar plus lourd.
- **Correction** : supprimer.
- **Priorité** : basse — **Auth** : non
- *(Auth, à toi)* : `UserRepository.findByUsername` (ligne 12) et `AuthService.getToken()` côté front
  (`auth.service.ts:64-69`) ne sont pas utilisés non plus.

### - [ ] ARCH-09 — Les mocks sont dans le code de production
- **Où** : `backend/src/main/java/com/stars/bigbang/mock/*`
- **Problème** : `IgdbApiMock`, `RawgApiMock`, `TwitchAuthApiMock` sont compilés dans le jar de production
  (activés par le profil `mock`). De plus ils renvoient **les 3 mêmes jeux quelle que soit la recherche**.
- **Impact** : faible en prod, mais le mock irréaliste fait échouer 2 tests e2e (voir `TEST-04`).
- **Correction** : rendre le mock réaliste (petit catalogue filtré par le texte cherché). Le garder dans `main`
  est acceptable tant qu'il sert à développer sans clé d'API.
- **Priorité** : basse — **Auth** : non

### - [ ] ARCH-10 — DTO utilisés dans les deux sens et types incohérents
- **Où** : `dto/payload/NewGamesListDto.java:5`, `dto/response/GameSearchResult.java`,
  `controller/JeuxRestController.java:27`
- **Problème** : `GameSearchResult` (package `response`) sert aussi de donnée d'entrée ; `games` est un tableau
  dans un DTO et une `List` dans l'autre ; un seul endpoint convertit l'entité en DTO dans le contrôleur, les
  autres le font dans le service.
- **Impact** : incohérences qui font hésiter à chaque nouvelle route.
- **Correction** : `List` partout, conversion en DTO dans le service partout.
- **Priorité** : basse — **Auth** : non

### - [ ] ARCH-11 — Texte d'interface généré par le backend
- **Où** : `service/GameService.java:106` (`"Liste N° " + position`)
- **Problème** : le nom par défaut d'une liste est écrit en français dans le backend.
- **Impact** : mélange des responsabilités ; impossible à traduire.
- **Correction** : à traiter avec la règle « au moins un nom ou des jeux » (ROADMAP phase 1).
- **Priorité** : basse — **Auth** : non

---

## 3. Performance

### - [ ] PERF-01 — Aucune mise en cache des recherches
- **Où** : `service/GameSearchService.java:47-59`
- **Problème** : chaque recherche appelle IGDB/RAWG, même si la même recherche vient d'être faite.
- **Impact** : latence (300–800 ms par recherche) et consommation du quota (IGDB : 4 requêtes/seconde ;
  RAWG gratuit : 20 000 requêtes/mois).
- **Correction** : cache en mémoire de quelques minutes, par (source, texte normalisé), avec une taille maximale.
- **Priorité** : moyenne — **Auth** : non

### - [ ] PERF-02 — Images trop lourdes pour des vignettes
- **Où** : `service/provider/IgdbGameProvider.java:20`, `apercu-games-list.component.html:6-14`,
  `game-item.component.html:2`, `my-game-list.component.html:8`
- **Problème** : on charge l'image `t_cover_big` (264×374) pour l'afficher en 35 ou 50 pixels, sans
  `loading="lazy"`.
- **Impact** : pages plus lentes, surtout sur mobile et avec beaucoup de listes.
- **Correction** : `loading="lazy"` + `decoding="async"` + `width`/`height` sur les `<img>`. Pour IGDB, il
  suffit de remplacer `t_cover_big` par `t_cover_small` dans l'URL au moment de l'affichage en vignette.
- **Priorité** : moyenne — **Auth** : non

### - [ ] PERF-03 — Rechargement complet après chaque action
- **Où** : `home.component.ts:57-61,72-76`, `my-game-list.component.ts:67-74`
- **Problème** : après une création, une modification ou une simple fermeture de popup, le front recharge
  toutes les listes avec tous leurs jeux.
- **Impact** : requêtes inutiles (même quand rien n'a changé) ; se résout avec `ARCH-03`.
- **Correction** : utiliser la réponse du serveur pour mettre à jour la liste concernée.
- **Priorité** : moyenne — **Auth** : non

### - [ ] PERF-04 — Requêtes SQL en trop lors d'une modification
- **Où** : `service/GameService.java:120-181`
- **Problème** : pour une modification : 1 `exists`, 1 `update`, 1 `delete` natif, 1 `select` + N `update`
  pour renuméroter, 1 `findById`, et jusqu'à 3 requêtes par jeu ajouté (`findOrCreateGame`).
- **Impact** : négligeable aujourd'hui (listes courtes).
- **Correction** : charger la liste une seule fois et la modifier en mémoire dans la transaction.
- **Priorité** : basse — **Auth** : non

### - [ ] PERF-05 — La home charge tous les jeux pour n'en afficher que 3
- **Où** : `repository/GamesListRepository.java:19-27`, `apercu-games-list.component.html`
- **Problème** : l'aperçu n'affiche que les 3 premiers jeux, mais la requête ramène tout.
- **Impact** : négligeable tant que les listes sont petites ; à surveiller.
- **Correction** : plus tard, un DTO « aperçu » (3 jeux + nombre total).
- **Priorité** : basse — **Auth** : non

### - [ ] PERF-06 — Polices chargées de façon bloquante
- **Où** : `frontend/src/index.html:8-9`
- **Problème** : deux feuilles Google Fonts sans `preconnect`.
- **Impact** : premier affichage retardé de quelques centaines de millisecondes.
- **Correction** : ajouter `<link rel="preconnect">` vers `fonts.googleapis.com` et `fonts.gstatic.com`.
- **Priorité** : basse — **Auth** : non

### - [ ] PERF-07 — Un accès base par requête pour l'authentification
- **Où** : `security/JwtAuthFilter.java:38`
- **Problème** : chaque requête authentifiée recharge l'utilisateur en base.
- **Impact** : faible ; c'est aussi ce qui permet de refuser un compte supprimé. À garder en tête.
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : rien pour l'instant. Si un jour ça compte : mettre `id` et `guest` dans le token pour
  éviter la lecture en base — mais tu perds la vérification « le compte existe encore » (voir `SEC-03`).

---

## 4. Conventions et lisibilité

### - [ ] CONV-01 — Mélange français / anglais dans l'interface
- **Où** : `navbar.component.html:12,23` (« Se déconnecter », « Invité »), `games-search-popup.component.html:4,7,12`
  (« Ajouter des jeux à », `list name`, `search your game`), `my-game-list.component.html:10,15,18,19`
  (`title="supprimer le jeu"`, `save`, `delete`), `apercu-games-list.component.html:2` (`empty`),
  `home.component.html:18`, `index.html:2` (`lang="en"`)
- **Problème** : l'utilisateur voit les deux langues sur le même écran.
- **Impact** : impression d'application pas finie ; les lecteurs d'écran prononcent mal (langue déclarée : anglais).
- **Correction** : choisir une langue. Le wiki et les messages d'erreur d'auth sont en anglais → tout passer en
  anglais (hors fichiers auth).
- **Priorité** : moyenne — **Auth** : non (les deux libellés du navbar touchent des `data-testid` d'auth : je ne
  change que le texte)

### - [ ] CONV-02 — Erreur de syntaxe dans un template
- **Où** : `my-game-list.component.html:9` (`... : '' " ]>`)
- **Problème** : un `]` en trop après l'attribut `[ngClass]`.
- **Impact** : Angular l'interprète comme un attribut nommé `]` ; HTML invalide.
- **Correction** : le retirer.
- **Priorité** : moyenne — **Auth** : non

### - [ ] CONV-03 — Aucun retour utilisateur ; `console.log` en production
- **Où** : `my-game-list.component.ts:99-102,111`, `games-search-popup.component.ts:74-76`
- **Problème** : les succès et les erreurs ne sont écrits que dans la console.
- **Impact** : voir `FRONT-03` et `FRONT-05` (l'utilisateur ne sait pas si son action a marché).
- **Correction** : `MatSnackBar` pour les messages, suppression des `console.log`.
- **Priorité** : moyenne — **Auth** : non

### - [ ] CONV-04 — `subscribe` imbriqués et abonnement dans un `effect`
- **Où** : `home.component.ts:32-41,57-61`, `my-game-list.component.ts:67-74`
- **Problème** : des `subscribe` dans des `subscribe` ; dans l'`effect`, une ancienne requête peut répondre
  après une plus récente (ex. invité → inscription).
- **Impact** : difficile à lire ; risque d'afficher les listes du mauvais état.
- **Correction** : `switchMap` (annule la requête précédente), et une seule méthode `reload()`.
- **Priorité** : moyenne — **Auth** : non

### - [ ] CONV-05 — Nommage et fautes de frappe
- **Où** : `home.component.ts:45` (`OpenGamesSearchPopup`, majuscule), `home.component.ts:65`
  (`openMyGaleListPopup`), `games-search-popup.component.ts:40` (`reponse`), `my-game-list.component.ts:95`
  (`saveModifGamesList`), `search-bar.component.ts:23` (`dataToParent`), `rawg/RawgApi.java:13` (`GameName`),
  `dto/payload/UpdateGamesListDto.java:7` (`removeGameId` est une liste), `repository/GamesListRepository.java:17`
  (`findMaxOrder` renvoie une position), dossier `components/share/` (→ `shared`),
  composant `apercu-games-list` (français), `controller/JeuxRestController.java` (français)
- **Problème** : noms qui ne disent pas ce qu'ils font, ou qui ne suivent pas la convention du langage.
- **Impact** : lecture plus lente.
- **Correction** : renommer (les renommages de fichiers/dossiers sont faits en dernier car ils créent des conflits).
- **Priorité** : basse — **Auth** : non
- *(Auth, à toi)* : `generateAuthReponse` (`AuthService.java:105`), `hashFactice` (`:28`), `updateAt` →
  `updatedAt` (`User.java:37`, attention : renommer la propriété renomme la colonne).

### - [ ] CONV-06 — Injection par `@AllArgsConstructor` sur des champs non `final`
- **Où** : `controller/GameSearchController.java:15,18`, `controller/JeuxRestController.java:18,22`
- **Problème** : les dépendances ne sont pas `final`, on pourrait les réassigner.
- **Impact** : faible ; incohérent avec les services, qui utilisent `@RequiredArgsConstructor` + `final`.
- **Correction** : `private final` + `@RequiredArgsConstructor`.
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-07 — `@Data` sur des entités JPA
- **Où** : `entity/Game.java:10`, `entity/GamesListEntry.java:11`, `entity/AppSetting.java:16`
- **Problème** : `@Data` génère `equals`/`hashCode`/`toString` sur tous les champs, y compris les relations
  paresseuses (`GamesListEntry.game`).
- **Impact** : un `toString()` ou un `equals()` peut déclencher des requêtes SQL ou une
  `LazyInitializationException` ; deux entités non enregistrées (id `null`) sont « égales ».
- **Correction** : `@Getter` + `@Setter` (comme `GamesList` et `User`).
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-08 — Deux syntaxes de templates mélangées, pas de `track`
- **Où** : `home.component.html:1,4` (`@if` puis `*ngFor`), `games-search-popup.component.html`,
  `my-game-list.component.html`
- **Problème** : ancienne syntaxe (`*ngIf`, `*ngFor`) et nouvelle (`@if`, `@for`) dans les mêmes fichiers ;
  les boucles n'ont pas de clé de suivi.
- **Impact** : cohérence ; sans clé, Angular recrée tout le DOM de la liste à chaque rechargement.
- **Correction** : `@for (...; track x.id)` partout.
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-09 — Styles en ligne dans les templates
- **Où** : `apercu-games-list.component.html:6,10,14`, `game-item.component.html:2`,
  `games-search-popup.component.html:11,16`, `my-game-list.component.html:8`, `home.component.html:18`
- **Problème** : `style="..."` dans le HTML alors que chaque composant a son fichier `.scss`.
- **Impact** : styles dispersés, dupliqués (la taille des vignettes est écrite 5 fois).
- **Correction** : déplacer dans les `.scss`.
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-10 — Aperçu écrit trois fois à la main
- **Où** : `apercu-games-list.component.html:5-16`
- **Problème** : le même bloc est copié pour les index 0, 1 et 2.
- **Impact** : toute modification doit être faite 3 fois.
- **Correction** : une boucle sur les 3 premiers jeux.
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-11 — Noms de packages et de champs hors convention Java
- **Où** : packages `dto/igdbDto`, `dto/rawgDto` ; champs `first_release_date`, `image_id`, `access_token`,
  `expires_in`, `background_image` (`IgdbGameDto.java:9`, `TwitchTokenDto.java:9`, `RawgResultsDto.java:5`) ;
  package racine `bigbang` alors que le projet s'appelle `bingbang`
- **Problème** : les packages Java sont en minuscules, les champs en camelCase.
- **Impact** : cosmétique.
- **Correction** : `@JsonProperty("first_release_date") Long firstReleaseDate`, packages `dto.igdb` / `dto.rawg`.
  Le package racine : ne pas y toucher (trop de conflits pour rien).
- **Priorité** : basse — **Auth** : non

### - [ ] CONV-12 — Dialogues : tailles en dur ignorées par le CSS
- **Où** : `home.component.ts:46-49,66-69`, `my-game-list.component.ts:51-54`, `styles.scss:163-169`
- **Problème** : les `width: '40%', height: '50%'` passés à `dialog.open` sont écrasés par des `!important`
  dans le CSS global.
- **Impact** : code trompeur (on croit régler la taille, ça ne fait rien pour la hauteur).
- **Correction** : retirer les hauteurs des `.ts`, une seule largeur maximale dans le CSS.
- **Priorité** : basse — **Auth** : non (le même défaut existe dans `navbar.component.ts:29-32`, à toi)

---

## 5. Tests

### - [ ] TEST-01 — Les tests unitaires du frontend sont cassés
- **Où** : `frontend/src/app/**/*.spec.ts` (10 tests), ex. `app.component.spec.ts:17-28`
- **Problème** : `ng test` donne **9 échecs sur 10**. Ce sont les fichiers générés par Angular CLI, jamais mis
  à jour : il manque les « providers » (`HttpClient`, `MatDialogRef`, `MAT_DIALOG_DATA`, animations), et
  `app.component.spec.ts` attend encore le titre « Hello, frontend ».
- **Impact** : aucune protection contre les régressions côté composants ; un test rouge en permanence habitue
  à ignorer les tests.
- **Correction** : réparer les specs (ajouter les providers de test), puis ajouter de vrais tests sur la logique
  des composants. Je ne supprime ni ne désactive aucun test.
- **Priorité** : haute — **Auth** : non (la spec de `auth-dialog` est réparée sans toucher au composant)

### - [ ] TEST-02 — Pas de test HTTP sur les routes de listes
- **Où** : `controller/JeuxRestController.java` (aucun `JeuxRestControllerTest`)
- **Problème** : seule la logique du service est testée, avec des mocks. Rien ne vérifie les codes HTTP
  (400 sur payload invalide, 404 sur la liste d'un autre) ni le JSON renvoyé.
- **Impact** : une erreur de mapping ou de validation passerait inaperçue.
- **Correction** : tests `@SpringBootTest` + `TestRestTemplate` (même style que `AuthControllerTest`).
- **Priorité** : moyenne — **Auth** : non

### - [ ] TEST-03 — Requêtes SQL personnalisées non testées
- **Où** : `repository/GamesListRepository.java:16-34`, `repository/GamesListEntryRepository.java:15-20`
- **Problème** : les requêtes JPQL/SQL natives ne sont exécutées par aucun test (les repositories sont mockés).
- **Impact** : une faute dans une requête n'est détectée qu'à l'exécution.
- **Correction** : couvert par les tests d'intégration de `TEST-02` (vraie base H2).
- **Priorité** : moyenne — **Auth** : non

### - [ ] TEST-04 — Les e2e dépendent d'un environnement manuel et de la vraie API
- **Où** : `frontend/playwright.config.ts:3-7`, `frontend/e2e/db.ts:9-36`, `mock/IgdbApiMock.java:16-22`
- **Problème** : il faut lancer à la main Postgres et le backend ; les recherches (`zelda`, `mario`…) appellent
  le vrai IGDB. Avec le profil `mock`, 2 tests échouent (`games-list-preview`, `modify-games-list`) car le mock
  renvoie les mêmes jeux pour toute recherche → le même jeu est ajouté deux fois (voir `ARCH-01`).
- **Impact** : tests impossibles à lancer en CI, et instables (réseau, quota).
- **Correction** : rendre le mock réaliste (`ARCH-09`), documenter la commande de lancement, et ajouter un
  `docker-compose.yml` pour Postgres.
- **Priorité** : moyenne — **Auth** : non

### - [ ] TEST-05 — Composants de fournisseurs peu testés
- **Où** : `service/provider/RawgGameProvider.java:46-55`, `service/provider/IgdbTokenService.java:33-49`
- **Problème** : pas de test pour le parsing de date RAWG, ni pour le cache/l'expiration du token Twitch.
- **Impact** : logique à risque (dates invalides, token expiré) non protégée.
- **Correction** : tests unitaires Mockito.
- **Priorité** : moyenne — **Auth** : non

### - [ ] TEST-06 — Sélecteurs e2e fragiles
- **Où** : `frontend/e2e/helpers.ts:101-103`, `frontend/e2e/modify-games-list.spec.ts:20`
- **Problème** : certains tests ciblent des classes CSS (`.game-item-container`, `.game-name`) ou la balise `ul`.
- **Impact** : un changement de style ou de balise casse les tests.
- **Correction** : `data-testid` (déjà utilisé ailleurs).
- **Priorité** : basse — **Auth** : non

### - [ ] TEST-07 — Logs SQL très bavards pendant les tests
- **Où** : `backend/src/test/resources/application.properties:9`
- **Problème** : `show-sql=true` écrit toutes les requêtes.
- **Impact** : la sortie des tests fait des milliers de lignes, les vraies erreurs sont noyées.
- **Correction** : `false` (à réactiver ponctuellement pour déboguer).
- **Priorité** : basse — **Auth** : non

### - [ ] TEST-08 — Pas de mesure de couverture
- **Où** : `backend/pom.xml`
- **Problème** : aucun rapport de couverture (JaCoCo).
- **Impact** : on ne sait pas quelles parties ne sont pas testées.
- **Correction** : plugin JaCoCo (rapport HTML dans `target/site/jacoco`), sans seuil bloquant au début.
- **Priorité** : basse — **Auth** : non

---

## 6. Frontend — UX technique et accessibilité

### - [ ] FRONT-01 — Le bouton « ✕ » de la recherche valide au lieu d'annuler
- **Où** : `games-search-popup.component.ts:58-64`, `games-search-popup.component.html:2`, `home.component.ts:52-55`
- **Problème** : il n'y a pas de bouton « Créer ». Pour créer la liste il faut cliquer sur la croix de fermeture.
  À l'inverse, cliquer à côté de la popup ou appuyer sur Échap annule tout sans prévenir. Et un nom saisi sans
  aucun jeu ne crée rien, en silence.
- **Impact** : l'utilisateur ne sait pas comment valider ; il perd sa sélection ou crée une liste sans le vouloir.
- **Correction** : un bouton explicite « Create list » / « Add games », la croix annule. Une liste avec
  seulement un nom doit pouvoir être créée (règle du wiki).
- **Priorité** : haute — **Auth** : non

### - [ ] FRONT-02 — Suppression d'une liste sans confirmation
- **Où** : `my-game-list.component.ts:106-113`, `my-game-list.component.html:19`
- **Problème** : un clic sur « delete » supprime définitivement la liste.
- **Impact** : perte de données irréversible sur un clic accidentel (le bouton est juste à côté de « save »).
- **Correction** : demander confirmation.
- **Priorité** : haute — **Auth** : non

### - [ ] FRONT-03 — « save » ne donne aucun signe de vie
- **Où** : `my-game-list.component.ts:95-104`
- **Problème** : après « save », la popup reste ouverte, les jeux barrés restent affichés, la sélection n'est pas
  vidée et rien n'indique le succès ou l'échec.
- **Impact** : l'utilisateur reclique, doute, ou ferme en pensant que rien n'est enregistré. À l'inverse,
  fermer sans « save » perd les changements sans prévenir.
- **Correction** : après succès : rafraîchir la liste affichée, vider la sélection, afficher un message ;
  en cas d'erreur : afficher un message.
- **Priorité** : haute — **Auth** : non

### - [ ] FRONT-04 — Éléments cliquables inaccessibles au clavier
- **Où** : `home.component.html:5-8` (carte de liste), `search-bar.component.html:4` (icône loupe)
- **Problème** : ce sont des éléments non interactifs avec un `(click)` : impossible de les atteindre avec
  Tab ni de les activer avec Entrée, et un lecteur d'écran ne les annonce pas comme des boutons.
- **Impact** : l'application est inutilisable au clavier (on ne peut pas ouvrir une liste).
- **Correction** : de vrais `<button>` (ou `role="button"` + `tabindex="0"` + gestion d'Entrée/Espace).
- **Priorité** : haute — **Auth** : non
- *(Auth, à toi)* : même problème pour les liens `<a (click)>` sans `href` de `auth-dialog.component.html:28,53`
  → utiliser `<button type="button">`.

### - [ ] FRONT-05 — Recherche : erreurs et « aucun résultat » invisibles
- **Où** : `games-search-popup.component.ts:66-78`, `games-search-popup.component.html:20-27`
- **Problème** : si la recherche échoue ou ne trouve rien, la zone de résultats reste vide, comme avant la recherche.
- **Impact** : l'utilisateur ne sait pas s'il doit attendre, réessayer ou changer de mot.
- **Correction** : trois états visibles : « No game found for … », « Search failed, try again », et l'état initial.
- **Priorité** : moyenne — **Auth** : non

### - [ ] FRONT-06 — Images sans texte alternatif, images cassées
- **Où** : `apercu-games-list.component.html:6,10,14`, `my-game-list.component.html:8`, `game-item.component.html:2`
- **Problème** : pas d'attribut `alt` ; si le jeu n'a pas d'image, `src="null"` affiche une icône d'image cassée.
- **Impact** : accessibilité ; rendu peu soigné pour les jeux sans jaquette.
- **Correction** : `alt=""` (image décorative, le nom est écrit à côté) et un bloc de remplacement sans image.
- **Priorité** : moyenne — **Auth** : non

### - [ ] FRONT-07 — Boutons-icônes sans nom accessible
- **Où** : `games-search-popup.component.html:2`, `my-game-list.component.html:1,10,15`, `home.component.html:12`,
  `game-item.component.html:4`
- **Problème** : des boutons qui ne contiennent qu'une icône ou un « + », sans `aria-label`.
- **Impact** : un lecteur d'écran annonce « close, bouton » (le nom de l'icône) ou « plus ».
- **Correction** : `aria-label` explicites (« Close », « Create a new list », « Add Portal 2 »…).
- **Priorité** : moyenne — **Auth** : non (le bouton compte du navbar, `navbar.component.html:6,18`, est à toi)

### - [ ] FRONT-08 — HTML invalide et hiérarchie de titres incohérente
- **Où** : `apercu-games-list.component.html:1-16`, `my-game-list.component.html:7-16`, `navbar.component.html:15,23,26`
- **Problème** : des `<ul>` qui contiennent directement `<img>`/`<span>`/`<button>` (sans `<li>`), un `<ul>`
  par jeu ; un `<h1>` par carte de liste en plus du `<h1>` du logo ; « empty » dans un `<h6>`.
- **Impact** : les lecteurs d'écran annoncent des listes vides et une structure de page fausse.
- **Correction** : `<ul><li>…</li></ul>`, `<h2>` pour le nom d'une liste, `<p>` pour « empty ».
- **Priorité** : moyenne — **Auth** : non (les `<h2>` du navbar : à toi)

### - [ ] FRONT-09 — Écran « Player not connected 🤖 » affiché pendant le chargement
- **Où** : `home.component.html:17-19`, `frontend/src/app/services/auth.service.ts:12-19`, `angular.json:39-42`
- **Problème** : tant que `/me` ou `/guest` n'a pas répondu, `connectedUser` vaut `null` et la home affiche
  « Player not connected ». Comme la page est pré-rendue côté serveur (SSR), ce texte est même dans le HTML
  initial. Il réapparaît après une déconnexion, jusqu'au rechargement.
- **Impact** : premier contact avec l'app = un message d'erreur qui clignote ; après un logout, l'app semble cassée.
- **Priorité** : moyenne — **Auth** : oui (c'est l'état d'authentification du front)
- **À faire toi-même** : distinguer trois états dans `AuthService` au lieu de deux : `loading`, `connected`,
  `anonymous` (par exemple un signal `status`). La home affiche un squelette pendant `loading`. Et après un
  logout, recréer tout de suite un invité (comme au démarrage) au lieu de rester sur `null`.

### - [ ] FRONT-10 — Pas d'état de chargement ni d'état vide sur la home
- **Où** : `home.component.html:1-16`, `home.component.ts:32-41`
- **Problème** : pendant le chargement des listes, et pour un nouvel utilisateur, la home n'affiche qu'une
  tuile « + » sans aucun texte. Une erreur de chargement n'est pas signalée.
- **Impact** : un nouveau visiteur ne comprend pas ce qu'est l'app ni quoi faire (voir IDEAS).
- **Correction** : état vide avec une phrase d'accueil et un bouton clair ; message en cas d'erreur.
- **Priorité** : moyenne — **Auth** : non

### - [ ] FRONT-11 — Jeux déjà dans la liste non signalés dans la recherche
- **Où** : `games-search-popup.component.html:23`, `game-item.component.ts:39-41`
- **Problème** : quand on ajoute des jeux à une liste existante, les résultats ne montrent pas ceux qui y sont déjà.
- **Impact** : doublons (voir `ARCH-01`).
- **Correction** : afficher ces jeux comme « déjà dans la liste » (icône cochée, bouton désactivé).
- **Priorité** : moyenne — **Auth** : non

### - [ ] FRONT-12 — Bundle initial de 799 kB (budget : 500 kB)
- **Où** : `frontend/angular.json:44-56`, sortie de `ng build`
- **Problème** : avertissement de budget à chaque build ; deux thèmes chargés (`indigo-pink.css` complet + surcharges).
- **Impact** : premier chargement plus lent sur mobile.
- **Correction** : charger les popups à la demande (`import()` dynamique des composants de dialogue).
- **Priorité** : basse — **Auth** : non

### - [ ] FRONT-13 — Pas de respect de `prefers-reduced-motion`
- **Où** : `frontend/src/styles.scss:123-129`, `home.component.scss:23-29`
- **Problème** : les animations de survol ne sont pas désactivées pour les personnes qui le demandent au système.
- **Impact** : inconfort pour certains utilisateurs.
- **Correction** : une règle `@media (prefers-reduced-motion: reduce)`.
- **Priorité** : basse — **Auth** : non

### - [ ] FRONT-14 — Recherche uniquement sur « Entrée », sans longueur minimale
- **Où** : `search-bar.component.html:3`, `search-bar.component.ts:33-35`
- **Problème** : une recherche vide est envoyée au backend (qui renvoie une liste vide) ; pas d'indication
  qu'il faut appuyer sur Entrée.
- **Impact** : hésitation au premier usage.
- **Correction** : ignorer les recherches vides, texte d'aide ; plus tard, recherche automatique après une pause
  de frappe (une fois le cache `PERF-01` en place).
- **Priorité** : basse — **Auth** : non

### - [ ] FRONT-15 — Formulaires d'auth : pas d'`autocomplete`, erreurs non annoncées
- **Où** : `auth-dialog.component.html:7,12,17,21,37,42,46`
- **Problème** : pas d'attributs `autocomplete` (`email`, `new-password`, `current-password`), pas de
  `role="alert"` sur le message d'erreur, titres « REGISTER »/« LOGIN » dans des `<p>`.
- **Impact** : les gestionnaires de mots de passe fonctionnent mal ; l'erreur n'est pas lue par un lecteur d'écran.
- **Priorité** : basse — **Auth** : oui
- **À faire toi-même** : ajouter les attributs `autocomplete`, `role="alert"` sur le message, un vrai titre
  `<h2 mat-dialog-title>`, et afficher la règle du mot de passe (8 à 72 caractères) **avant** l'erreur.

---

## 7. Build / config

### - [ ] BUILD-01 — `mvnw` n'est pas exécutable
- **Où** : `mvnw` (mode git `100644`)
- **Problème** : `./mvnw test` répond « Permission denied » sous Linux/macOS.
- **Impact** : première commande du projet en échec ; bloquera une CI.
- **Correction** : `git update-index --chmod=+x mvnw`.
- **Priorité** : haute (1 minute) — **Auth** : non

### - [ ] BUILD-02 — Deux versions de Springdoc
- **Où** : `backend/pom.xml:68-77`
- **Problème** : `springdoc-openapi-ui` 1.8.0 (prévu pour Spring Boot 2) est présent en plus de
  `springdoc-openapi-starter-webmvc-ui` 2.5.0 (Spring Boot 3).
- **Impact** : classes en double sur le classpath, jar plus lourd, comportements imprévisibles.
- **Correction** : supprimer la dépendance 1.8.0.
- **Priorité** : moyenne — **Auth** : non

### - [ ] BUILD-03 — Version de Java incohérente
- **Où** : `pom.xml:12` (21) et `backend/pom.xml:17` (17)
- **Problème** : le pom parent dit 21, le module dit 17 ; c'est le 17 qui gagne.
- **Impact** : confusion sur la version à installer.
- **Correction** : une seule valeur (17, celle réellement utilisée).
- **Priorité** : moyenne — **Auth** : non

### - [ ] BUILD-04 — Versions de dépendances gérées à la main
- **Où** : `backend/pom.xml:55-59,90-105,108-113`
- **Problème** : `spring-cloud-starter` 4.1.1, `spring-cloud-starter-openfeign` 4.1.0, `feign-core` et
  `feign-httpclient` 13.2.1, `junit-jupiter` 5.10.2 sont fixés un par un, alors que Spring Boot et le BOM
  Spring Cloud savent choisir des versions compatibles entre elles.
- **Impact** : risque d'incompatibilités à chaque mise à jour.
- **Correction** : importer le BOM `spring-cloud-dependencies` et retirer les versions ; retirer `feign-core`,
  `feign-httpclient` et `junit-jupiter` (déjà apportés par les starters).
- **Priorité** : moyenne — **Auth** : non

### - [ ] BUILD-05 — Versions en fin de vie
- **Où** : `backend/pom.xml:8` (Spring Boot 3.2.2, janvier 2024), `frontend/package.json:15-25` (Angular 17)
- **Problème** : ces versions ne reçoivent plus de correctifs de sécurité.
- **Impact** : failles connues non corrigées (Spring Security, Tomcat).
- **Correction** : au minimum la dernière 3.2.x ; puis planifier 3.5.x et Angular récent. **Non appliqué
  automatiquement** pour la montée majeure : Spring Security fait partie du périmètre auth, à valider avec tes tests.
- **Priorité** : moyenne — **Auth** : partiel

### - [ ] BUILD-06 — Pas d'intégration continue
- **Où** : pas de dossier `.github/workflows/`
- **Problème** : rien ne lance les tests automatiquement à chaque push.
- **Impact** : une régression peut arriver sur `dev` sans que personne ne la voie (cas de `TEST-01`).
- **Correction** : un workflow GitHub Actions : tests backend + build et tests frontend.
- **Priorité** : moyenne — **Auth** : non

### - [ ] BUILD-07 — README incomplet
- **Où** : `README.md`, `frontend/README.md`
- **Problème** : pas d'instructions d'installation (Postgres, variables `IGDB_CLIENT_ID`, `RAWG_API_KEY`,
  `JWT_SECRET`, profil `mock`, lancement des tests) ; `frontend/README.md` est le texte par défaut d'Angular ;
  la commande OpenAPI contient une faute (`-o .src/app/openApi`).
- **Impact** : impossible de lancer le projet sans lire le code.
- **Correction** : écrire un vrai README.
- **Priorité** : moyenne — **Auth** : non

### - [ ] BUILD-08 — Pas de `docker-compose` pour la base
- **Où** : racine du dépôt
- **Problème** : il faut installer et configurer Postgres à la main.
- **Impact** : friction au démarrage et pour les e2e.
- **Correction** : `docker-compose.yml` avec un Postgres préconfiguré (base `bingbang`, mot de passe `test`).
- **Priorité** : basse — **Auth** : non

### - [ ] BUILD-09 — `.gitignore` : faute et fichiers générés commités
- **Où** : `.gitignore:36` (`node_module` au lieu de `node_modules`), `test-results/.last-run.json`,
  `.claude/settings.local.json`
- **Problème** : la règle ne correspond à rien ; un fichier de résultat de test et un fichier de réglages
  locaux sont dans le dépôt.
- **Impact** : bruit dans les diffs.
- **Correction** : corriger la règle, ignorer `test-results/`, retirer le fichier du suivi git.
- **Priorité** : basse — **Auth** : non

### - [ ] BUILD-10 — Pas de linter ni de formateur
- **Où** : `frontend/package.json`, `backend/pom.xml`
- **Problème** : ni ESLint/Prettier côté front, ni Spotless/Checkstyle côté back.
- **Impact** : style irrégulier (indentation, guillemets simples/doubles mélangés).
- **Correction** : `ng add @angular-eslint/schematics` + Prettier. **Non appliqué** : reformater tout le code
  créerait des conflits avec tes branches en cours ; à faire à un moment calme.
- **Priorité** : basse — **Auth** : non

### - [ ] BUILD-11 — Pas de profils Spring
- **Où** : `backend/src/main/resources/application.properties`
- **Problème** : un seul fichier pour le dev et la prod (`ddl-auto=update`, `format_sql`, secrets par défaut).
- **Impact** : la config de dev partirait telle quelle en production.
- **Correction** : `application-dev.properties` / `application-prod.properties`. Lié à `SEC-04`, `SEC-08`,
  `SEC-12`, `ARCH-04` : à faire en même temps que ta partie auth.
- **Priorité** : basse — **Auth** : partiel

### - [ ] BUILD-12 — Outils du front désalignés
- **Où** : `frontend/package.json:42` (`@types/node` ^18), `README.md` (openapi-generator absent des dépendances)
- **Problème** : les types Node datent de la version 18 ; la commande OpenAPI du README dépend d'un outil non
  installé par `npm ci`, et le client généré n'est pas utilisé (les modèles sont écrits à la main).
- **Impact** : faible.
- **Correction** : aligner `@types/node` sur ta version de Node ; soit utiliser vraiment le client généré,
  soit retirer `openapitools.json`.
- **Priorité** : basse — **Auth** : non

### - [ ] BUILD-13 — SSR + prérendu pour une app entièrement derrière un token
- **Où** : `frontend/angular.json:39-42`, `frontend/server.ts`
- **Problème** : le rendu côté serveur ne peut pas connaître l'utilisateur (le token est dans `localStorage`),
  il ne rend donc que la coquille vide (voir `FRONT-09`), mais impose un serveur Node en production et des
  précautions partout (`isPlatformBrowser`).
- **Impact** : complexité sans bénéfice aujourd'hui. Il deviendra utile pour les **pages de partage publiques**
  (aperçu du lien, référencement).
- **Correction** : le garder (utile pour le partage, ROADMAP phase 4), mais le savoir. Rien à modifier maintenant.
- **Priorité** : basse — **Auth** : non

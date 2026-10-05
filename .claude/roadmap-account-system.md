# Roadmap — Reconstruire le système de compte de bingbang

## Comment utiliser ce document

L'objectif n'est pas de recopier ce qui existait avant, mais de le reconstruire en comprenant chaque brique. Pour chaque étape :

1. Lis l'objectif et les concepts clés.
2. Essaie d'écrire le code toi-même (demande de l'aide à Claude si tu bloques sur un concept, pas pour qu'il écrive à ta place).
3. Compare avec l'implémentation d'origine via la commande indiquée, **une fois que tu as essayé** :
   ```
   git show <hash>
   ```
   Ces commits existent toujours dans l'historique local du repo même si `dev` ne les contient plus (ils ont été annulés proprement par `git revert`, pas supprimés).
4. Teste avant de passer à l'étape suivante (compile/build + un test manuel réel, pas juste "ça compile").

Travaille sur la branche `feature/account-system` (déjà créée, basée sur `dev` sans aucun système de compte).

---

## Vue d'ensemble

| Phase | Sujet | Commits de référence | Estimation |
|---|---|---|---|
| 1 | Authentification de base (inscription/connexion JWT) | `92dd504` (back), `26af1c4` (front) | 4–6h |
| 2 | Chaque liste appartient à un compte | `8bf13e6` (back), `c318ea4` (front) | 2–3h |
| 3 | Compte invité (sans inscription) | `9c608ae` (back), `9d2fb05` (front) | 2–3h |
| 4 | Bugs réels à anticiper | `fd658a3`, `600b722`, `326c305` | 1–2h |

**Total estimé : 1 à 2 jours pleins**, en avançant à ton rythme et en comprenant chaque étape (pas en copiant).

Deux commits ne sont **pas** à refaire : `778abfd` et `34116fa` concernent la fusion des deux repos (`bingbang` + `bingbang-front` → dossier `frontend/`), un sujet totalement différent du système de compte. Ils sont expliqués en annexe pour le contexte, mais la fusion existe déjà et doit juste être conservée.

---

## Phase 1 — Authentification de base (inscription / connexion par JWT)

### 1a. Backend — commit de référence : `92dd504`

**Objectif** : un visiteur peut créer un compte (email + mot de passe) et se connecter, et reçoit un token prouvant son identité pour les requêtes suivantes.

**Concepts clés à comprendre avant de coder** :
- Pourquoi un mot de passe ne doit **jamais** être stocké en clair (hachage, `BCryptPasswordEncoder`).
- Ce qu'est un JWT : une chaîne signée contenant des informations (ici l'email), que le serveur peut vérifier sans avoir besoin de la stocker en base (contrairement à une session classique).
- Le principe "stateless" : chaque requête porte son propre token, le serveur ne garde aucun état de connexion entre deux requêtes.
- Le rôle de la **chaîne de filtres** de Spring Security : chaque requête HTTP traverse une série de filtres avant d'atteindre ton contrôleur ; l'un d'eux doit lire le token et authentifier la requête.

**Fichiers à créer** (package `com.stars.bigbang`) :
- `entity/User.java` — table `users` (id, username, email, password haché, createdAt). Attention : `user` est un mot réservé en PostgreSQL, d'où le nom de table `users`.
- `repository/UserRepository.java` — recherche par email, vérification d'unicité.
- `dto/record/RegisterRequestDto.java`, `LoginRequestDto.java` — payloads des requêtes (avec validation `@NotBlank`, `@Email`).
- `dto/response/UserDto.java`, `AuthResponseDto.java` — ce qu'on renvoie (jamais le mot de passe).
- `security/JwtService.java` — génère et valide les tokens (bibliothèque `jjwt`).
- `security/UserPrincipal.java` — adapte ton `User` à l'interface `UserDetails` de Spring Security (utilise l'email comme identifiant, pas le username).
- `security/CustomUserDetailsService.java` — charge un `User` par email pour Spring Security.
- `security/JwtAuthFilter.java` — filtre qui lit l'en-tête `Authorization: Bearer <token>`, vérifie le token, authentifie la requête.
- `config/SecurityConfig.java` — déclare la chaîne de filtres, les routes publiques (`/api/auth/**`) vs protégées, le `PasswordEncoder`.
- `service/AuthService.java` — logique métier : hacher le mot de passe à l'inscription, vérifier les identifiants à la connexion, générer le token.
- `controller/AuthController.java` — endpoints `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.

**Dépendances à ajouter** (`backend/pom.xml`) : `spring-boot-starter-security`, `jjwt-api`/`jjwt-impl`/`jjwt-jackson`, `spring-boot-starter-validation`.

**Pièges déjà rencontrés** (pour t'éviter de les redécouvrir à la dure, ou au contraire pour les laisser volontairement sur ta route si tu préfères apprendre en bloquant) :
- Par défaut, Spring Security cache le vrai message d'erreur ("No message available" au lieu de "Email already in use") → propriété `server.error.include-message=always`.
- Le CORS doit être configuré pour autoriser ton frontend (`http://localhost:4200`), sinon le navigateur bloque toutes les requêtes avant même qu'elles n'atteignent ton contrôleur.

**Test de validation** : `curl -X POST http://localhost:8080/api/auth/register -d '{"username":"test","email":"a@a.com","password":"password123"}' -H "Content-Type: application/json"` doit renvoyer un token.

---

### 1b. Frontend — commit de référence : `26af1c4`

**Objectif** : une popup de connexion/inscription, et le navbar qui reflète l'état connecté/déconnecté.

**Concepts clés** :
- Un **signal Angular** (`signal<User|null>`) pour représenter l'utilisateur courant, partagé entre tous les composants via un service `providedIn: 'root'`.
- Un **intercepteur HTTP** : du code qui s'exécute automatiquement sur chaque requête pour y ajouter l'en-tête `Authorization`.
- Pourquoi stocker le token dans `localStorage` pose un piège en SSR (Angular Universal/server-side rendering) : `localStorage` n'existe pas côté serveur, il faut vérifier `isPlatformBrowser` avant d'y toucher.

**Fichiers à créer** (`frontend/src/app/`) :
- `models/user.model.ts` — interfaces `User`, `LoginRequest`, `RegisterRequest`, `AuthResponse`.
- `services/auth.service.ts` — signal `currentUser`, méthodes `login()`, `register()`, `logout()`, `getToken()`.
- `interceptors/auth.interceptor.ts` — ajoute `Authorization: Bearer <token>` aux requêtes sortantes.
- `components/auth-dialog/` — popup de connexion/inscription (`MatDialog`, cohérent avec le style des autres popups du projet).
- Modifier `navbar.component.ts`/`.html` — il existe déjà un bloc HTML commenté prévu pour ça (`currentUser()`, `logout()`, `openAuthDialog()`) : regarde-le avant de tout réécrire.
- Modifier `app.config.ts` — enregistrer l'intercepteur via `provideHttpClient(withInterceptors([authInterceptor]))`.

**Piège à anticiper** (qui deviendra très important en Phase 3, autant le comprendre maintenant) : si ton intercepteur fait `inject(AuthService)` pour lire le token, et qu'`AuthService` lui-même déclenche un appel HTTP depuis son propre constructeur, tu obtiens une dépendance circulaire (Angular refuse de construire le service). Vois si tu tombes dedans par toi-même, sinon regarde `600b722` en Phase 4.

**Test de validation** : se connecter depuis l'interface doit faire apparaître ton pseudo dans le navbar.

---

## Phase 2 — Chaque liste appartient à un compte

### 2a. Backend — commit de référence : `8bf13e6`

**Objectif** : un utilisateur ne doit voir, modifier ou supprimer que ses propres listes.

**Concepts clés** :
- Ajouter une relation `@ManyToOne` entre `GamesList` et `User` (clé étrangère `user_id`).
- La différence entre "non authentifié" (401) et "authentifié mais pas propriétaire" (404 — on ne révèle même pas que la ressource existe).
- `@AuthenticationPrincipal` : comment un contrôleur Spring récupère l'utilisateur actuellement authentifié.

**Fichiers à modifier** :
- `entity/GamesList.java` — ajouter le champ `user`.
- `repository/GamesListRepository.java` — requêtes filtrées par utilisateur (`findAllByUser`, `findByIdAndUser`).
- `service/GameService.java` — chaque méthode prend l'utilisateur en paramètre, vérifie la propriété avant de modifier/supprimer.
- `controller/JeuxRestController.java` — injecter `@AuthenticationPrincipal UserPrincipal principal` sur chaque endpoint.
- `config/SecurityConfig.java` — les endpoints de listes ne sont plus publics, ils exigent une authentification.

**Test de validation** : crée un compte A et un compte B, vérifie que B ne peut pas voir/modifier/supprimer une liste créée par A (teste avec `curl` + deux tokens différents, pas seulement depuis l'interface).

### 2b. Frontend — commit de référence : `c318ea4`

**Objectif** : la page d'accueil ne charge/affiche les listes que si un utilisateur est connecté.

**Concepts clés** :
- Un `effect()` Angular qui réagit automatiquement aux changements d'un signal (ici `currentUser`), pour recharger les listes à chaque connexion/déconnexion sans code de synchronisation manuel.

**Fichiers à modifier** : `home.component.ts`/`.html`.

---

## Phase 3 — Compte invité (sans inscription)

### 3a. Backend — commit de référence : `9c608ae`

**Objectif** : un visiteur sans compte peut quand même créer des listes, retrouvées à sa prochaine visite.

**Concepts clés** :
- Un compte "invité" est un `User` comme un autre, avec un email/mot de passe générés aléatoirement (jamais communiqués) — toute la logique de propriété de la Phase 2 fonctionne alors sans rien changer.
- Pourquoi ce token a besoin d'une durée de vie différente (très longue, ~3 ans, puisqu'il n'existe aucun moyen de se "reconnecter" à un compte invité si le token est perdu).

**Fichiers à modifier** :
- `entity/User.java` — champ booléen `guest`.
- `security/JwtService.java` — une deuxième méthode de génération de token avec une expiration différente.
- `service/AuthService.java` — méthode `registerGuest()`.
- `controller/AuthController.java` — endpoint `POST /api/auth/guest`.
- `dto/response/UserDto.java` — exposer le champ `guest` pour que le frontend puisse adapter son affichage.

### 3b. Frontend — commit de référence : `9d2fb05`

**Objectif** : si aucun token n'est présent au chargement de la page, en créer un automatiquement, de façon invisible pour l'utilisateur.

**Fichiers à modifier** :
- `services/auth.service.ts` — dans le constructeur, si pas de token → appeler `/api/auth/guest` automatiquement.
- `navbar.component.ts`/`.html` — distinguer un vrai compte (menu + déconnexion) d'un compte invité (qui doit pouvoir créer un vrai compte à tout moment, donc garder le bouton "se connecter/s'inscrire" visible).

---

## Phase 4 — Bugs réels rencontrés (à lire après avoir fini, ou à utiliser comme grille de debug si tu tombes dedans)

Ces trois commits ne sont pas des fonctionnalités : ce sont de vrais bugs trouvés en testant l'application en conditions réelles. Les comprendre vaut autant que le code lui-même.

### `fd658a3` — Le bootstrap invité cassé en mode production (SSR)
En lançant le build de production (`npm run serve:ssr:frontend` au lieu de `npm start`), toutes les requêtes `/api/**` échouaient : le serveur Express (`server.ts`) ne les redirigeait jamais vers le backend (ligne laissée en commentaire depuis le tout début du projet). Leçon : un intercepteur ou un service qui marche en dev (`ng serve`, qui a son propre proxy) peut être cassé en prod pour une raison totalement différente (absence de proxy équivalent).

### `600b722` — La dépendance circulaire Angular (NG0200)
Expliquée en Phase 1b. Si l'intercepteur HTTP dépend du service qui déclenche lui-même des appels HTTP depuis son constructeur, Angular ne peut pas construire le service. Solution : l'intercepteur lit le token directement dans `localStorage`, sans passer par le service.

### `326c305` — Les erreurs transformées en 403 vides
Par défaut, Spring Security intercepte aussi la route interne `/error` utilisée pour formatter les réponses d'erreur (409, 401...), et la transforme en 403 vide si elle n'est pas explicitement autorisée (`permitAll`). Résultat : le frontend recevait un 403 sans aucun message, impossible à debugger sans creuser côté serveur.

---

## Annexe — Pour contexte seulement (ne pas refaire)

- `778abfd` : fusion de l'historique complet du repo `bingbang-front` dans `bingbang`, sous `frontend/` (via `git subtree`), pour n'avoir qu'un seul repo à terme.
- `34116fa` : mise à jour du README pour documenter cette nouvelle structure.

Ces deux commits sont indépendants du système de compte — ils concernent l'organisation des repos Git, pas l'authentification.

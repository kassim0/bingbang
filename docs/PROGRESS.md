# Suivi d'avancement

Ce fichier permet à une autre session de reprendre le travail si celle-ci est interrompue.

## Plan

| Étape | Branche | Contenu |
|---|---|---|
| 1 | `claude/docs` | `AUDIT.md`, `ROADMAP.md`, `IDEAS.md`, `BUSINESS-MODEL.md` |
| 2 | `claude/improvements` | application des points de `AUDIT.md` (hors auth) |
| 3 | `claude/features` | phases de `ROADMAP.md` (hors auth) |

Toutes les branches partent de `dev`. Rien n'est poussé sur `dev` ni `master`, aucune pull request.

## État

**Les trois étapes sont terminées.** Rien n'est en cours.

| Branche | Contenu | Tests (backend / unitaires front / e2e) |
|---|---|---|
| `claude/docs` | les documents de `docs/` uniquement (dont `GROWTH.md`), aucun changement de code | — |
| `claude/improvements` | 43 points d'audit appliqués, 8 en partie | 118 / 59 / 28 ✅ |
| `claude/features` | phases 1 à 4 de la roadmap (V0.1 complète hors auth) | 148 / 55 / 35 ✅ |

Le dossier `docs/` est identique sur les trois branches, sauf `GROWTH.md` (seulement sur `claude/docs`).

## Fait

### Étape 1 — Documents (branche `claude/docs`) ✅

- [x] `docs/AUDIT.md` — 83 points, dont 15 entièrement liés à l'auth et 3 partiellement (non modifiés, expliqués)
- [x] `docs/ROADMAP.md` — comparaison wiki / code, phases 0 à 6 + phase A (auth)
- [x] `docs/IDEAS.md` — 13 frictions du parcours utilisateur + 15 idées
- [x] `docs/BUSINESS-MODEL.md` — 3 modèles, recommandation : freemium « Wallet+ » en deux temps

### Étape 2 — Améliorations (branche `claude/improvements`) ✅

Sur les 83 points de l'audit : **43 appliqués**, **8 appliqués en partie**, **17 non appliqués** (raison écrite
sous chaque point dans `AUDIT.md`), **15 liés à l'auth** (non touchés, expliqués).

Commits, du plus prioritaire au moins prioritaire :

| Commit | Contenu |
|---|---|
| `build: rend mvnw exécutable` | BUILD-01 |
| `test: mock de recherche réaliste` | ARCH-09, TEST-04 — permet de lancer les e2e sans IGDB |
| `test: répare les tests unitaires du frontend` | TEST-01 (1/10 → 22 verts au moment du commit) |
| `fix(back): valide les données de listes et empêche les doublons` | SEC-01, SEC-02, ARCH-01, TEST-02, TEST-03 |
| `feat(front): popup de recherche…` | FRONT-01, FRONT-05, FRONT-11, FRONT-06, PERF-02, TEST-06 |
| `feat(front): popup de liste…` | FRONT-02, FRONT-03, CONV-02, CONV-03 |
| `feat(front): home…` | FRONT-04, FRONT-10, CONV-01, CONV-04 et petits points CONV |
| `perf(back): cache des recherches, timeouts…` | PERF-01, SEC-10, SEC-11, SEC-12, BUILD-02 à 05, ARCH-08, CONV-06/07, TEST-05/07 |
| `build: README, docker-compose, CI…` | BUILD-06 à 09, PERF-06, FRONT-13 |

État des tests sur la branche : 118 tests backend, 59 tests unitaires frontend, 28 e2e — tous verts.

À savoir :

- Le commit `perf(back): cache des recherches…` ne compile pas seul : la dépendance Caffeine du `pom.xml` est
  partie dans le commit suivant (`build: README…`) à cause d'une erreur de ma part dans la commande `git add`.
  Les deux commits ensemble sont cohérents ; ne fais pas de `cherry-pick` du premier sans le second.
- Les e2e ont été modifiés (pas désactivés) : la création de liste passe par le nouveau bouton
  `popup-confirm`, car la croix annule désormais.
- Aucun fichier du périmètre auth n'a été modifié, à deux exceptions de texte près dans
  `navbar.component.html` (« Se déconnecter » → « Log out », « Invité » → « Guest ») et l'ajout de
  providers dans `auth-dialog.component.spec.ts` pour que ce test existant passe.

### Étape 3 — Nouvelles fonctionnalités (branche `claude/features`) ✅

Un commit par fonctionnalité, avec ses tests :

| Commit | Contenu |
|---|---|
| 4 commits repris de `claude/improvements` | `mvnw` exécutable, mock réaliste, tests unitaires front réparés, validation + anti-doublon (phase 0) |
| `feat: une liste a au moins un nom ou des jeux` | phase 1 |
| `feat(back): commentaires…` puis `feat(front): commentaires…` | phase 2 |
| `feat: fond personnel du gamesWallet` | phase 3 |
| `feat(back): liens de partage…` puis `feat(front): partage…` | phase 4 |

Pourquoi 4 commits repris : `dev` avait des tests unitaires front en échec et des e2e impossibles à lancer
sans IGDB. Pour respecter « tests verts avant de modifier », la branche repart des mêmes correctifs que
`claude/improvements` (contenu identique, donc sans conflit entre les deux branches sur ces fichiers-là).

Non fait : phase A (auth, à toi), phases 5 (types de listes) et 6 (liste coop). Voir `ROADMAP.md`.

## Ce qui reste à faire

1. **Toi — auth** : les 15 points « Auth : oui » de `AUDIT.md` (priorité : `SEC-03` logout backend, `SEC-04`
   secret JWT, `SEC-05` invités illimités) et la phase A de `ROADMAP.md`.
2. **Toi — décisions** : valider ou changer les réponses proposées aux questions ouvertes du wiki
   (`ROADMAP.md`, phases 1 à 4) ; décider de Flyway (`ARCH-04`) avant d'avoir de vraies données.
3. **Fusion** des deux branches de code (voir ci-dessous), puis les points d'audit reportés à ce moment-là
   (`ARCH-02`, `ARCH-03`, `ARCH-07`, `ARCH-10`).
4. Mettre le wiki à jour (page Features) une fois les branches fusionnées.
5. Phases 5 et 6 de la roadmap.

## Conflits entre `claude/improvements` et `claude/features`

Liste **mesurée** (fusion d'essai des deux branches, non poussée) : 13 fichiers de code en conflit.
Ordre conseillé : fusionner `claude/improvements` dans `dev` d'abord, puis `claude/features`.

| Fichier | Zones | Pourquoi | Comment résoudre |
|---|---|---|---|
| `my-game-list.component.html` | 1 | improvements a réécrit la liste en `ul > li` ; features a ajouté `<app-game-commentary>` sous chaque jeu | garder la version improvements, et ajouter la balise `<app-game-commentary …>` juste après chaque `</li>` de jeu (dans la boucle `@for`) |
| `my-game-list.component.ts` | 2 | imports et tableau `imports` modifiés des deux côtés | garder improvements + ajouter `GameCommentaryComponent` aux imports |
| `home.component.html` | 1 | improvements a réécrit le template ; features a ajouté la barre `.wallet-tools` (partage + fond) | garder improvements, et remettre le bloc `<div class="wallet-tools">…</div>` en première ligne du `@if (connectedUser())` |
| `games-search-popup.component.ts` | 1 | improvements : `confirm()` / `cancel()` ; features : `close()` accepte un nom seul | garder improvements, et dans `confirm()` accepter aussi « nouvelle liste avec un nom non vide et aucun jeu » ; même condition pour `[disabled]` du bouton `popup-confirm` et dans `home.component.ts` (`afterClosed`) |
| `styles.scss` | 1 | deux blocs ajoutés au même endroit (reduced-motion / fonds du wallet) | garder les deux blocs |
| `GameService.java` | 1 | features ajoute la règle « nom ou jeux » dans `createGamesList` | garder les deux : le bloc `if (listName.isEmpty() && games.length == 0)` va juste après le calcul de `games` |
| `JeuxRestController.java` | 2 | improvements : `private final` + `@RequiredArgsConstructor` ; features : champ `commentaryService` | garder le style improvements et y ajouter `private final CommentaryService commentaryService;` + l'appel `withCommentaries(...)` |
| `JeuxRestControllerTest.java`, `GameServiceTest.java` | 1 + 1 | features ajoute des tests à la suite | garder les tests des deux côtés |
| 4 fichiers `*.spec.ts` (aperçu, popup de recherche, home, popup de liste) | 15 | les tests ont évolué avec les composants sur improvements | garder la version improvements ; reprendre de features les 3 tests « name only » de la popup en les adaptant à `confirm()` |

Après résolution, deux tests e2e de features sont à adapter au nouveau bouton : dans `commentary.spec.ts`,
remplacer `popup-close` par `popup-confirm` ; `create-games-list.spec.ts` (« only a name ») dépend du point
`games-search-popup.component.ts` ci-dessus.

Sans conflit mais à vérifier après fusion : `pom.xml` (Spring Boot 3.2.12 côté improvements) avec les
nouvelles entités de features — relancer `./mvnw test`.

## À signaler

- **`docs/GROWTH.md`** (acquisition, activation, rétention, KPI, plan d'action) : demandé ensuite, écrit sur
  `claude/docs` uniquement. C'est la seule différence du dossier `docs/` entre les trois branches.
- **État de `dev` au départ** : 9 tests unitaires front sur 10 en échec (specs Angular jamais adaptées).
- **Un commit de `claude/improvements` ne compile pas seul** (voir la section Étape 2).
- **Conséquence de la règle « branches créées depuis `dev` »** : `claude/features` garde l'ancienne interface
  (la croix qui valide, pas de confirmation de suppression…). Ces défauts sont corrigés sur
  `claude/improvements` ; ils disparaissent à la fusion.
- **Chiffres de `BUSINESS-MODEL.md`** (tarifs et conditions IGDB / RAWG, taux de conversion) : ordres de
  grandeur donnés de mémoire, non vérifiés en ligne. À contrôler avant toute décision.
- **Images dans mon environnement de test** : le conteneur ne pouvait pas charger les jaquettes d'IGDB ;
  l'affichage réel des images n'a donc pas été vérifié visuellement (les tests, eux, passent).

## Comment lancer les vérifications (utile pour reprendre)

```bash
# Backend (62 tests au départ)
sh mvnw -B test

# Frontend
cd frontend && npm ci && npx ng build
npx ng test --watch=false --browsers=ChromeHeadless   # 9 échecs sur 10 sur dev (voir AUDIT TEST-01)
npm run test:ci                                       # sur claude/improvements et claude/features

# E2E : il faut Postgres (base bingbang, postgres/test) + le backend lancé
java -jar backend/target/bigbang-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=mock
cd frontend && npx playwright test
```

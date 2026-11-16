# Suivi d'avancement

Ce fichier permet à une autre session de reprendre le travail si celle-ci est interrompue.

## Plan

| Étape | Branche | Contenu |
|---|---|---|
| 1 | `claude/docs` | `AUDIT.md`, `ROADMAP.md`, `IDEAS.md`, `BUSINESS-MODEL.md` |
| 2 | `claude/improvements` | application des points de `AUDIT.md` (hors auth) |
| 3 | `claude/features` | phases de `ROADMAP.md` (hors auth) |

Toutes les branches partent de `dev`. Rien n'est poussé sur `dev` ni `master`, aucune pull request.

## Étape en cours

**Étape 3 — Nouvelles fonctionnalités** (branche `claude/features`) — à démarrer

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

## Reste à faire

- Étape 3 — `claude/features`
- Résumé final dans ce fichier

## Comment lancer les vérifications (utile pour reprendre)

```bash
# Backend (62 tests au départ)
sh mvnw -B test

# Frontend
cd frontend && npm ci && npx ng build
npx ng test --watch=false --browsers=ChromeHeadless   # 9 échecs sur 10 sur dev (voir AUDIT TEST-01)
npm run test:ci                                       # sur claude/improvements (lanceur sans fenêtre)

# E2E : il faut Postgres (base bingbang, postgres/test) + le backend lancé
java -jar backend/target/bigbang-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=mock
cd frontend && npx playwright test
```

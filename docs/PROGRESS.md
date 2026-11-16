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

**Étape 2 — Améliorations** (branche `claude/improvements`) — à démarrer

## Fait

### Étape 1 — Documents (branche `claude/docs`) ✅

- [x] `docs/AUDIT.md` — 83 points, dont 15 entièrement liés à l'auth et 3 partiellement (non modifiés, expliqués)
- [x] `docs/ROADMAP.md` — comparaison wiki / code, phases 0 à 6 + phase A (auth)
- [x] `docs/IDEAS.md` — 13 frictions du parcours utilisateur + 15 idées
- [x] `docs/BUSINESS-MODEL.md` — 3 modèles, recommandation : freemium « Wallet+ » en deux temps

## Reste à faire

- Étape 2 — `claude/improvements`
- Étape 3 — `claude/features`
- Résumé final dans ce fichier

## Comment lancer les vérifications (utile pour reprendre)

```bash
# Backend (62 tests au départ)
sh mvnw -B test

# Frontend
cd frontend && npm ci && npx ng build
npx ng test --watch=false --browsers=ChromeHeadless   # 9 échecs sur 10 au départ (voir AUDIT TEST-01)

# E2E : il faut Postgres (base bingbang, postgres/test) + le backend lancé
java -jar backend/target/bigbang-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=mock
cd frontend && npx playwright test
```

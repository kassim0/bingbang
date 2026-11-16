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

**Étape 1 — Documents** (branche `claude/docs`)

- [x] `docs/AUDIT.md`
- [ ] `docs/ROADMAP.md`
- [ ] `docs/IDEAS.md`
- [ ] `docs/BUSINESS-MODEL.md`

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

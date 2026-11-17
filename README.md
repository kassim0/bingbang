# BingBang

BingBang est une application web pour garder la trace des jeux vidéo qu'on a aimés et de ceux auxquels on veut
jouer : son **gamesWallet** personnel. Le wiki du projet décrit la vision et les fonctionnalités.

- `backend/` : API Spring Boot (Java 17, PostgreSQL)
- `frontend/` : application Angular 17
- `docs/` : audit du code, roadmap, idées, modèles économiques, suivi d'avancement

## Prérequis

- Java 17 ou plus
- Node.js 20 ou plus
- Docker (pour la base de données), ou un PostgreSQL installé à la main

## Démarrer en local

```bash
# 1. La base de données (base "bingbang", utilisateur "postgres", mot de passe "test")
docker compose up -d

# 2. Le backend, sur http://localhost:8080
./mvnw -pl backend spring-boot:run -Dspring-boot.run.profiles=mock

# 3. Le frontend, sur http://localhost:4200 (les appels /api sont redirigés vers le backend)
cd frontend
npm ci
npm start
```

Le profil `mock` remplace les API de jeux par un petit catalogue local : pratique pour développer sans clé.
Sans ce profil, le backend appelle les vraies API et a besoin des variables ci-dessous.

## Configuration

Les secrets ne sont jamais commités. En local, mets-les dans `backend/.env` (ignoré par git), une ligne
`NOM=valeur` par variable ; ailleurs, définis-les comme variables d'environnement.

| Variable | Rôle | Valeur par défaut |
|---|---|---|
| `IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET` | accès à IGDB (compte développeur Twitch) | aucune |
| `RAWG_API_KEY` | accès à RAWG | aucune |
| `JWT_SECRET` | clé de signature des tokens de connexion | une clé de dev, **à remplacer en production** |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | connexion à PostgreSQL | la base locale ci-dessus |

La source de jeux utilisée pour la recherche se choisit en base, sans redémarrage :

```sql
UPDATE app_setting SET setting_value = 'RAWG' WHERE setting_key = 'game_provider'; -- ou 'IGDB'
```

## Tests

```bash
# Backend : base H2 en mémoire et fausses API, rien d'autre à lancer
./mvnw test

# Frontend : tests unitaires (une seule exécution, sans fenêtre)
cd frontend
npm run test:ci

# Tests de bout en bout (Playwright) : il faut la base et le backend lancés (étapes 1 et 2 ci-dessus)
npx playwright install chromium   # la première fois seulement
npm run e2e
```

Les e2e créent des comptes de test puis les suppriment de la base locale à la fin.

## Documentation de l'API

Backend lancé : http://localhost:8080/swagger-ui.html

## Idées de nom du projet

- pegi island
- bingbang

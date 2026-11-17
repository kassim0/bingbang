# BingBang — frontend

Application Angular 17 (composants « standalone », Angular Material, rendu côté serveur).
L'installation et le lancement de tout le projet sont décrits dans le [README à la racine](../README.md).

## Commandes

| Commande | Effet |
|---|---|
| `npm start` | serveur de développement sur http://localhost:4200, rechargé à chaque modification |
| `npm run build` | build de production dans `dist/` |
| `npm test` | tests unitaires dans un Chrome visible, relancés à chaque modification |
| `npm run test:ci` | tests unitaires, une seule exécution, sans fenêtre |
| `npm run e2e` | tests de bout en bout Playwright (backend lancé nécessaire) |
| `npm run e2e:ui` | les mêmes, dans l'interface de Playwright |

## Organisation

- `src/app/components/` : les écrans et leurs composants (`share/` contient ceux qui sont réutilisés)
- `src/app/services/` : appels à l'API (`game-api.service.ts`) et connexion (`auth.service.ts`)
- `src/app/models/` : types des données échangées avec le backend
- `src/app/testing/` : outils communs aux tests unitaires
- `e2e/` : tests Playwright
- `proxy.conf.json` : en développement, redirige `/api` vers `http://localhost:8080`

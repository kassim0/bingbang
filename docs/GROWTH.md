# Croissance de BingBang : attirer, activer, retenir

Ce document dit **comment faire venir des utilisateurs et les garder**, pour un développeur seul avec peu de
budget. Il s'appuie sur les autres documents :

- `IDEAS.md` : les frictions du parcours (`P-xx`) et les idées de fonctionnalités (`I-xx`) citées ici ;
- `BUSINESS-MODEL.md` : la stratégie en deux temps (gratuit + dons d'abord, freemium « Wallet+ » ensuite) ;
- `ROADMAP.md` : ce qui existe déjà (phases 1 à 4 sur `claude/features`) et ce qui reste.

> Les coûts et les efforts sont des **ordres de grandeur**. Effort : S = quelques heures, M = 1 à 3 jours,
> L = une semaine ou plus. Coût : en euros, hors ton temps.

## 0. Le principe de départ

Une app de listes de jeux n'a pas besoin de beaucoup d'utilisateurs pour réussir au début : elle a besoin
d'utilisateurs **qui reviennent**. Faire venir 1 000 personnes qui ne reviennent jamais ne sert à rien ; 50
personnes qui ouvrent leur wallet chaque semaine prouvent que l'app a de la valeur.

D'où l'ordre de ce document, qui est aussi l'ordre dans lequel travailler :

1. **Activation** : qu'un visiteur comprenne et aime l'app en moins de 2 minutes ;
2. **Rétention** : qu'il ait une raison de revenir ;
3. **Acquisition** : seulement ensuite, faire venir du monde.

C'est pourquoi le plan d'action final (partie 5) commence par l'activation, pas par la publicité.

### Le moteur de croissance propre à BingBang : le lien de partage

BingBang a un avantage que les outils de notes n'ont pas : **chaque liste partagée est une publicité**.
Quelqu'un partage « Mes 10 jeux préférés » sur Discord ; ses amis l'ouvrent ; une partie crée sa propre liste ;
et la partage à son tour. C'est la boucle :

```
créer une liste ─► la partager ─► un ami l'ouvre ─► « Create my own » ─► il crée une liste ─► il la partage ─► …
```

Cette boucle existe en partie depuis la phase 4 (`claude/features`). Trois choses manquent pour qu'elle
fonctionne vraiment (détaillées plus bas) :

- la page partagée doit être **publique** (aujourd'hui elle crée un compte invité pour chaque visiteur) —
  🔒 c'est dans ta partie auth (ROADMAP, phase 4, encadré) ;
- un **aperçu attrayant** du lien dans Discord/WhatsApp (image + titre : IDEAS `I-09`) ;
- un bouton **« Create my own »** bien visible sur la page partagée (aujourd'hui : « Go to my own wallet »).

---

## 1. Acquisition — attirer des utilisateurs

### Les canaux, du plus adapté au moins adapté

| # | Canal | Pourquoi il convient à BingBang | Coût | Effort | Délai avant effet |
|---|---|---|---|---|---|
| A1 | **Partage viral des listes** | le produit fait sa propre publicité (voir la boucle ci-dessus) | 0 € | M (aperçu de lien + page publique) | immédiat dès qu'il y a des utilisateurs |
| A2 | **Reddit** (r/patientgamers, r/gamingsuggestions, r/backlog, r/ShouldIbuythisgame, sous-reddits de jeux indés) | ces communautés passent leur temps à échanger des listes de jeux | 0 € | S par message, mais à faire avec soin | quelques jours |
| A3 | **Discord** (serveurs de jeux, de backlog, de jeux indés) | c'est là que les liens de listes circuleraient naturellement | 0 € | S à M | quelques jours |
| A4 | **SEO des listes partagées** | « meilleurs jeux cozy », « jeux comme Hades » : des recherches très fréquentes, auxquelles une liste publique répond | 0 € | M (page publique + rendu serveur + balises) | 3 à 6 mois |
| A5 | **Communautés de développeurs** (Show HN, dev.to, r/webdev, r/angular, r/java) | un projet perso Angular + Spring bien documenté intéresse ces publics ; bon pour le portfolio | 0 € | S (un article) | ponctuel (un pic) |
| A6 | **Petits créateurs de contenu** (YouTube/Twitch/TikTok 1 000 à 20 000 abonnés, jeux indés, « backlog ») | ils font déjà des « tier lists » et des « top 10 » ; BingBang leur donne une page à montrer | 0 à 50 € (ou un abonnement offert plus tard) | M (trouver, écrire, relancer) | quelques semaines |
| A7 | **Product Hunt**, annuaires d'outils (AlternativeTo, en face de Backloggd / Grouvee) | public curieux de nouveaux outils ; AlternativeTo capte ceux qui cherchent « une alternative à… » | 0 € | S | ponctuel |
| A8 | **Événements de jeux** (Steam Next Fest, soldes Steam, sorties très attendues) | moments où tout le monde fait des listes : « ma wishlist des soldes », « mes démos du Next Fest » | 0 € | S (un message bien placé au bon moment) | ponctuel, mais récurrent |
| A9 | **Publicité payante** (Reddit Ads, Meta) | ciblage possible, mais cher pour une app gratuite sans revenus | 100 € et + pour un premier test | M | immédiat |

**À éviter pour l'instant** : la publicité payante (A9). Sans revenus et sans rétention mesurée, chaque euro
dépensé fait venir des gens qui risquent de repartir aussitôt.

### Comment faire sur Reddit et Discord sans être vu comme du spam

Ces communautés rejettent l'auto-promotion. Ce qui marche :

- **Apporter d'abord du contenu**, pas un lien : « Voici mes 15 jeux indés de 2025 avec un mot sur chacun »,
  avec le lien BingBang en fin de message comme simple support. Les commentaires (phase 2) sont faits pour ça.
- **Raconter l'histoire du projet** : « J'en avais marre de perdre mes listes de jeux dans Google Keep, alors
  je me suis fait une app » est exactement l'histoire de la page Vision du wiki, et elle parle aux joueurs.
- **Lire les règles de chaque sous-reddit** (certains interdisent les liens vers ses propres projets, d'autres
  ont un fil hebdomadaire dédié).
- **Répondre à tous les commentaires** les premiers jours : c'est la meilleure source de retours gratuits.

### SEO des listes partagées : ce qu'il faut techniquement

Pour qu'une liste partagée apparaisse dans Google, il faut :

1. une page **publique**, accessible sans compte (🔒 partie auth : rendre `/api/share/view/**` public) ;
2. que le **rendu serveur** (SSR, déjà en place dans le projet — AUDIT `BUILD-13`) produise le contenu de la
   liste dans le HTML, ce qui suppose le point 1 ;
3. des balises `<title>`, `<meta name="description">` et Open Graph propres à chaque liste
   (« Les 10 jeux préférés d'Alex — BingBang ») ;
4. un **choix explicite** du propriétaire : « rendre cette liste trouvable sur Google » doit être une option,
   **désactivée par défaut** (une liste peut être personnelle ; et c'est une donnée personnelle au sens du RGPD).

Effort : M une fois la partie auth faite. Effet lent (plusieurs mois) mais durable et gratuit.

---

## 2. Activation — le « moment wow » de la première visite

**Le moment wow de BingBang** : voir apparaître *ses* jeux, avec leurs jaquettes, dans *sa* liste, en moins
d'une minute — et se dire « c'est mieux que ma note Google Keep ». Tout ce qui retarde ce moment fait perdre
des visiteurs.

### Ce qui est déjà en place

- **Mode invité automatique** : pas de formulaire d'inscription avant d'essayer (gros avantage face à
  Backloggd ou Grouvee qui demandent un compte).
- Sur `claude/improvements` : état vide accueillant (`FRONT-10`), bouton « Create list » explicite
  (`FRONT-01` / IDEAS `P-02`), confirmations avant perte de données.

### Ce qu'il faut ajouter, dans l'ordre

| # | Action | Problème résolu | Effort | Lien |
|---|---|---|---|---|
| AC1 | **Deux listes suggérées en un clic** sur l'état vide : « Games I loved » et « Games I want to play » | la page blanche ; ce sont exactement les deux notes d'origine du projet | S | IDEAS `P-01` |
| AC2 | **Recherche pendant la frappe** + année de sortie | il faut deviner « Entrée » ; on hésite entre les remakes | S | IDEAS `P-04` |
| AC3 | **Coller une liste de titres** (un par ligne) pour la créer d'un coup | le plus gros frein : ressaisir 30 jeux un par un. Un utilisateur de Keep a déjà sa liste en texte | M | IDEAS `I-07` |
| AC4 | **Fin de création qui félicite** : la liste s'affiche avec ses jaquettes et un bouton « Share it » | prolonge le moment wow et amorce la boucle de partage | S | — |
| AC5 | **Expliquer le mode invité au bon moment** (après la 1ʳᵉ liste, pas avant) | l'invité ne sait pas que tout est lié à ce navigateur | S | IDEAS `P-07` |
| AC6 | **Page partagée → « Create my own »** qui ouvre directement la création d'une liste | le visiteur d'un lien est l'utilisateur le plus facile à convertir | S | boucle de partage |

### Le premier parcours idéal (objectif : moins de 2 minutes)

1. Arrivée → une phrase qui dit ce qu'est l'app + « Games I loved » / « Games I want to play ».
2. Clic sur « Games I loved » → la recherche s'ouvre, déjà nommée.
3. Taper « hades » → résultats pendant la frappe → ajouter 3 jeux.
4. « Create list » → la carte apparaît avec les jaquettes. ✨
5. Message discret : « Share it with a friend » / « Saved on this browser — create a free account to keep it everywhere ».

Pas de tutoriel en plusieurs écrans : sur une app aussi simple, l'état vide bien fait **est** l'onboarding.

---

## 3. Rétention — des raisons concrètes de revenir

Le risque principal de BingBang : on crée ses listes une fois, puis on oublie l'app. Il faut des raisons de
revenir **liées à la vie réelle d'un joueur** (finir un jeu, attendre une sortie, chercher quoi jouer).

| # | Raison de revenir | Comment | Effort | Lien |
|---|---|---|---|---|
| R1 | **« J'ai fini un jeu »** | statut d'un jeu (à jouer / en cours / terminé) et « déplacer vers une autre liste » avec son commentaire. C'est le geste le plus fréquent d'un joueur | M | IDEAS `I-02` |
| R2 | **« Quand sort ce jeu ? »** | badge « sort dans 12 jours » sur les jeux à venir (la date est déjà en base), puis email optionnel le jour de sortie | S puis M | IDEAS `I-10` |
| R3 | **« Il est en promo ? »** | alerte de prix sur la wishlist. Fort pour la rétention, mais c'est le modèle B de `BUSINESS-MODEL.md` (affiliation) : à n'activer qu'à la demande de l'utilisateur, et pas avant d'avoir décidé du modèle | L | BUSINESS-MODEL, modèle B |
| R4 | **Wishlist et score d'envie** | « +1 » à chaque fois qu'on repense à un jeu : un geste minuscule, donc fréquent, qui donne une raison d'ouvrir l'app même sans rien ajouter | M | ROADMAP phase 5 |
| R5 | **« À quoi je joue ce soir ? »** | tirage au sort dans une liste, pondéré par le score d'envie | S (après R4) | IDEAS `I-08` |
| R6 | **Listes coop** | une liste partagée et modifiable à plusieurs (« nos jeux pour la soirée ») : on revient parce que **les autres** l'ont modifiée | L | ROADMAP phase 6 |
| R7 | **Découverte des types de listes + récompenses** | « Discoveries 2/5 » : il reste des choses à trouver ; chaque découverte débloque un fond exclusif (phase 3) | M | IDEAS `I-13`, ROADMAP phase 5 |
| R8 | **Le wallet comme objet personnel** | fonds (fait, phase 3), puis thèmes ; on revient voir et montrer « son » espace | fait, puis M | BUSINESS-MODEL, modèle A |
| R9 | **Bilan de fin d'année** (« Ton année de jeu ») | statistiques simples (jeux ajoutés, terminés, commentés) à partager — rétention **et** acquisition | M | — |

### Les rappels : utiles, mais avec mesure

Un rappel n'a de valeur que s'il apporte une information (« Silksong sort demain », « 3 jeux de ta wishlist
sont sortis ce mois-ci »). Règles proposées :

- **jamais** de rappel du type « Tu nous manques » ;
- uniquement pour les **comptes** (un invité n'a pas d'email) — c'est aussi un argument pour créer un compte ;
- **opt-in** explicite et désinscription en un clic (obligation légale) ;
- au maximum un email par semaine, regroupé.

Coût : un service d'envoi d'emails (gratuit jusqu'à quelques milliers d'emails par mois chez la plupart des
fournisseurs, à vérifier) + une tâche planifiée côté backend.

---

## 4. Indicateurs à suivre (KPI)

### Les 6 indicateurs qui comptent

| KPI | Définition | Pourquoi | Objectif réaliste au début |
|---|---|---|---|
| **Activation** | % des nouveaux visiteurs qui créent une liste avec au moins 1 jeu le jour même | mesure le moment wow | > 30 % |
| **Liste « riche »** | % des utilisateurs actifs avec au moins une liste de 5 jeux ou plus | une liste de 1 jeu n'est qu'un essai | > 40 % |
| **Rétention S1 / S4** | % des utilisateurs revenus la semaine suivante / 4 semaines après | **le** chiffre qui dit si l'app a de la valeur | S1 > 20 %, S4 > 10 % |
| **Conversion invité → compte** | % des invités ayant créé ≥ 1 liste qui créent un compte | mesure la confiance, et prépare le modèle freemium | > 10 % |
| **Coefficient de partage** | liens créés par utilisateur actif, et visiteurs par lien | fait tourner la boucle de croissance | > 0,2 lien / actif |
| **Conversion du partage** | % des visiteurs d'un lien qui créent leur propre liste | dit si la boucle fonctionne vraiment | > 5 % |

Pour comparer : si la rétention S4 est très basse, **inutile** d'investir dans l'acquisition — corriger
l'activation et la rétention d'abord.

### Comment les mesurer simplement

**Étape 1 — sans aucun outil, avec la base existante (gratuit, effort S).** Presque tout est déjà en base :

```sql
-- Nouveaux comptes (invités compris) par semaine
SELECT date_trunc('week', created_at) AS semaine, count(*) FROM users GROUP BY 1 ORDER BY 1;

-- Activation : comptes créés cette semaine qui ont au moins une liste avec un jeu
SELECT count(DISTINCT u.id) FROM users u
JOIN games_list gl ON gl.user_id = u.id
JOIN games_list_entry e ON e.games_list_id = gl.id
WHERE u.created_at > now() - interval '7 days';

-- Conversion invité -> compte : part des comptes non invités
SELECT guest, count(*) FROM users GROUP BY guest;

-- Partage (branche claude/features)
SELECT count(*) AS liens, count(DISTINCT user_id) AS partageurs FROM share_link;
```

**Ce qui manque en base** pour la rétention : une date de **dernière activité**. Proposition simple : une
colonne `last_seen_at` mise à jour au plus une fois par jour quand l'utilisateur charge ses listes
(`GET /api/game/getGamesLists`). ⚠️ La table `users` est dans ton périmètre auth : c'est à toi de l'ajouter,
ou de la mettre dans la table `wallet` (phase 3), qui est hors auth.

**Étape 2 — une mesure d'audience respectueuse de la vie privée (0 à 10 €/mois, effort S).** Un outil sans
cookie (Plausible, Umami auto-hébergé, GoatCounter…) donne les visites, les sources (Reddit ? Discord ?
Google ?) et quelques événements (« liste créée », « lien partagé », « lien ouvert »). Sans cookie de suivi,
pas de bandeau de consentement à afficher : important pour une app « cosy ». À vérifier selon l'outil choisi.

**Étape 3 — un tableau de bord d'une page**, mis à jour chaque semaine (même un tableur suffit) avec les 6 KPI.
Le regarder toujours le même jour pour voir la tendance, pas les variations d'un jour.

**À ne pas faire** : suivre les utilisateurs individuellement, revendre ou partager ces données, ou ajouter des
outils publicitaires de suivi. C'est contraire au positionnement du modèle recommandé (`BUSINESS-MODEL.md`) et
au RGPD.

---

## 5. Plan d'action : les 5 premières actions

Pour un développeur seul, peu de budget, et l'app dans son état actuel (`claude/improvements` +
`claude/features` à fusionner). Dans cet ordre :

### 1. Fusionner les branches et mettre l'app en ligne (effort M, ≈ 10 €/mois)

Rien ne peut être mesuré tant que l'app n'est pas accessible. Préalables :

- fusionner `claude/improvements` puis `claude/features` (conflits listés dans `PROGRESS.md`) ;
- 🔒 **les trois points d'auth bloquants** de l'audit : secret JWT obligatoire (`SEC-04`), limiter la création
  d'invités (`SEC-05`), logout backend (`SEC-03`) ;
- l'usage commercial des API n'est pas un problème tant que l'app reste gratuite (temps 1 de `BUSINESS-MODEL.md`).

**Fini quand** : une adresse publique fonctionne, et les requêtes SQL de la partie 4 donnent des chiffres.

### 2. Soigner le moment wow (effort S à M, 0 €)

AC1 (listes suggérées), AC2 (recherche pendant la frappe), AC5 (expliquer le mode invité), puis AC3 (coller
une liste de titres). **Fini quand** : 3 personnes de ton entourage qui ne connaissent pas l'app créent une
liste de 5 jeux en moins de 2 minutes, sans ton aide. Les observer en silence est le test le plus utile de tout
ce document.

### 3. Rendre le partage vraiment viral (effort M, 0 €)

- 🔒 page partagée publique (sans création d'invité) ;
- aperçu du lien avec image et titre (IDEAS `I-09`) ;
- bouton « Create my own » sur la page partagée (AC6).

**Fini quand** : un lien collé dans Discord affiche la jaquette des jeux et le nom de la liste, et un visiteur
peut créer sa liste en un clic depuis cette page.

### 4. Ajouter une raison de revenir (effort M, 0 €)

R1 (statut « terminé » + déplacer vers une autre liste) et R2 (badge de date de sortie). Ce sont les deux
moments de la vie d'un joueur où il pense spontanément à ses listes. **Fini quand** : la rétention S4 est
mesurable (colonne `last_seen_at` ou outil d'audience) et suivie chaque semaine.

### 5. Lancer, à petite échelle, là où sont les joueurs (effort S, 0 €)

Un premier message sur **2 ou 3** communautés seulement (un sous-reddit, un serveur Discord, et un « Show HN »
ou un article dev.to pour le portfolio), avec une vraie liste commentée comme contenu. Répondre à tout. Noter
d'où viennent les visiteurs. Recommencer au prochain événement (soldes Steam, Next Fest) seulement si
l'activation et la rétention sont bonnes.

**Fini quand** : tu sais quel canal amène des utilisateurs **qui reviennent** — c'est celui-là qu'il faudra
développer.

### Ce qui vient après (pas maintenant)

- SEO des listes publiques (A4) : quand il y aura plusieurs centaines de listes partagées ;
- créateurs de contenu (A6) : quand l'image de partage et la page publique seront jolies ;
- wishlist + score d'envie + découverte des types (R4, R7) : ROADMAP phase 5 ;
- passage au freemium « Wallet+ » : selon les critères du temps 2 de `BUSINESS-MODEL.md`.

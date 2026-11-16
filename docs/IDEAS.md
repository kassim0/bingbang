# Idées pour BingBang

Deux parties :

1. **Analyse du parcours utilisateur** : où quelqu'un peut se perdre, hésiter, abandonner ou mal comprendre.
2. **Nouvelles fonctionnalités** qui ne sont pas dans le wiki.

Chaque idée suit le même format : **problème résolu**, **proposition**, **effort** (S = quelques heures,
M = 1 à 3 jours, L = une semaine ou plus) et **valeur pour l'utilisateur** (★ à ★★★).

Les observations viennent de la lecture du code de `dev` et de l'utilisation réelle de l'app (via les tests
e2e). Quand un point correspond à l'audit, son identifiant est indiqué (ex. `FRONT-01`).

---

## Partie 1 — Parcours utilisateur

### Le parcours actuel, étape par étape

| Étape | Ce que l'utilisateur voit | Ce qui peut mal se passer |
|---|---|---|
| 1. Arrivée | « Player not connected 🤖 » pendant une fraction de seconde, puis une barre « BINGBANG », le mot « Invité » et une tuile « + » | Aucune phrase ne dit ce qu'est l'app. Le premier mot affiché ressemble à une erreur. |
| 2. Clic sur « + » | Une popup avec « list name » et « search your game » | Faut-il d'abord nommer ou d'abord chercher ? Rien ne dit qu'on est en train de créer une liste. |
| 3. Recherche | Rien ne se passe tant qu'on n'appuie pas sur Entrée | Pas d'indice ; si rien n'est trouvé ou si l'API échoue, l'écran reste vide. |
| 4. Ajout de jeux | L'icône « + » devient une coche | Correct. Mais la sélection n'est visible nulle part si on lance une autre recherche. |
| 5. Validation | Il n'y a pas de bouton « Créer » : il faut cliquer sur la croix ✕ | La croix signifie « annuler » partout ailleurs. Cliquer à côté ou Échap perd tout. |
| 6. Retour à la home | La liste apparaît avec 3 jeux | Correct. |
| 7. Ouverture d'une liste | Une popup avec le nom modifiable, les jeux, « save », « delete Ma liste » | « save » ne montre rien ; « delete » supprime sans confirmation ; fermer perd les changements. |
| 8. Compte | Une icône de profil ; la popup s'ouvre sur « REGISTER » | Aucune raison donnée de créer un compte. Un utilisateur déjà inscrit doit trouver le petit lien « login ». |
| 9. Déconnexion | « Player not connected 🤖 » | L'app semble cassée ; il faut recharger la page pour redevenir invité. |

### Les biais et frictions repérés

#### P-01 — Premier écran : aucune promesse, aucun exemple
- **Problème résolu** : un nouveau visiteur arrive sur une page presque vide (une tuile « + ») et ne sait pas ce
  que l'app fait pour lui. C'est le moment où l'on abandonne le plus (effet « page blanche »).
- **Proposition** : un état vide accueillant sur la home : une phrase (« Your games, your lists. Keep track of
  what you loved and what you want to play. »), un gros bouton « Create my first list », et deux suggestions
  cliquables qui pré-remplissent le nom : « Games I loved » et « Games I want to play » (ce sont exactement
  les deux notes Google Keep à l'origine du projet).
- **Effort** : S — **Valeur** : ★★★

#### P-02 — La croix qui valide (`FRONT-01`)
- **Problème résolu** : l'utilisateur ne trouve pas comment créer sa liste, ou perd sa sélection en cliquant
  à côté. C'est contraire à une convention très forte (✕ = annuler).
- **Proposition** : un bouton principal « Create list » / « Add 3 games » (avec le nombre), la croix et Échap
  annulent, avec une confirmation s'il y a une sélection en cours.
- **Effort** : S — **Valeur** : ★★★

#### P-03 — Sélection invisible pendant la recherche
- **Problème résolu** : après une deuxième recherche, on ne voit plus les jeux déjà choisis ; on hésite à
  continuer de peur de les avoir perdus.
- **Proposition** : une rangée de « pastilles » au-dessus des résultats avec les jeux sélectionnés (jaquette +
  nom + ✕ pour retirer), et un compteur dans le bouton de validation.
- **Effort** : S — **Valeur** : ★★

#### P-04 — Recherche muette (`FRONT-05`, `FRONT-14`)
- **Problème résolu** : il faut deviner qu'on doit appuyer sur Entrée ; « aucun résultat » et « erreur »
  ressemblent à « rien n'a été cherché ».
- **Proposition** : recherche automatique 300 ms après la dernière frappe (à partir de 2 caractères), messages
  explicites pour « aucun résultat » et « erreur », et affichage de l'année de sortie à côté du nom pour
  distinguer les remakes (ex. *Resident Evil 4* 2005 / 2023).
- **Effort** : S — **Valeur** : ★★★

#### P-05 — Modifications « en attente » sans le dire (`FRONT-03`)
- **Problème résolu** : dans une liste, retirer un jeu ne fait que le barrer ; il faut ensuite cliquer « save ».
  Mais **ajouter** un jeu, lui, est enregistré immédiatement. Deux comportements différents dans le même écran :
  l'utilisateur ne sait jamais si son action est enregistrée.
- **Proposition** : tout enregistrer immédiatement (comme l'ajout), avec un message « Game removed — Undo »
  pendant 5 secondes. Plus de bouton « save », plus de perte à la fermeture. C'est aussi le modèle mental de
  Google Keep, que l'app remplace.
- **Effort** : M — **Valeur** : ★★★

#### P-06 — Suppression définitive en un clic (`FRONT-02`)
- **Problème résolu** : le bouton « delete » est à côté de « save » et supprime une liste entière sans retour possible.
- **Proposition** : au minimum une confirmation. Mieux : suppression avec « Undo » pendant quelques secondes
  (la liste n'est réellement supprimée qu'après).
- **Effort** : S (confirmation) / M (annulation) — **Valeur** : ★★★

#### P-07 — Invité : un risque de perte que personne n'explique
- **Problème résolu** : l'invité voit le mot « Invité », sans savoir que ses listes n'existent que dans **ce
  navigateur**. S'il vide ses données de navigation, change de téléphone ou utilise la navigation privée, tout
  est perdu, et rien ne permet de les récupérer (le compte invité n'a ni email ni mot de passe utilisable).
- **Proposition** : après la création de la première liste (pas avant : il faut d'abord montrer la valeur), une
  bannière discrète : « Your lists are saved on this browser only. Create a free account to keep them
  everywhere. » avec un bouton. La rappeler à la 3ᵉ liste, puis ne plus insister.
- **Effort** : S — **Valeur** : ★★★ (c'est aussi le principal levier de création de comptes)

#### P-08 — La popup de compte s'ouvre sur « Register » sans argument
- **Problème résolu** : un utilisateur qui revient sur un autre appareil veut se connecter, et tombe sur
  l'inscription. Un invité, lui, ne voit pas pourquoi s'inscrire.
- **Proposition** : deux onglets visibles « Log in » / « Sign up » ; sous « Sign up », une ligne « You keep your
  N lists » (l'app le fait déjà, autant le dire). *(Fichiers d'auth : à faire par toi, voir AUDIT `FRONT-15`.)*
- **Effort** : S — **Valeur** : ★★

#### P-09 — Se connecter depuis un invité fait disparaître ses listes
- **Problème résolu** : un invité qui a créé des listes puis se connecte à son compte existant ne retrouve plus
  les listes qu'il vient de créer. Il croit à un bug, ou pire, ne s'en rend compte que plus tard.
- **Proposition** : c'est la règle 📋 du wiki « A guest who logs in to an existing account keeps their
  gamesLists too ». En attendant qu'elle soit faite : prévenir avant le login (« Your 2 guest lists will not be
  moved to this account »). *(Auth : voir ROADMAP, phase A.)*
- **Effort** : M — **Valeur** : ★★★

#### P-10 — Après la déconnexion, une impasse (`FRONT-09`)
- **Problème résolu** : « Player not connected 🤖 » sans bouton ni explication ; il faut deviner qu'il faut
  recharger la page.
- **Proposition** : après la déconnexion, redevenir immédiatement invité (wallet vide + état vide de P-01), ou
  afficher un écran « You are logged out » avec « Log in » et « Continue as guest ». *(Auth.)*
- **Effort** : S — **Valeur** : ★★

#### P-11 — Langues mélangées (`CONV-01`)
- **Problème résolu** : « Se déconnecter », « save », « Invité », « empty », « Ajouter des jeux à » sur les
  mêmes écrans : impression de brouillon, et doute sur le public visé.
- **Proposition** : une seule langue maintenant (anglais, comme le wiki), puis une vraie traduction
  français/anglais avec l'outil i18n d'Angular quand l'app sera stable.
- **Effort** : S (unifier) / M (traduire) — **Valeur** : ★★

#### P-12 — Le partage : à anticiper avant de le construire
Le partage n'existe pas encore (ROADMAP phase 4), mais voici les incompréhensions prévisibles :
- **« Est-ce que mon ami peut modifier ma liste ? »** → écrire « Read-only link » sur le bouton et dans la vue partagée.
- **« Est-ce que le lien montre mes commentaires ? »** → une case à cocher explicite, décochée par défaut.
- **« Comment j'arrête de partager ? »** → la liste des liens actifs et un bouton « Revoke » au même endroit
  que la création, pas dans un menu caché.
- **Le visiteur d'un lien** ne doit pas se demander s'il est sur son propre wallet : bandeau « You are viewing
  Alex's games wallet » + bouton « Create my own ».
- **Un invité qui partage** puis perd son navigateur ne pourra **plus jamais révoquer** son lien → le dire au
  moment de créer le lien (encore un argument pour P-07).
- **Effort** : inclus dans la phase 4 — **Valeur** : ★★★

#### P-13 — Clavier, mobile et lecteurs d'écran (`FRONT-04`, `FRONT-06` à `FRONT-08`)
- **Problème résolu** : impossible d'ouvrir une liste au clavier ; boutons sans nom ; les popups font 40 % de
  largeur sur ordinateur et occupent tout l'écran sur mobile sans vraie mise en page.
- **Proposition** : corriger les points de l'audit, puis tester les 3 parcours principaux (créer, modifier,
  partager) sur un téléphone et au clavier seul. L'usage naturel de l'app est **sur le canapé, téléphone en
  main** : le mobile devrait être le premier écran visé.
- **Effort** : M — **Valeur** : ★★★

---

## Partie 2 — Nouvelles fonctionnalités

### Petites, à forte valeur

#### I-01 — Réordonner les jeux et les listes par glisser-déposer
- **Problème résolu** : une liste « mes jeux préférés » est naturellement un **classement**, mais l'ordre est
  figé à l'ordre d'ajout. Le champ `position` existe déjà en base pour les jeux et pour les listes.
- **Proposition** : glisser-déposer (Angular CDK `DragDrop`) dans une liste et sur la home ; une route pour
  enregistrer le nouvel ordre.
- **Effort** : M — **Valeur** : ★★★

#### I-02 — Statut d'un jeu : « à jouer », « en cours », « terminé », « abandonné »
- **Problème résolu** : le créateur avait deux notes (« aimés » et « à jouer »). Le passage d'un jeu de l'une à
  l'autre est le geste le plus fréquent d'un joueur, et il oblige aujourd'hui à retirer puis rajouter.
- **Proposition** : un statut optionnel par jeu dans une liste, et une action « déplacer vers une autre liste »
  (qui emporte le commentaire).
- **Effort** : M — **Valeur** : ★★★

#### I-03 — Voir tous les jeux d'une carte, pas seulement 3
- **Problème résolu** : l'aperçu montre 3 jeux sans dire combien il y en a ; une liste de 40 jeux ressemble à
  une liste de 3.
- **Proposition** : « +37 more » sous l'aperçu, ou une mosaïque de jaquettes (les listes de jeux sont très
  visuelles : les jaquettes sont le meilleur « fond personnel » possible).
- **Effort** : S — **Valeur** : ★★

#### I-04 — Fiche d'un jeu
- **Problème résolu** : on ne peut rien savoir d'un jeu depuis l'app (plateformes, genre, résumé) ; pour décider
  quoi lancer ce soir il faut ouvrir un autre site.
- **Proposition** : un clic sur un jeu ouvre une fiche (résumé, date, plateformes, genres, captures) chargée
  depuis IGDB à la demande, avec le commentaire de l'utilisateur en haut.
- **Effort** : M — **Valeur** : ★★

#### I-05 — Note personnelle (étoiles ou cœur)
- **Problème résolu** : le commentaire demande d'écrire ; souvent on veut juste dire « j'ai adoré ».
- **Proposition** : une note de 1 à 5 (ou un simple cœur) par jeu dans une liste, affichée dans l'aperçu.
- **Effort** : S — **Valeur** : ★★

#### I-06 — Recherche dans mes listes
- **Problème résolu** : « est-ce que j'ai déjà noté ce jeu quelque part ? » devient difficile au-delà de 5 listes.
- **Proposition** : un champ de recherche sur la home qui filtre les listes et surligne les jeux trouvés ;
  dans la recherche de jeux, indiquer « déjà dans : Wishlist ».
- **Effort** : S — **Valeur** : ★★

### Moyennes

#### I-07 — Importer depuis Steam (ou coller une liste de titres)
- **Problème résolu** : la saisie initiale est le plus gros frein : quelqu'un qui a 200 jeux ne les ajoutera pas un par un.
- **Proposition** : étape 1 (simple) : coller une liste de titres, un par ligne — c'est exactement le format
  d'une note Google Keep — et l'app propose une correspondance pour chacun. Étape 2 : import d'un profil Steam
  public.
- **Effort** : M (coller) / L (Steam) — **Valeur** : ★★★

#### I-08 — « À quoi je joue ce soir ? »
- **Problème résolu** : une wishlist longue paralyse (paradoxe du choix).
- **Proposition** : un bouton sur une liste qui tire un jeu au hasard, pondéré par le score d'envie (ROADMAP
  phase 5). Lien naturel avec l'idée de liste coop (tirage commun).
- **Effort** : S (après la phase 5) — **Valeur** : ★★

#### I-09 — Image de partage (« carte » de sa liste)
- **Problème résolu** : un lien nu ne donne pas envie de cliquer dans une conversation Discord ou WhatsApp.
- **Proposition** : générer une image (titre + mosaïque de jaquettes) utilisée comme aperçu du lien
  (balises Open Graph) et téléchargeable. C'est aussi la meilleure publicité gratuite pour l'app.
- **Effort** : M (après la phase 4, nécessite une route de partage publique) — **Valeur** : ★★★

#### I-10 — Rappel de sortie
- **Problème résolu** : on ajoute à sa wishlist des jeux pas encore sortis, puis on oublie la date.
- **Proposition** : badge « sort dans 12 jours » sur les jeux à venir (la date de sortie est déjà stockée) ;
  plus tard, un email pour les comptes qui le demandent.
- **Effort** : S (badge) / M (email) — **Valeur** : ★★

#### I-11 — Export et suppression de ses données
- **Problème résolu** : confiance (« si l'app ferme, je perds tout ») et obligation légale en Europe (RGPD :
  droit d'accès et droit à l'effacement).
- **Proposition** : « Export my wallet » (JSON/CSV) et « Delete my account » dans le menu du compte.
- **Effort** : S (export) / M (suppression, touche à l'auth) — **Valeur** : ★★

#### I-12 — Application installable (PWA)
- **Problème résolu** : l'usage visé est mobile, mais ouvrir un navigateur et taper une adresse est un frein.
- **Proposition** : `ng add @angular/pwa` : icône sur l'écran d'accueil, ouverture plein écran, consultation
  hors ligne des listes déjà chargées.
- **Effort** : M — **Valeur** : ★★

### Plus grosses, ou à discuter

#### I-13 — Découverte guidée des types de listes
- **Problème résolu** : l'idée du wiki « laisser l'utilisateur découvrir les types tout seul » risque de produire
  l'effet inverse de celui voulu : la plupart des gens ne chercheront pas et ne verront jamais la fonctionnalité.
- **Proposition** : garder le plaisir de la découverte, mais **semer des indices** : quand un nom de liste
  contient « wish », « envie », « best », « top »…, proposer « This looks like a wishlist — unlock the envy
  score? ». Une page « Discoveries 2/5 » avec des emplacements grisés montre qu'il reste des choses à trouver,
  sans dire lesquelles. La récompense : un fond exclusif (phase 3).
- **Effort** : M — **Valeur** : ★★

#### I-14 — Profil public et suivi d'amis
- **Problème résolu** : un lien de partage est ponctuel ; on ne peut pas « suivre » le wallet d'un ami.
- **Proposition** : une adresse publique optionnelle (`/u/pseudo`) et un flux « ce que mes amis ont ajouté ».
- **Risque** : change la nature de l'app (de carnet personnel à réseau social) et impose de la modération.
  À ne faire que si les utilisateurs le demandent.
- **Effort** : L — **Valeur** : ★★ (incertaine)

#### I-15 — Recommandations
- **Problème résolu** : « j'ai aimé ces jeux, à quoi jouer ensuite ? »
- **Proposition** : pour une liste, proposer des jeux proches (IGDB fournit `similar_games`).
- **Effort** : M — **Valeur** : ★★ — voir BUSINESS-MODEL pour le lien avec l'affiliation.

---

## Synthèse : par où commencer

Si je ne devais garder que cinq choses, dans cet ordre :

1. **P-02** (bouton « Create ») et **P-06** (confirmation de suppression) : deux pièges qui font perdre des
   données. Quelques heures.
2. **P-01** (état vide accueillant) : décide si un visiteur reste plus de dix secondes.
3. **P-07** (expliquer le mode invité) : évite la pire expérience possible (tout perdre) et crée des comptes.
4. **P-05** (enregistrement immédiat + annulation) : rend l'app prévisible.
5. **I-01** (glisser-déposer) : transforme une liste en classement, ce que les joueurs adorent partager.

# Modèles économiques possibles pour BingBang

> Ce document propose des pistes. Les chiffres de marché et de coûts sont des **ordres de grandeur** donnés de
> mémoire : les tarifs et conditions d'utilisation des API (IGDB, RAWG) et des hébergeurs changent, il faut les
> revérifier sur leurs sites avant toute décision. Les hypothèses de conversion sont des estimations prudentes,
> pas des mesures.

## 1. Le point de départ

**Ce que BingBang est aujourd'hui** : un carnet personnel de listes de jeux vidéo (le « gamesWallet »),
utilisable sans compte, avec recherche dans un catalogue externe. Sa promesse (page Vision du wiki) :
*simple, très personnel, cosy*.

**Qui pourrait l'utiliser** : des joueurs qui tiennent déjà leurs listes ailleurs — notes de téléphone,
tableur, Discord — et que les gros sites rebutent.

**Les alternatives existantes** (toutes gratuites à l'entrée) :

| Service | Positionnement | Modèle |
|---|---|---|
| Backloggd | « le Letterboxd du jeu vidéo » : journal, notes, social | gratuit + abonnement de soutien |
| HowLongToBeat | durées de jeu, backlog | publicité |
| GG.app, Grouvee, Infinite Backlog | suivi de collection | gratuit / dons / abonnement |
| Steam, PlayStation, Xbox | listes de souhaits intégrées | vente de jeux (une seule plateforme) |
| Google Keep, Notion, tableur | notes génériques | — |

**Ce qui peut distinguer BingBang** : pas de réseau social imposé, pas de compte obligatoire, des listes
**libres** (pas seulement « joué / à jouer »), un espace qu'on personnalise et qu'on partage par un simple lien.
C'est une niche, pas un marché de masse : les modèles ci-dessous sont dimensionnés pour un projet tenu par une
personne.

## 2. Les coûts, quel que soit le modèle

### Les API de jeux — le point le plus important

| API | Usage non commercial | Usage commercial | Limites techniques |
|---|---|---|---|
| **IGDB** (Twitch/Amazon) | gratuit avec un compte développeur Twitch | autorisé seulement après accord avec Twitch (partenariat à demander) ; attribution demandée | environ 4 requêtes/seconde |
| **RAWG** | gratuit jusqu'à environ 20 000 requêtes/mois, avec lien vers RAWG obligatoire | offre payante (de l'ordre de quelques dizaines à quelques centaines de dollars par mois selon le volume) | quota mensuel |

Conséquences directes :

1. **Dès que BingBang gagne de l'argent** (abonnement, publicité, affiliation), l'usage devient commercial :
   il faut l'accord de Twitch pour IGDB ou un abonnement RAWG. C'est un prérequis à tous les modèles payants.
2. **Les images** (jaquettes) sont servies par les serveurs d'IGDB/RAWG : gratuit pour BingBang, mais dépendant
   de leur bonne volonté. À ne pas recopier sans vérifier la licence.
3. Le code est déjà prêt à changer de fournisseur (`GameProvider`, réglage `game_provider` en base) : c'est un
   vrai atout de négociation et de continuité.
4. **Le cache est un levier de coût** : un jeu ajouté une fois est déjà stocké dans la table `game`. En
   mettant aussi les recherches en cache (AUDIT `PERF-01`), le nombre d'appels payants devient faible.

### Les autres coûts (ordre de grandeur mensuel)

| Poste | Petit (< 1 000 utilisateurs actifs) | Moyen (≈ 20 000 utilisateurs actifs) |
|---|---|---|
| Hébergement backend + front (SSR) | 5–15 € | 40–100 € |
| Base PostgreSQL gérée | 0–15 € | 25–60 € |
| Nom de domaine, emails transactionnels | ≈ 2 € | 10–20 € |
| API de jeux (si usage commercial RAWG) | 0 € (accord IGDB) à ≈ 150 € | idem, selon l'accord |
| Paiement (Stripe ou équivalent) | ≈ 1,5–3 % + frais fixes par transaction | idem |
| Stockage d'images envoyées (si proposé) | — | 5–20 € |
| **Ton temps** | le vrai coût : développement, support, modération des contenus partagés | |

S'ajoutent des obligations dès qu'il y a des comptes et de l'argent : mentions légales, politique de
confidentialité, RGPD (export et suppression des données, voir IDEAS `I-11`), statut (micro-entreprise),
TVA sur les abonnements vendus dans l'UE.

---

## 3. Modèle A — Freemium « Wallet+ » (abonnement de personnalisation)

- **Cible** : les utilisateurs réguliers qui s'attachent à leur wallet et aiment le montrer.
- **Proposition de valeur** : tout ce qui sert à *noter ses jeux* reste gratuit et illimité. On paie pour
  **personnaliser et mettre en valeur**.
- **Gratuit** : listes illimitées, commentaires, compte, partage en lecture seule, fonds de base.
- **Payant (≈ 2–3 €/mois ou ≈ 20 €/an)** :
  - fonds exclusifs et **image de fond personnelle** (upload) ;
  - thèmes de couleur, mise en page des cartes ;
  - adresse de partage personnalisée (`bingbang.app/alex`) et **image de partage** générée (IDEAS `I-09`) ;
  - import Steam et export avancé (IDEAS `I-07`) ;
  - statistiques (« ton année de jeu ») ;
  - listes coop au-delà de 2 ou 3 participants.
- **Sources de revenus** : abonnements mensuels/annuels ; option « paiement unique à vie » pour les premiers soutiens.
- **Coûts spécifiques** : intégration du paiement, stockage et modération des images envoyées, gestion des abonnements.
- **Estimation prudente** : sur ce type d'outil, 1 à 3 % des utilisateurs actifs s'abonnent. 5 000 actifs ×
  2 % × 20 €/an ≈ **2 000 €/an**. Rentable seulement si les coûts restent sous ≈ 100 €/mois.
- **Risques** :
  - les concurrents donnent beaucoup gratuitement : la valeur payante doit être *émotionnelle*
    (personnalisation), pas fonctionnelle ;
  - faire payer trop tôt, avant que l'app ne soit irréprochable, abîme la confiance ;
  - l'usage commercial des API doit être réglé avant le premier euro.

## 4. Modèle B — Gratuit, financé par l'affiliation

- **Cible** : tous les utilisateurs, et surtout ceux qui tiennent une **liste de souhaits**.
- **Proposition de valeur** : l'app reste entièrement gratuite ; elle aide en plus à **acheter au bon prix** :
  « ce jeu de ta wishlist est à −60 % ».
- **Sources de revenus** : commission sur les achats faits après un clic vers une boutique partenaire
  (programmes d'affiliation de boutiques de clés et de revendeurs, Amazon, Humble, GOG, Fanatical… — en général
  quelques pour cent du panier). Steam n'a pas de programme d'affiliation public.
- **Fonctionnalités nécessaires** : types de listes / wishlist (ROADMAP phase 5), prix par boutique
  (via une API d'agrégation de prix), alertes de baisse de prix (email → compte obligatoire).
- **Coûts spécifiques** : API de prix (souvent gratuites à petit volume, avec attribution), envoi d'emails,
  suivi des programmes d'affiliation (chacun a ses règles et ses seuils de paiement).
- **Estimation prudente** : 5 000 actifs, 10 % cliquent dans le mois, 5 % de ces clics achètent, panier de 25 €,
  commission de 4 % → 5 000 × 0,10 × 0,05 × 25 × 0,04 ≈ **25 €/mois**. Il faut plusieurs dizaines de milliers
  d'utilisateurs pour que ce soit significatif.
- **Risques** :
  - revenus très faibles à petite échelle et dépendants de tiers (un programme peut fermer du jour au lendemain) ;
  - **conflit avec la vision** : une app « cosy et personnelle » qui pousse à l'achat perd son caractère ;
    les liens affiliés doivent être signalés clairement (obligation légale de transparence) ;
  - l'affiliation est un usage commercial des données IGDB/RAWG ;
  - certaines boutiques de clés ont mauvaise réputation auprès des joueurs.

## 5. Modèle C — Gratuit et open source, financé par les dons

- **Cible** : les joueurs sensibles aux projets indépendants, sans publicité ni revente de données.
- **Proposition de valeur** : « un carnet de jeux honnête, fait par un joueur » ; le code est public, on peut
  l'héberger soi-même.
- **Sources de revenus** : dons (Ko-fi, GitHub Sponsors, Liberapay) ; badge « supporter » et fonds offerts aux
  donateurs, sans rien verrouiller.
- **Coûts spécifiques** : presque aucun ; reste dans le cadre **non commercial** des API tant qu'il n'y a ni
  vente ni publicité (à confirmer dans leurs conditions : les dons sont une zone grise).
- **Estimation prudente** : la plupart des projets de cette taille reçoivent de **0 à 50 €/mois** : de quoi
  payer l'hébergement, pas le temps passé.
- **Risques** : pas de revenu réel ; la motivation repose entièrement sur toi. En contrepartie : aucun
  engagement envers des clients, et un projet public est un excellent **portfolio** — ce qui, pour un
  développeur en apprentissage, a une vraie valeur économique indirecte (emploi, missions).

---

## 6. Comparaison

| Critère | A — Freemium Wallet+ | B — Affiliation | C — Dons / open source |
|---|---|---|---|
| Cohérence avec la vision « cosy, personnel » | ✅ forte (on vend de la personnalisation) | ⚠️ faible (incitation à l'achat) | ✅ forte |
| Revenu possible à petite échelle | faible mais réel | très faible | quasi nul |
| Revenu possible à grande échelle | moyen | élevé | faible |
| Travail avant le premier euro | moyen (paiement, offre) | élevé (prix, alertes, partenariats) | très faible |
| Dépendance à des tiers | API de jeux, prestataire de paiement | API de jeux **et** boutiques | API de jeux |
| Contraintes légales | abonnements, TVA, RGPD | transparence sur l'affiliation, RGPD | minimales |
| Compatible avec l'usage gratuit des API | ❌ (accord commercial requis) | ❌ | ✅ probablement |

## 7. Recommandation

**Modèle recommandé : A — le freemium « Wallet+ », lancé en deux temps, en commençant par C.**

### Pourquoi A

1. **C'est le seul modèle aligné avec ce que l'app veut être.** La vision parle d'un espace personnel dont on
   est fier. Vendre des fonds, des thèmes et une belle page de partage, c'est vendre *plus de cette vision* ;
   l'affiliation, elle, vend l'attention de l'utilisateur à des boutiques.
2. **Les fonctionnalités payantes sont déjà sur la roadmap** : le fond personnel (phase 3) et le partage
   (phase 4) sont les deux briques de l'offre. Il n'y a pas de produit parallèle à construire.
3. **Le gratuit reste complet.** Listes, commentaires et partage ne sont jamais bridés : c'est ce qui permet
   de rivaliser avec des concurrents gratuits, et ça respecte la règle du wiki « utilisable à 100 % sans compte ».
4. **Les coûts sont maîtrisables** grâce à l'architecture existante (catalogue en base, fournisseur
   interchangeable, cache à ajouter).
5. **L'affiliation (B) reste possible plus tard**, discrètement et sur demande de l'utilisateur (« voir les
   prix ») une fois les wishlists en place. L'inverse — retirer de la publicité d'une app — est beaucoup plus dur.

### Les deux temps

- **Temps 1 — maintenant, jusqu'à quelques centaines d'utilisateurs réguliers : modèle C.**
  Tout est gratuit, un lien de don discret. Objectif : finir la V0.1 (commentaires, fond, partage), corriger la
  sécurité (phase A de la roadmap) et **mesurer** si les gens reviennent. Aucun contrat d'API à négocier, aucun
  risque légal lié à des paiements. Indicateurs à suivre : part des invités qui créent une 2ᵉ liste, part qui
  crée un compte, nombre de liens de partage ouverts.
- **Temps 2 — si l'usage est là : modèle A.**
  Avant d'encaisser : obtenir l'accord commercial IGDB (ou prendre l'offre RAWG), choisir un statut,
  écrire les mentions légales. Lancer avec **3 ou 4 avantages seulement** (fonds exclusifs + image personnelle,
  adresse de partage personnalisée, image de partage, import Steam) et un tarif annuel bas.

### Ce qui ferait changer d'avis

- Si les utilisateurs utilisent surtout BingBang comme **liste de souhaits** → réévaluer B (alertes de prix).
- Si personne ne partage son wallet → la personnalisation payante a moins de valeur ; rester en C et traiter le
  projet comme un portfolio.
- Si Twitch refuse l'usage commercial d'IGDB → chiffrer RAWG (ou un autre catalogue) **avant** de construire l'offre.

# Consignes générales

- Des fois dans mes prompts, tu pourrais être amené à voir un "N" à la fin d'une phrase. C'est juste une faute de frappe, au lieu de mettre un "?" je mets un "N" qui sont juste à côté dans un clavier AZERTY.

# Façon de travailler

- Je code moi-même les fonctionnalités (back et front logique). Ton rôle : expliquer, guider, relire mon code, m'aider à débloquer. N'écris pas l'implémentation à ma place sauf si je le demande explicitement.
- Exceptions où tu peux écrire le code directement : les tests (back et e2e) et, si je le demande, le design/CSS du front.

# Roadmap en cours

- Je suis la roadmap `docs/roadmap-v0.1.md`. Quand je parle de « la roadmap », c'est celle-ci.
- `docs/ROADMAP.md` : document de référence seulement (état wiki vs code, renvois à `docs/AUDIT.md`, ce qui existe sur `claude/features`). Ce n'est pas la roadmap que je suis.
- `docs/roadmap-account-system.md` est terminée (le 10/11/2026), elle n'est gardée que comme archive.

# Branches

- `claude/user-account-system-c58q0u` : ancienne implémentation de l'auth, obsolète. L'auth a été refaite à la main sur `dev`. Ne pas s'en inspirer.
- `claude/features` : prototype des futures fonctionnalités généré par Claude. Ne sera pas mergé, ne pas s'en servir comme base de code.

# Outlaw Lottery

Plugin Spigot/Paper ajoutant un système de loterie bilingue (FR/EN) pour les serveurs Minecraft.

## Fonctionnalités

- NPC vendeur de billets (`/outlawlottery create npc`) ouvrant une interface d'achat.
- Billets générés aléatoirement (4 numéros uniques entre 1 et 24) stockés numériquement pour chaque joueur.
- L'interface du NPC affiche en survol combien de billets un joueur possède actuellement et permet l'achat/encaissement.
- Prix configurable (`/outlawlottery setprice <montant>`), cagnotte = billets vendus × prix × 1,75.
- Tirage automatique toutes les 24 h (modifiable avec `/outlawlottery settime now` ou forcé via `/outlawlottery makelottery now`).
- Gains disponibles immédiatement après le tirage mais doivent être récupérés avant 23h59 sinon ils expirent.
- Support Vault pour prélever les achats et verser les gains à la demande.
- Traductions EN/FR sélectionnables (`/outlawlottery language en|fr`).

## Commandes principales

| Commande | Rôle |
| --- | --- |
| `/outlawlottery language <fr|en>` | Change la langue des messages. |
| `/outlawlottery create npc` | Crée/déplace le NPC vendeur à votre position. |
| `/outlawlottery setprice <montant>` | Définit le prix d'un billet. |
| `/outlawlottery remove npc` | Supprime le NPC et ses données enregistrées. |
| `/outlawlottery settime now` | Redémarre un cycle de 24 h à partir de maintenant. |
| `/outlawlottery makelottery now` | Lance immédiatement un tirage (tests). |
| `/outlawlottery debug` | Affiche l'état courant (prix, billets vendus, prochaine heure de tirage). |

Toutes les commandes requièrent la permission `outlawlottery.admin` (OP par défaut).

## Dépendances

- [Vault](https://www.spigotmc.org/resources/vault.34315/) (économie).
- Serveur Paper 1.20.4+ conseillé.

Compilez avec Maven :

```bash
mvn -DskipTests package
```

Le JAR est généré dans `target/outlaw-lottery-1.0.0-SNAPSHOT.jar`.

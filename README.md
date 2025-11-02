# Outlaw Lottery

Plugin Spigot/Paper ajoutant un système de loterie bilingue (FR/EN) pour les serveurs Minecraft.

## Fonctionnalités

- NPC vendeur de billets (`/outlawlottery create npc`) ouvrant une interface d'achat.
- Billets générés aléatoirement (4 numéros uniques entre 1 et 24) remis sur papier « Loto Ticket » / « Ticket de Loto ».
- Prix configurable (`/outlawlottery setprice <montant>`), cagnotte = billets vendus × prix × 1,75.
- Tirage automatique toutes les 24 h (modifiable avec `/outlawlottery settime now` ou forcé via `/outlawlottery makelottery now`).
- Support Vault pour prélever et créditer les joueurs.
- Hologramme FancyHolograms affichant prix, prochain tirage et dernier ticket gagnant au-dessus du NPC.
- Traductions EN/FR sélectionnables (`/outlawlottery language en|fr`).

## Commandes principales

| Commande | Rôle |
| --- | --- |
| `/outlawlottery language <fr|en>` | Change la langue des messages. |
| `/outlawlottery create npc` | Crée/déplace le NPC vendeur à votre position. |
| `/outlawlottery setprice <montant>` | Définit le prix d'un billet. |
| `/outlawlottery settime now` | Redémarre un cycle de 24 h à partir de maintenant. |
| `/outlawlottery makelottery now` | Lance immédiatement un tirage (tests). |
| `/outlawlottery debug` | Affiche l'état courant (prix, billets vendus, prochaine heure de tirage). |

Toutes les commandes requièrent la permission `outlawlottery.admin` (OP par défaut).

## Dépendances

- [Vault](https://www.spigotmc.org/resources/vault.34315/) (économie).
- [FancyHolograms](https://modrinth.com/plugin/fancyholograms) pour l'affichage.
- Serveur Paper 1.20.4+ conseillé.

Compilez avec Maven :

```bash
mvn -DskipTests package
```

Le JAR est généré dans `target/outlaw-lottery-1.0.0-SNAPSHOT.jar`.

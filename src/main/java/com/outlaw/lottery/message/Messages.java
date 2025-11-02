package com.outlaw.lottery.message;

import com.outlaw.lottery.Language;
import java.util.EnumMap;
import java.util.Map;

public final class Messages {
    private static final Map<Language, Map<String, String>> MESSAGES = new EnumMap<>(Language.class);

    static {
        Map<String, String> en = new java.util.HashMap<>();
        en.put("prefix", "§6[OutlawLottery]§r ");
        en.put("language-updated", "Language set to English.");
        en.put("npc-created", "Lottery NPC created at your location.");
        en.put("price-set", "Ticket price set to %s.");
        en.put("time-set", "Next draw scheduled in 24 hours.");
        en.put("lottery-started", "A new lottery draw has begun!");
        en.put("ticket-purchased", "You bought a ticket: %s.");
        en.put("insufficient-funds", "You don't have enough money to buy a ticket.");
        en.put("no-economy", "Economy service not available. Contact an administrator.");
        en.put("tickets-reset", "Tickets cleared for the next draw.");
        en.put("no-tickets", "No tickets were sold. The draw has been postponed.");
        en.put("winner-announcement", "Congratulations %s! Ticket %s won %s.");
        en.put("multiple-winners", "Multiple winners! Each receives %s.");
        en.put("language-invalid", "Please choose between EN or FR.");
        en.put("lottery-forced", "Lottery draw triggered manually.");
        en.put("debug-header", "§eLottery debug information:");
        en.put("debug-line", "§7%s: §f%s");
        en.put("hologram-updated", "Lottery hologram updated.");
        en.put("no-npc", "No lottery NPC configured.");
        en.put("npc-removed", "Existing NPC removed.");
        en.put("shop-title", "Outlaw Lottery");
        en.put("shop-buy", "§aBuy ticket");
        en.put("shop-info", "§7Price: §f%s");
        en.put("next-draw", "Next draw in %s");
        en.put("shop-claim", "§bClaim reward");
        en.put("shop-claim-ready", "§7Ready: §f%s");
        en.put("shop-claim-wait", "§7Available in: §f%s");
        en.put("shop-claim-none", "§7No pending reward.");
        en.put("claim-success", "You claimed %s. Congratulations!");
        en.put("claim-too-early", "Your winnings will be available in %s.");
        en.put("claim-none", "You have no winnings to claim.");
        en.put("winner-claim", "Visit the lottery NPC at 23:59 to claim your prize.");
        en.put("hologram-winner", "§eWinners: §f%s §7| §e%s §7| §6%s");
        en.put("hologram-claim", "§7Claim in %s");

        Map<String, String> fr = new java.util.HashMap<>();
        fr.put("prefix", "§6[OutlawLottery]§r ");
        fr.put("language-updated", "Langue définie sur français.");
        fr.put("npc-created", "NPC de loterie créé à votre position.");
        fr.put("price-set", "Prix du billet défini à %s.");
        fr.put("time-set", "Prochain tirage dans 24 heures.");
        fr.put("lottery-started", "Un nouveau tirage de loterie commence !");
        fr.put("ticket-purchased", "Vous avez acheté un billet : %s.");
        fr.put("insufficient-funds", "Vous n'avez pas assez d'argent pour acheter un billet.");
        fr.put("no-economy", "Aucun système économique disponible. Contactez un administrateur.");
        fr.put("tickets-reset", "Billets réinitialisés pour le prochain tirage.");
        fr.put("no-tickets", "Aucun billet vendu. Tirage reporté.");
        fr.put("winner-announcement", "Félicitations %s ! Le billet %s gagne %s.");
        fr.put("multiple-winners", "Plusieurs gagnants ! Chacun reçoit %s.");
        fr.put("language-invalid", "Veuillez choisir entre EN ou FR.");
        fr.put("lottery-forced", "Tirage lancé manuellement.");
        fr.put("debug-header", "§eInformations de débogage de la loterie :");
        fr.put("debug-line", "§7%s : §f%s");
        fr.put("hologram-updated", "Hologramme de loterie mis à jour.");
        fr.put("no-npc", "Aucun NPC de loterie configuré.");
        fr.put("npc-removed", "NPC existant supprimé.");
        fr.put("shop-title", "Loterie Outlaw");
        fr.put("shop-buy", "§aAcheter un billet");
        fr.put("shop-info", "§7Prix : §f%s");
        fr.put("next-draw", "Prochain tirage dans %s");
        fr.put("shop-claim", "§bRécupérer le gain");
        fr.put("shop-claim-ready", "§7Prêt : §f%s");
        fr.put("shop-claim-wait", "§7Disponible dans : §f%s");
        fr.put("shop-claim-none", "§7Aucun gain en attente.");
        fr.put("claim-success", "Vous avez récupéré %s. Félicitations !");
        fr.put("claim-too-early", "Votre gain sera disponible dans %s.");
        fr.put("claim-none", "Vous n'avez aucun gain à récupérer.");
        fr.put("winner-claim", "Passez voir le NPC à 23:59 pour récupérer votre gain.");
        fr.put("hologram-winner", "§eGagnants : §f%s §7| §e%s §7| §6%s");
        fr.put("hologram-claim", "§7Récupération dans %s");

        MESSAGES.put(Language.EN, en);
        MESSAGES.put(Language.FR, fr);
    }

    private Messages() {}

    public static String get(Language language, String key) {
        return MESSAGES.getOrDefault(language, MESSAGES.get(Language.EN)).getOrDefault(key, key);
    }
}

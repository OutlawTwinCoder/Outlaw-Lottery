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
        en.put("winner-announcement", "Congratulations %s! You won %s with ticket %s.");
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
        fr.put("winner-announcement", "Félicitations %s ! Vous gagnez %s avec le billet %s.");
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

        MESSAGES.put(Language.EN, en);
        MESSAGES.put(Language.FR, fr);
    }

    private Messages() {}

    public static String get(Language language, String key) {
        return MESSAGES.getOrDefault(language, MESSAGES.get(Language.EN)).getOrDefault(key, key);
    }
}

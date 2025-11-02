package com.outlaw.lottery.command;

import com.outlaw.lottery.Language;
import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.message.Messages;
import com.outlaw.lottery.npc.NpcManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LotteryCommand implements CommandExecutor, TabCompleter {
    private final OutlawLotteryPlugin plugin;

    public LotteryCommand(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "language" -> handleLanguage(sender, args);
            case "create" -> handleCreate(sender, args);
            case "setprice" -> handleSetPrice(sender, args);
            case "settime" -> handleSetTime(sender, args);
            case "makelottery" -> handleMakeLottery(sender, args);
            case "debug" -> handleDebug(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix")
                + "/outlawlottery language <fr|en>, create npc, setprice <amount>, settime now, makelottery now, debug");
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "language-invalid"));
            return;
        }
        Language language = Language.fromString(args[1]);
        if (language == null) {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "language-invalid"));
            return;
        }
        plugin.setLanguage(language);
        sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(language, "language-updated"));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommand available in-game only.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("npc")) {
            NpcManager manager = plugin.getNpcManager();
            manager.createOrMoveNpc(player.getLocation());
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "npc-created"));
        } else {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "/outlawlottery create npc");
        }
    }

    private void handleSetPrice(CommandSender sender, String[] args) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "/outlawlottery setprice <amount>");
            return;
        }
        try {
            double price = Double.parseDouble(args[1]);
            plugin.setTicketPrice(price);
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "price-set").formatted(price));
        } catch (NumberFormatException exception) {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "/outlawlottery setprice <amount>");
        }
    }

    private void handleSetTime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("now")) {
            plugin.setNextDraw(Instant.now().plus(Duration.ofHours(24)));
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "time-set"));
        } else {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "/outlawlottery settime now");
        }
    }

    private void handleMakeLottery(CommandSender sender, String[] args) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("now")) {
            plugin.triggerDraw();
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "lottery-forced"));
        } else {
            sender.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "/outlawlottery makelottery now");
        }
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("outlawlottery.admin")) {
            sender.sendMessage("§cNo permission.");
            return;
        }
        sender.sendMessage(Messages.get(plugin.getLanguage(), "debug-header"));
        sender.sendMessage(Messages.get(plugin.getLanguage(), "debug-line").formatted("Language", plugin.getLanguage()));
        sender.sendMessage(Messages.get(plugin.getLanguage(), "debug-line").formatted("Price", plugin.getTicketPrice()));
        sender.sendMessage(Messages.get(plugin.getLanguage(), "debug-line").formatted("Tickets", plugin.getLotteryManager().getTicketSummary()));
        sender.sendMessage(Messages.get(plugin.getLanguage(), "debug-line").formatted("NextDraw", plugin.getNextDraw()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("language", "create", "setprice", "settime", "makelottery", "debug");
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "language" -> Arrays.asList("en", "fr");
                case "create" -> Arrays.asList("npc");
                case "settime" -> Arrays.asList("now");
                case "makelottery" -> Arrays.asList("now");
                default -> new ArrayList<>();
            };
        }
        return new ArrayList<>();
    }
}

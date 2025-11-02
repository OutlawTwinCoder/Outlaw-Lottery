package com.outlaw.lottery.hologram;

import com.outlaw.lottery.Language;
import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.lottery.Ticket;
import com.outlaw.lottery.message.Messages;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class FancyHologramService {
    private final OutlawLotteryPlugin plugin;

    public FancyHologramService(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnOrUpdateHologram(Location location) {
        if (!isFancyHologramsEnabled()) {
            return;
        }
        String hologramId = plugin.getConfig().getString("hologram-id", "outlawlottery");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "fancyholograms delete " + hologramId);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "fancyholograms create " + hologramId + " " + formatLocation(location));
        refreshHologram();
    }

    public void refreshHologram() {
        if (!isFancyHologramsEnabled()) {
            return;
        }
        String hologramId = plugin.getConfig().getString("hologram-id", "outlawlottery");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fancyholograms edit " + hologramId + " clear");
        Language language = plugin.getLanguage();
        String priceLine = Messages.get(language, "shop-info").formatted(String.format("%.2f", plugin.getTicketPrice()));
        String nextDrawLine = Messages.get(language, "next-draw")
                .formatted(formatDuration(plugin.getNextDraw().toEpochMilli() - System.currentTimeMillis()));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "fancyholograms edit " + hologramId + " addtext '" + priceLine.replace("'", "") + "'");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "fancyholograms edit " + hologramId + " addtext '" + nextDrawLine.replace("'", "") + "'");
        List<Integer> lastNumbers = plugin.getLotteryManager().getLastWinningNumbers();
        if (!lastNumbers.isEmpty()) {
            String lastTicket = plugin.getLotteryManager().formatNumbers(lastNumbers);
            String lastLine = "§e" + lastTicket + " §7- " + String.format("%.2f", plugin.getLotteryManager().getLastJackpot());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "fancyholograms edit " + hologramId + " addtext '" + lastLine.replace("'", "") + "'");
        }
    }

    public void updateWinnerDisplay(Ticket ticket) {
        if (!isFancyHologramsEnabled()) {
            return;
        }
        refreshHologram();
    }

    private boolean isFancyHologramsEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("FancyHolograms");
    }

    private String formatLocation(Location location) {
        return String.format(java.util.Locale.US, "%s %.2f %.2f %.2f",
                location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    private String formatDuration(long millis) {
        if (millis <= 0) {
            return "0h";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        return hours + "h" + String.format("%02d", minutes);
    }
}

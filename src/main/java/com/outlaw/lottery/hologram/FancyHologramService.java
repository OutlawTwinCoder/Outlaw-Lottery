package com.outlaw.lottery.hologram;

import com.outlaw.lottery.Language;
import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.lottery.PendingReward;
import com.outlaw.lottery.message.Messages;
import java.util.Comparator;
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
        List<String> names = plugin.getLotteryManager().getLastWinnerNames();
        long lastDraw = plugin.getLotteryManager().getLastDrawTime();
        if (!names.isEmpty() && !lastNumbers.isEmpty()
                && System.currentTimeMillis() - lastDraw < TimeUnit.HOURS.toMillis(24)) {
            String lastTicket = plugin.getLotteryManager().formatNumbers(lastNumbers);
            String winners = String.join(", ", names);
            String jackpotLine = Messages.get(language, "hologram-winner")
                    .formatted(winners, lastTicket, String.format("%.2f", plugin.getLotteryManager().getLastJackpot()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "fancyholograms edit " + hologramId + " addtext '" + jackpotLine.replace("'", "") + "'");
            PendingReward pending = plugin.getLotteryManager().getAllPendingRewards().stream()
                    .min(Comparator.comparingLong(PendingReward::remainingMillis))
                    .orElse(null);
            if (pending != null) {
                String claimLine = Messages.get(language, "hologram-claim")
                        .formatted(formatDuration(pending.remainingMillis()));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "fancyholograms edit " + hologramId + " addtext '" + claimLine.replace("'", "") + "'");
            }
        }
    }

    public void updateWinnerDisplay() {
        if (!isFancyHologramsEnabled()) {
            return;
        }
        refreshHologram();
    }

    public void removeHologram() {
        if (!isFancyHologramsEnabled()) {
            return;
        }
        String hologramId = plugin.getConfig().getString("hologram-id", "outlawlottery");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fancyholograms delete " + hologramId);
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

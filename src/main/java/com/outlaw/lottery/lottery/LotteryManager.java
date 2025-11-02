package com.outlaw.lottery.lottery;

import com.outlaw.lottery.Language;
import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.message.Messages;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LotteryManager {
    private final OutlawLotteryPlugin plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;
    private final Map<UUID, Ticket> tickets = new HashMap<>();
    private final Map<UUID, PendingReward> pendingRewards = new HashMap<>();
    private List<Integer> lastWinningNumbers = new ArrayList<>();
    private List<String> lastWinnerNames = new ArrayList<>();
    private double lastJackpot = 0.0;
    private long lastDrawTime = 0L;

    public LotteryManager(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadTickets();
    }

    private void loadTickets() {
        if (dataFile.exists()) {
            try {
                dataConfig.load(dataFile);
            } catch (IOException | InvalidConfigurationException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load lottery data", ex);
            }
        }
        tickets.clear();
        List<Map<?, ?>> ticketList = dataConfig.getMapList("tickets");
        for (Map<?, ?> entry : ticketList) {
            try {
                UUID id = UUID.fromString(entry.get("id").toString());
                UUID owner = UUID.fromString(entry.get("owner").toString());
                @SuppressWarnings("unchecked")
                List<Integer> numbers = (List<Integer>) entry.get("numbers");
                long purchaseTime = ((Number) entry.get("time")).longValue();
                Ticket ticket = new Ticket(id, owner, numbers, purchaseTime);
                tickets.put(id, ticket);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to load ticket entry: " + entry, exception);
            }
        }
        pendingRewards.clear();
        List<Map<?, ?>> pendingList = dataConfig.getMapList("pending");
        for (Map<?, ?> entry : pendingList) {
            try {
                UUID id = UUID.fromString(entry.get("id").toString());
                UUID owner = UUID.fromString(entry.get("owner").toString());
                @SuppressWarnings("unchecked")
                List<Integer> numbers = (List<Integer>) entry.get("numbers");
                double amount = Double.parseDouble(entry.get("amount").toString());
                long availableAt = ((Number) entry.getOrDefault("availableAt", System.currentTimeMillis())).longValue();
                PendingReward reward = new PendingReward(id, owner, numbers, amount, availableAt);
                pendingRewards.put(id, reward);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to load pending reward entry: " + entry, exception);
            }
        }
        lastWinningNumbers = dataConfig.getIntegerList("last-winning");
        lastWinnerNames = dataConfig.getStringList("last-winners");
        lastJackpot = dataConfig.getDouble("last-jackpot", 0.0);
        lastDrawTime = dataConfig.getLong("last-draw", 0L);
    }

    public void saveTickets() {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ticket.id().toString());
            map.put("owner", ticket.owner().toString());
            map.put("numbers", ticket.numbers());
            map.put("time", ticket.purchaseTime());
            serialized.add(map);
        }
        dataConfig.set("tickets", serialized);
        List<Map<String, Object>> pendingSerialized = new ArrayList<>();
        for (PendingReward reward : pendingRewards.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", reward.ticketId().toString());
            map.put("owner", reward.owner().toString());
            map.put("numbers", reward.numbers());
            map.put("amount", reward.amount());
            map.put("availableAt", reward.availableAt());
            pendingSerialized.add(map);
        }
        dataConfig.set("pending", pendingSerialized);
        dataConfig.set("last-winning", lastWinningNumbers);
        dataConfig.set("last-winners", lastWinnerNames);
        dataConfig.set("last-jackpot", lastJackpot);
        dataConfig.set("last-draw", lastDrawTime);
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save lottery data", exception);
        }
    }

    public Ticket purchaseTicket(Player player) {
        List<Integer> numbers = generateCombination();
        Ticket ticket = new Ticket(UUID.randomUUID(), player.getUniqueId(), numbers, System.currentTimeMillis());
        tickets.put(ticket.id(), ticket);
        saveTickets();
        return ticket;
    }

    public int getTicketCount() {
        return tickets.size();
    }

    public long getTicketCount(UUID owner) {
        return tickets.values().stream().filter(ticket -> ticket.owner().equals(owner)).count();
    }

    public List<Ticket> getTicketsForOwner(UUID owner) {
        return tickets.values().stream()
                .filter(ticket -> ticket.owner().equals(owner))
                .sorted(Comparator.comparingLong(Ticket::purchaseTime))
                .collect(Collectors.toList());
    }

    public List<Integer> getLastWinningNumbers() {
        return lastWinningNumbers;
    }

    public double getLastJackpot() {
        return lastJackpot;
    }

    public List<String> getLastWinnerNames() {
        return lastWinnerNames;
    }

    public long getLastDrawTime() {
        return lastDrawTime;
    }

    public Optional<Ticket> getTicket(UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public Optional<DrawResult> drawLottery() {
        if (tickets.isEmpty()) {
            return Optional.empty();
        }
        List<Ticket> soldTickets = new ArrayList<>(tickets.values());

        List<Integer> drawnNumbers = generateCombination();
        Collections.sort(drawnNumbers);
        List<Ticket> winners = getTicketsByNumbers(drawnNumbers);
        if (winners.isEmpty()) {
            List<Ticket> shuffled = new ArrayList<>(soldTickets);
            Collections.shuffle(shuffled);
            Ticket forcedWinner = shuffled.get(0);
            drawnNumbers = new ArrayList<>(forcedWinner.numbers());
            Collections.sort(drawnNumbers);
            winners = getTicketsByNumbers(drawnNumbers);
        }

        lastWinningNumbers = new ArrayList<>(drawnNumbers);
        double jackpot = calculateJackpot();
        lastJackpot = jackpot;
        lastDrawTime = System.currentTimeMillis();
        lastWinnerNames = winners.stream()
                .map(ticket -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ticket.owner());
                    return offlinePlayer != null && offlinePlayer.getName() != null
                            ? offlinePlayer.getName()
                            : ticket.owner().toString();
                })
                .collect(Collectors.toList());

        long claimAvailableAt = lastDrawTime + Duration.ofHours(23).toMillis() + Duration.ofMinutes(59).toMillis();
        double share = winners.isEmpty() ? 0.0 : jackpot / winners.size();
        for (Ticket ticket : winners) {
            PendingReward reward = new PendingReward(
                    ticket.id(), ticket.owner(), ticket.numbers(), share, claimAvailableAt);
            pendingRewards.put(ticket.id(), reward);
        }

        tickets.clear();
        saveTickets();
        return Optional.of(new DrawResult(new ArrayList<>(drawnNumbers), winners, jackpot, claimAvailableAt));
    }

    private double calculateJackpot() {
        double price = plugin.getTicketPrice();
        return Math.round(getTicketCount() * price * plugin.getJackpotMultiplier() * 100.0) / 100.0;
    }

    public Optional<Ticket> findMatchingTicket(List<Integer> numbers) {
        List<Integer> sorted = new ArrayList<>(numbers);
        Collections.sort(sorted);
        return tickets.values().stream()
                .filter(ticket -> {
                    List<Integer> ticketNumbers = new ArrayList<>(ticket.numbers());
                    Collections.sort(ticketNumbers);
                    return ticketNumbers.equals(sorted);
                })
                .min(Comparator.comparingLong(Ticket::purchaseTime));
    }

    public List<Ticket> getTicketsByNumbers(List<Integer> numbers) {
        List<Integer> sorted = new ArrayList<>(numbers);
        Collections.sort(sorted);
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            List<Integer> ticketNumbers = new ArrayList<>(ticket.numbers());
            Collections.sort(ticketNumbers);
            if (ticketNumbers.equals(sorted)) {
                result.add(ticket);
            }
        }
        return result;
    }

    public String getTicketSummary() {
        if (tickets.isEmpty()) {
            return "0";
        }
        Map<String, Long> owners = new HashMap<>();
        for (Ticket ticket : tickets.values()) {
            owners.merge(ticket.owner().toString(), 1L, Long::sum);
        }
        return owners.size() + " owners / " + tickets.size() + " tickets";
    }

    public List<PendingReward> getPendingRewards(UUID owner) {
        return pendingRewards.values().stream()
                .filter(reward -> reward.owner().equals(owner))
                .sorted(Comparator.comparingLong(PendingReward::availableAt))
                .collect(Collectors.toList());
    }

    public List<PendingReward> getAllPendingRewards() {
        return new ArrayList<>(pendingRewards.values());
    }

    public ClaimResult claimRewards(Player player) {
        List<PendingReward> pending = getPendingRewards(player.getUniqueId());
        if (pending.isEmpty()) {
            return new ClaimResult(ClaimState.NONE, 0.0, List.of(), null);
        }
        List<PendingReward> ready = pending.stream().filter(PendingReward::isReady).collect(Collectors.toList());
        if (ready.isEmpty()) {
            PendingReward next = pending.stream()
                    .min(Comparator.comparingLong(PendingReward::availableAt))
                    .orElse(null);
            return new ClaimResult(ClaimState.TOO_EARLY, 0.0, List.of(), next);
        }
        double total = ready.stream().mapToDouble(PendingReward::amount).sum();
        ready.forEach(reward -> pendingRewards.remove(reward.ticketId()));
        saveTickets();
        if (plugin.getEconomy() != null) {
            plugin.getEconomy().depositPlayer(player, total);
        }
        return new ClaimResult(ClaimState.SUCCESS, total, ready, null);
    }

    private List<Integer> generateCombination() {
        List<Integer> pool = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            pool.add(i);
        }
        Collections.shuffle(pool, ThreadLocalRandom.current());
        List<Integer> result = new ArrayList<>(pool.subList(0, 4));
        Collections.sort(result);
        return result;
    }

    public String formatNumbers(List<Integer> numbers) {
        List<Integer> sorted = new ArrayList<>(numbers);
        Collections.sort(sorted);
        return sorted.stream().map(n -> String.format("%02d", n)).collect(java.util.stream.Collectors.joining("-"));
    }

    public void announceWinners(Language language, DrawResult result) {
        List<Ticket> winners = result.winners();
        String formattedJackpot = String.format("%.2f", result.jackpot());
        if (winners.isEmpty()) {
            plugin.broadcast(Messages.get(language, "no-tickets"));
            return;
        }
        if (winners.size() > 1) {
            plugin.broadcast(Messages.get(language, "multiple-winners").formatted(formattedJackpot));
        }
        for (Ticket ticket : winners) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ticket.owner());
            String playerName = offlinePlayer != null && offlinePlayer.getName() != null
                    ? offlinePlayer.getName()
                    : ticket.owner().toString();
            String message = Messages.get(language, "winner-announcement")
                    .formatted(playerName, formatNumbers(ticket.numbers()), formattedJackpot);
            plugin.broadcast(message);
        }
        plugin.broadcast(Messages.get(language, "winner-claim"));
    }

    public record DrawResult(List<Integer> numbers, List<Ticket> winners, double jackpot, long claimAvailableAt) {}

    public enum ClaimState {
        NONE,
        TOO_EARLY,
        SUCCESS
    }

    public record ClaimResult(ClaimState state, double amount, List<PendingReward> claimed, PendingReward nextAvailable) {}
}

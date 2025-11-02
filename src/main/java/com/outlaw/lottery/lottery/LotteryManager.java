package com.outlaw.lottery.lottery;

import com.outlaw.lottery.Language;
import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.message.Messages;
import java.io.File;
import java.io.IOException;
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
    private List<Integer> lastWinningNumbers = new ArrayList<>();
    private double lastJackpot = 0.0;

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
        lastWinningNumbers = dataConfig.getIntegerList("last-winning");
        lastJackpot = dataConfig.getDouble("last-jackpot", 0.0);
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
        dataConfig.set("last-winning", lastWinningNumbers);
        dataConfig.set("last-jackpot", lastJackpot);
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

    public void resetTickets() {
        tickets.clear();
        saveTickets();
    }

    public List<Integer> getLastWinningNumbers() {
        return lastWinningNumbers;
    }

    public double getLastJackpot() {
        return lastJackpot;
    }

    public Optional<Ticket> getTicket(UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public Optional<Ticket> drawLottery() {
        if (tickets.isEmpty()) {
            return Optional.empty();
        }
        List<Ticket> soldTickets = new ArrayList<>(tickets.values());

        List<Integer> drawnNumbers = generateCombination();
        Collections.sort(drawnNumbers);
        Optional<Ticket> winner = findMatchingTicket(drawnNumbers);
        if (winner.isEmpty()) {
            List<Ticket> shuffled = new ArrayList<>(soldTickets);
            Collections.shuffle(shuffled);
            Ticket forcedWinner = shuffled.get(0);
            drawnNumbers = new ArrayList<>(forcedWinner.numbers());
            Collections.sort(drawnNumbers);
            winner = Optional.of(forcedWinner);
        }

        lastWinningNumbers = drawnNumbers;
        double jackpot = calculateJackpot();
        lastJackpot = jackpot;
        saveTickets();
        return winner;
    }

    private double calculateJackpot() {
        double price = plugin.getTicketPrice();
        return Math.round(getTicketCount() * price * plugin.getJackpotMultiplier() * 100.0) / 100.0;
    }

    public void rewardWinners(List<Ticket> winners) {
        if (winners.isEmpty()) {
            return;
        }
        double jackpot = calculateJackpot();
        lastJackpot = jackpot;
        double share = jackpot / winners.size();
        for (Ticket ticket : winners) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(ticket.owner());
            if (player != null && plugin.getEconomy() != null) {
                plugin.getEconomy().depositPlayer(player, share);
            }
        }
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

    public void announceWinners(Language language, List<Ticket> winners) {
        double jackpot = calculateJackpot();
        lastJackpot = jackpot;
        String formattedJackpot = String.format("%.2f", jackpot);
        if (winners.isEmpty()) {
            plugin.broadcast(Messages.get(language, "no-tickets"));
            return;
        }
        if (winners.size() > 1) {
            plugin.broadcast(Messages.get(language, "multiple-winners").formatted(formattedJackpot));
        }
        for (Ticket ticket : winners) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ticket.owner());
            String playerName = offlinePlayer != null && offlinePlayer.getName() != null ? offlinePlayer.getName() : ticket.owner().toString();
            String message = Messages.get(language, "winner-announcement")
                    .formatted(playerName, formattedJackpot, formatNumbers(ticket.numbers()));
            plugin.broadcast(message);
        }
    }
}

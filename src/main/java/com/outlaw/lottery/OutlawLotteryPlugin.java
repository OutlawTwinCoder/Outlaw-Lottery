package com.outlaw.lottery;

import com.outlaw.lottery.command.LotteryCommand;
import com.outlaw.lottery.hologram.FancyHologramService;
import com.outlaw.lottery.lottery.LotteryManager;
import com.outlaw.lottery.lottery.Ticket;
import com.outlaw.lottery.npc.NpcManager;
import com.outlaw.lottery.shop.ShopListener;
import com.outlaw.lottery.shop.ShopManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class OutlawLotteryPlugin extends JavaPlugin {
    private LotteryManager lotteryManager;
    private ShopManager shopManager;
    private ShopListener shopListener;
    private NpcManager npcManager;
    private FancyHologramService hologramService;
    private Economy economy;
    private Language language;
    private double ticketPrice;
    private double jackpotMultiplier;
    private Instant nextDraw;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        lotteryManager = new LotteryManager(this);
        shopManager = new ShopManager(this);
        shopListener = new ShopListener(shopManager);
        npcManager = new NpcManager(this);
        hologramService = new FancyHologramService(this);
        getServer().getPluginManager().registerEvents(shopListener, this);
        getServer().getPluginManager().registerEvents(npcManager, this);
        setupEconomy();
        registerCommand(new LotteryCommand(this));
        scheduleDrawTask();
        hologramService.refreshHologram();
    }

    @Override
    public void onDisable() {
        if (lotteryManager != null) {
            lotteryManager.saveTickets();
        }
        if (shopListener != null) {
            shopListener.closeAll();
        }
        if (npcManager != null) {
            npcManager.cleanupNpc();
        }
    }

    public void reloadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();
        language = Language.orDefault(config.getString("language", "EN"), Language.EN);
        ticketPrice = config.getDouble("price", 100.0);
        jackpotMultiplier = config.getDouble("jackpot-multiplier", 1.75);
        long nextDrawMillis = config.getLong("next-draw", 0L);
        nextDraw = nextDrawMillis <= 0 ? Instant.now().plus(Duration.ofHours(24)) : Instant.ofEpochMilli(nextDrawMillis);
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found. Economy support disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            getLogger().warning("No economy provider found. Economy support disabled.");
        }
    }

    private void registerCommand(LotteryCommand command) {
        CommandExecutor executor = command;
        TabCompleter completer = command;
        getCommand("outlawlottery").setExecutor(executor);
        getCommand("outlawlottery").setTabCompleter(completer);
    }

    public void broadcast(String message) {
        String prefixed = com.outlaw.lottery.message.Messages.get(language, "prefix") + message;
        Bukkit.broadcastMessage(prefixed);
    }

    public LotteryManager getLotteryManager() {
        return lotteryManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public FancyHologramService getHologramService() {
        return hologramService;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        getConfig().set("language", language.name());
        saveConfig();
        hologramService.refreshHologram();
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
        getConfig().set("price", ticketPrice);
        saveConfig();
        hologramService.refreshHologram();
    }

    public double getJackpotMultiplier() {
        return jackpotMultiplier;
    }

    public Instant getNextDraw() {
        return nextDraw;
    }

    public void setNextDraw(Instant nextDraw) {
        this.nextDraw = nextDraw;
        getConfig().set("next-draw", nextDraw.toEpochMilli());
        saveConfig();
        hologramService.refreshHologram();
    }

    public void setNextDrawIn24h() {
        setNextDraw(Instant.now().plus(Duration.ofHours(24)));
    }

    public void scheduleDrawTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Instant.now().isBefore(nextDraw)) {
                    return;
                }
                triggerDraw();
            }
        }.runTaskTimer(this, 20L, 20L * 60L);
    }

    public void triggerDraw() {
        if (lotteryManager.getTicketCount() == 0) {
            broadcast(com.outlaw.lottery.message.Messages.get(language, "no-tickets"));
            setNextDrawIn24h();
            return;
        }
        lotteryManager.drawLottery().ifPresentOrElse(winner -> {
            List<Ticket> winners = lotteryManager.getTicketsByNumbers(winner.numbers());
            lotteryManager.rewardWinners(winners);
            lotteryManager.announceWinners(language, winners);
            hologramService.updateWinnerDisplay(winner);
            lotteryManager.resetTickets();
        }, () -> broadcast(com.outlaw.lottery.message.Messages.get(language, "no-tickets")));
        setNextDrawIn24h();
    }

    public void openShop(Player player) {
        shopManager.openShop(player);
    }

    public void setNpcLocation(Location location, UUID npcId) {
        getConfig().set("npc.world", location.getWorld().getName());
        getConfig().set("npc.x", location.getX());
        getConfig().set("npc.y", location.getY());
        getConfig().set("npc.z", location.getZ());
        getConfig().set("npc.yaw", location.getYaw());
        getConfig().set("npc.pitch", location.getPitch());
        getConfig().set("npc.uuid", npcId != null ? npcId.toString() : "");
        saveConfig();
    }

    public Location loadNpcLocation() {
        FileConfiguration config = getConfig();
        String worldName = config.getString("npc.world", "");
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("World not found for NPC: " + worldName);
            return null;
        }
        double x = config.getDouble("npc.x");
        double y = config.getDouble("npc.y");
        double z = config.getDouble("npc.z");
        float yaw = (float) config.getDouble("npc.yaw");
        float pitch = (float) config.getDouble("npc.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public UUID getStoredNpcId() {
        String uuid = getConfig().getString("npc.uuid", "");
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ex) {
            getLogger().log(Level.WARNING, "Invalid NPC UUID stored: " + uuid, ex);
            return null;
        }
    }

    public void persist() {
        saveConfig();
        lotteryManager.saveTickets();
    }

    public void removeStoredNpc() {
        getConfig().set("npc.world", "");
        getConfig().set("npc.uuid", "");
        saveConfig();
    }

    public void removeNpcEntity() {
        UUID id = getStoredNpcId();
        if (id == null) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(id)) {
                    entity.remove();
                    return;
                }
            }
        }
    }

    public void sendMessage(Player player, String key, Object... args) {
        String message = com.outlaw.lottery.message.Messages.get(language, key);
        if (args.length > 0) {
            message = message.formatted(args);
        }
        player.sendMessage(com.outlaw.lottery.message.Messages.get(language, "prefix") + message);
    }
}

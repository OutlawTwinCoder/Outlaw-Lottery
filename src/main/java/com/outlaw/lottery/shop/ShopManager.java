package com.outlaw.lottery.shop;

import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.lottery.LotteryManager;
import com.outlaw.lottery.lottery.PendingReward;
import com.outlaw.lottery.lottery.Ticket;
import com.outlaw.lottery.message.Messages;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopManager {
    private static final int BUY_SLOT = 3;
    private static final int CLAIM_SLOT = 5;
    private static final int INFO_SLOT = 4;

    private final OutlawLotteryPlugin plugin;
    private final Set<Inventory> openInventories = new HashSet<>();

    public ShopManager(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory createShop(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, Messages.get(plugin.getLanguage(), "shop-title"));
        inventory.setItem(BUY_SLOT, buildBuyItem());
        inventory.setItem(INFO_SLOT, buildInfoItem(player));
        inventory.setItem(CLAIM_SLOT, buildClaimItem(player));
        openInventories.add(inventory);
        return inventory;
    }

    public void openShop(Player player) {
        Inventory inventory = createShop(player);
        player.openInventory(inventory);
    }

    public void closeInventory(Inventory inventory) {
        openInventories.remove(inventory);
    }

    public boolean isShopInventory(Inventory inventory) {
        return inventory != null && openInventories.contains(inventory);
    }

    public Set<Inventory> getOpenInventories() {
        return java.util.Collections.unmodifiableSet(openInventories);
    }

    public void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.equals(Messages.get(plugin.getLanguage(), "shop-buy"))) {
            handlePurchase(player);
        } else if (displayName.equals(Messages.get(plugin.getLanguage(), "shop-claim"))) {
            handleClaim(player);
        }
        updateShopView(player);
    }

    private void handlePurchase(Player player) {
        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "no-economy"));
            return;
        }
        double price = plugin.getTicketPrice();
        if (!economy.has(player, price)) {
            player.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "insufficient-funds"));
            return;
        }
        economy.withdrawPlayer(player, price);
        Ticket ticket = plugin.getLotteryManager().purchaseTicket(player);
        player.sendMessage(Messages.get(plugin.getLanguage(), "prefix")
                + Messages.get(plugin.getLanguage(), "ticket-purchased")
                        .formatted(plugin.getLotteryManager().formatNumbers(ticket.numbers())));
        plugin.getHologramService().refreshHologram();
    }

    private void handleClaim(Player player) {
        LotteryManager.ClaimResult result = plugin.getLotteryManager().claimRewards(player);
        switch (result.state()) {
            case NONE -> plugin.sendMessage(player, "claim-none");
            case TOO_EARLY -> {
                PendingReward next = result.nextAvailable();
                long remaining = next != null ? next.remainingMillis() : 0L;
                plugin.sendMessage(player, "claim-too-early", formatDuration(remaining));
            }
            case SUCCESS -> {
                plugin.sendMessage(player, "claim-success", String.format("%.2f", result.amount()));
                for (PendingReward reward : result.claimed()) {
                    player.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + "§7" + reward.formatNumbers());
                }
                plugin.getHologramService().refreshHologram();
            }
        }
    }

    private void updateShopView(Player player) {
        Inventory top = player.getOpenInventory().getTopInventory();
        if (!isShopInventory(top)) {
            return;
        }
        top.setItem(BUY_SLOT, buildBuyItem());
        top.setItem(INFO_SLOT, buildInfoItem(player));
        top.setItem(CLAIM_SLOT, buildClaimItem(player));
    }

    private ItemStack buildBuyItem() {
        ItemStack buyItem = new ItemStack(Material.EMERALD);
        ItemMeta meta = buyItem.getItemMeta();
        meta.setDisplayName(Messages.get(plugin.getLanguage(), "shop-buy"));
        meta.setLore(java.util.List.of(Messages.get(plugin.getLanguage(), "shop-info")
                .formatted(String.format("%.2f", plugin.getTicketPrice()))));
        buyItem.setItemMeta(meta);
        return buyItem;
    }

    private ItemStack buildInfoItem(Player player) {
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName(Messages.get(plugin.getLanguage(), "shop-owned-title"));
        long count = plugin.getLotteryManager().getTicketCount(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(Messages.get(plugin.getLanguage(), "shop-owned-count").formatted(count));
        meta.setLore(lore);
        infoItem.setItemMeta(meta);
        return infoItem;
    }

    private ItemStack buildClaimItem(Player player) {
        ItemStack claimItem = new ItemStack(Material.CHEST);
        ItemMeta meta = claimItem.getItemMeta();
        meta.setDisplayName(Messages.get(plugin.getLanguage(), "shop-claim"));
        List<String> lore = new ArrayList<>();
        List<PendingReward> pending = plugin.getLotteryManager().getPendingRewards(player.getUniqueId());
        if (pending.isEmpty()) {
            lore.add(Messages.get(plugin.getLanguage(), "shop-claim-none"));
        } else {
            List<PendingReward> ready = pending.stream().filter(PendingReward::isReady).toList();
            if (!ready.isEmpty()) {
                double total = ready.stream().mapToDouble(PendingReward::amount).sum();
                lore.add(Messages.get(plugin.getLanguage(), "shop-claim-ready")
                        .formatted(String.format("%.2f", total)));
            } else {
                PendingReward next = pending.get(0);
                lore.add(Messages.get(plugin.getLanguage(), "shop-claim-wait")
                        .formatted(formatDuration(next.remainingMillis())));
            }
            lore.add("§7" + pending.get(0).formatNumbers());
        }
        meta.setLore(lore);
        claimItem.setItemMeta(meta);
        return claimItem;
    }

    private String formatDuration(long millis) {
        long totalMinutes = Math.max(0L, millis) / 60000L;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h" + String.format("%02d", minutes);
    }
}

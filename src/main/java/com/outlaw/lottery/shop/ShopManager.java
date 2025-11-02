package com.outlaw.lottery.shop;

import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.lottery.Ticket;
import com.outlaw.lottery.message.Messages;
import java.util.HashSet;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopManager {
    private final OutlawLotteryPlugin plugin;
    private final Set<Inventory> openInventories = new HashSet<>();

    public ShopManager(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory createShop(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, Messages.get(plugin.getLanguage(), "shop-title"));
        ItemStack buyItem = new ItemStack(Material.PAPER);
        ItemMeta meta = buyItem.getItemMeta();
        meta.setDisplayName(Messages.get(plugin.getLanguage(), "shop-buy"));
        meta.setLore(java.util.List.of(Messages.get(plugin.getLanguage(), "shop-info").formatted(String.format("%.2f", plugin.getTicketPrice()))));
        buyItem.setItemMeta(meta);
        inventory.setItem(4, buyItem);
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

    public void handlePurchase(Player player) {
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
        giveTicketItem(player, ticket);
        player.sendMessage(Messages.get(plugin.getLanguage(), "prefix") + Messages.get(plugin.getLanguage(), "ticket-purchased").formatted(plugin.getLotteryManager().formatNumbers(ticket.numbers())));
        plugin.getHologramService().refreshHologram();
    }

    private void giveTicketItem(Player player, Ticket ticket) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        String title = plugin.getLanguage() == com.outlaw.lottery.Language.FR ? "Ticket de Loto" : "Loto Ticket";
        meta.setDisplayName("§e" + title);
        meta.setLore(java.util.List.of("§7" + plugin.getLotteryManager().formatNumbers(ticket.numbers())));
        paper.setItemMeta(meta);
        player.getInventory().addItem(paper);
    }
}

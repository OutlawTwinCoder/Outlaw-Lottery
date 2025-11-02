package com.outlaw.lottery.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ShopListener implements Listener {
    private final ShopManager shopManager;

    public ShopListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!shopManager.isShopInventory(event.getView().getTopInventory())) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        shopManager.handlePurchase(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (shopManager.isShopInventory(event.getView().getTopInventory())) {
            shopManager.closeInventory(event.getView().getTopInventory());
        }
    }

    public void closeAll() {
        for (org.bukkit.inventory.Inventory inventory :
                new java.util.ArrayList<>(shopManager.getOpenInventories())) {
            inventory.getViewers().forEach(human -> human.closeInventory());
        }
    }
}

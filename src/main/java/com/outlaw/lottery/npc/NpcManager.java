package com.outlaw.lottery.npc;

import com.outlaw.lottery.OutlawLotteryPlugin;
import com.outlaw.lottery.message.Messages;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class NpcManager implements Listener {
    private final OutlawLotteryPlugin plugin;
    private UUID npcId;

    public NpcManager(OutlawLotteryPlugin plugin) {
        this.plugin = plugin;
        this.npcId = plugin.getStoredNpcId();
        if (npcId != null) {
            Bukkit.getScheduler().runTaskLater(plugin, this::refreshNpc, 20L);
        }
    }

    public void createOrMoveNpc(Location location) {
        cleanupNpc();
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setPersistent(true);
        villager.setCustomNameVisible(true);
        villager.setCustomName("§6Outlaw Lottery");
        villager.getEquipment().setItemInMainHand(new ItemStack(Material.PAPER));
        npcId = villager.getUniqueId();
        plugin.setNpcLocation(location, npcId);
    }

    public void cleanupNpc() {
        if (npcId != null) {
            for (Entity entity : Bukkit.getWorlds().stream().flatMap(world -> world.getEntities().stream()).toList()) {
                if (entity.getUniqueId().equals(npcId)) {
                    entity.remove();
                }
            }
            npcId = null;
        }
    }

    public void deleteNpc() {
        cleanupNpc();
        plugin.removeStoredNpc();
    }

    private void refreshNpc() {
        Location stored = plugin.loadNpcLocation();
        if (stored == null) {
            return;
        }
        Entity existing = stored.getWorld().getEntities().stream()
                .filter(entity -> entity.getUniqueId().equals(npcId))
                .findFirst()
                .orElse(null);
        if (existing == null || existing.isDead()) {
            createOrMoveNpc(stored);
        }
    }

    @EventHandler
    public void onNpcInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (npcId == null || !event.getRightClicked().getUniqueId().equals(npcId)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        plugin.openShop(player);
        player.sendMessage(Messages.get(plugin.getLanguage(), "prefix")
                + Messages.get(plugin.getLanguage(), "shop-info").formatted(String.format("%.2f", plugin.getTicketPrice())));
    }

    public UUID getNpcId() {
        return npcId;
    }
}

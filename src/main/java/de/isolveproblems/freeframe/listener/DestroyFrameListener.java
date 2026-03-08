package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class DestroyFrameListener implements Listener {
    private final FreeFrame freeframe;

    public DestroyFrameListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDestroyItemFrame(HangingBreakByEntityEvent event) {
        Hanging hangingEntity = event.getEntity();
        Entity remover = event.getRemover();
        if (!(hangingEntity instanceof ItemFrame) || !(remover instanceof Player)) {
            return;
        }

        ItemFrame itemFrame = (ItemFrame) hangingEntity;
        FreeFrameData frameData = this.freeframe.getFrameRegistry().findByFrame(itemFrame);
        if (frameData == null) {
            return;
        }

        Player player = (Player) remover;
        if (!player.hasPermission(this.freeframe.getConfigHandler().getDestroyPermissionNode())) {
            player.sendMessage(this.freeframe.getErrorPermissionMessage());
            event.setCancelled(true);
            return;
        }

        if (!this.hasOwnerAccess(player, frameData)) {
            this.freeframe.getMetricsTracker().incrementDeniedAccess();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.access.denied",
                "%prefix% &cYou are not allowed to use this FreeFrame."
            ));
            event.setCancelled(true);
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.destroy.gamemode",
                "%prefix% &cYou have to be in creative mode to destroy this FreeFrame."
            ));
            event.setCancelled(true);
            return;
        }

        if (!player.isSneaking()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.destroy.haveToSneak",
                "%prefix% &cYou have to sneak if you want to destroy this FreeFrame."
            ));
            event.setCancelled(true);
            return;
        }

        player.sendMessage(this.freeframe.getMessage(
            "freeframe.destroy.message",
            "%prefix% &eYou've destroyed the &6FreeFrame &esuccessfully.",
            player
        ));
        this.freeframe.getAuditLogger().logAdminAction(player, "destroy", frameData.getId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageWithBow(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if (entity instanceof ItemFrame
            && damager.getType() == EntityType.ARROW
            && this.freeframe.getFrameRegistry().isTracked((ItemFrame) entity)) {
            event.setCancelled(true);
        }
    }

    private boolean hasOwnerAccess(Player player, FreeFrameData frameData) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.access.requireOwner", false)) {
            return true;
        }

        if (player.hasPermission(this.freeframe.getConfigHandler().getAccessBypassPermissionNode())) {
            return true;
        }

        return frameData.isOwnedBy(player.getUniqueId().toString());
    }
}

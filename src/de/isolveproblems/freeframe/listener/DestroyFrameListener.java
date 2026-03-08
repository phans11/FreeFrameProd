
package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.Var;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class DestroyFrameListener
implements Listener {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);

    @EventHandler
    public void destroyItemFrame(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        Hanging entity = event.getEntity();
        if (!(entity instanceof ItemFrame) || !(remover instanceof Player)) {
            return;
        }

        Player player = (Player)remover;
        if (!player.hasPermission(Var.PERMISSION_FREEFRAME_DESTROY)) {
            player.sendMessage(this.freeframe.getError_Permission());
            event.setCancelled(true);
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            String gamemode = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.destroy.gamemode");
            gamemode = gamemode.replace("%prefix%", this.freeframe.getPrefix());
            gamemode = ChatColor.translateAlternateColorCodes((char)'&', (String)gamemode);
            player.sendMessage(gamemode);
            event.setCancelled(true);
            return;
        }

        if (!player.isSneaking()) {
            String isSneaking = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.destroy.haveToSneak");
            isSneaking = isSneaking.replace("%prefix%", this.freeframe.getPrefix());
            isSneaking = ChatColor.translateAlternateColorCodes((char)'&', (String)isSneaking);
            player.sendMessage(isSneaking);
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
        String destroyItemFrame = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.destroy.message");
        destroyItemFrame = destroyItemFrame.replace("%prefix%", this.freeframe.getPrefix());
        destroyItemFrame = ChatColor.translateAlternateColorCodes((char)'&', (String)destroyItemFrame);
        player.sendMessage(destroyItemFrame);
    }

    @EventHandler
    public void damagedWithBow(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof ItemFrame && damager.getType() == EntityType.ARROW) {
            event.setCancelled(true);
        }
    }
}

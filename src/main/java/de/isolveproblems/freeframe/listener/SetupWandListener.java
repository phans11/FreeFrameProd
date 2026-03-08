package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.ItemPolicy;
import de.isolveproblems.freeframe.utils.SetupWandItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class SetupWandListener implements Listener {
    private final FreeFrame freeframe;

    public SetupWandListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSetupWandUse(PlayerInteractEntityEvent event) {
        if (this.isOffHandInteraction(event)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission(this.freeframe.getConfigHandler().getAdminPermissionNode())) {
            return;
        }

        ItemStack inHand = player.getItemInHand();
        if (!SetupWandItem.isWand(this.freeframe, inHand)) {
            return;
        }

        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof ItemFrame)) {
            return;
        }

        ItemFrame itemFrame = (ItemFrame) clicked;
        ItemStack itemStack = itemFrame.getItem();
        if (itemStack == null || itemStack.getType() == null || "AIR".equals(itemStack.getType().name())) {
            player.sendMessage(this.freeframe.formatMessage("%prefix% &cThe selected ItemFrame is empty."));
            event.setCancelled(true);
            return;
        }

        ItemPolicy.Decision decision = this.freeframe.getItemPolicy().check(this.freeframe.getPluginConfig(), itemStack.getType());
        if (!decision.isAllowed()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.items.blockedMessage",
                "%prefix% &cThis item type is blocked by item policy."
            ));
            event.setCancelled(true);
            return;
        }

        FreeFrameData frameData = this.freeframe.getFrameRegistry().getOrCreate(itemFrame, player, itemStack);
        if (frameData == null) {
            event.setCancelled(true);
            return;
        }

        SetupEditorListener.openEditor(this.freeframe, player, frameData);
        this.freeframe.getAuditLogger().logAdminAction(player, "wand-open", frameData.getId());
        event.setCancelled(true);
    }

    private boolean isOffHandInteraction(PlayerInteractEntityEvent event) {
        try {
            Object hand = event.getClass().getMethod("getHand").invoke(event);
            return hand != null && "OFF_HAND".equals(String.valueOf(hand));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}

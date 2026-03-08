package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PurchaseRequest {
    private final Player player;
    private final FreeFrameData frameData;
    private final ItemStack templateItem;
    private final PurchaseProfile profile;

    public PurchaseRequest(Player player, FreeFrameData frameData, ItemStack templateItem, PurchaseProfile profile) {
        this.player = player;
        this.frameData = frameData;
        this.templateItem = templateItem == null ? null : templateItem.clone();
        this.profile = profile;
    }

    public Player getPlayer() {
        return this.player;
    }

    public FreeFrameData getFrameData() {
        return this.frameData;
    }

    public ItemStack getTemplateItem() {
        return this.templateItem == null ? null : this.templateItem.clone();
    }

    public PurchaseProfile getProfile() {
        return this.profile;
    }
}

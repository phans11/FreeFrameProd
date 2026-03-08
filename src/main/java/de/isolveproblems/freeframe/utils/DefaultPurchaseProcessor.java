package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ChestRestockService;
import de.isolveproblems.freeframe.api.DiscountService;
import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProcessor;
import de.isolveproblems.freeframe.api.PurchaseRequest;
import de.isolveproblems.freeframe.api.PurchaseResult;
import de.isolveproblems.freeframe.api.StatisticsService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DefaultPurchaseProcessor implements PurchaseProcessor {
    private final FreeFrame freeframe;
    private final DiscountService discountService;
    private final ChestRestockService chestRestockService;
    private final StatisticsService statisticsService;

    public DefaultPurchaseProcessor(
        FreeFrame freeframe,
        DiscountService discountService,
        ChestRestockService chestRestockService,
        StatisticsService statisticsService
    ) {
        this.freeframe = freeframe;
        this.discountService = discountService;
        this.chestRestockService = chestRestockService;
        this.statisticsService = statisticsService;
    }

    @Override
    public PurchaseResult process(PurchaseRequest request) {
        if (request == null || request.getPlayer() == null || request.getFrameData() == null || request.getProfile() == null) {
            return PurchaseResult.error("freeframe.error.invalidPurchase", "%prefix% &cPurchase request was invalid.");
        }

        Player player = request.getPlayer();
        FreeFrameData frameData = request.getFrameData();
        ItemStack template = request.getTemplateItem();
        if (template == null || template.getType() == null) {
            return PurchaseResult.error("freeframe.error.invalidPurchase", "%prefix% &cPurchase request was invalid.");
        }

        Location location = this.resolveLocation(frameData);
        if (!this.freeframe.getRegionAccessService().canUse(location, player)) {
            return PurchaseResult.blocked("freeframe.restrictions.denied", "%prefix% &cFreeFrame is disabled in this world/region.");
        }

        FrameType frameType = frameData.getFrameType();
        if (frameType == FrameType.ADMIN_ONLY && !player.hasPermission(this.freeframe.getPluginConfig().getString("freeframe.types.adminOnlyPermission", "freeframe.adminonly"))) {
            return PurchaseResult.blocked("freeframe.types.adminOnlyDenied", "%prefix% &cThis frame is restricted to admins.");
        }

        if (frameType == FrameType.PREVIEW_ONLY) {
            return PurchaseResult.preview("freeframe.types.previewOnly", "%prefix% &7Preview only. No item was granted.");
        }

        int amount = request.getProfile().getAmount();
        if (this.freeframe.getPluginConfig().getBoolean("freeframe.chestRestock.enabled", true) && frameData.getStock() < amount) {
            this.chestRestockService.restock(frameData, template);
        }

        if (frameData.getStock() < amount) {
            return PurchaseResult.blocked("freeframe.purchase.stockOut", "%prefix% &cThis frame is out of stock.");
        }

        double price = frameType == FrameType.FREE ? 0.0D : request.getProfile().getPrice();
        double finalPrice = this.discountService.applyDiscount(player, frameData, price);
        return PurchaseResult.success("freeframe.purchase.success", "%prefix% &aPurchase completed.", finalPrice, amount);
    }

    public void recordSuccessfulPurchase(Player player, FreeFrameData frameData, int amount, double finalPrice) {
        this.statisticsService.recordPurchase(player, frameData, amount, finalPrice);
    }

    private Location resolveLocation(FreeFrameData frameData) {
        if (frameData == null || frameData.getReference() == null) {
            return null;
        }
        return new Location(
            this.freeframe.getServer().getWorld(frameData.getReference().getWorldName()),
            frameData.getReference().getX(),
            frameData.getReference().getY(),
            frameData.getReference().getZ()
        );
    }
}

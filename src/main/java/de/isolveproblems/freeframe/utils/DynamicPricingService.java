package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DynamicPricingService {
    private final FreeFrame freeframe;
    private final Map<String, List<Long>> purchaseTimestamps = new HashMap<String, List<Long>>();

    public DynamicPricingService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public synchronized double apply(FreeFrameData frameData, double basePrice, int currentStock, int maxStock, long now) {
        double price = Math.max(0.0D, basePrice);
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.dynamicPricing.enabled", false) || price <= 0.0D || frameData == null) {
            return price;
        }

        long windowMillis = Math.max(5_000L, this.freeframe.getPluginConfig().getLong("freeframe.dynamicPricing.windowMillis", 600_000L));
        int threshold = Math.max(1, this.freeframe.getPluginConfig().getInt("freeframe.dynamicPricing.demandThreshold", 5));
        double demandStepPercent = Math.max(0.0D, this.freeframe.getPluginConfig().getDouble("freeframe.dynamicPricing.demandStepPercent", 2.5D));
        double lowStockThresholdPercent = Math.max(1.0D, this.freeframe.getPluginConfig().getDouble("freeframe.dynamicPricing.lowStockThresholdPercent", 20.0D));
        double lowStockBonusPercent = Math.max(0.0D, this.freeframe.getPluginConfig().getDouble("freeframe.dynamicPricing.lowStockBonusPercent", 5.0D));
        double minMultiplier = Math.max(0.10D, this.freeframe.getPluginConfig().getDouble("freeframe.dynamicPricing.minMultiplier", 0.75D));
        double maxMultiplier = Math.max(minMultiplier, this.freeframe.getPluginConfig().getDouble("freeframe.dynamicPricing.maxMultiplier", 2.5D));

        String key = this.resolveDemandKey(frameData);
        int purchasesInWindow = this.countInWindow(key, now, windowMillis);
        int steps = purchasesInWindow / threshold;
        double multiplier = 1.0D + ((steps * demandStepPercent) / 100.0D);

        if (maxStock > 0) {
            double stockPercent = ((double) currentStock / (double) maxStock) * 100.0D;
            if (stockPercent <= lowStockThresholdPercent) {
                multiplier += (lowStockBonusPercent / 100.0D);
            }
        }

        if (multiplier < minMultiplier) {
            multiplier = minMultiplier;
        }
        if (multiplier > maxMultiplier) {
            multiplier = maxMultiplier;
        }
        return Math.max(0.0D, price * multiplier);
    }

    public synchronized void recordPurchase(FreeFrameData frameData, long now) {
        if (frameData == null) {
            return;
        }
        String key = this.resolveDemandKey(frameData);
        List<Long> timestamps = this.purchaseTimestamps.get(key);
        if (timestamps == null) {
            timestamps = new ArrayList<Long>();
            this.purchaseTimestamps.put(key, timestamps);
        }
        timestamps.add(Long.valueOf(now));
    }

    private String resolveDemandKey(FreeFrameData frameData) {
        String networkId = frameData.getNetworkId();
        if (networkId != null && !networkId.trim().isEmpty()) {
            return "network:" + networkId;
        }
        return "frame:" + frameData.getId();
    }

    private int countInWindow(String key, long now, long windowMillis) {
        List<Long> timestamps = this.purchaseTimestamps.get(key);
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }

        long cutoff = now - windowMillis;
        int count = 0;
        Iterator<Long> iterator = timestamps.iterator();
        while (iterator.hasNext()) {
            long value = iterator.next().longValue();
            if (value < cutoff) {
                iterator.remove();
                continue;
            }
            count++;
        }
        return count;
    }
}

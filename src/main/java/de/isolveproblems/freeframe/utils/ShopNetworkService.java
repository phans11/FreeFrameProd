package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;

import java.util.ArrayList;
import java.util.List;

public class ShopNetworkService {
    private final FreeFrame freeframe;

    public ShopNetworkService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public int getAvailableStock(FreeFrameData source) {
        if (source == null) {
            return 0;
        }

        String networkId = source.getNetworkId();
        if (networkId == null || networkId.trim().isEmpty()) {
            return Math.max(0, source.getStock());
        }

        int stock = 0;
        List<FreeFrameData> members = this.freeframe.getFrameRegistry().listByNetwork(networkId);
        for (FreeFrameData member : members) {
            stock += Math.max(0, member.getStock());
        }
        return stock;
    }

    public boolean consumeStock(FreeFrameData source, int amount) {
        if (source == null) {
            return false;
        }

        int requested = Math.max(1, amount);
        String networkId = source.getNetworkId();
        if (networkId == null || networkId.trim().isEmpty()) {
            boolean consumed = source.consumeStock(requested);
            if (consumed) {
                this.freeframe.getNetworkSyncService().publishFrameUpdate(source, "network-consume");
            }
            return consumed;
        }

        if (this.getAvailableStock(source) < requested) {
            return false;
        }

        List<FreeFrameData> members = new ArrayList<FreeFrameData>(this.freeframe.getFrameRegistry().listByNetwork(networkId));
        int remaining = requested;
        for (FreeFrameData member : members) {
            if (remaining <= 0) {
                break;
            }

            int available = Math.max(0, member.getStock());
            if (available <= 0) {
                continue;
            }
            int consume = Math.min(available, remaining);
            member.setStock(available - consume);
            this.freeframe.getNetworkSyncService().publishFrameUpdate(member, "network-consume");
            remaining -= consume;
        }
        return remaining <= 0;
    }

    public void restoreStock(FreeFrameData source, int amount) {
        if (source == null || amount <= 0) {
            return;
        }
        String networkId = source.getNetworkId();
        if (networkId == null || networkId.trim().isEmpty()) {
            source.setStock(source.getStock() + amount);
            this.freeframe.getNetworkSyncService().publishFrameUpdate(source, "network-restore");
            return;
        }

        // Conservative rollback: return stock to the clicked frame to avoid over-distribution.
        source.setStock(source.getStock() + amount);
        this.freeframe.getNetworkSyncService().publishFrameUpdate(source, "network-restore");
    }

    public int sizeOfNetwork(String networkId) {
        if (networkId == null || networkId.trim().isEmpty()) {
            return 0;
        }
        return this.freeframe.getFrameRegistry().listByNetwork(networkId).size();
    }

    public int setNetworkPrice(String networkId, double price, String currency) {
        if (networkId == null || networkId.trim().isEmpty()) {
            return 0;
        }

        int changed = 0;
        List<FreeFrameData> members = this.freeframe.getFrameRegistry().listByNetwork(networkId);
        for (FreeFrameData member : members) {
            member.setPrice(price);
            if (currency != null && !currency.trim().isEmpty()) {
                member.setCurrency(currency.trim());
            }
            changed++;
        }
        if (changed > 0) {
            this.freeframe.getFrameRegistry().saveToConfig();
            for (FreeFrameData member : members) {
                this.freeframe.getNetworkSyncService().publishFrameUpdate(member, "network-price");
            }
        }
        return changed;
    }
}

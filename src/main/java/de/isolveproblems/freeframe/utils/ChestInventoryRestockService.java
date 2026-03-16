package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ChestRestockService;
import de.isolveproblems.freeframe.api.RestockRouteReport;
import de.isolveproblems.freeframe.api.RestockRoutingService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChestInventoryRestockService implements ChestRestockService, RestockRoutingService {
    private final FreeFrame freeframe;

    public ChestInventoryRestockService() {
        this.freeframe = null;
    }

    public ChestInventoryRestockService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public int restock(FreeFrameData frameData, ItemStack templateItem) {
        if (frameData == null || templateItem == null || templateItem.getType() == null) {
            return 0;
        }

        int missing = Math.max(0, frameData.getMaxStock() - frameData.getStock());
        if (missing <= 0) {
            return 0;
        }

        List<RouteNode> candidates = this.resolveRoute(frameData);
        int moved = this.moveItems(candidates, templateItem.getType(), missing);
        if (moved > 0) {
            frameData.setStock(frameData.getStock() + moved);
            frameData.setLastRefillAt(System.currentTimeMillis());
        }
        return moved;
    }

    @Override
    public RestockRouteReport previewRoute(FreeFrameData frameData, ItemStack templateItem) {
        if (frameData == null || templateItem == null || templateItem.getType() == null) {
            return new RestockRouteReport(0, 0, Collections.<String>emptyList());
        }

        int requested = Math.max(0, frameData.getMaxStock() - frameData.getStock());
        if (requested <= 0) {
            return new RestockRouteReport(0, 0, Collections.<String>emptyList());
        }

        List<RouteNode> route = this.resolveRoute(frameData);
        List<String> labels = new ArrayList<String>();
        int available = 0;
        for (RouteNode node : route) {
            int stock = this.countItems(node.inventory, templateItem.getType());
            if (stock <= 0) {
                continue;
            }
            available += stock;
            labels.add(node.label + " (" + stock + ")");
        }

        return new RestockRouteReport(requested, Math.min(requested, available), labels);
    }

    private List<RouteNode> resolveRoute(FreeFrameData frameData) {
        List<RouteNode> route = new ArrayList<RouteNode>();
        Set<String> seen = new HashSet<String>();

        if (frameData.getLinkedChest() != null) {
            Inventory linked = this.inventoryAt(frameData.getLinkedChest());
            if (linked != null) {
                this.addRouteNode(route, seen, linked, "linked:" + frameData.getLinkedChest().serialize(), 0, 0);
            }
        }

        this.addNearbyRouteNodes(frameData, route, seen);
        this.addNetworkRouteNodes(frameData, route, seen);

        Collections.sort(route, new Comparator<RouteNode>() {
            @Override
            public int compare(RouteNode left, RouteNode right) {
                int byPriority = Integer.compare(left.priority, right.priority);
                if (byPriority != 0) {
                    return byPriority;
                }
                return Integer.compare(left.distance, right.distance);
            }
        });
        return route;
    }

    private void addNearbyRouteNodes(FreeFrameData frameData, List<RouteNode> route, Set<String> seen) {
        if (frameData.getReference() == null) {
            return;
        }
        World world = Bukkit.getWorld(frameData.getReference().getWorldName());
        if (world == null) {
            return;
        }

        int radius = this.freeframe == null
            ? 1
            : Math.max(1, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_CHESTRESTOCK_ROUTE_SCANRADIUS));
        int baseX = frameData.getReference().getX();
        int baseY = frameData.getReference().getY();
        int baseZ = frameData.getReference().getZ();

        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int y = baseY - radius; y <= baseY + radius; y++) {
                for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                    int distance = Math.abs(baseX - x) + Math.abs(baseY - y) + Math.abs(baseZ - z);
                    if (distance == 0) {
                        continue;
                    }
                    BlockReference reference = new BlockReference(world.getName(), x, y, z);
                    Inventory inventory = this.inventoryAt(reference);
                    if (inventory == null) {
                        continue;
                    }
                    this.addRouteNode(route, seen, inventory, "nearby:" + reference.serialize(), 10, distance);
                }
            }
        }
    }

    private void addNetworkRouteNodes(FreeFrameData frameData, List<RouteNode> route, Set<String> seen) {
        if (this.freeframe == null || !this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_CHESTRESTOCK_ROUTE_NETWORKENABLED)) {
            return;
        }
        if (frameData.getNetworkId() == null || frameData.getNetworkId().trim().isEmpty()) {
            return;
        }

        List<FreeFrameData> members = this.freeframe.getFrameRegistry().listByNetwork(frameData.getNetworkId());
        for (FreeFrameData member : members) {
            if (member == null || member.getLinkedChest() == null) {
                continue;
            }
            Inventory inventory = this.inventoryAt(member.getLinkedChest());
            if (inventory == null) {
                continue;
            }
            this.addRouteNode(route, seen, inventory, "network:" + member.getId() + ":" + member.getLinkedChest().serialize(), 20, 0);
        }
    }

    private void addRouteNode(List<RouteNode> route, Set<String> seen, Inventory inventory, String label, int priority, int distance) {
        if (inventory == null) {
            return;
        }
        String key = label == null ? String.valueOf(inventory.hashCode()) : label.toLowerCase(Locale.ENGLISH);
        if (!seen.add(key)) {
            return;
        }
        route.add(new RouteNode(inventory, label, priority, distance));
    }

    private int moveItems(List<RouteNode> candidates, Material material, int requested) {
        int moved = 0;
        for (RouteNode candidate : candidates) {
            if (moved >= requested) {
                break;
            }

            ItemStack[] contents = candidate.inventory.getContents();
            if (contents == null) {
                continue;
            }

            for (ItemStack stack : contents) {
                if (stack == null || stack.getType() != material) {
                    continue;
                }
                int toMove = Math.min(requested - moved, stack.getAmount());
                if (toMove <= 0) {
                    continue;
                }

                stack.setAmount(stack.getAmount() - toMove);
                moved += toMove;
                if (moved >= requested) {
                    break;
                }
            }
        }
        return moved;
    }

    private int countItems(Inventory inventory, Material material) {
        if (inventory == null || material == null) {
            return 0;
        }
        int amount = 0;
        ItemStack[] contents = inventory.getContents();
        if (contents == null) {
            return 0;
        }
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == material) {
                amount += Math.max(0, stack.getAmount());
            }
        }
        return amount;
    }

    private Inventory inventoryAt(BlockReference reference) {
        if (reference == null) {
            return null;
        }

        World world = Bukkit.getWorld(reference.getWorldName());
        if (world == null) {
            return null;
        }

        Block block = world.getBlockAt(reference.getX(), reference.getY(), reference.getZ());
        if (block == null || block.getType() == null || block.getType() == Material.AIR) {
            return null;
        }

        BlockState state = block.getState();
        if (!(state instanceof InventoryHolder)) {
            return null;
        }
        return ((InventoryHolder) state).getInventory();
    }

    private static final class RouteNode {
        private final Inventory inventory;
        private final String label;
        private final int priority;
        private final int distance;

        private RouteNode(Inventory inventory, String label, int priority, int distance) {
            this.inventory = inventory;
            this.label = label == null ? "unknown" : label;
            this.priority = priority;
            this.distance = distance;
        }
    }
}

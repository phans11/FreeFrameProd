package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.ChestRestockService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ChestInventoryRestockService implements ChestRestockService {
    @Override
    public int restock(FreeFrameData frameData, ItemStack templateItem) {
        if (frameData == null || templateItem == null || templateItem.getType() == null) {
            return 0;
        }

        Inventory inventory = this.resolveInventory(frameData);
        if (inventory == null) {
            return 0;
        }

        int missing = Math.max(0, frameData.getMaxStock() - frameData.getStock());
        if (missing <= 0) {
            return 0;
        }

        int moved = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType() != templateItem.getType()) {
                continue;
            }

            int toMove = Math.min(missing - moved, stack.getAmount());
            if (toMove <= 0) {
                break;
            }

            stack.setAmount(stack.getAmount() - toMove);
            moved += toMove;
            if (moved >= missing) {
                break;
            }
        }

        if (moved > 0) {
            frameData.setStock(frameData.getStock() + moved);
            frameData.setLastRefillAt(System.currentTimeMillis());
        }
        return moved;
    }

    private Inventory resolveInventory(FreeFrameData frameData) {
        BlockReference linkedChest = frameData.getLinkedChest();
        if (linkedChest != null) {
            Inventory inventory = this.inventoryAt(linkedChest);
            if (inventory != null) {
                return inventory;
            }
        }

        if (frameData.getReference() == null) {
            return null;
        }

        World world = Bukkit.getWorld(frameData.getReference().getWorldName());
        if (world == null) {
            return null;
        }

        int baseX = frameData.getReference().getX();
        int baseY = frameData.getReference().getY();
        int baseZ = frameData.getReference().getZ();
        int[][] offsets = new int[][] {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
        };

        for (int[] offset : offsets) {
            Inventory inventory = this.inventoryAt(new BlockReference(world.getName(), baseX + offset[0], baseY + offset[1], baseZ + offset[2]));
            if (inventory != null) {
                frameData.setLinkedChest(new BlockReference(world.getName(), baseX + offset[0], baseY + offset[1], baseZ + offset[2]));
                return inventory;
            }
        }
        return null;
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
}

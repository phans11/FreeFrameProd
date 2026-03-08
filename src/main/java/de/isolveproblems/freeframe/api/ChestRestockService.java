package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.inventory.ItemStack;

public interface ChestRestockService {
    int restock(FreeFrameData frameData, ItemStack templateItem);
}

package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.inventory.ItemStack;

public interface RestockRoutingService {
    RestockRouteReport previewRoute(FreeFrameData frameData, ItemStack templateItem);
}

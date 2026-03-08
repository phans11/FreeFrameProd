package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class FrameBreakCleanupListener implements Listener {
    private final FreeFrame freeframe;

    public FrameBreakCleanupListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFrameBroken(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }

        this.freeframe.getFrameRegistry().untrack((ItemFrame) event.getEntity());
    }
}

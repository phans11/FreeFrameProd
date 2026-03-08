package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.FreeFrameData;
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

        ItemFrame frame = (ItemFrame) event.getEntity();
        FreeFrameData tracked = this.freeframe.getFrameRegistry().findByFrame(frame);
        this.freeframe.getFrameRegistry().untrack(frame);

        if (tracked != null) {
            this.freeframe.getAuditLogger().logAdminAction(
                this.freeframe.getServer().getConsoleSender(),
                "cleanup",
                tracked.getId() + " cause=" + event.getCause()
            );
        }
    }
}

package de.isolveproblems.freeframe.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestockRouteReport {
    private final int requested;
    private final int moved;
    private final List<String> routeNodes;

    public RestockRouteReport(int requested, int moved, List<String> routeNodes) {
        this.requested = Math.max(0, requested);
        this.moved = Math.max(0, moved);
        this.routeNodes = new ArrayList<String>();
        if (routeNodes != null) {
            this.routeNodes.addAll(routeNodes);
        }
    }

    public int getRequested() {
        return this.requested;
    }

    public int getMoved() {
        return this.moved;
    }

    public int getMissing() {
        return Math.max(0, this.requested - this.moved);
    }

    public List<String> getRouteNodes() {
        return Collections.unmodifiableList(this.routeNodes);
    }
}

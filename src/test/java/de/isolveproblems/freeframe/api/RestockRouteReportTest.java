package de.isolveproblems.freeframe.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestockRouteReportTest {
    @Test
    void missingShouldBeDerivedFromRequestedAndMoved() {
        RestockRouteReport report = new RestockRouteReport(64, 40, Arrays.asList("a", "b"));
        assertEquals(64, report.getRequested());
        assertEquals(40, report.getMoved());
        assertEquals(24, report.getMissing());
        assertEquals(2, report.getRouteNodes().size());
    }

    @Test
    void valuesShouldBeClampedToZero() {
        RestockRouteReport report = new RestockRouteReport(-1, -10, null);
        assertEquals(0, report.getRequested());
        assertEquals(0, report.getMoved());
        assertEquals(0, report.getMissing());
    }
}

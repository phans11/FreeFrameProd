package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardServerAuthTest {

    @Test
    void tokenMatchRequiresExactTokenParameter() {
        assertTrue(DashboardServer.tokenMatches("a=1&token=secret&b=2", "secret"));
        assertFalse(DashboardServer.tokenMatches("a=1&token=secret123&b=2", "secret"));
        assertFalse(DashboardServer.tokenMatches("a=1&mytoken=secret&b=2", "secret"));
    }

    @Test
    void queryParamShouldDecodeEncodedValues() {
        assertEquals("s e c r e t", DashboardServer.queryParam("token=s+e+c+r+e+t", "token"));
        assertEquals("abc=123", DashboardServer.queryParam("x=1&token=abc%3D123", "token"));
        assertNull(DashboardServer.queryParam("x=1&y=2", "token"));
    }
}

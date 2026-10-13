package com.jansetu.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final HealthController controller = new HealthController(jdbc);

    @Test
    void readinessRequiresWorkingPostgresConnection() {
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        var response = controller.ready();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void readinessFailsClosedWithoutExposingDatabaseError() {
        when(jdbc.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("private-db.example:5432 secret detail"));
        var response = controller.ready();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("NOT_READY", response.getBody().get("status"));
    }

    @Test
    void livenessRemainsIndependentOfDatabaseState() {
        assertEquals("UP", controller.health().status());
        assertEquals("2.0.0", controller.health().version());
    }
}

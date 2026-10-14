package com.jansetu.backend.common;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdFilterTest {
    @Test
    void newCorrelationIdIsReturnedToClientAndClearedAfterRequest() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/schemes");
        request.addHeader("X-Request-ID", "malicious-header-with-private-account-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            String value = MDC.get(RequestIdFilter.MDC_KEY);
            assertNotNull(value);
            assertEquals(value, ((jakarta.servlet.http.HttpServletResponse) res)
                    .getHeader(RequestIdFilter.HEADER));
        });
        String first = response.getHeader(RequestIdFilter.HEADER);
        assertNotNull(first);
        assertDoesNotThrow(() -> UUID.fromString(first));
        assertNotEquals("malicious-header-with-private-account-id", first);
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    @Test
    void separateRequestsHaveDifferentIds() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletResponse a = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/health"), a, (req, res) -> {});
        MockHttpServletResponse b = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/health"), b, (req, res) -> {});
        assertNotEquals(a.getHeader(RequestIdFilter.HEADER), b.getHeader(RequestIdFilter.HEADER));
    }
}

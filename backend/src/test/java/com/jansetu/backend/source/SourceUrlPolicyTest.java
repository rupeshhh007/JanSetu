package com.jansetu.backend.source;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SourceUrlPolicyTest {
    @Test
    void acceptsAnExactAllowlistedHttpsHostAndRejectsLookalikes() throws Exception {
        SourceUrlPolicy policy = new SourceUrlPolicy("mahadbt.maharashtra.gov.in",
                host -> List.of(InetAddress.getByName("203.0.113.10")));

        URI canonical = policy.canonicalize("https://mahadbt.maharashtra.gov.in/schemes/../scheme?id=1");

        assertEquals("https://mahadbt.maharashtra.gov.in/scheme?id=1", canonical.toString());
        assertThrows(SourceIntakeException.class,
                () -> policy.canonicalize("https://mahadbt.maharashtra.gov.in.attacker.example/scheme"));
    }

    @Test
    void blocksLocalAddressesBeforeAnHttpRequestCanStart() throws Exception {
        SourceUrlPolicy policy = new SourceUrlPolicy("india.gov.in",
                host -> List.of(InetAddress.getByName("127.0.0.1")));

        assertThrows(SourceIntakeException.class,
                () -> policy.validateForRetrieval(URI.create("https://india.gov.in/catalogue")));
    }

    @Test
    void rejectsCredentialsFragmentsAndNonStandardPorts() {
        SourceUrlPolicy policy = new SourceUrlPolicy("india.gov.in", host -> List.of());

        assertThrows(SourceIntakeException.class, () -> policy.canonicalize("https://user:pass@india.gov.in/catalogue"));
        assertThrows(SourceIntakeException.class, () -> policy.canonicalize("https://india.gov.in:8443/catalogue"));
        assertThrows(SourceIntakeException.class, () -> policy.canonicalize("https://india.gov.in/catalogue#section"));
    }
}

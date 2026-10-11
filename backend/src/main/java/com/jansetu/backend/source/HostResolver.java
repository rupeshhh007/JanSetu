package com.jansetu.backend.source;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/** Small boundary around DNS so source safety rules stay deterministic in tests. */
@FunctionalInterface
public interface HostResolver {
    List<InetAddress> resolve(String host) throws UnknownHostException;
}

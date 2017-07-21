package com.jetty.server;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhawangayash on 7/19/17.
 */
public class ClientProcessor {
    private static final String uriPath = "/";

    public List<String> processingURI(String uri) {
        if (uri == null)
            return null;

        List<String> clients = new ArrayList<>();
        for (String client : uri.split(uriPath)) {
            if (client.equalsIgnoreCase("jetty") || StringUtils.isEmpty(client))
                continue;
            clients.add(client);
        }
        return clients;
    }
}

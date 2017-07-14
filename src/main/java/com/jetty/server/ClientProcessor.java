/**
 * Copyright (c) 2017, DATAVISOR, INC.
 * All rights reserved.
 * __________________
 * <p>
 * NOTICE: All information contained herein is, and remains the property
 * of DataVisor, Inc.  The intellectual and technical concepts contained
 * herein are proprietary to DataVisor, Inc. and may be covered by
 * U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law.  Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from DataVisor, Inc.
 */
package com.jetty.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClientProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProcessor.class);
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

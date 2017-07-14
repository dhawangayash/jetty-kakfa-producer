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

import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ClientProcessorTest {
    ClientProcessor clientProcessor = new ClientProcessor();

    @Test
    public void testProcessingURI() {
        String fullyQualifiedURI = "/jetty/client/client_id/client_EVENT_NAME";
        assertEquals("fully qualified URI:client_name/sub_client_name",
                "[client,client_id,client_EVENT_NAME]",
                clientProcessor.processingURI(fullyQualifiedURI)
                        .stream().collect(Collectors.joining(",", "[", "]")));
        assertEquals("URI is null", null, clientProcessor.processingURI(null));
    }
}


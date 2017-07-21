package com.jetty.server;

import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by dhawangayash on 7/20/17.
 */
public class ClientProcessorTest {
    ClientProcessor clientProcessor = new ClientProcessor();

    @Test
    public void testProcessingURI() {
        String fullyQualifiedURI = "/jetty/client/client_id/client_EVENT_NAME";
        assertEquals("fully qualified URI:client_name/sub_client_name",
                "[client,client_id,client_EVENT_NAME]", clientProcessor.processingURI(fullyQualifiedURI)
                        .stream().collect(Collectors.joining(",", "[", "]")));
        assertEquals("URI is null", null, clientProcessor.processingURI(null));
    }
}


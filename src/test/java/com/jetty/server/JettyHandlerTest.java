package com.jetty.server;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dhawangayash on 7/20/17.
 */
public class JettyHandlerTest {

    @Test(dataProvider = "provideStringAndExpectedLength")
    public void testRetrieveJSONReq(String json, String msg) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        assertEquals(msg, json, new JettyServletHandler().getInstance().retriveJSONRequest(request));
    }

    @Test(dataProvider = "convertJSONToKafka")
    public void testconvertJSONToKafka(String msg, String expectedJSON, String json) throws Exception {
        assertEquals(msg, expectedJSON, new JettyServletHandler().getInstance().convertJSONToKafka(json).toString());
    }

    @Test(dataProvider = "calculateKafkaTopic")
    public void testCalculateKafkaTopic(String msg, String expectedTopicName, List<String> clients, String json) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        assertEquals(msg, expectedTopicName, new JettyServletHandler().getInstance().calculateKafkaTopic(clients,json));
    }

    @DataProvider
    public static Object[][] convertJSONToKafka() {
        return new Object[][]{
                {"Json payload", "{\"records\":[{\"value\":{\"json\":\"help\"}}]}", "{\"json\":\"help\"}"},
                {"null payload", "{\"records\":[{\"value\":{}}]}", null}
        };
    }

    @DataProvider
    public static Object[][] calculateKafkaTopic() {
        return new Object[][]{
                {"List of client names:",
                        "king-appsFlyer-adjust",
                        new ArrayList() {{
                    add("king");
                    add("appsFlyer");
                    add("adjust");
                }}, "{\"records\":[{\"value\":{\"json\":\"help\", \"clientName\":\"appsFlyer\"}}]}"}
        };
    }

    @DataProvider
    public static Object[][] provideStringAndExpectedLength() {
        return new Object[][]{
                {"{\"json\":\"help\"}", "Json parsing from Http Request Fails."},
                {"null", "Non-Json text"},
        };
    }
}

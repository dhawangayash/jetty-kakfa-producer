package com.jetty.server;

import io.prometheus.client.Counter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JettyServletHandler extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(JettyServletHandler.class);

    private final HttpClient client = new HttpClient();
    private final static String DELIMITER = "-";
    CuratorFramework curatorClient = CuratorFrameworkFactory
            .newClient("localhost:2181,localhost:2182,localhost:2183",
                    new ExponentialBackoffRetry(1000, 3));


    enum Events {
        success_request,
        failed_json_parsing_request,
        failed_json_translation,
        failed_publish_to_kafka,
        topic_resolution_error;
    }

    static final Counter success_request_cnt = Counter.build().name(Events.success_request.name()).help("Incoming requests.").register();
    static final Counter failed_json_parsing_request_cnt = Counter.build().name(Events.failed_json_parsing_request.name()).help("Total failed json parsing requests.").register();
    static final Counter failed_json_translation_cnt = Counter.build().name(Events.failed_json_translation.name()).help("Total failed json translation requests").register();
    static final Counter failed_publish_to_kafka_cnt = Counter.build().name(Events.failed_publish_to_kafka.name()).help("Total failed publish to kafka requests.").register();
    static final Counter topic_resolution_error = Counter.build().name(Events.topic_resolution_error.name()).help("Topic failed resolutoin error.").register();

    public JettyServletHandler() throws Exception {
        LOG.info("Starting Jetty HttpClient...");
        client.start();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UtilityClass utilityClass = new UtilityClass();
        LOG.debug("Called at: " + System.currentTimeMillis());
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String jsonRequest = utilityClass.retriveJSONRequest(request);
        LOG.debug("'" + jsonRequest + "'");
        JSONObject newJsonOBject = null;
        try {
            newJsonOBject = utilityClass.convertJSONToKafka(jsonRequest);
        } catch (JSONException e) {
            failed_json_translation_cnt.inc();
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("URI:'" + request.getRequestURI() + "'");
        List<String> clients = new ClientProcessor().processingURI(request.getRequestURI());
        LOG.info(clients.stream().collect(Collectors.joining(", ", "[", "]")));
        String topicName = utilityClass.calculateKafkaTopic(clients, jsonRequest);

        utilityClass.sendReqToKafkaExecutor(newJsonOBject);
        response.getWriter().println("SUCCESS");
    }

    protected UtilityClass getInstance() {
        return new UtilityClass();
    }

    public class UtilityClass {
        protected String calculateKafkaTopic(List<String> clientName, String jsonRequest) {
            StringBuilder kafkaTopic = new StringBuilder();
            kafkaTopic.append(clientName.stream().collect(Collectors.joining(DELIMITER)));
            try {
                JSONObject json = new JSONObject(jsonRequest);
                Iterator iter = json.keys();
                while (iter.hasNext()) {
                    String key1 = (String) iter.next();
                    if (key1.equalsIgnoreCase("client")) {
                        kafkaTopic.append(DELIMITER).append((String) json.get(key1));
                    }
                }
            } catch (JSONException e) {
                topic_resolution_error.inc();
                LOG.error(e.getMessage(), e);
            }
            return kafkaTopic.toString();
        }

        protected String retriveJSONRequest(HttpServletRequest request) {
            if (request == null)
                return null;
            try (BufferedReader reader = request.getReader()) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                return sb.toString();
            } catch (Exception e) {
                failed_json_parsing_request_cnt.inc();
                LOG.error(e.getMessage(), e);
            }
            return null;
        }

        protected JSONObject convertJSONToKafka(String json) throws JSONException {
            JSONObject jsonObject = (null != json) ? new JSONObject(json) : new JSONObject();
            JSONObject newJsonOBject = new JSONObject();

            JSONObject value = new JSONObject();
            value.put("value", jsonObject);
            newJsonOBject.put("records", new JSONArray().put(value));
            return newJsonOBject;
        }

        public void sendReqToKafkaExecutor(JSONObject json) {
            if (json == null || json.toString() == null) {
                return;
            }
            LOG.debug("Sending clientRequest...");
            try {
                Timer timer = new Timer();
                client.newRequest("http://0.0.0.0:8082/topics/topic-5678")
                        .method(HttpMethod.POST)
                        .header(HttpHeader.CONTENT_TYPE, "application/vnd.kafka.json.v2+json")
                        .header(HttpHeader.ACCEPT, "application/json")
                        .content(new StringContentProvider(json.toString()))
                        .send(new Response.CompleteListener() {
                            @Override
                            public void onComplete(Result result) {
                                LOG.debug("Completed: " + result.getResponse().getStatus());
                            }
                        });
                timer.getElapsedTime();
                success_request_cnt.inc();
            } catch (Exception e) {
                failed_publish_to_kafka_cnt.inc();
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    class Timer {
        long startTime = System.currentTimeMillis();

        public void getElapsedTime() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.debug("ElapsedTime:'" + elapsedTime + "'");
        }
    }
}

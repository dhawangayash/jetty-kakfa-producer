package com.jetty.server;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dhawangayash on 7/14/17.
 */
public class JettyServletHandler extends HttpServlet {

    private final HttpClient client = new HttpClient();
    private final static String DELIMITER = "-";

    enum Events {
        success_request,
        failed_json_parsing_request,
        failed_json_translation,
        failed_publish_to_kafka;
    }

    static final Counter success_request_cnt = Counter.build().name(Events.success_request.name()).help("Incoming requests.").register();
    static final Counter failed_json_parsing_request_cnt = Counter.build().name(Events.failed_json_parsing_request.name()).help("Total failed json parsing requests.").register();
    static final Counter failed_json_translation_cnt = Counter.build().name(Events.failed_json_translation.name()).help("Total failed json translation requests").register();
    static final Counter failed_publish_to_kafka_cnt = Counter.build().name(Events.failed_publish_to_kafka.name()).help("Total failed publish to kafka requests.").register();

    public JettyServletHandler() throws Exception {
        System.out.println("Starting Jetty HttpClient...");
        client.start();
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UtilityClass utilityClass = new UtilityClass();
        System.out.println("Called at: " + System.currentTimeMillis());
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String jsonRequest = utilityClass.retriveJSONRequest(request);
        System.out.println("+++++" + jsonRequest);
        JSONObject newJsonOBject = null;
        try {
            newJsonOBject = utilityClass.convertJSONToKafka(jsonRequest);
        } catch (JSONException e) {
            failed_json_translation_cnt.inc();
            e.printStackTrace();
        }

        System.out.println("URI:'" + request.getRequestURI() + "'");
        List<String> clients = new ClientProcessor().processingURI(request.getRequestURI());
        System.out.println(clients.stream().collect(Collectors.joining(", ", "[", "]")));
        String topicName = utilityClass.calculateKafkaTopic(clients, jsonRequest);

        utilityClass.sendReqToKafkaExecutor(newJsonOBject);
        response.getWriter().println("SUCCESS");

//      System.out.println(newJsonOBject.toString());

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    sendReqToKafka(newJsonOBject);
//                }
//            }).start();

//        String payload = "{\"application_nm\":\"datastream\",\"customer\":\"apple\"}";

//        PostToKafka post2Kafka = new PostToKafka();
//        try {
//            post2Kafka.write2Kafka(payload);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        post2Kafka = null;
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
                        kafkaTopic.append(DELIMITER).append((String)json.get(key1));
                    }
                }
            } catch (JSONException e) {

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
                e.printStackTrace();
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
            System.out.println("Sending clientRequest...");
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
                                System.out.println("Completed: " + result.getResponse().getStatus());
                            }
                        });
                timer.getElapsedTime();
                success_request_cnt.inc();
            } catch (Exception e) {
                failed_publish_to_kafka_cnt.inc();
                e.printStackTrace();
            }
        }
    }

    /**
     * public void sendReqToKafka(JSONObject newJsonObject) {
     * //        Timer timer = new Timer();
     * HttpClientTransportOverHTTP transport = new HttpClientTransportOverHTTP();
     * HttpClient httpClient = new HttpClient(transport, null);
     * transport.setHttpClient(httpClient);
     * <p>
     * org.eclipse.jetty.client.api.Request request =
     * httpClient.POST("http://0.0.0.0:8082/topics/topic-1234");
     * <p>
     * request.header(HttpHeader.CONTENT_TYPE, "application/vnd.kafka.json.v2+json")
     * .header(HttpHeader.ACCEPT, "application/json");
     * <p>
     * request.content(new StringContentProvider(newJsonObject.toString()));
     * try {
     * httpClient.start();
     * Timer timer = new Timer();
     * request.send();
     * timer.getElapsedTime();
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * <p>
     * request.send(new BufferingResponseListener() {
     *
     * @Override public void onComplete(Result result) {
     * if (result.isFailed()) {
     * System.out.println(result.getResponse().getStatus());
     * System.out.println(result);
     * System.out.println("OMG");
     * }
     * if (result.isSucceeded()) {
     * System.out.println("WOW!!");
     * }
     * }
     * });
     * }
     * <p>
     * public void sendReqToKafkaPOST(JSONObject newJsonObject) {
     * CloseableHttpClient client = HttpClients.createMinimal();
     * HttpPost post = new HttpPost("http://0.0.0.0:8082/topics/topic-rest-client");
     * post.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.kafka.json.v2+json");
     * try {
     * post.setEntity(new StringEntity(newJsonObject.toString()));
     * <p>
     * Timer timer = new Timer();
     * CloseableHttpResponse response = client.execute(post);
     * timer.getElapsedTime();
     * System.out.println(response.getStatusLine().getStatusCode());
     * client.close();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }
     */

    class Timer {
        long startTime = System.currentTimeMillis();

        public void getElapsedTime() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("ElapsedTime:'" + elapsedTime + "'");
        }
    }
}

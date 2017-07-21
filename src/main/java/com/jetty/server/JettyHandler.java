package com.jetty.server;
/*
import io.prometheus.client.Counter;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
*/

public class JettyHandler {}
/*
extends AbstractHandler {

    private final HttpClient client = new HttpClient();

    enum Events {
        success_request,
        failed_json_parsing_request,
        failed_json_translation,
        failed_publish_to_kafka;
    }

    static final Counter success_request_cnt = Counter.build()
            .name(Events.success_request.name()).help("Total successful requests.").register();
    static final Counter failed_json_parsing_request_cnt = Counter.build()
            .name(Events.failed_json_parsing_request.name()).help("Total failed json parsing requests.").register();
    static final Counter failed_json_translation_cnt = Counter.build()
            .name(Events.failed_json_translation.name()).help("Total failed json translation requests").register();
    static final Counter failed_publish_to_kafka_cnt = Counter.build()
            .name(Events.failed_publish_to_kafka.name()).help("Total failed publish to kafka requests.").register();

    public JettyHandler() throws Exception {
        System.out.println("Starting Jetty HttpClient...");
        client.start();
    }

    public void handle(String target,
                       Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        System.out.println("Called at: " + System.currentTimeMillis());
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String jsonReqeust = retriveJSONRequest(request);
        System.out.println("+++++" + jsonReqeust);
        JSONObject newJsonOBject = null;
        try {
            newJsonOBject = convertJSONToKafka(jsonReqeust);
        } catch (JSONException e) {
            failed_json_translation_cnt.inc();
            e.printStackTrace();
        }

        sendReqToKafkaExecutor(newJsonOBject);
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

    protected String retriveJSONRequest(HttpServletRequest request) {
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
        JSONObject jsonObject = new JSONObject(json);
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
*/

/**
    public void sendReqToKafka(JSONObject newJsonObject) {
//        Timer timer = new Timer();
        HttpClientTransportOverHTTP transport = new HttpClientTransportOverHTTP();
        HttpClient httpClient = new HttpClient(transport, null);
        transport.setHttpClient(httpClient);

        org.eclipse.jetty.client.api.Request request =
                httpClient.POST("http://0.0.0.0:8082/topics/topic-1234");

        request.header(HttpHeader.CONTENT_TYPE, "application/vnd.kafka.json.v2+json")
                .header(HttpHeader.ACCEPT, "application/json");

        request.content(new StringContentProvider(newJsonObject.toString()));
        try {
            httpClient.start();
            Timer timer = new Timer();
            request.send();
            timer.getElapsedTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    System.out.println(result.getResponse().getStatus());
                    System.out.println(result);
                    System.out.println("OMG");
                }
                if (result.isSucceeded()) {
                    System.out.println("WOW!!");
                }
            }
        });
    }

    public void sendReqToKafkaPOST(JSONObject newJsonObject) {
        CloseableHttpClient client = HttpClients.createMinimal();
        HttpPost post = new HttpPost("http://0.0.0.0:8082/topics/topic-rest-client");
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.kafka.json.v2+json");
        try {
            post.setEntity(new StringEntity(newJsonObject.toString()));

            Timer timer = new Timer();
            CloseableHttpResponse response = client.execute(post);
            timer.getElapsedTime();
            System.out.println(response.getStatusLine().getStatusCode());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

/**
    class Timer {
        long startTime = System.currentTimeMillis();

        public void getElapsedTime() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("ElapsedTime:'" + elapsedTime + "'");
        }
    }

}*/

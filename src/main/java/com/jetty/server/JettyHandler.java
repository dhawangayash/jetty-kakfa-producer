package com.jetty.server;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class JettyHandler extends AbstractHandler {
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
//        System.out.println("Request ==> " + request.getReader());

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONObject newJsonOBject = new JSONObject();

            JSONObject value = new JSONObject();
            value.put("value", jsonObject);
            newJsonOBject.put("records", new JSONArray().put(value));

//            System.out.println(newJsonOBject.toString());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendReqToKafka(newJsonOBject);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
//        String payload = "{\"application_nm\":\"datastream\",\"customer\":\"apple\"}";

//        PostToKafka post2Kafka = new PostToKafka();
//        try {
//            post2Kafka.write2Kafka(payload);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        post2Kafka = null;
        response.getWriter().println("SUCCESS");
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

    public void sendReqToKafka(JSONObject newJsonObject) {
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
            request.send();
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

    class Timer {
        long startTime = System.currentTimeMillis();

        public void getElapsedTime() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("ElapsedTime:'" + elapsedTime + "'");
        }
    }
}

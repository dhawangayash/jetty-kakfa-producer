## Simple Http Server Using Jetty API

## What it does ?
- Accepts requests from the server
- Reads the incoming Http Request 
- Pushes the contents to Kafka

## How to run the project
```
mvn clean install; java -jar target/jetty-server-0.0.1-jar-with-dependencies.jar com.jetty.server.JettySinkServer
```
Running the server on @port:`9090`

```
 ab -t 15 -k -T "application/json" -p file.tmp http://localhost:9080/
```

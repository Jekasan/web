package ru.fisunov.http.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Handler implements Runnable {
    Socket socket;
    Map<String, MyWebApplication> router;
    Logger logger;
    String threadName = Thread.currentThread().getName();
    int threadCount = Thread.activeCount();

    public Handler(Socket socket, Map<String, MyWebApplication> router, Logger logger) {
        this.socket = socket;
        this.router = router;
        this.logger = logger;
    }

    @Override
    public void run() {

        try {
            logger.info(threadName + threadCount + ": Клиент подключился");
            byte[] buffer = new byte[4096];
            int n = socket.getInputStream().read(buffer);
            String rawRequest = new String(buffer, 0, n);
            Request request = new Request(rawRequest);
            logger.info(threadName + threadCount + ": Получен запрос:");
            logger.info(threadName + threadCount + ": " + request.show());
            boolean executed = false;
            for (Map.Entry<String, MyWebApplication> e : router.entrySet()) {
                if (request.getUri().startsWith(e.getKey())) {
                    e.getValue().execute(request, socket.getOutputStream());
                    executed = true;

                    break;
                }
            }
            if (!executed) {
                socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Unknown application</h1></body></html>").getBytes(StandardCharsets.UTF_8));
            }
            socket.close();

        } catch (IOException e) {
            logger.error(threadName + ": " + e.getMessage());
        }
    }
}

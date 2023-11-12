package ru.fisunov.http.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    private int port;
    private static final Logger logger = LogManager.getLogger(MainApplication.class);
    public static ExecutorService serv;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запустился на порту: " + port);
            Map<String, MyWebApplication> router = new HashMap<>();
            router.put("/calculator", new CalculatorWebApplication());
            router.put("/greetings", new GreetingsWebApplication());

            serv = Executors.newFixedThreadPool(6);

            while (true) {
                serv.execute(() -> {
                    try (Socket socket = serverSocket.accept()) {
                        logger.info("Клиент подключился");
                        byte[] buffer = new byte[4096];
                        int n = socket.getInputStream().read(buffer);
                        String rawRequest = new String(buffer, 0, n);
                        Request request = new Request(rawRequest);
                        logger.info("Получен запрос:");
                        request.show();
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
                    } catch (IOException e) {
                        logger.error(e.getStackTrace());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

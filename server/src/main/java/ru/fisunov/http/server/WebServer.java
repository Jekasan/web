package ru.fisunov.http.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
                try  {
                    Socket socket = serverSocket.accept();
                    Handler handler = new Handler(socket, router, logger);
                    serv.execute(handler::run);

                } catch (IOException e) {
                    logger.error("Main: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
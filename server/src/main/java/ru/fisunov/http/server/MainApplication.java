package ru.fisunov.http.server;

public class MainApplication {
    public static final int PORT = 8189;

    public static void main(String[] args) {
        WebServer webServer = new WebServer(PORT);
        webServer.start();
    }
}

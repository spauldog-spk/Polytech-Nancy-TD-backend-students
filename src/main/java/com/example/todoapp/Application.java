package com.example.todoapp;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.todoapp.presentation.Controller;
import com.example.todoapp.presentation.dto.ValidationErrorDto;
import com.example.todoapp.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;

/**
 * classe principale de l'application, qui va démarrer le serveur HTTP et gérer le routage des requêtes HTTP vers la classe Controller.
 */

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // création d'un serveur HTTP qui écoute sur le port 8080, et qui va utiliser la classe Controller pour gérer les requêtes HTTP reçues sur le path /tasks
        server.createContext("/tasks", exchange -> {
            try {
                Controller.handleTasks(exchange);
            } catch (Exception e) {
                log.error("Failed to handle request", e);
                try {
                    String body = JsonUtils.serialize(new ValidationErrorDto("internal", "Internal server error"));
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                    byte[] bytes = body.getBytes(UTF_8);
                    exchange.sendResponseHeaders(500, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (IOException io) {
                    log.error("Failed to send error response", io);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }
}

package com.example.todoapp;


import java.net.InetSocketAddress;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.todoapp.presentation.Controller;
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
            } catch (SQLException e) {
                log.error("Failed to handle request", e);
                }
        });
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }
}

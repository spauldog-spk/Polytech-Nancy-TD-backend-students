package com.example.todoapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Collection;
import static java.util.Objects.nonNull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final Pattern COUNT_PATH = Pattern.compile("^/tasks/count$");
    private static final TaskDao dao = new TaskDao();

    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", Application::handleTasks);
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        addCorsHeaders(exchange);
        if ("OPTIONS".equals(method)) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        //region Manage POST /tasks
        if ("POST".equals(method) && "/tasks".equals(path)) {
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            Task createdTask = dao.save(input);
            log.debug("Task created with id: {}", createdTask.id());

            exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
            sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            return;
        }
        //endregion

        //region Manage GET /tasks (list all tasks)
        if ("GET".equals(method) && "/tasks".equals(path)) {
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            boolean todoOnly = false;
            
            if (nonNull(query)) {
                todoOnly = query.contains("todo-only=true");
            }
            
            Collection<Task> tasks = dao.findAll(todoOnly);
            if (tasks.isEmpty()) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
            }
            return;
        }
        //endregion

        //region Manage GET /tasks/count (bonus)
        if ("GET".equals(method) && COUNT_PATH.matcher(path).matches()) {
            int count = dao.count();
            sendResponse(exchange, 200, JsonUtils.serialize(count));
            return;
        }
        //endregion

        //region Manage GET /tasks/{id}
        Matcher m = ID_PATH.matcher(path);
        if ("GET".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<Task> task = dao.findById(id);

            if (task.isPresent()) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage PUT /tasks/{id}
        if ("PUT".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            Task updatedTask = new Task(id, input.title(), input.description(), input.done());
            
            if (dao.update(updatedTask)) {
                log.debug("Task updated with id: {}", id);
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage DELETE /tasks/{id}
        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            
            if (dao.deleteById(id)) {
                log.debug("Task deleted with id: {}", id);
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage DELETE /tasks (bonus - delete all)
        if ("DELETE".equals(method) && "/tasks".equals(path)) {
            dao.deleteAll();
            log.debug("All tasks deleted");
            sendResponse(exchange, 204, null);
            return;
        }
        //endregion

        // Sinon → 404
        sendResponse(exchange, 404, null);
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }
}

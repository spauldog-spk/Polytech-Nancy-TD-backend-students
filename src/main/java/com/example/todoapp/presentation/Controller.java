package com.example.todoapp.presentation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Collection;
import static java.util.Objects.nonNull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.todoapp.business.task.Task;
import com.example.todoapp.taskDAO.TaskDAO;
import com.example.todoapp.util.JsonUtils;
import com.sun.net.httpserver.HttpExchange;

/**
 * couche de présentation, gérant la partie HTTP et le routage. C'est la classe qui va recevoir les requêtes HTTP, faire le lien avec la couche service (via l'objet TaskDAO) et renvoyer les réponses HTTP.
 */
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class); // objet de type Logger qui va permettre de faire des logs dans la console, utile pour le debug et pour suivre l'exécution de l'application
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$"); // pattern qui va permettre de matcher les requêtes qui ont pour path /tasks/{id} où {id} est un nombre, et de récupérer cet id pour pouvoir faire le lien avec la couche service
    private static final Pattern COUNT_PATH = Pattern.compile("^/tasks/count$"); // pattern qui va permettre de matcher les requêtes qui ont pour path /tasks/count, utilisé pour le bonus qui permet de compter le nombre de taches
    private static final TaskDAO dao = new TaskDAO(); // objet de type TaskDAO qui va permettre de faire le lien entre la couche présentation et la couche service, c'est grâce à cet objet que la classe Controller va pouvoir appeler les méthodes de la classe TaskService pour faire le lien avec la couche DAO et accéder aux données des taches

    
    public static void handleTasks(HttpExchange exchange) throws IOException { // méthode qui va gérer les requêtes HTTP qui ont pour path /tasks, 
        String method = exchange.getRequestMethod();       // c'est la méthode qui va être appelée par le serveur HTTP lorsqu'une requête est reçue sur ce path,
        String path = exchange.getRequestURI().getPath(); //  elle va analyser la requête (méthode HTTP, path, query parameters), faire le lien avec la couche
                                                         //  service via l'objet TaskDAO, et renvoyer la réponse HTTP appropriée
        addCorsHeaders(exchange);
        if ("OPTIONS".equals(method)) { 
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // gestion des requêtes CORS préflight, en répondant aux requêtes OPTIONS avec les méthodes autorisées et les headers autorisés
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); 
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        //region Manage POST /tasks
        // partie qui va permettre de créer une nouvelle tache.
        if ("POST".equals(method) && "/tasks".equals(path)) {
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            Task createdTask = dao.save(input);
            log.debug("Task created with id: {}", createdTask.id());

            exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
            sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            return;
        }
        //endregion

        //region Manage GET /tasks
        // partie qui va permettre de lister toutes les taches, éventuellement filtrées par leur statut d'accomplissement (todo-only=true)
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

        //region Manage GET /tasks/count 
        // partie qui va permettre de compter le nombre de taches
        if ("GET".equals(method) && COUNT_PATH.matcher(path).matches()) {
            int count = dao.count();
            sendResponse(exchange, 200, JsonUtils.serialize(count));
            return;
        }
        //endregion

        //region Manage GET /tasks/{id}
        // partie qui va permettre de récupérer une tache par son id
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
        // partie qui va permettre de mettre à jour une tache par son id
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
        // partie qui va permettre de supprimer une tache par son id
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
        // partie qui va permettre de supprimer toutes les taches
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

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException { // méthode qui va permettre d'envoyer une réponse HTTP
        if(nonNull(json)) { // si le corps de la réponse n'est pas null, 
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); // on set le header Content-Type à application/json pour indiquer que le corps de la réponse est au format JSON
            byte[] bytes = json.getBytes(UTF_8); // on convertit le corps de la réponse en bytes pour pouvoir l'envoyer dans la réponse HTTP
            exchange.sendResponseHeaders(status, bytes.length); // on envoie les headers de la réponse HTTP avec le status et la longueur du corps de la réponse
            try (OutputStream os = exchange.getResponseBody()) { // on ouvre un OutputStream pour écrire le corps de la réponse HTTP
                os.write(bytes); // on écrit le corps de la réponse HTTP dans l'OutputStream
            }
        } else { // si le corps de la réponse est null, on envoie les headers de la réponse HTTP avec le status et une longueur de 0, puis on ferme la connexion
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) { // méthode qui va permettre d'ajouter les headers à la réponse HTTP pour permettre les requêtes depuis le frontend
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }
}

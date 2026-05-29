package com.example.todoapp.taskDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.todoapp.business.task.Task;


public class TaskDAO {
    private static final String DB_URL = "jdbc:sqlite:database.db"; 
    private static final Logger log = LoggerFactory.getLogger(TaskDAO.class);

    { 
        try {
            createTableIfNotExists();
            if (count() == 0) {
                save(new Task(1, "Task 1", "Description 1", false));
                save(new Task(2, "Task 2", "Description 2", true));
                save(new Task(3, "Task 3", "Description 3", false));
            }
        } catch (SQLException e) {
            log.error("Error initializing TaskDAO", e);
    
        }
    }
    private void createTableIfNotExists() throws SQLException {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
                String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                            title TEXT NOT NULL,
                            description TEXT,
                            done BOOLEAN NOT NULL
                            )
                """;
                stmt.execute(sql);
            }
    }




    /** // commentaire de la méthode save qui explique que cette méthode permet de sauvegarder une tache en lui attribuant un id si elle n'en a pas déjà un, et en la stockant dans la map de stockage
     * Persist {@link Task} model.
     * @param task tache à sauvegarder
     * @return tache sauvegardée avec un id attribué
     */

    public Task save(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?) RETURNING id, title, description, done;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setBoolean(3, task.done());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done"));
            } else {
                throw new SQLException("Failed to insert task, no ID obtained.");
            }
        }
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) throws SQLException {
            String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?;";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return Optional.of(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done")));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                log.error("Error finding task by id: {}", id, e);
                return Optional.empty();
            }
    }

    /**
     * Retrieve all {@link Task} models.
     * @return Collection of all tasks.
     */
    public Collection<Task> findAll() throws SQLException {
        String sql = "SELECT id, title, description, done FROM tasks;";
        Collection<Task> tasks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done")));
            }
        } catch (SQLException e) {
            log.error("Error finding all tasks", e);
        }
        return tasks;
    }

    /**
     * Retrieve all {@link Task} models, optionally filtered by done status.
     * @param todoOnly if true, only return tasks where done is false.
     * @return Collection of filtered tasks.
     */
    public Collection<Task> findAll(boolean todoOnly) throws SQLException {
        String sql = "SELECT id, title, description, done FROM tasks";
        if (todoOnly) {
            sql += " WHERE done = false";
        }
        Collection<Task> tasks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done")));
            }
        } catch (SQLException e) {
            log.error("Error finding tasks", e);
        }
        return tasks;
    }

    /**
     * Update an existing {@link Task} model.
     * @param task task to update.
     * @return true if updated, false if task not found.
     */
    public boolean update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setBoolean(3, task.done());
            ps.setInt(4, task.id());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error updating task with id: {}", task.id(), e);
            return false;
        }
    }

    /**
     * Delete {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return true if deleted, false if task not found.
     */
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error deleting task with id: {}", id, e);

            return false;
        }
    }

    /**
     * Delete all tasks.
     */
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM tasks;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Error deleting all tasks", e);
            throw e;
        }
    }

    /**
     * Count total number of tasks.
     * @return number of tasks.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error counting tasks", e);
            throw e;
        }
        return 0;
    }
}

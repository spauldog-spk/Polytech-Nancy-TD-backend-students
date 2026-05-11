package com.example.todoapp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();
    private int nextId = 4;

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        Task toSave = task;
        if (task.id() == null || task.id() == 0) {
            toSave = new Task(nextId++, task.title(), task.description(), task.done());
        } else {
            if (task.id() >= nextId) {
                nextId = task.id() + 1;
            }
        }
        storage.put(toSave.id(), toSave);
        return toSave;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Retrieve all {@link Task} models.
     * @return Collection of all tasks.
     */
    public Collection<Task> findAll() {
        return storage.values();
    }

    /**
     * Retrieve all {@link Task} models, optionally filtered by done status.
     * @param todoOnly if true, only return tasks where done is false.
     * @return Collection of filtered tasks.
     */
    public Collection<Task> findAll(boolean todoOnly) {
        if (todoOnly) {
            return storage.values().stream()
                    .filter(task -> !task.done())
                    .collect(Collectors.toList());
        }
        return findAll();
    }

    /**
     * Update an existing {@link Task} model.
     * @param task task to update.
     * @return true if updated, false if task not found.
     */
    public boolean update(Task task) {
        if (storage.containsKey(task.id())) {
            storage.put(task.id(), task);
            return true;
        }
        return false;
    }

    /**
     * Delete {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return true if deleted, false if task not found.
     */
    public boolean deleteById(int id) {
        return storage.remove(id) != null;
    }

    /**
     * Delete all tasks.
     */
    public void deleteAll() {
        storage.clear();
    }

    /**
     * Count total number of tasks.
     * @return number of tasks.
     */
    public int count() {
        return storage.size();
    }
}

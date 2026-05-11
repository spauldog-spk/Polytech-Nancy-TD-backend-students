package com.example.todoapp;

/**
 * Task model.
 * @param id            task identifier (can be null for creation)
 * @param title         task title
 * @param description   task description
 * @param done          task accomplishment status (false by default)
 */
public record Task(Integer id, String title, String description, boolean done) {

    public Task(String title, String description, boolean done) {
        this(null, title, description, done);
    }

    public Task(String title, String description) {
        this(null, title, description, false);
    }
}

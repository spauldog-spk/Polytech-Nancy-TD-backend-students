package com.example.todoapp.presentation.dto;

/**
 * DTO pour la modiftion d'icaune tâche.
 */
public record TaskUpdateDto(String title, String description, Boolean done) {
}

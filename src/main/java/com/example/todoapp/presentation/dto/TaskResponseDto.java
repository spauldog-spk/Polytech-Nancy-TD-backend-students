package com.example.todoapp.presentation.dto;

/**
 * DTO utilisé pour renvoyer une tâche dans les réponses HTTP.
 */
public record TaskResponseDto(Integer id, String title, String description, boolean done) {
}

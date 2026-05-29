package com.example.todoapp.presentation.dto;

/**
 * DTO retourné lorsqu'une validation échoue.
 */
public record ValidationErrorDto(String field, String message) {
}

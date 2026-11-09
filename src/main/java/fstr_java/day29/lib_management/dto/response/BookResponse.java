package fstr_java.day29.lib_management.dto.response;

public record BookResponse(
        Long id,
        String title,
        String author,
        Integer totalCopies,
        Integer availableCopies
) {
}
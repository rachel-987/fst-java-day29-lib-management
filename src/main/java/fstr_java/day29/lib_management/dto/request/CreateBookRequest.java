package fstr_java.day29.lib_management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
    record: là kiểu dữ liệu giúp định nghĩa các object
    - chủ yếu dùng để mang dữ liệu,
    - tự sinh constructor, accessor, equals(), hashCode() và toString().
 */
public record CreateBookRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotNull(message = "Total copies is required")
        @Min(value = 1, message = "Total copies must be >= 1")
        Integer totalCopies
) {
}
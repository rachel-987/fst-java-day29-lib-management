package fstr_java.day29.lib_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateMemberRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")    //@NotBlank: kiểm tra giá trị không được để trống
        @Email(message = "Invalid email format")    //@Email: kiểm tra định dạng email
        String email
) {
}
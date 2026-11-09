package fstr_java.day29.lib_management.dto.request;

import fstr_java.day29.lib_management.common.FinePolicyType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BorrowBookRequest(
        @NotNull Long memberId,
        @NotNull Long bookId,
        @NotNull @Future LocalDate dueDate,
        @NotNull FinePolicyType finePolicyType
) {
}
package fstr_java.day29.lib_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
    Dùng DTO để tránh trả trực tiếp Loan Entity, đặc biệt khi Loan có các quan hệ @ManyToOne(fetch = LAZY).
 */
public record LoanResponse(
        Long id,
        Long memberId,
        Long bookId,
        LocalDate borrowedAt,
        LocalDate dueDate,
        LocalDate returnedAt,
        String status,
        BigDecimal fineAmount
) {
}
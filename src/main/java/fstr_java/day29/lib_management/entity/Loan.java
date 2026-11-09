package fstr_java.day29.lib_management.entity;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.common.LoanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
        - Một Loan liên kết với một Book.
        - Nhiều Loan có thể liên kết với cùng một Book.
        - JoinColumn: loans có cột book_id, tham chiếu tới PK của bảng books.
        - FetchType.LAZY: Hibernate có thể chưa tải ngay dữ liệu của Book và Member.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // LocalDate: A date without a time-zone in the ISO-8601 calendar system, such as 2007-12-03.
    @Column(name = "borrowed_at", nullable = false)
    private LocalDate borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "fine_policy_type", nullable = false)
    private FinePolicyType finePolicyType;

    @Column(name = "fine_amount", precision = 12, scale = 2)
    private BigDecimal fineAmount;
}
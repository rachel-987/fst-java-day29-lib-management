package fstr_java.day29.lib_management.service.impl;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.common.LoanStatus;
import fstr_java.day29.lib_management.dto.request.BorrowBookRequest;
import fstr_java.day29.lib_management.dto.response.LoanResponse;
import fstr_java.day29.lib_management.entity.Book;
import fstr_java.day29.lib_management.entity.Loan;
import fstr_java.day29.lib_management.entity.Member;
import fstr_java.day29.lib_management.exception.BusinessException;
import fstr_java.day29.lib_management.policy.FinePolicy;
import fstr_java.day29.lib_management.policy.FinePolicyFactory;
import fstr_java.day29.lib_management.repository.BookRepository;
import fstr_java.day29.lib_management.repository.LoanRepository;
import fstr_java.day29.lib_management.repository.MemberRepository;
import fstr_java.day29.lib_management.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LoanServiceImpl implements LoanService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final FinePolicyFactory finePolicyFactory;

    public LoanServiceImpl(
            BookRepository bookRepository,
            MemberRepository memberRepository,
            LoanRepository loanRepository, FinePolicyFactory finePolicyFactory
    ) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.finePolicyFactory = finePolicyFactory;
    }

    @Override
    @Transactional
    public LoanResponse borrowBook(BorrowBookRequest request) {

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(
                        "Member not found",
                        HttpStatus.NOT_FOUND
                ));

        Book book = bookRepository.findByIdForUpdate(request.bookId())
                .orElseThrow(() -> new BusinessException(
                        "Book not found",
                        HttpStatus.NOT_FOUND
                ));

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException(
                    "Book is out of stock",
                    HttpStatus.CONFLICT
            );
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowedAt(LocalDate.now());
        loan.setDueDate(request.dueDate());
        loan.setStatus(LoanStatus.BORROWED);
        loan.setFinePolicyType(request.finePolicyType());
        loan.setFineAmount(BigDecimal.ZERO);

        Loan savedLoan = loanRepository.save(loan);

        return new LoanResponse(
                savedLoan.getId(),
                member.getId(),
                book.getId(),
                savedLoan.getBorrowedAt(),
                savedLoan.getDueDate(),
                savedLoan.getReturnedAt(),
                savedLoan.getStatus().name(),
                savedLoan.getFineAmount()
        );
    }

    @Override
    @Transactional
    public LoanResponse returnBook(Long loanId) {

        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new BusinessException(
                        "Loan not found",
                        HttpStatus.NOT_FOUND
                ));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessException(
                    "Book has already been returned",
                    HttpStatus.CONFLICT
            );
        }

        Book book = bookRepository
                .findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new BusinessException(
                        "Book not found",
                        HttpStatus.NOT_FOUND
                ));

        LocalDate returnedAt = LocalDate.now();

        long overdueDays = Math.max(
                0,
                ChronoUnit.DAYS.between(
                        loan.getDueDate(),
                        returnedAt
                )
        );

        FinePolicy policy = finePolicyFactory.getPolicy(
                loan.getFinePolicyType()
        );

        BigDecimal fine = policy.calculateFine(overdueDays);

        loan.setReturnedAt(returnedAt);
        loan.setFineAmount(fine);
        loan.setStatus(LoanStatus.RETURNED);

        book.setAvailableCopies(book.getAvailableCopies() + 1);

        return new LoanResponse(
                loan.getId(),
                loan.getMember().getId(),
                book.getId(),
                loan.getBorrowedAt(),
                loan.getDueDate(),
                loan.getReturnedAt(),
                loan.getStatus().name(),
                loan.getFineAmount()
        );
    }
}
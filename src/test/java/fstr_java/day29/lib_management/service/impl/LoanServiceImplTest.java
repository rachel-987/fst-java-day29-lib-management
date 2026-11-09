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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FinePolicyFactory finePolicyFactory;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(101L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);

        member = new Member();
        member.setId(201L);
        member.setName("Test Member");
        member.setEmail("test@example.com");
    }

    @Test
    void shouldBorrowBookSuccessfully() {
        // ARRANGE
        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                book.getId(),
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        when(memberRepository.findById(201L))
                .thenReturn(Optional.of(member));

        when(bookRepository.findByIdForUpdate(101L))
                .thenReturn(Optional.of(book));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> {
                    Loan savedLoan = invocation.getArgument(0);
                    savedLoan.setId(301L);
                    return savedLoan;
                });

        // ACT
        LoanResponse response = loanService.borrowBook(request);

        // ASSERT
        assertEquals(301L, response.id());
        assertEquals(201L, response.memberId());
        assertEquals(101L, response.bookId());
        assertEquals("BORROWED", response.status());
        assertEquals(4, book.getAvailableCopies());

        verify(loanRepository, times(1))
                .save(any(Loan.class));
    }


    @Test
    void shouldThrowExceptionWhenBookOutOfStock() {
        // ARRANGE
        book.setAvailableCopies(0);

        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                book.getId(),
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        when(bookRepository.findByIdForUpdate(book.getId()))
                .thenReturn(Optional.of(book));

        // ACT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.borrowBook(request)
        );

        // ASSERT
        assertEquals("Book is out of stock", exception.getMessage());
        assertEquals(409, exception.getStatus().value());
        assertEquals(0, book.getAvailableCopies());

        verify(loanRepository, never()).save(any(Loan.class));
    }


    @Test
    void shouldThrowExceptionWhenMemberNotFound() {
        // ARRANGE
        Long unknownMemberId = 999L;

        BorrowBookRequest request = new BorrowBookRequest(
                unknownMemberId,
                book.getId(),
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        when(memberRepository.findById(unknownMemberId))
                .thenReturn(Optional.empty());

        // ACT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.borrowBook(request)
        );

        // ASSERT
        assertEquals("Member not found", exception.getMessage());
        assertEquals(404, exception.getStatus().value());

        verifyNoInteractions(bookRepository, loanRepository);
    }


    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // ARRANGE
        Long unknownBookId = 999L;

        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                unknownBookId,
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        when(bookRepository.findByIdForUpdate(unknownBookId))
                .thenReturn(Optional.empty());

        // ACT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.borrowBook(request)
        );

        // ASSERT
        assertEquals("Book not found", exception.getMessage());
        assertEquals(404, exception.getStatus().value());

        verify(loanRepository, never()).save(any(Loan.class));
    }

    private Loan createBorrowedLoan() {
        Loan loan = new Loan();
        loan.setId(301L);
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowedAt(LocalDate.now().minusDays(5));
        loan.setDueDate(LocalDate.now().plusDays(2));
        loan.setStatus(LoanStatus.BORROWED);
        loan.setFinePolicyType(FinePolicyType.DAILY);
        loan.setFineAmount(BigDecimal.ZERO);

        return loan;
    }


    @Test
    void shouldReturnBookSuccessfully() {
        // ARRANGE
        Loan loan = createBorrowedLoan();
        book.setAvailableCopies(4);

        FinePolicy policy = mock(FinePolicy.class);

        when(loanRepository.findByIdForUpdate(301L))
                .thenReturn(Optional.of(loan));

        when(bookRepository.findByIdForUpdate(101L))
                .thenReturn(Optional.of(book));

        when(finePolicyFactory.getPolicy(FinePolicyType.DAILY))
                .thenReturn(policy);

        when(policy.calculateFine(0))
                .thenReturn(BigDecimal.ZERO);

        // ACT
        LoanResponse response = loanService.returnBook(301L);

        // ASSERT
        assertEquals("RETURNED", response.status());
        assertEquals(LocalDate.now(), response.returnedAt());
        assertEquals(BigDecimal.ZERO, response.fineAmount());
        assertEquals(5, book.getAvailableCopies());

        verify(policy).calculateFine(0);
    }


    @Test
    void shouldCalculateFineWhenReturningLate() {
        // ARRANGE
        Loan loan = createBorrowedLoan();
        loan.setDueDate(LocalDate.now().minusDays(3));

        book.setAvailableCopies(4);

        FinePolicy policy = mock(FinePolicy.class);

        when(loanRepository.findByIdForUpdate(301L))
                .thenReturn(Optional.of(loan));

        when(bookRepository.findByIdForUpdate(101L))
                .thenReturn(Optional.of(book));

        when(finePolicyFactory.getPolicy(FinePolicyType.DAILY))
                .thenReturn(policy);

        when(policy.calculateFine(3))
                .thenReturn(BigDecimal.valueOf(15000));

        // ACT
        LoanResponse response = loanService.returnBook(301L);

        // ASSERT
        assertEquals("RETURNED", response.status());
        assertEquals(
                BigDecimal.valueOf(15000),
                response.fineAmount()
        );
        assertEquals(5, book.getAvailableCopies());

        verify(policy).calculateFine(3);
    }


    @Test
    void shouldRejectAlreadyReturnedLoan() {
        // ARRANGE
        Loan loan = createBorrowedLoan();
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(LocalDate.now());

        book.setAvailableCopies(5);

        when(loanRepository.findByIdForUpdate(301L))
                .thenReturn(Optional.of(loan));

        // ACT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.returnBook(301L)
        );

        // ASSERT
        assertEquals(
                "Book has already been returned",
                exception.getMessage()
        );

        assertEquals(409, exception.getStatus().value());
        assertEquals(5, book.getAvailableCopies());

        verifyNoInteractions(bookRepository, finePolicyFactory);
    }

}

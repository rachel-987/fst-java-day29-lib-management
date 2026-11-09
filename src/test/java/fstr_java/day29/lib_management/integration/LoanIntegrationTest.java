package fstr_java.day29.lib_management.integration;

import fstr_java.day29.lib_management.common.FinePolicyType;
import fstr_java.day29.lib_management.dto.request.BorrowBookRequest;
import fstr_java.day29.lib_management.dto.response.LoanResponse;
import fstr_java.day29.lib_management.entity.Book;
import fstr_java.day29.lib_management.entity.Loan;
import fstr_java.day29.lib_management.entity.Member;
import fstr_java.day29.lib_management.exception.BusinessException;
import fstr_java.day29.lib_management.repository.BookRepository;
import fstr_java.day29.lib_management.repository.LoanRepository;
import fstr_java.day29.lib_management.repository.MemberRepository;
import fstr_java.day29.lib_management.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class LoanIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("library_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldBorrowBookAndPersistLoan() {

        // ARRANGE: Tạo Book mới
        Book book = new Book();
        book.setTitle("Integration Test Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);

        book = bookRepository.saveAndFlush(book);

        // ARRANGE: Tạo Member mới
        Member member = new Member();
        member.setName("Integration Test Member");
        member.setEmail(
                "member-" + java.util.UUID.randomUUID() + "@test.com"
        );

        member = memberRepository.saveAndFlush(member);

        // ARRANGE: Tạo request
        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                book.getId(),
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        // ACT: Gọi Service thật
        LoanResponse response = loanService.borrowBook(request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(response.id());
        assertEquals("BORROWED", response.status());

        // Kiểm tra dữ liệu đã được JPA ghi vào DB test
        Loan savedLoan = loanRepository.findById(response.id())
                .orElseThrow();

        assertEquals(member.getId(), savedLoan.getMember().getId());
        assertEquals(book.getId(), savedLoan.getBook().getId());

        Book updatedBook = bookRepository.findById(book.getId())
                .orElseThrow();

        assertEquals(4, updatedBook.getAvailableCopies());
    }

    @Test
    void shouldRejectBorrowWhenBookIsOutOfStock() {

        // ARRANGE: Tạo Book mới, không còn bản nào
        Book book = new Book();
        book.setTitle("Out of Stock Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(0);
        book = bookRepository.saveAndFlush(book);

        // ARRANGE: Tạo Member mới
        Member member = new Member();
        member.setName("Out of Stock Member");
        member.setEmail(
                "member-" + UUID.randomUUID() + "@test.com"
        );
        member = memberRepository.saveAndFlush(member);

        Long bookId = book.getId();
        Long memberId = member.getId();

        BorrowBookRequest request = new BorrowBookRequest(
                memberId,
                bookId,
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        // ACT: Thử mượn sách đã hết
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.borrowBook(request)
        );

        // ASSERT: Kiểm tra exception
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        // ASSERT: Số lượng sách vẫn bằng 0
        Book unchangedBook = bookRepository.findById(bookId)
                .orElseThrow();

        assertEquals(0, unchangedBook.getAvailableCopies());

        // ASSERT: Không có Loan nào được tạo
        assertEquals(
                0,
                loanRepository.findAll().stream()
                        .filter(loan ->
                                loan.getBook().getId() == bookId
                                        && loan.getMember().getId() == memberId
                        )
                        .count()
        );
    }

    @Test
    void shouldReturnBookOnTimeSuccessfully() {

        // ARRANGE: Tạo Book mới cho riêng test này
        Book book = new Book();
        book.setTitle("Return On Time Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book = bookRepository.saveAndFlush(book);

        // ARRANGE: Tạo Member mới
        Member member = new Member();
        member.setName("Return Test Member");
        member.setEmail("return-" + UUID.randomUUID() + "@test.com");
        member = memberRepository.saveAndFlush(member);

        Long bookId = book.getId();

        // ACT 1: Mượn sách, hạn trả sau 7 ngày
        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                bookId,
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        LoanResponse borrowed = loanService.borrowBook(request);

        assertEquals("BORROWED", borrowed.status());
        assertEquals(4, book.getAvailableCopies());

        // ACT 2: Trả sách ngay hôm nay
        LoanResponse returned = loanService.returnBook(borrowed.id());

        // ASSERT: Kiểm tra kết quả trả sách
        assertEquals("RETURNED", returned.status());
        assertEquals(LocalDate.now(), returned.returnedAt());

        assertEquals(0, BigDecimal.ZERO.compareTo(returned.fineAmount()));

        // ASSERT: Số lượng sách tăng trở lại
        assertEquals(5, book.getAvailableCopies());

        // Flush các thay đổi xuống database
        loanRepository.flush();

        // ASSERT: Loan trong persistence context
        Loan savedLoan = loanRepository.findById(borrowed.id())
                .orElseThrow();

        assertEquals("RETURNED", savedLoan.getStatus().name());
        assertNotNull(savedLoan.getReturnedAt());
    }

    @Test
    void shouldCalculateDailyFineWhenReturningOverdueBook() {

        // ARRANGE: Tạo Book mới
        Book book = new Book();
        book.setTitle("Overdue Test Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book = bookRepository.saveAndFlush(book);

        // ARRANGE: Tạo Member mới
        Member member = new Member();
        member.setName("Overdue Test Member");
        member.setEmail(
                "overdue-" + UUID.randomUUID() + "@test.com"
        );
        member = memberRepository.saveAndFlush(member);

        Long bookId = book.getId();

        // Hạn trả là 3 ngày trước
        LocalDate dueDate = LocalDate.now().minusDays(3);

        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                bookId,
                dueDate,
                FinePolicyType.DAILY
        );

        // ACT 1: Mượn sách
        LoanResponse borrowed = loanService.borrowBook(request);

        assertEquals("BORROWED", borrowed.status());
        assertEquals(4, book.getAvailableCopies());

        // ACT 2: Trả sách quá hạn
        LoanResponse returned = loanService.returnBook(borrowed.id());

        // ASSERT: Kiểm tra trạng thái
        assertEquals("RETURNED", returned.status());

        // 3 ngày × 5000 = 15000
        BigDecimal expectedFine = new BigDecimal("15000");

        assertEquals(
                0,
                expectedFine.compareTo(returned.fineAmount())
        );

        // Sách được hoàn lại kho
        assertEquals(5, book.getAvailableCopies());

        // Flush SQL và clear Persistence Context
        entityManager.flush();  //- flush(): Đồng bộ các thay đổi đang chờ từ Hibernate Persistence Context xuống database bằng SQL, nhưng chưa commit.

        entityManager.clear();  //- clear(): Xóa các entity đang được quản lý khỏi Persistence Context (L1 Cache).

        // Kiểm tra Loan được lưu trong PostgreSQL
        Loan savedLoan = loanRepository.findById(borrowed.id())
                .orElseThrow();

        assertEquals("RETURNED", savedLoan.getStatus().name());
        assertEquals(
                0,
                expectedFine.compareTo(savedLoan.getFineAmount())
        );
        assertNotNull(savedLoan.getReturnedAt());

        // Kiểm tra Book từ database
        Book savedBook = bookRepository.findById(bookId)
                .orElseThrow();

        assertEquals(5, savedBook.getAvailableCopies());
    }

    @Test
    void shouldRejectReturningBookTwice() {
        // ARRANGE: Dữ liệu riêng cho test
        Book book = new Book();
        book.setTitle("Double Return Test");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book = bookRepository.saveAndFlush(book);

        Member member = new Member();
        member.setName("Double Return Member");
        member.setEmail("double-" + UUID.randomUUID() + "@test.com");
        member = memberRepository.saveAndFlush(member);

        Long bookId = book.getId();

        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                bookId,
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        // ACT 1: Mượn sách
        LoanResponse borrowed = loanService.borrowBook(request);

        // ACT 2: Trả lần đầu
        LoanResponse firstReturn =
                loanService.returnBook(borrowed.id());

        assertEquals("RETURNED", firstReturn.status());

        // ACT 3: Trả lần hai -> phải bị từ chối
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanService.returnBook(borrowed.id())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        // Kiểm tra tồn kho không bị tăng thêm
        assertEquals(5, book.getAvailableCopies());
    }
}
package fstr_java.day29.lib_management.integration;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Testcontainers
class LoanIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanService loanService;

    @Test
    void shouldBorrowBookAndUpdateDatabase() {
        // ARRANGE - tạo dữ liệu thật, mới cho test
        Book book = new Book();
        book.setTitle("Integration Test Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book = bookRepository.save(book);

        Member member = new Member();
        member.setName("Integration Test Member");
        member.setEmail(
                "test-" + UUID.randomUUID() + "@example.com"
        );
        member = memberRepository.save(member);

        BorrowBookRequest request = new BorrowBookRequest(
                member.getId(),
                book.getId(),
                LocalDate.now().plusDays(7),
                FinePolicyType.DAILY
        );

        // ACT
        LoanResponse response = loanService.borrowBook(request);

        // ASSERT
        assertNotNull(response.id());
        assertEquals("BORROWED", response.status());

        Book updatedBook = bookRepository.findById(book.getId())
                .orElseThrow();

        assertEquals(4, updatedBook.getAvailableCopies());
    }
}
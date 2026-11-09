package fstr_java.day29.lib_management.service.impl;

import fstr_java.day29.lib_management.dto.request.CreateBookRequest;
import fstr_java.day29.lib_management.dto.response.BookResponse;
import fstr_java.day29.lib_management.entity.Book;
import fstr_java.day29.lib_management.repository.BookRepository;
import fstr_java.day29.lib_management.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /*
        @Transactional khai báo transaction boundary.
        Nếu quá trình xử lý phát sinh lỗi phù hợp với rollback rule, Spring sẽ rollback transaction.
     */
    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        Book book = new Book();

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies());

        Book savedBook = bookRepository.save(book);

        return toResponse(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BookResponse toResponse(Book book) {

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getTotalCopies(),
                book.getAvailableCopies()
        );
    }
}
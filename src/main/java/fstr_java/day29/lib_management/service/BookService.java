package fstr_java.day29.lib_management.service;

import fstr_java.day29.lib_management.dto.request.CreateBookRequest;
import fstr_java.day29.lib_management.dto.response.BookResponse;

import java.util.List;

public interface BookService  {
    BookResponse createBook(CreateBookRequest request);

    List<BookResponse> getAllBooks();

}

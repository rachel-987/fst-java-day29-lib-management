package fstr_java.day29.lib_management.repository;

import fstr_java.day29.lib_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}

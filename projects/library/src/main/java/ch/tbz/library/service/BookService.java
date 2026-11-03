package ch.tbz.library.service;

import ch.tbz.library.client.IsbnInfo;
import ch.tbz.library.client.IsbnLookupClient;
import ch.tbz.library.entity.Book;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final IsbnLookupClient isbnLookupClient;

    public BookService(BookRepository bookRepository, IsbnLookupClient isbnLookupClient) {
        this.bookRepository = bookRepository;
        this.isbnLookupClient = isbnLookupClient;
    }

    public Book add(String isbn, String title, String author) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new ConflictException("Ein Buch mit der ISBN " + isbn + " existiert bereits");
        }

        if (title == null || title.isBlank()) {
            IsbnInfo info = isbnLookupClient.lookup(isbn)
                    .orElseThrow(() -> new NotFoundException("Die ISBN " + isbn + " ist unbekannt"));
            title = info.title();
            author = info.author();
        }

        return bookRepository.save(new Book(isbn, title, author));
    }

    public Book get(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Buch " + id + " nicht gefunden"));
    }

    public List<Book> getAll() {
        return bookRepository.findAll();
    }
}

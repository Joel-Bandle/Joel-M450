package ch.tbz.library.service;

import ch.tbz.library.client.IsbnInfo;
import ch.tbz.library.client.IsbnLookupClient;
import ch.tbz.library.entity.Book;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String ISBN = "9780132350884";

    @Mock
    private BookRepository bookRepository;
    @Mock
    private IsbnLookupClient isbnLookupClient;

    @InjectMocks
    private BookService service;

    @Test
    void addMitTitelRuftDenIsbnDienstNichtAuf() {
        when(bookRepository.save(any(Book.class))).thenAnswer(call -> call.getArgument(0));

        Book book = service.add(ISBN, "Clean Code", "Robert C. Martin");

        assertEquals("Clean Code", book.getTitle());
        verifyNoInteractions(isbnLookupClient);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addOhneTitelHoltTitelUndAutorVomIsbnDienst(String leererTitel) {
        when(isbnLookupClient.lookup(ISBN))
                .thenReturn(Optional.of(new IsbnInfo("Clean Code", "Robert C. Martin")));
        when(bookRepository.save(any(Book.class))).thenAnswer(call -> call.getArgument(0));

        Book book = service.add(ISBN, leererTitel, null);

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        verify(isbnLookupClient).lookup(ISBN);
    }

    @Test
    void addMitUnbekannterIsbnWirftNotFoundUndSpeichertNichts() {
        when(isbnLookupClient.lookup(ISBN)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.add(ISBN, null, null));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void addMitBereitsVorhandenerIsbnWirftConflict() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.add(ISBN, "Clean Code", "Robert C. Martin"));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getMitUnbekannterIdWirftNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(99L));
    }

    @Test
    void getAllGibtAlleBuecherDesRepositorysZurueck() {
        List<Book> books = List.of(new Book(ISBN, "Clean Code", "Robert C. Martin"));
        when(bookRepository.findAll()).thenReturn(books);

        assertEquals(books, service.getAll());
    }
}

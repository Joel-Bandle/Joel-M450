package ch.tbz.library.client;

import java.util.Optional;

public interface IsbnLookupClient {

    Optional<IsbnInfo> lookup(String isbn);
}

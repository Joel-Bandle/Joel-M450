package ch.tbz.library.client;

import ch.tbz.library.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class OpenLibraryIsbnClient implements IsbnLookupClient {

    private final RestClient restClient;

    public OpenLibraryIsbnClient(RestClient.Builder builder,
                                 @Value("${library.isbn.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<IsbnInfo> lookup(String isbn) {
        String key = "ISBN:" + isbn;
        JsonNode response;
        try {
            response = restClient.get()
                    .uri("/api/books?bibkeys={key}&format=json&jscmd=data", key)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException("ISBN-Dienst ist nicht erreichbar", e);
        }

        if (response == null || !response.has(key)) {
            return Optional.empty();
        }

        JsonNode book = response.get(key);
        String title = book.path("title").asText();
        String author = book.path("authors").path(0).path("name").asText("Unbekannt");
        return Optional.of(new IsbnInfo(title, author));
    }
}

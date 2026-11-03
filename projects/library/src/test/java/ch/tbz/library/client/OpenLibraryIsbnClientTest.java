package ch.tbz.library.client;

import ch.tbz.library.exception.ExternalServiceException;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenLibraryIsbnClientTest {

    private static final String ISBN = "9780132350884";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private OpenLibraryIsbnClient client;

    @BeforeEach
    void setUp() {
        client = new OpenLibraryIsbnClient(RestClient.builder(), wireMock.baseUrl());
    }

    @Test
    void liefertTitelUndErstenAutor() {
        stubResponse(okJson("""
                {"ISBN:9780132350884": {"title": "Clean Code",
                 "authors": [{"name": "Robert C. Martin"}, {"name": "Zweiter Autor"}]}}
                """));

        Optional<IsbnInfo> result = client.lookup(ISBN);

        assertEquals(new IsbnInfo("Clean Code", "Robert C. Martin"), result.orElseThrow());
    }

    @Test
    void ohneAutorenListeIstDerAutorUnbekannt() {
        stubResponse(okJson("""
                {"ISBN:9780132350884": {"title": "Clean Code"}}
                """));

        assertEquals("Unbekannt", client.lookup(ISBN).orElseThrow().author());
    }

    @Test
    void unbekannteIsbnGibtLeeresOptionalZurueck() {
        stubResponse(okJson("{}"));

        assertTrue(client.lookup(ISBN).isEmpty());
    }

    @Test
    void serverfehlerWirdAlsExternalServiceExceptionGemeldet() {
        stubResponse(aResponse().withStatus(500));

        assertThrows(ExternalServiceException.class, () -> client.lookup(ISBN));
    }

    private void stubResponse(ResponseDefinitionBuilder response) {
        wireMock.stubFor(get(urlPathEqualTo("/api/books"))
                .withQueryParam("bibkeys", equalTo("ISBN:" + ISBN))
                .willReturn(response));
    }
}

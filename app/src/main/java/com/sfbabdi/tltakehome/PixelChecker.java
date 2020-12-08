package com.sfbabdi.tltakehome;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Check validness of pixel url.
 */
@Slf4j
@AllArgsConstructor
@Component
public class PixelChecker {
    private final WebClient client;

    public Optional<HttpStatus> check(String urlStr) {
        log.trace("Checking pixel:{}", urlStr);

        try {
            ClientResponse response = client
                    .get()
                    .uri(urlStr)
                    .exchangeToMono(Mono::just)
                    .block();

            HttpStatus statusCode = response.statusCode();
            log.trace("Url:{}, StatusCode:{}", urlStr, statusCode);
            return Optional.of(statusCode);
        } catch (Exception e) {
            log.debug("Unable to process url:{}", urlStr, e);
            return Optional.empty();
        }
    }
}

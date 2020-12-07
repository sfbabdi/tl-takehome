package com.sfbabdi.tltakehome;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Component
public class PixelChecker {
    private final WebClient client;

    public Optional<Boolean> check(String urlStr) {
        log.trace("Checking pixel:{}", urlStr);

        try {
            ClientResponse response = client
                    .get()
                    .uri(urlStr)
                    .exchangeToMono(Mono::just)
                    .block();

            HttpStatus statusCode = response.statusCode();
            boolean result = !statusCode.isError();
            log.trace("Url:{}, StatusCode:{}, result:{}", urlStr, statusCode, result);
            return Optional.of(result);
        } catch (Exception e) {
            log.debug("Unable to process url:{}", urlStr, e);
            return Optional.empty();
        }
    }
}

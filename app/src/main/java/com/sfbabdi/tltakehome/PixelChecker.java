package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Check validness of pixel url.
 */
@Slf4j
@AllArgsConstructor
@Component
public class PixelChecker {
    private final WebClient client;
    @Getter
    private final PixelCheckMetrics metrics;

    public PixelCheckResult check(PixelCheckEntry entry) {
        log.trace("Checking pixel:{}", entry.getUrl());
        metrics.getTotalCount().increment();

        try {
            ClientResponse response = client
                    .get()
                    .uri(entry.getUrl())
                    .exchangeToMono(Mono::just)
                    .block();

            assert response != null;
            HttpStatus statusCode = response.statusCode();
            log.trace("TacticId: {}, Url:{} - StatusCode:{}", entry.getTacticId(), entry.getUrl(), statusCode);

            if (statusCode.isError()) {
                metrics.getFailCount().increment();
            } else {
                metrics.getPassCount().increment();
            }

            return new PixelCheckResult(
                    entry.getTacticId(),
                    entry.getUrl(),
                    statusCode,
                    PixelCheckResult.ResultStatus.VALID);
        } catch (Exception e) {
            log.debug("TacticId: {}, Url: {} - Error during processing", entry.getUrl(), e);
            metrics.getErrorCount().increment();
        }
        return new PixelCheckResult(
                entry.getTacticId(),
                entry.getUrl(),
                // in case of error the status code is meaningless, just pick one
                HttpStatus.I_AM_A_TEAPOT,
                PixelCheckResult.ResultStatus.ERROR);
    }
}

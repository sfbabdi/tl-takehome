package com.sfbabdi.tltakehome.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class PixelCheckMetrics {
    /**
     * Number of results with 2xx/3xx response
     */
    @Getter
    private final Counter passCount;
    /**
     * Number of results with 4xx/5xx response
     */
    @Getter
    private final Counter failCount;
    /**
     * Number of checks failed due to other errors
     */
    @Getter
    private final Counter errorCount;
    /**
     * Total number of checks performed
     */
    @Getter
    private final Counter totalCount;

    public PixelCheckMetrics(MeterRegistry meterRegistry) {
        this.passCount = meterRegistry.counter("checker.pass");
        this.failCount = meterRegistry.counter("checker.fail");
        this.errorCount = meterRegistry.counter("checker.error");
        this.totalCount = meterRegistry.counter("checker.total");
    }
}

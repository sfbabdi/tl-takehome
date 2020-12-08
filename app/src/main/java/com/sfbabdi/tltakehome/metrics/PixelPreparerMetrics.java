package com.sfbabdi.tltakehome.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class PixelPreparerMetrics {
    @Getter
    private final Counter noImpressionPixelCount;
    @Getter
    private final Counter invalidImpressionPixelCount;
    @Getter
    private final Counter validImpressionPixelCount;
    @Getter
    private final Counter totalProcessed;

    public PixelPreparerMetrics(MeterRegistry meterRegistry) {
        noImpressionPixelCount = meterRegistry.counter("preparer.no_impression_pixel");
        invalidImpressionPixelCount = meterRegistry.counter("preparer.invalid_impression_pixel");
        validImpressionPixelCount = meterRegistry.counter("preparer.valid_impression_pixel");
        totalProcessed = meterRegistry.counter("preparer.total_processed");
    }
}

package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.model.PixelCheckResult;
import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class ConsolePixelCheckReporter {

    private final List<PixelCheckResult> results;
    private final PixelPreparerMetrics pixelPreparerMetrics;
    private final PixelCheckMetrics pixelCheckMetrics;

    public void report() {
        log.info(reportPrepareMetrics());
        log.info(reportCheckMetrics());
        log.info(reportFailedDetail());
    }

    private String reportPrepareMetrics() {
        return "Input preparation metric:\n" +
                "Total tacticId found: " +
                (int) (pixelPreparerMetrics.getTotalProcessed().count()) +
                '\n' +
                "Number of tacticId with valid impression pixel URLs: " +
                (int) (pixelPreparerMetrics.getValidImpressionPixelCount().count()) +
                '\n' +
                "Number of tacticId with invalid impression pixel URLs: " +
                (int) (pixelPreparerMetrics.getInvalidImpressionPixelCount().count()) +
                '\n' +
                "Number of tacticId with missing impression pixel URLs: " +
                (int) (pixelPreparerMetrics.getNoImpressionPixelCount().count()) +
                '\n';
    }

    private String reportCheckMetrics() {
        return "Pixel URL check metrics:\n" +
                "Total URL checked: " +
                (int) (pixelCheckMetrics.getTotalCount().count()) +
                '\n' +
                "Number of 2xx/3xx responses: " +
                (int) (pixelCheckMetrics.getPassCount().count()) +
                '\n' +
                "Number of 4xx/5xx responses: " +
                (int) (pixelCheckMetrics.getFailCount().count()) +
                '\n' +
                "Number of error while getting response: " +
                (int) (pixelCheckMetrics.getErrorCount().count()) +
                '\n';
    }

    private String reportFailedDetail() {
        StringBuilder errorDetail = new StringBuilder();
        StringBuilder failedDetail = new StringBuilder();
        results.forEach(r -> {
            if (r.getResultStatus() == PixelCheckResult.ResultStatus.ERROR) {
                errorDetail.append("TacticId:");
                errorDetail.append(r.getTacticId());
                errorDetail.append(" URL:");
                errorDetail.append(r.getUrl());
                errorDetail.append('\n');
            } else if (r.getHttpCode().isError()) {
                failedDetail.append("TacticId:");
                failedDetail.append(r.getTacticId());
                failedDetail.append(" HttpCode:");
                failedDetail.append(r.getHttpCode());
                failedDetail.append(" URL:");
                failedDetail.append(r.getUrl());
                failedDetail.append('\n');
            }
        });
        return "Failed Detail:\n" + failedDetail.toString() + '\n' + "Error Detail:\n" + errorDetail.toString() + '\n';
    }
}

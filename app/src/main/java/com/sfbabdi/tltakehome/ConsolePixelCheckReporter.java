package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class ConsolePixelCheckReporter implements PixelCheckReporter {

    @Override
    public void reportPrepareMetrics(PixelPreparerMetrics pixelPreparerMetrics) {
        String s = "Input preparation metric:\n" +
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
        log.info(s);
    }

    @Override
    public void reportCheckMetrics(PixelCheckMetrics pixelCheckMetrics) {
        String s = "Pixel URL check metrics:\n" +
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
        log.info(s);
    }

    @Override
    public void reportFailedDetail(List<PixelCheckResult> results) {
        StringBuilder errorDetail = new StringBuilder();
        StringBuilder failedDetail = new StringBuilder();
        results.forEach(r -> {
            if (r.getResultStatus() == PixelCheckResult.ResultStatus.ERROR) {
                errorDetail.append("TacticId:");
                errorDetail.append(r.getTacticId());
                errorDetail.append(",URL:");
                errorDetail.append(r.getUrl());
                errorDetail.append('\n');
            } else if (r.getHttpCode().isError()) {
                failedDetail.append("TacticId:");
                failedDetail.append(r.getTacticId());
                failedDetail.append(",HttpCode:");
                failedDetail.append(r.getHttpCode());
                failedDetail.append(",URL:");
                failedDetail.append(r.getUrl());
                failedDetail.append('\n');
            }
        });
        log.info("Failed Detail:\n" + failedDetail.toString() + '\n' + "Error Detail:\n" + errorDetail.toString() + '\n');
    }
}

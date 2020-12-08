package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ConsolePixelCheckReporter implements PixelCheckReporter {

  @Override
  public void reportPrepareMetrics(PixelPreparerMetrics pixelPreparerMetrics) {
    String s = "Input preparation metric:\n"
        + "Total tacticId found: "
        + (int) (pixelPreparerMetrics.getTotalProcessed().count())
        + '\n'
        + "Number of tacticId with valid impression pixel URLs: "
        + (int) (pixelPreparerMetrics.getValidImpressionPixelCount().count())
        + '\n'
        + "Number of tacticId with invalid impression pixel URLs: "
        + (int) (pixelPreparerMetrics.getInvalidImpressionPixelCount().count())
        + '\n'
        + "Number of tacticId with missing impression pixel URLs: "
        + (int) (pixelPreparerMetrics.getNoImpressionPixelCount().count())
        + '\n';
    log.info(s);
  }

  @Override
  public void reportCheckMetrics(PixelCheckMetrics pixelCheckMetrics) {
    String s = "Pixel URL check metrics:\n"
        + "Total URL checked: "
        + (int) (pixelCheckMetrics.getTotalCount().count())
        + '\n'
        + "Number of 2xx/3xx responses: "
        + (int) (pixelCheckMetrics.getPassCount().count())
        + '\n'
        + "Number of 4xx/5xx responses: "
        + (int) (pixelCheckMetrics.getFailCount().count())
        + '\n'
        + "Number of error while getting response: "
        + (int) (pixelCheckMetrics.getErrorCount().count())
        + '\n';
    log.info(s);
  }

  @Override
  public void reportFailedDetail(List<PixelCheckResult> results) {
    log.info("Failed Pixel (4xx/5xx) Detail Report");
    results.forEach(r -> {
      if (r.getResultStatus() == PixelCheckResult.ResultStatus.VALID
          && r.getHttpCode().isError()) {
        log.info("TacticId:{},HttpCode:{},URL:{}", r.getTacticId(), r.getHttpCode(), r.getUrl());
      }
    });
  }

  @Override
  public void reportErrorDetail(List<PixelCheckResult> results) {
    log.info("Error Pixel Detail Report");
    results.forEach(r -> {
      if (r.getResultStatus() == PixelCheckResult.ResultStatus.ERROR) {
        log.info("TacticId:{},URL:{}", r.getTacticId(), r.getUrl());
      }
    });
  }
}

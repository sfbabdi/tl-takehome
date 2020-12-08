package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import java.util.List;

public interface PixelCheckReporter {

  void reportPrepareMetrics(PixelPreparerMetrics pixelPreparerMetrics);

  void reportCheckMetrics(PixelCheckMetrics pixelCheckMetrics);

  void reportFailedDetail(List<PixelCheckResult> results);

  void reportErrorDetail(List<PixelCheckResult> results);
}

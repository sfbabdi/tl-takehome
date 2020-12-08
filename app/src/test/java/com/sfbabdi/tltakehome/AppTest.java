package com.sfbabdi.tltakehome;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class AppTest {

  @Mock
  ConfigurableApplicationContext context;
  // Use annotation to avoid the problem of instantiate the generic type captor
  @Captor
  ArgumentCaptor<List<PixelCheckResult>> checkResultCaptor;
  @Mock
  private PixelPreparer preparer;
  @Mock
  private PixelChecker checker;
  @Mock
  private PixelCheckReporter reporter;
  private ThreadPoolExecutor executor;
  private CompletionService<PixelCheckResult> completionService;

  @Before
  public void setup() {
    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    completionService = new ExecutorCompletionService<>(executor);

  }

  @After
  public void tearDown() {
    completionService = null;
    executor = null;
  }

  @Test
  public void testRunSuccess() throws Exception {
    final String arg = "testfile";
    List<PixelCheckEntry> checkEntries = new ArrayList<>(3);
    checkEntries.add(new PixelCheckEntry("1", "u1"));
    checkEntries.add(new PixelCheckEntry("2", "u2"));
    checkEntries.add(new PixelCheckEntry("3", "u3"));

    when(preparer.processFile(arg)).thenReturn(checkEntries);

    List<PixelCheckResult> checkResults = new ArrayList<>(3);
    checkResults.add(
        new PixelCheckResult(
            "1",
            "u1",
            HttpStatus.OK,
            PixelCheckResult.ResultStatus.VALID));
    checkResults.add(
        new PixelCheckResult(
            "2",
            "u2",
            HttpStatus.INTERNAL_SERVER_ERROR,
            PixelCheckResult.ResultStatus.VALID));
    checkResults.add(
        new PixelCheckResult(
            "3",
            "u3",
            HttpStatus.I_AM_A_TEAPOT,
            PixelCheckResult.ResultStatus.ERROR));
    when(checker.check(eq(checkEntries.get(0)))).thenReturn(checkResults.get(0));
    when(checker.check(eq(checkEntries.get(1)))).thenReturn(checkResults.get(1));
    when(checker.check(eq(checkEntries.get(2)))).thenReturn(checkResults.get(2));

    MeterRegistry registry = new SimpleMeterRegistry();
    PixelPreparerMetrics pixelPreparerMetrics = new PixelPreparerMetrics(registry);
    PixelCheckMetrics pixelCheckMetrics = new PixelCheckMetrics(registry);
    when(checker.getMetrics()).thenReturn(pixelCheckMetrics);
    when(preparer.getMetrics()).thenReturn(pixelPreparerMetrics);

    App dut = new App(context, executor, completionService, preparer, checker, reporter);
    dut.run(arg);

    // goal of the test is to assert the reporter methods has been called with the correct arguments
    ArgumentCaptor<PixelCheckMetrics> checkMetricsCaptor = ArgumentCaptor.forClass(PixelCheckMetrics.class);
    verify(reporter, times(1)).reportCheckMetrics(checkMetricsCaptor.capture());
    assertSame(pixelCheckMetrics, checkMetricsCaptor.getValue());

    ArgumentCaptor<PixelPreparerMetrics> preparerMetricsCaptor =
        ArgumentCaptor.forClass(PixelPreparerMetrics.class);
    verify(reporter, times(1)).reportPrepareMetrics(preparerMetricsCaptor.capture());
    assertSame(pixelPreparerMetrics, preparerMetricsCaptor.getValue());

    verify(reporter, times(1)).reportFailedDetail(checkResultCaptor.capture());
    Object[] capturedCheckResultArr = checkResultCaptor.getValue().toArray();
    assertArrayEquals(checkResults.toArray(), capturedCheckResultArr);
  }

  @Test
  public void testRunNoArg() throws Exception {
    App dut = new App(context, executor, completionService, preparer, checker, reporter);
    dut.run();
    verify(context, times(1)).close();
  }

  @Test
  public void testRunFileNotFound() throws Exception {
    when(preparer.processFile(anyString())).thenThrow(new FileNotFoundException());
    App dut = new App(context, executor, completionService, preparer, checker, reporter);
    dut.run("dummyfile");
    verify(context, times(1)).close();
  }
}

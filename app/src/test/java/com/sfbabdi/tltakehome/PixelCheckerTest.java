package com.sfbabdi.tltakehome;

import static org.junit.Assert.assertEquals;

import com.sfbabdi.tltakehome.metrics.PixelCheckMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@RunWith(MockitoJUnitRunner.class)
public class PixelCheckerTest {

  private MockWebServer webServer;
  private WebClient client;
  private PixelCheckMetrics metrics;

  @Before
  public void setup() throws IOException {
    webServer = new MockWebServer();
    webServer.start();
    client = WebClient.create();
    MeterRegistry registry = new SimpleMeterRegistry();
    metrics = new PixelCheckMetrics(registry);
  }

  @After
  public void tearDown() throws IOException {
    webServer.shutdown();
    client = null;
    metrics = null;
  }

  @Test
  public void testCheckOk() throws InterruptedException {
    webServer.enqueue(new MockResponse().setResponseCode(200));
    HttpUrl url = webServer.url("/testCheck200");

    PixelChecker dut = new PixelChecker(client, metrics);
    PixelCheckEntry e = new PixelCheckEntry("123", url.toString());
    PixelCheckResult result = dut.check(e);

    assertEquals(e.getTacticId(), result.getTacticId());
    assertEquals(e.getUrl(), result.getUrl());
    assertEquals(PixelCheckResult.ResultStatus.VALID, result.getResultStatus());
    assertEquals(HttpStatus.OK, result.getHttpCode());

    assertEquals(1, (int) dut.getMetrics().getPassCount().count());
    assertEquals(0, (int) dut.getMetrics().getFailCount().count());
    assertEquals(0, (int) dut.getMetrics().getErrorCount().count());
    assertEquals(1, (int) dut.getMetrics().getTotalCount().count());

    RecordedRequest recordedRequest = webServer.takeRequest();
    assertEquals("GET", recordedRequest.getMethod());
    assertEquals("/testCheck200", recordedRequest.getPath());
  }

  @Test
  public void testCheckFail() {
    webServer.enqueue(new MockResponse().setResponseCode(500));
    HttpUrl url = webServer.url("/testCheck500");

    PixelChecker dut = new PixelChecker(client, metrics);
    PixelCheckEntry e = new PixelCheckEntry("123", url.toString());
    PixelCheckResult result = dut.check(e);

    assertEquals(e.getTacticId(), result.getTacticId());
    assertEquals(e.getUrl(), result.getUrl());
    assertEquals(PixelCheckResult.ResultStatus.VALID, result.getResultStatus());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpCode());

    assertEquals(0, (int) dut.getMetrics().getPassCount().count());
    assertEquals(1, (int) dut.getMetrics().getFailCount().count());
    assertEquals(0, (int) dut.getMetrics().getErrorCount().count());
    assertEquals(1, (int) dut.getMetrics().getTotalCount().count());
  }

  @Test
  public void testCheckError() {
    PixelChecker dut = new PixelChecker(client, metrics);
    PixelCheckEntry e = new PixelCheckEntry("123", "gibberish");
    PixelCheckResult result = dut.check(e);

    assertEquals(e.getTacticId(), result.getTacticId());
    assertEquals(e.getUrl(), result.getUrl());
    assertEquals(PixelCheckResult.ResultStatus.ERROR, result.getResultStatus());

    assertEquals(0, (int) dut.getMetrics().getPassCount().count());
    assertEquals(0, (int) dut.getMetrics().getFailCount().count());
    assertEquals(1, (int) dut.getMetrics().getErrorCount().count());
    assertEquals(1, (int) dut.getMetrics().getTotalCount().count());
  }

}

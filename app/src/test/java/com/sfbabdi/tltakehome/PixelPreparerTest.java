package com.sfbabdi.tltakehome;

import static org.junit.Assert.assertEquals;

import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PixelPreparerTest {

  // 2020-01-01 00:00:00 UTC
  private static final long MOCK_NOW = 1577836800;
  private static final int MOCK_RANDOM_INT = 1776;

  @Mock
  private Clock clock;
  @Mock
  private Random random;

  private PixelPreparerMetrics metrics;
  private UrlValidator validator;

  @Before
  public void setup() {
    Mockito.when(clock.millis()).thenReturn(MOCK_NOW);
    String[] schemes = {"http", "https"};
    Mockito.when(random.nextInt()).thenReturn(MOCK_RANDOM_INT);
    validator = new UrlValidator(schemes);
    MeterRegistry registry = new SimpleMeterRegistry();
    metrics = new PixelPreparerMetrics(registry);
  }

  @After
  public void tearDown() {
    metrics = null;
    validator = null;
  }

  @Test
  public void testSuccess() throws FileNotFoundException {
    final String input = "src/test/resources/tactic.csv";
    final int expectedTotalCount = 24907;
    final int expectedValidUrlCount = 17742;
    final int expectedInvalidUrlCount = 5;
    final int expectedEmptyUrlCount = 7160;
    PixelPreparer dut = new PixelPreparer(clock, random, validator, metrics);
    List<PixelCheckEntry> result = dut.processFile(input);
    assertEquals(expectedValidUrlCount, result.size());

    assertEquals(expectedTotalCount, (int) metrics.getTotalProcessed().count());
    assertEquals(expectedEmptyUrlCount, (int) metrics.getNoImpressionPixelCount().count());
    assertEquals(expectedInvalidUrlCount, (int) metrics.getInvalidImpressionPixelCount().count());
    assertEquals(expectedValidUrlCount, (int) metrics.getValidImpressionPixelCount().count());
  }

  @Test(expected = FileNotFoundException.class)
  public void testFileNotFound() throws FileNotFoundException {
    PixelPreparer dut = new PixelPreparer(clock, random, validator, metrics);
    dut.processFile("???");
  }

  @Test
  public void testSanitizeLotsOfQuotes() {
    final String urlRaw = "\"\"\"\"\"\"\"http://servedby.f\"lashtalking.com/?cachebuster=\"17760704\"\"\"";
    final String expected = "http://servedby.flashtalking.com/?cachebuster=17760704";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeLotsOfBackslashes() {
    final String urlRaw = "http:\\/\\/servedby.flashtalking.com/?\\\\\\\\cachebuster=17760704\\\\";
    final String expected = "http://servedby.flashtalking.com/?cachebuster=17760704";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeHttpLotsOfSlashes() {
    final String urlRaw = "http://////////servedby.flashtalking.com/?cachebuster=17760704";
    final String expected = "http://servedby.flashtalking.com/?cachebuster=17760704";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeHttpsLotsOfSlashes() {
    final String urlRaw = "https://////////servedby.flashtalking.com/?cachebuster=17760704";
    final String expected = "https://servedby.flashtalking.com/?cachebuster=17760704";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeTrailingHtmlTag() {
    final String urlRaw = "http://servedby.flashtalking.com/?cachebuster=17760704\"\"/> img/> why are you <running />";
    final String expected = "http://servedby.flashtalking.com/?cachebuster=17760704";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeSimpleVariableReplacement() {
    final String urlRaw = "http://api.choicestream.com/imp?au=${AUCTION_ID}&cr=$!{CREATIVE_ID}&li=[timestamp]&ts={ts}&foo=%%CACHEBREAKER%%";
    final String expected = "http://api.choicestream.com/imp?au=1776&cr=1776&li=1577836800&ts=1577836800&foo=1776";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeComplexVariableReplacement() {
    final String urlRaw = "http://api.choicestream.com/imp?a=%pmmsid=!&b=%epid!&c=%n&d=$$%%var1%%||%%var2%%$$&e=%s";
    final String expected = "http://api.choicestream.com/imp?a=1776&b=1776&c=1776&d=1776&e=1776";
    testSanitize(urlRaw, true, expected);
  }

  @Test
  public void testSanitizeInvalid() {
    final String urlRaw = "gibberish";
    testSanitize(urlRaw, false, "");
  }

  private void testSanitize(String urlRaw, boolean isResultValid, String expectedResult) {
    PixelPreparer preparer = new PixelPreparer(clock, random, validator, metrics);
    Optional<String> s = preparer.sanitizeUrl(urlRaw);
    assertEquals(isResultValid, s.isPresent());
    s.ifPresent(value -> assertEquals(expectedResult, value));
  }
}

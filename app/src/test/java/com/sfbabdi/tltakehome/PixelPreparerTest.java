package com.sfbabdi.tltakehome;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PixelPreparerTest {

    // 2020-01-01 00:00:00 UTC
    private static final long MOCK_NOW = 1577836800;
    @Mock
    private Clock clock;

    private UrlValidator validator;

    @Mock
    private Random random;

    @Before
    public void setup() {
        Mockito.when(clock.millis()).thenReturn(MOCK_NOW);
        String[] schemes = {"http", "https"};
        Mockito.when(random.nextInt()).thenReturn(1776);
        validator = new UrlValidator(schemes);
    }

    @Test
    public void testSuccess() throws FileNotFoundException {
        final String input = "src/test/resources/tactic.csv";
        final int expectedValidUrlCount = 17742;
        PixelPreparer dut = new PixelPreparer(clock, random, validator);
        List<String> result = dut.processFile(input);
        assertEquals(expectedValidUrlCount, result.size());
    }

    @Test(expected = FileNotFoundException.class)
    public void testFileNotFound() throws FileNotFoundException {
        PixelPreparer dut = new PixelPreparer(clock, random, validator);
        List<String> result = dut.processFile("???");
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
        PixelPreparer preparer = new PixelPreparer(clock, random, validator);
        Optional<String> s = preparer.sanitizeUrl(urlRaw);
        assertEquals(isResultValid, s.isPresent());
        s.ifPresent(value -> assertEquals(expectedResult, value));
    }
}

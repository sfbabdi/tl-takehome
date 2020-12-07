package com.sfbabdi.tltakehome;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PixelCheckerTest {

    private MockWebServer webServer;
    private WebClient client;

    @Before
    public void setup() throws IOException {
        webServer = new MockWebServer();
        webServer.start();
        client = WebClient.create();
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
        client = null;
    }

    @Test
    public void testCheckOk() throws InterruptedException {
        webServer.enqueue(new MockResponse().setResponseCode(200));
        HttpUrl url = webServer.url("/testCheck200");

        PixelChecker dut = new PixelChecker(client);
        Optional<Boolean> result = dut.check(url.toString());

        assertTrue(result.isPresent());
        assertTrue(result.get());

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/testCheck200", recordedRequest.getPath());
    }

    @Test
    public void testCheckError() {
        webServer.enqueue(new MockResponse().setResponseCode(500));
        HttpUrl url = webServer.url("/testCheck500");

        PixelChecker dut = new PixelChecker(client);
        Optional<Boolean> result = dut.check(url.toString());

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testCheckException() {
        PixelChecker dut = new PixelChecker(client);
        Optional<Boolean> result = dut.check("gibberish");

        assertFalse(result.isPresent());
    }

}

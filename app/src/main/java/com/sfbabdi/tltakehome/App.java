package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@SpringBootApplication
public class App implements CommandLineRunner {

    private final ThreadPoolExecutor executor;
    private final CompletionService<PixelCheckResult> completionService;
    private final PixelPreparer preparer;
    private final PixelChecker checker;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            log.error("No input filename found, exiting.");
            System.exit(1);
        }
        String inputFile = args[0];
        log.info("Processing input file: {}", inputFile);

        List<PixelCheckEntry> pixelCheckEntries = null;
        try {
            pixelCheckEntries = preparer.processFile(inputFile);
        } catch (IOException e) {
            log.error("Cannot process file: {}", inputFile, e);
            System.exit(1);
        }

        assert pixelCheckEntries != null;

        List<PixelCheckResult> pixelCheckResults = new ArrayList<>(pixelCheckEntries.size());
        pixelCheckEntries.forEach(e -> {
            completionService.submit(() -> checker.check(e));
        });

        int received = 0;
        while (received < pixelCheckEntries.size()) {
            Future<PixelCheckResult> resultFuture = completionService.take();
            pixelCheckResults.add(resultFuture.get());
            log.info("Executor poolSize:{}, queueSize:{}", executor.getPoolSize(), executor.getQueue().size());
            received++;
        }

        ConsolePixelCheckReporter reporter =
                new ConsolePixelCheckReporter(pixelCheckResults, preparer.getMetrics(), checker.getMetrics());
        reporter.report();
    }
}



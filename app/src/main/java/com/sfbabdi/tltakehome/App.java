package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import com.sfbabdi.tltakehome.model.PixelCheckResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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

    private final ConfigurableApplicationContext context;
    private final ThreadPoolExecutor executor;
    private final CompletionService<PixelCheckResult> completionService;
    private final PixelPreparer preparer;
    private final PixelChecker checker;
    private final PixelCheckReporter reporter;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            log.error("No input filename found in arg, done.");
            context.close();
            return;
        }
        String inputFile = args[0];
        log.info("Processing input file: {}", inputFile);

        List<PixelCheckEntry> pixelCheckEntries = null;
        try {
            pixelCheckEntries = preparer.processFile(inputFile);
        } catch (IOException e) {
            log.error("Cannot process file: {}", inputFile, e);
            context.close();
            return;
        }

        assert pixelCheckEntries != null;

        List<PixelCheckResult> pixelCheckResults = new ArrayList<>(pixelCheckEntries.size());
        pixelCheckEntries.forEach(e -> {
            completionService.submit(() -> checker.check(e));
        });

        int received = 0;
        int logInterval = 1000;
        while (received < pixelCheckEntries.size()) {
            Future<PixelCheckResult> resultFuture = completionService.take();
            pixelCheckResults.add(resultFuture.get());
            if (received % logInterval == 0) {
                log.info("Executor queueSize:{}", executor.getQueue().size());
            }
            received++;
        }

        reporter.reportFailedDetail(pixelCheckResults);
        reporter.reportPrepareMetrics(preparer.getMetrics());
        reporter.reportCheckMetrics(checker.getMetrics());

        context.close();
    }
}



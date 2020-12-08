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

@Slf4j
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@SpringBootApplication
public class App implements CommandLineRunner {

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

        // We could parallelize the checking with thread pool and so. But maybe just relax and get some coffee?
        List<PixelCheckResult> pixelCheckResults = new ArrayList<>(pixelCheckEntries.size());
        pixelCheckEntries.forEach(e -> pixelCheckResults.add(checker.check(e)));

        ConsolePixelCheckReporter reporter =
                new ConsolePixelCheckReporter(pixelCheckResults, preparer.getMetrics(), checker.getMetrics());
        reporter.report();
    }
}



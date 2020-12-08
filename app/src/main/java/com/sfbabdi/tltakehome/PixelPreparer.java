package com.sfbabdi.tltakehome;

import com.sfbabdi.tltakehome.metrics.PixelPreparerMetrics;
import com.sfbabdi.tltakehome.model.PixelCheckEntry;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

/**
 * Sanitize a list of pixel urls from input csv. Assuming the input csv is always structured the same, that is: - Tactic
 * id is on 2nd column - Url to check is on 9th column and is in format of JSON list consisting at most 1 url - If csv
 * header is present, it should match HEADER_SIGNATURE
 */
@Slf4j
@AllArgsConstructor
@Component
public class PixelPreparer {

  private static final String COMMA_DELIMITER = ",";
  private static final String HEADER_SIGNATURE = "id,tactic_id,creative_library_id";
  private static final int TACTIC_ID_FIELD = 1;
  private static final int IMPRESSION_PIXEL_FIELD = 8;
  private static final String IMPRESSION_PIXEL_NULL = "NULL";
  private static final String IMPRESSION_PIXEL_EMPTY = "[]";
  private final Clock clock;
  private final Random random;
  private final UrlValidator validator;
  @Getter
  private final PixelPreparerMetrics metrics;

  public List<PixelCheckEntry> processFile(String fileName) throws FileNotFoundException {
    List<List<String>> inputCsv = new ArrayList<>();
    Scanner scanner = new Scanner(new File(fileName), "UTF-8");
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      // skip header line
      if (line.startsWith(HEADER_SIGNATURE)) {
        continue;
      }
      inputCsv.add(processLine(line));
    }

    List<PixelCheckEntry> pixelCheckResultEntryList = new ArrayList<>();
    for (List<String> line : inputCsv) {
      metrics.getTotalProcessed().increment();

      String tacticId = line.get(TACTIC_ID_FIELD);
      String impressionPixelJson = line.get(IMPRESSION_PIXEL_FIELD);

      // lots of entries with NULL or []. Straight ignore.
      if (impressionPixelJson.equalsIgnoreCase(IMPRESSION_PIXEL_NULL)
          || impressionPixelJson.equals(IMPRESSION_PIXEL_EMPTY)) {
        metrics.getNoImpressionPixelCount().increment();
        continue;
      }

      log.trace("Sanitizing tacticId:{} url: {}", tacticId, impressionPixelJson);
      Optional<String> sanitizedUrl = sanitizeUrl(impressionPixelJson);
      if (sanitizedUrl.isPresent()) {
        pixelCheckResultEntryList.add(new PixelCheckEntry(tacticId, sanitizedUrl.get()));
        metrics.getValidImpressionPixelCount().increment();
      } else {
        log.debug("Tactic_Id: {} has invalid URL: {}", tacticId, impressionPixelJson);
        metrics.getInvalidImpressionPixelCount().increment();
      }
    }
    return pixelCheckResultEntryList;
  }

  private List<String> processLine(String line) {
    List<String> values = new ArrayList<>();
    try (Scanner rowScanner = new Scanner(line)) {
      rowScanner.useDelimiter(COMMA_DELIMITER);
      while (rowScanner.hasNext()) {
        values.add(rowScanner.next());
      }
    }
    return values;
  }

  protected Optional<String> sanitizeUrl(String urlRaw) {

    // Raw string is too far gone to fix into any parsable JSON, do this manually instead
    // Probably can optimize this into more efficient calls to replace but we lay it out for readability.
    String s = urlRaw
        // Remove all quotes
        .replaceAll("\"", "")
        // Remove all back slashes
        // 4 backslashes: \ to escape \ in regex, and 2 more \s to escape the 2 \s in java string
        .replaceAll("\\\\", "")

        // Do variable replacements
        // Replace all timestamp variables in form of [timestamp] and {ts} with current millis
        .replaceAll("\\[timestamp\\]|\\{ts\\}", String.valueOf(clock.millis()))
        // Note the non greedy match using '?'
        // Giving things like [RANDOM] or [CACHEBUSTER] a value
        // These also comes in form of %%CACHEBUSTER%%, ${CACHEBUSTER}, and $!{CACHEBREAKER}
        .replaceAll("\\[\\w+?\\]|\\$!?\\{\\w+?\\}|%%\\w+?%%", String.valueOf(random.nextInt()))
        // Also in form of '%xxx=!;' i.e. '%pmmsid=!;' or '%epid!'
        .replaceAll("%\\w+?[=!;]+", String.valueOf(random.nextInt()))
        // And %n
        .replaceAll("%\\w$", String.valueOf(random.nextInt()))
        .replaceAll("%\\w&", String.valueOf(random.nextInt()) + "&")
        // And $${$var1}||{var2}$$ should just be one value (and we don't care what the value is)
        .replaceAll("\\$\\$(.+?)\\|\\|.+?\\$\\$", "$1")
        // http[s]?://// should be http[s]?://
        .replaceFirst("(https?:)[\\/]+", "$1//")

        // Remove any []{}$
        .replaceAll("[\\[\\]\\{\\}\\$]", "")

        // Remove html tag and extra at the end
        .replaceFirst(" .*$", "")
        .replaceFirst("\\/\\>$", "");

    // Verify this is a valid URL
    if (!validator.isValid(s)) {
      log.debug("Sanitized URL failed validation:{}", s);
      return Optional.empty();
    }
    return Optional.of(s);
  }
}

package com.sfbabdi.tltakehome.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class PixelCheckResult {

  @Getter
  private final String tacticId;
  @Getter
  private final String url;
  @Getter
  private final HttpStatus httpCode;
  @Getter
  private final ResultStatus resultStatus;

  public enum ResultStatus {
    /**
     * Test has not been run.
     */
    NOT_RUN,
    /**
     * Test has ran and httpCode is valid.
     */
    VALID,
    /**
     * Test has ran but no result due to error.
     */
    ERROR,
  }
}

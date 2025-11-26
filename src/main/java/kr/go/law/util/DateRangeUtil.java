package kr.go.law.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Date Range 객체 관리 유틸리티 클래스 */
public class DateRangeUtil {
  private DateRangeUtil() {
  }

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  /** Represents a date range with from and to dates. */
  @Getter
  @AllArgsConstructor
  public static class DateRange {
    private final String fromDate;
    private final String toDate;

    @Override
    public String toString() {
      return fromDate + "~" + toDate;
    }
  }

  /**
   * Splits a date range into N-day chunks.
   *
   * @param fromDate  start date (YYYYMMDD)
   * @param toDate    end date (YYYYMMDD)
   * @param chunkDays number of days per chunk
   * @param reverse   if true, return chunks in reverse order (newest first)
   * @return list of date ranges, each spanning up to chunkDays days
   */
  public static List<DateRange> splitIntoChunks(String fromDate, String toDate, int chunkDays) {
    if (chunkDays <= 0) {
      throw new IllegalArgumentException("chunkDays must be positive");
    }

    LocalDate start = LocalDate.parse(fromDate, FORMATTER);
    LocalDate end = LocalDate.parse(toDate, FORMATTER);

    if (start.isAfter(end)) {
      throw new IllegalArgumentException("fromDate must be before or equal to toDate");
    }

    List<DateRange> ranges = new ArrayList<>();
    LocalDate current = start;

    while (!current.isAfter(end)) {
      LocalDate chunkEnd = current.plusDays(chunkDays - 1);
      if (chunkEnd.isAfter(end)) {
        chunkEnd = end;
      }

      ranges.add(new DateRange(
          current.format(FORMATTER),
          chunkEnd.format(FORMATTER)));

      current = chunkEnd.plusDays(1);
    }

    return ranges;
  }
}

import CookieAnalyzer.CookieAnalyzerError._

import java.time.LocalDate
import scala.io.Source
import scala.util.{Failure, Success, Using}

object CookieAnalyzer {

  final case class Cookie(value: String) extends AnyVal
  private final case class CookieLogEntry(cookie: Cookie, date: LocalDate)

  /** Final Ok Output */
  final case class MostActiveCookies(cookies: Set[Cookie])

  /** Error types that may occur during cookie analysis. */
  sealed trait CookieAnalyzerError { def message: String }

  object CookieAnalyzerError {
    final case class FileReadFailure(path: String, cause: String)
        extends CookieAnalyzerError {
      override def message: String = s"Failed to read file: $path - $cause"
    }

    final case class MalformedCsvRowError(line: Int)
        extends CookieAnalyzerError {
      override def message: String = s"Malformed CSV row at line $line"
    }

    final case class InvalidTimestampError(line: Int)
        extends CookieAnalyzerError {
      override def message: String =
        s"Invalid timestamp at line $line. Expected ISO_OFFSET_DATE_TIME (e.g., 2025-06-09T18:00:00+00:00)"
    }

    final case object NoCookiesOnDateError extends CookieAnalyzerError {
      override val message: String = "No cookies found for the specified date."
    }

    final case object EmptyCsvFileError extends CookieAnalyzerError {
      override val message: String =
        "The CSV file is empty. Provide at least one data row."
    }
  }

  /**
    * Analyzes the CSV file to find the most active cookie(s) for a given date.
    *
    * @param filePath   Path to the CSV file
    * @param targetDate Date to filter on
    * @return Either an error or the set of most active cookies
    */
  def findMostActiveCookiesForDate(
      filePath: String,
      targetDate: LocalDate
  ): Either[CookieAnalyzerError, MostActiveCookies] =
    for {
      lines <- loadCsvFile(filePath)
      entries <- parseCookieEntries(lines)
      filtered = entries.filter(_.date == targetDate)
      result <- mostFrequentCookies(filtered)
    } yield MostActiveCookies(result)

  /** Loads all lines from the file and checks for empty content. */
  private def loadCsvFile(
      filePath: String
  ): Either[CookieAnalyzerError, List[String]] =
    Using(Source.fromFile(filePath))(_.getLines().toList) match {
      case Failure(ex)    => Left(FileReadFailure(filePath, ex.getMessage))
      case Success(Nil)   => Left(EmptyCsvFileError)
      case Success(lines) => Right(lines)
    }

  /**
    * Parses CSV rows into domain-specific entries.
    * Skips header and returns parse errors with accurate line numbers.
    */
  private def parseCookieEntries(
      lines: List[String]
  ): Either[CookieAnalyzerError, List[CookieLogEntry]] = {
    val dataLines = lines.drop(1)
    val init: Either[CookieAnalyzerError, List[CookieLogEntry]] = Right(Nil)
    dataLines.zipWithIndex.foldLeft(init) { (acc, lineTuple) =>
      for {
        prevResult <- acc
        logEntry <- parseCookieEntry(lineTuple._1, lineTuple._2 + 2)
      } yield prevResult :+ logEntry

    }
  }

  /**
    * Parses a single CSV line into a CookieLogEntry.
    * @param line The raw line
    * @param lineNumber 1-based CSV line number (used for errors)
    */
  private def parseCookieEntry(
      line: String,
      lineNumber: Int
  ): Either[CookieAnalyzerError, CookieLogEntry] = {
    line.split(",").map(_.trim) match {
      case Array(cookieStr, timestampStr) =>
        DateParser.parseIsoOffsetDate(timestampStr) match {
          case Some(date) => Right(CookieLogEntry(Cookie(cookieStr), date))
          case None       => Left(InvalidTimestampError(lineNumber))
        }
      case _ => Left(MalformedCsvRowError(lineNumber))
    }
  }

  /**
    * Computes the most frequent cookies from the given entries.
    * Returns an error if the list is empty.
    */
  private def mostFrequentCookies(
      entries: List[CookieLogEntry]
  ): Either[NoCookiesOnDateError.type, Set[Cookie]] = {
    val frequencies = entries.groupMapReduce(_.cookie)(_ => 1)(_ + _)
    frequencies.values.maxOption match {
      case None      => Left(NoCookiesOnDateError)
      case Some(max) => Right(frequencies.filter(_._2 == max).keySet)
    }
  }
}

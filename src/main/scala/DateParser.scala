import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object DateParser {

  /**
    * Formatter for "yyyy-MM-dd" pattern (e.g "2025-06-08")
    */
  private val SimpleDatePattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /**
    * Parses a string in "yyyy-MM-dd" format to LocalDate.
    */
  val parseSimpleDate: String => Option[LocalDate] =
    parseLocalDate(SimpleDatePattern)

  /**
    * Parses a string in ISO offset date-time format (e.g., "2025-06-08T15:40:00+03:30") to LocalDate.
    */
  val parseIsoOffsetDate: String => Option[LocalDate] =
    parseLocalDate(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

  /**
    * Attempts to parse a string into a LocalDate using the provided formatter.
    *
    * @param formatter The DateTimeFormatter to use for parsing.
    * @param dateStr The date string to parse,
    * @return A function that takes a date string and returns an Option[LocalDate].
    */
  private def parseLocalDate(formatter: DateTimeFormatter)(dateStr: String) =
    Try(LocalDate.parse(dateStr, formatter)).toOption

}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate
import DateParser._

class DateParserSpec extends AnyFlatSpec with Matchers {

  "parseSimpleDate" should "return Some(LocalDate) for a valid yyyy-MM-dd string" in {
    parseSimpleDate("2025-06-08") should be(Some(LocalDate.of(2025, 6, 8)))
  }

  it should "return None for an invalid format like dd-MM-yyyy" in {
    parseSimpleDate("08-06-2025") should be(None)
  }

  it should "return None for garbage input" in {
    parseSimpleDate("foo") should be(None)
  }

  it should "return None for an empty string" in {
    parseSimpleDate("") should be(None)
  }

  "parseIsoOffsetDate" should "return Some(LocalDate) for a valid ISO offset string" in {
    parseIsoOffsetDate("2025-06-08T12:30:00+03:30") should be(Some(LocalDate.of(2025, 6, 8)))
  }

  it should "return None for a simple date without offset" in {
    parseIsoOffsetDate("2025-06-08") should be(None)
  }

  it should "return None for junk input" in {
    parseIsoOffsetDate("not-a-date") should be(None)
  }
}

import CookieAnalyzer.CookieAnalyzerError._
import CookieAnalyzer._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}
import java.time.LocalDate

class CookieAnalyzerSpec extends AnyFlatSpec with Matchers {

  // Utility to create temp files
  def createTestFile(content: String): Path = {
    val path = Files.createTempFile("test_cookie_log", ".csv")
    Files.write(path, content.stripMargin.getBytes)
    path.toFile.deleteOnExit()
    path
  }

  "findMostActiveCookiesForDate" should "return the single most active cookie on a valid date" in {
    val csv =
      """cookie,timestamp
        |A,2025-06-09T10:00:00+00:00
        |B,2025-06-09T12:00:00+00:00
        |A,2025-06-09T14:00:00+00:00
        |B,2025-06-08T14:00:00+00:00
        |""".stripMargin
    val file = createTestFile(csv)

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe Right(MostActiveCookies(Set(Cookie("A"))))
  }

  it should "return multiple cookies in case of tie" in {
    val csv =
      """cookie,timestamp
        |A,2025-06-09T10:00:00+00:00
        |B,2025-06-09T11:00:00+00:00
        |""".stripMargin
    val file = createTestFile(csv)

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe Right(MostActiveCookies(Set(Cookie("A"), Cookie("B"))))
  }

  it should "return NoCookiesOnDateError when no cookie is found for the given date" in {
    val csv =
      """cookie,timestamp
        |A,2025-06-08T10:00:00+00:00
        |B,2025-06-08T12:00:00+00:00
        |""".stripMargin
    val file = createTestFile(csv)

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe Left(NoCookiesOnDateError)
  }

  it should "return InvalidTimestampError if a timestamp is malformed" in {
    val csv =
      """cookie,timestamp
        |A,2025-06-08T10:00:00+00:00
        |A,not-a-valid-timestamp
        |B,2025-06-08T12:00:00+00:00
        |""".stripMargin
    val file = createTestFile(csv)

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe a[Left[_, InvalidTimestampError]]
    result.left.get.asInstanceOf[InvalidTimestampError].line shouldBe 3
  }

  it should "return MalformedCsvRowError if a row has missing fields" in {
    val csv =
      """cookie,timestamp
        |A-only-one-column
        |""".stripMargin
    val file = createTestFile(csv)

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe a[Left[_, MalformedCsvRowError]]
    result.left.get.asInstanceOf[MalformedCsvRowError].line shouldBe 2
  }

  it should "return EmptyCsvFileError if file is empty" in {
    val file = createTestFile("")

    val result =
      findMostActiveCookiesForDate(file.toString, LocalDate.parse("2025-06-09"))

    result shouldBe Left(EmptyCsvFileError)
  }

  it should "return FileReadFailure if file does not exist" in {
    val fakePath = "non_existent_file.csv"

    val result =
      findMostActiveCookiesForDate(fakePath, LocalDate.parse("2025-06-09"))

    result shouldBe a[Left[_, FileReadFailure]]
    val error = result.left.get.asInstanceOf[FileReadFailure]
    error.path shouldBe fakePath
  }
}

import CliConfig._
import InputHandler.ParseError.InvalidFlagValue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

class CliConfigSpec extends AnyFlatSpec with Matchers {

  implicit class RichFile(file: File) {
    def writeText(text: String): Unit = {
      import java.nio.file.{Files => JFiles}
      JFiles.write(file.toPath, text.getBytes)
    }
  }

  "inputFileFlag.validateValue" should "return an instance of Right for a valid file with content" in {
    val file = Files.createTempFile("valid", ".csv").toFile
    file.writeText("cookie,timestamp\n")
    try {
      val result = inputFileFlag().validateValue(file.getAbsolutePath)
      result should be(Right(()))
    } finally {
      file.delete()
    }
  }

  it should "return InvalidFlagValue for a non-existent file" in {
    val result = inputFileFlag().validateValue("nonexistent.csv")
    assert(result.left.exists(_.isInstanceOf[InvalidFlagValue]))
  }

  it should "return InvalidFlagValue using a mock FileChecker that fails" in {
    val mockChecker = new FileChecker {
      override def isValidFile(path: String): Boolean = false
    }
    val result = inputFileFlag(mockChecker).validateValue("/mock/path.csv")
    assert(result.left.exists(_.isInstanceOf[InvalidFlagValue]))
  }

  it should "return an instance of Right using a mock FileChecker that always passes" in {
    val mockChecker = new FileChecker {
      override def isValidFile(path: String): Boolean = true
    }
    val result = inputFileFlag(mockChecker).validateValue("/mock/path.csv")
    result should be(Right(()))
  }

  "targetDateFlag.validateValue" should "return Right(()) for a valid YYYY-MM-DD date" in {
    targetDateFlag.validateValue("2025-06-07") should be(Right(()))
  }

  it should "return InvalidFlagValue for an invalid date format" in {
    val result = targetDateFlag.validateValue("07-06-2025")
    assert(result.left.exists(_.isInstanceOf[InvalidFlagValue]))
  }

  it should "return InvalidFlagValue for garbage input" in {
    val result = targetDateFlag.validateValue("not-a-date")
    assert(result.left.exists(_.isInstanceOf[InvalidFlagValue]))
  }
}

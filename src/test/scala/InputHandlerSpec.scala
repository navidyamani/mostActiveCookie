import org.scalatest.funsuite.AnyFunSuite

import InputHandler._
import InputHandler.ParseError._
import InputHandler.InputFlag

class InputHandlerSpec extends AnyFunSuite {

  val fileFlag = FlagConfig(InputFlag("-f"), "file path", validateNonEmpty)
  val dateFlag = FlagConfig(InputFlag("-d"), "target date", validateNonEmpty)

  val allFlags = Set(fileFlag, dateFlag)

  def validateNonEmpty: String => Either[InvalidFlagValue, Unit] = { value =>
    if (value.trim.nonEmpty) Right(())
    else Left(InvalidFlagValue("Empty value"))
  }

  test("Valid args -f and -d return correct ParsedArgs") {
    val args = Array("-f", "cookies.csv", "-d", "2025-06-01")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isRight)
    assert(
      result.contains(
        ParsedArgs(
          Map(
            InputFlag("-d") -> "2025-06-01",
            InputFlag("-f") -> "cookies.csv"
          )
        )
      )
    )
  }

  test("Missing required flag returns MissingFlags error") {
    val args = Array("-f", "cookies.csv") // Missing -d
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case MissingFlags(flags) => flags == Set(InputFlag("-d"))
      case _                   => false
    })
  }

  test("Unrecognized flag returns UnrecognizedFlag error") {
    val args = Array("-f", "cookies.csv", "-x", "something", "-d", "2025-06-01")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case UnrecognizedFlag("-x") => true
      case _                      => false
    })
  }

  test("Duplicate flag returns DuplicateFlag error") {
    val args = Array("-f", "cookies.csv", "-f", "again.csv", "-d", "2025-06-01")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case DuplicateFlag(flag) => flag == InputFlag("-f")
      case _                   => false
    })
  }

  test("Missing value returns MissingValue error") {
    val args = Array("-f")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case MissingValue(InputFlag("-f")) => true
      case _                             => false
    })
  }

  test("Value is mistaken for flag returns MissingValue error") {
    val args = Array("-f", "-d") // thinks -d is a value
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case MissingValue(InputFlag("-f")) => true
      case _                             => false
    })
  }

  test("Empty value triggers custom validator error") {
    val args = Array("-f", " ", "-d", "2025-06-01")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isLeft)
    assert(result.left.exists {
      case InvalidFlagValue(msg) => msg == "Empty value"
      case _                     => false
    })
  }

  test("Flags are case-insensitive by InputFlag definition") {
    val args = Array("-F", "cookies.csv", "-D", "2025-06-01")
    val result = InputHandler.parse(args, allFlags)

    assert(result.isRight)
    assert(
      result.contains(
        ParsedArgs(
          Map(
            InputFlag("-f") -> "cookies.csv",
            InputFlag("-d") -> "2025-06-01"
          )
        )
      )
    )
  }
}

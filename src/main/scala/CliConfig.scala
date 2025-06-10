import InputHandler.{FlagConfig, InputFlag}
import InputHandler.ParseError.InvalidFlagValue


trait FileChecker {
  def isValidFile(path: String): Boolean
}
object DefaultFileChecker extends FileChecker {
  override def isValidFile(path: String): Boolean = {
    val file = new java.io.File(path)
    file.isFile && file.canRead
  }
}

object CliConfig {

  private def validateInputFile(input: String, checker: FileChecker) =
    Either.cond(
      test = checker.isValidFile(input),
      right = (),
      left = InvalidFlagValue(s"Error: File not found or unreadable: '$input'.")
    )

  def inputFileFlag(checker: FileChecker = DefaultFileChecker) = FlagConfig(
    flag = InputFlag("-f"),
    description = "Path to CSV file (Required)",
    validateValue = input => validateInputFile(input, checker)
  )

  private def validateInputDate(input: String): Either[InvalidFlagValue, Unit] =
    DateParser.parseSimpleDate(input).map(_ => ()).toRight {
      InvalidFlagValue(
        "Error: Invalid date. Enter a valid date in YYYY-MM-DD format (e.g., 2025-06-09)"
      )
    }

  val targetDateFlag = FlagConfig(
    flag = InputFlag("-d"),
    description = "Date in yyyy-mm-dd format (Required)",
    validateValue = validateInputDate
  )
}
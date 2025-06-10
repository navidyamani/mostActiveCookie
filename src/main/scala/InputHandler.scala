import InputHandler.ParseError.{DuplicateFlag, MissingFlags}

object InputHandler {

  /**
    * Represents the successfully parsed command-line arguments.
    * @param values A map from recognized input flags to their associated string values.
    */
  final case class ParsedArgs(values: Map[InputFlag, String])

  /**
    * Represents the errors that may occur during parsing or validation of input arguments.
    */
  sealed trait ParseError { val message: String }
  object ParseError {
    final case class InvalidFlagValue(message: String) extends ParseError

    final case class UnrecognizedFlag(arg: String) extends ParseError {
      override val message =
        s"'$arg' is not a recognized command. Try `--help` for usage details."
    }

    final case class DuplicateFlag(flag: InputFlag) extends ParseError {
      override val message =
        s"The flag '${flag.key}' was specified multiple times. Please provide it only once!"
    }

    final case class MissingValue(flag: InputFlag) extends ParseError {
      override val message = s"Missing value for flag '${flag.key}'."
    }

    final case class MissingFlags(flags: Set[InputFlag]) extends ParseError {
      override val message: String = {
         val flagsStr = flags.map(_.key).mkString(",")
        s"The following required flag(s) are missing: $flagsStr"
      }
    }
  }

  /**
    * A value-class represents a normalized command-line flag (case-insensitive).
    * @param key The flag key string (e.g., "-d" or "-f"), in lowercase.
    */
  final case class InputFlag(key: String) extends AnyVal
  object InputFlag {
    def apply(flag: String): InputFlag = new InputFlag(flag.toLowerCase)
  }

  /**
    * Describes the configuration for a single expected input flag.
    * @param flag The flag to match (e.g., "-f").
    * @param description A short description of the flag for help messages.
    * @param validateValue A function to validate the flag's value.
    *                        It returns either a validation error or Unit (success).
    */
  final case class FlagConfig(
      flag: InputFlag,
      description: String,
      validateValue: String => Either[ParseError.InvalidFlagValue, Unit]
  )

  /**
    * Parses the given command-line arguments into flag-value pairs.
    * @param inputArgs The raw argument array (from `args` in `main`).
    * @param configs   The set of valid flag definitions with their validation rules.
    * @return Either a parsing error, or an instance of ParseResult.
    */
  def parse(
      inputArgs: Array[String],
      configs: Set[FlagConfig]
  ): Either[ParseError, ParsedArgs] = {

    val findFlagConfig: InputFlag => Option[FlagConfig] =
      f => configs.find(_.flag == f)

    for {
      parsedMap <- processArgs(inputArgs.toList, getConfig = findFlagConfig)
      requiredFlags = configs.map(_.flag)
      missingFlags = requiredFlags -- parsedMap.keySet
      _ <- Either.cond(missingFlags.isEmpty, (), MissingFlags(missingFlags))
    } yield ParsedArgs(parsedMap)

  }

  /**
    * Recursively processes a list of input arguments/
    * @param args The remaining command-line arguments to parse.
    * @param parsed The accumulator for already-parsed flag-value pairs.
    * @param getConfig A function to look up a flag's configuration.
    * @return Either a parsing error, or the final parsed map.
    */
  private def processArgs(
      args: List[String],
      parsed: Map[InputFlag, String] = Map.empty,
      getConfig: InputFlag => Option[FlagConfig]
  ): Either[ParseError, Map[InputFlag, String]] = {

    def checkDuplication(flag: InputFlag): Either[DuplicateFlag, Unit] = {
      Right(()).filterOrElse(_ => !parsed.contains(flag), DuplicateFlag(flag))
    }

    args match {
      case Nil => Right(parsed)

      case (flagStr :: _) if getConfig(InputFlag(flagStr)).isEmpty =>
        Left(ParseError.UnrecognizedFlag(flagStr))

      // Only key-value flags are supported.
      case flagStr :: Nil =>
        Left(ParseError.MissingValue(InputFlag(flagStr)))

      // Only key-value flags are supported.
      case flagStr :: value :: _ if getConfig(InputFlag(value)).isDefined =>
        Left(ParseError.MissingValue(InputFlag(flagStr)))

      case flagStr :: value :: tail =>
        val flag = InputFlag(flagStr)
        for {
          config <-
            getConfig(flag).toRight(ParseError.UnrecognizedFlag(flagStr))
          _ <- config.validateValue(value)
          _ <- checkDuplication(flag)
          result <- processArgs(tail, parsed + (flag -> value), getConfig)
        } yield result
    }
  }
}

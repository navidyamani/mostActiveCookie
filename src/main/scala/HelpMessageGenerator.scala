import InputHandler.{FlagConfig, InputFlag}

object HelpMessageGenerator {

  /** Set of flags that trigger help display. */
  private val HelpOptions: Set[InputFlag] = Set(InputFlag("-h"), InputFlag("--help"))

  /**
   * @param args The raw argument array (from `args` in `main`).
   * @return true if help was requested via known help flags.
   * */
  def shouldShowHelp(args: Array[String]): Boolean =
    args.exists(arg => HelpOptions.contains(InputFlag(arg)))

  /**
   * @param availableFlags   The set of valid flag definitions with their validation rules.
   * @return a formatted help message based on the provided flags.
   */
  def renderHelpMessage(availableFlags: Set[FlagConfig]): String = {
    val sortedFlags = availableFlags.toList.sortBy(_.flag.key)

    val renderedFlags = sortedFlags
      .map(cfg => f"  ${cfg.flag.key}%-18s ${cfg.description}")
      .mkString("\n")

    s"""
       |Usage:
       |  sbt "run [options]"
       |
       |Description:
       |  This application processes a cookie log file to identify and return the most active cookie for a specified date.
       |
       |Options:
       |  -h | --help        Show this help message
       |$renderedFlags
       |
       |Examples:
       |  sbt "run -f cookie_log.csv -d 2025-06-09"
       |
       |Notes:
       |  - Flags are case-insensitive.
       |  - Required flags must be supplied.
       |  - Unrecognized flags will result in an error.
       |  - Duplicate flags will result in an error.
       |""".stripMargin
  }
}

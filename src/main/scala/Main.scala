
import org.slf4j.LoggerFactory
import CliConfig.targetDateFlag

object Main {

  private val logger = LoggerFactory.getLogger(getClass)

  private val fileArgConfig = CliConfig.inputFileFlag()
  private val flagConfigs = Set(targetDateFlag, fileArgConfig)

  /**
   * Entry point for the CLI application.
   * Parses arguments, handles help flags, runs main cookie analysis logic.
   */
  def main(args: Array[String]): Unit = {

    println("===>")

    if (HelpMessageGenerator.shouldShowHelp(args)) {
      logger.info(HelpMessageGenerator.renderHelpMessage(flagConfigs))
      sys.exit(0)
    }

    InputHandler.parse(args, flagConfigs) match {
      case Left(err) =>
        logger.error(err.message)
        sys.exit(1)

      case Right(parsedArgs) =>
                val result = for {
                  filePath <- parsedArgs.values.get(fileArgConfig.flag)
                  dateStr <- parsedArgs.values.get(targetDateFlag.flag)
                  date <- DateParser.parseSimpleDate(dateStr)
                } yield CookieAnalyzer.findMostActiveCookiesForDate(filePath, date)

                result match {
                  case Some(Right(result)) =>
                    result.cookies.foreach(cookie => println(cookie.value))

                  case Some(Left(error)) =>
                    logger.error(error.message)
                      sys.exit(1)

                  case None =>
                    logger.error(("Missing required flags. Try --help for usage."))
                      sys.exit(1)
                }
    }
  }
}

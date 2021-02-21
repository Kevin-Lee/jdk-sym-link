package jdksymlink.core

import java.io.{IOException, PrintWriter, StringWriter}

/** @author Kevin Lee
  * @since 2019-12-25
  */
sealed trait JdkSymLinkError

object JdkSymLinkError {

  final case class LsFailure(errorCode: Int, message: String, commands: List[String])            extends JdkSymLinkError
  final case class PathExistsAndNoSymLink(path: String, message: String, commands: List[String]) extends JdkSymLinkError
  final case class CommandFailure(throwable: Throwable, commands: List[String])                  extends JdkSymLinkError

  def lsFailure(errorCode: Int, message: String, commands: List[String]): JdkSymLinkError =
    LsFailure(errorCode, message, commands)

  def pathExistsAndNoSymLink(path: String, message: String, commands: List[String]): JdkSymLinkError =
    PathExistsAndNoSymLink(path, message, commands)

  def commandFailure(throwable: Throwable, commands: List[String]): JdkSymLinkError =
    CommandFailure(throwable, commands)

  def render(jdkSymLinkError: JdkSymLinkError): String = jdkSymLinkError match {
    case LsFailure(errorCode, message, commands) =>
      s"""${Console.RED}ErrorCode${Console.RESET}: ${errorCode.toString}
         |${Console.RED}Error${Console.RESET}: $message
         |  when running ${commands.mkString("[", ", ", "]")}
         |""".stripMargin

    case PathExistsAndNoSymLink(path, message, commands) =>
      s"""${Console.RED}Failed to run ${commands.mkString(" ")}
         |Error${Console.RESET}: $message
         |  when running ${commands.mkString("[", ", ", "]")}
         |""".stripMargin

    case CommandFailure(throwable, commands) =>
      val out         = new StringWriter()
      val printWriter = new PrintWriter(out)
      throwable.printStackTrace(printWriter)
      val stackTrace = out.toString
      throwable match {
        case ex: IOException =>
          s"""${Console.RED}Error${Console.RESET} when running ${commands.mkString("[", ", ", "]")}:
             |  - ${ex.getMessage}
             |""".stripMargin
        case _ =>
          s"""${Console.RED}Error${Console.RESET} when running ${commands.mkString("[", ", ", "]")}:
             |$stackTrace
             |""".stripMargin

      }

  }
}

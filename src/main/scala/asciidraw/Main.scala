package asciidraw

import scala.annotation.tailrec
import scala.io.StdIn

object Main:
  @main def asciiDraw(): Unit =
    loop(Session.initial)

  @tailrec
  private def loop(session: Session): Unit =
    prompt()
    Option(StdIn.readLine()).map(_.trim) match
      case None       => ()
      case Some("")   => loop(session)
      case Some(line) =>
        Parser.parse(line) match
          case Right(Command.Quit) => ()
          case parsed              => loop(step(session, parsed))

  private def step(session: Session, parsed: Either[AppError, Command]): Session =
    parsed.flatMap(Interpreter.interpret(session, _)) match
      case Left(error) =>
        println(s"Erreur: ${error.message}")
        session
      case Right(result) =>
        result.output.foreach(println)
        result.session

  private def prompt(): Unit =
    print("AsciiDraw> ")
    Console.flush()

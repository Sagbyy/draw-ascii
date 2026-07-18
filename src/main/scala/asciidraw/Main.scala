package asciidraw

import scala.annotation.tailrec
import scala.io.StdIn

object Main:
  @main def asciiDraw(): Unit =
    loop(None)

  @tailrec
  private def loop(canvas: Option[Canvas]): Unit =
    prompt()
    Option(StdIn.readLine()).map(_.trim) match
      case None         => ()
      case Some("")     => loop(canvas)
      case Some(line) =>
        Parser.parse(line) match
          case Right(Command.Quit) => ()
          case parsed              => loop(step(canvas, parsed))

  private def step(canvas: Option[Canvas], parsed: Either[AppError, Command]): Option[Canvas] =
    parsed.flatMap(Interpreter.interpret(canvas, _)) match
      case Left(error) =>
        println(s"Erreur: ${error.message}")
        canvas
      case Right(result) =>
        result.output.foreach(println)
        result.canvas

  private def prompt(): Unit =
    print("AsciiDraw> ")
    Console.flush()

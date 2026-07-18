package asciidraw

object Parser:
  def parse(input: String): Either[AppError, Command] =
    tokenize(input) match
      case "canvas" :: args                    => parseCanvas(args)
      case "point" :: args                     => parsePoint(args)
      case "line" :: args                      => parseLine(args)
      case "rect" :: args                      => parseRect(args)
      case "fill" :: args                      => parseFill(args)
      case "setchar" :: args                   => parseSetChar(args)
      case "clear" :: args                     => parseNoArgs("clear", Command.Clear, args)
      case "undo" :: args                      => parseNoArgs("undo", Command.Undo, args)
      case "redo" :: args                      => parseNoArgs("redo", Command.Redo, args)
      case "render" :: args                    => parseNoArgs("render", Command.Render, args)
      case (name @ ("quit" | "exit")) :: args  => parseNoArgs(name, Command.Quit, args)
      case name :: _                           => Left(AppError.UnknownCommand(name))
      case Nil                                 => Left(AppError.UnknownCommand(""))

  private def tokenize(input: String): List[String] =
    input.trim.split(' ').toList.filter(_.nonEmpty)

  private def parseNoArgs(name: String, command: Command, args: List[String]): Either[AppError, Command] =
    args match
      case Nil => Right(command)
      case _   => Left(AppError.InvalidArgumentCount(name))

  private def parseCanvas(args: List[String]): Either[AppError, Command] =
    args match
      case width :: height :: Nil =>
        for
          w <- parseInt(width)
          h <- parseInt(height)
        yield Command.CreateCanvas(w, h)
      case _ => Left(AppError.InvalidArgumentCount("canvas"))

  private def parsePoint(args: List[String]): Either[AppError, Command] =
    args match
      case x :: y :: Nil =>
        for
          px <- parseInt(x)
          py <- parseInt(y)
        yield Command.DrawPoint(px, py)
      case _ => Left(AppError.InvalidArgumentCount("point"))

  private def parseLine(args: List[String]): Either[AppError, Command] =
    args match
      case x1 :: y1 :: x2 :: y2 :: Nil =>
        for
          px1 <- parseInt(x1)
          py1 <- parseInt(y1)
          px2 <- parseInt(x2)
          py2 <- parseInt(y2)
        yield Command.DrawLine(px1, py1, px2, py2)
      case _ => Left(AppError.InvalidArgumentCount("line"))

  private def parseRect(args: List[String]): Either[AppError, Command] =
    args match
      case x :: y :: width :: height :: Nil =>
        for
          px <- parseInt(x)
          py <- parseInt(y)
          w <- parseInt(width)
          h <- parseInt(height)
        yield Command.DrawRect(px, py, w, h)
      case _ => Left(AppError.InvalidArgumentCount("rect"))

  private def parseFill(args: List[String]): Either[AppError, Command] =
    args match
      case x :: y :: char :: Nil =>
        for
          px <- parseInt(x)
          py <- parseInt(y)
          c <- parseChar(char)
        yield Command.Fill(px, py, c)
      case _ => Left(AppError.InvalidArgumentCount("fill"))

  private def parseSetChar(args: List[String]): Either[AppError, Command] =
    args match
      case value :: Nil => parseChar(value).map(Command.SetChar.apply)
      case _            => Left(AppError.InvalidArgumentCount("setchar"))

  private def parseInt(value: String): Either[AppError, Int] =
    value.toIntOption.toRight(AppError.InvalidNumber(value))

  private def parseChar(value: String): Either[AppError, Char] =
    value.toList match
      case c :: Nil => Right(c)
      case _        => Left(AppError.InvalidChar(value))

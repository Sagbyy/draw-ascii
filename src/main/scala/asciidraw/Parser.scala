package asciidraw

object Parser:
  def parse(input: String): Either[AppError, Command] =
    tokenize(input) match
      case "canvas" :: args        => parseCanvas(args)
      case "point" :: args         => parsePoint(args)
      case "line" :: args          => parseLine(args)
      case "render" :: Nil         => Right(Command.Render)
      case ("quit" | "exit") :: Nil => Right(Command.Quit)
      case name :: _               => Left(AppError.UnknownCommand(name))
      case Nil                     => Left(AppError.UnknownCommand(""))

  private def tokenize(input: String): List[String] =
    input.trim.split(' ').toList.filter(_.nonEmpty)

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

  private def parseInt(value: String): Either[AppError, Int] =
    value.toIntOption.toRight(AppError.InvalidNumber(value))

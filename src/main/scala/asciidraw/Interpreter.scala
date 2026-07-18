package asciidraw

object Interpreter:
  case class Result(canvas: Option[Canvas], output: Option[String])

  def interpret(canvas: Option[Canvas], command: Command): Either[AppError, Result] =
    command match
      case Command.CreateCanvas(width, height) =>
        Canvas.create(width, height).map(created => Result(Some(created), None))
      case Command.DrawPoint(x, y) =>
        drawPoint(canvas, x, y).map(drawn => Result(Some(drawn), None))
      case Command.Render =>
        requireCanvas(canvas).map(current => Result(canvas, Some(current.render)))
      case Command.Quit =>
        Right(Result(canvas, None))

  private def drawPoint(canvas: Option[Canvas], x: Int, y: Int): Either[AppError, Canvas] =
    requireCanvas(canvas).flatMap(current =>
      if current.contains(x, y) then Right(current.withPixel(x, y, Canvas.pointChar))
      else Left(AppError.OutOfBounds(x, y))
    )

  private def requireCanvas(canvas: Option[Canvas]): Either[AppError, Canvas] =
    canvas.toRight(AppError.NoCanvas)

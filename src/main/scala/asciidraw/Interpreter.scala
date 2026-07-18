package asciidraw

object Interpreter:
  case class Result(canvas: Option[Canvas], output: Option[String])

  def interpret(canvas: Option[Canvas], command: Command): Either[AppError, Result] =
    command match
      case Command.CreateCanvas(width, height) =>
        Canvas.create(width, height).map(created => Result(Some(created), None))
      case Command.DrawPoint(x, y) =>
        drawPoint(canvas, x, y).map(drawn => Result(Some(drawn), None))
      case Command.DrawLine(x1, y1, x2, y2) =>
        drawLine(canvas, x1, y1, x2, y2).map(drawn => Result(Some(drawn), None))
      case Command.Render =>
        requireCanvas(canvas).map(current => Result(canvas, Some(current.render)))
      case Command.Quit =>
        Right(Result(canvas, None))

  private def drawPoint(canvas: Option[Canvas], x: Int, y: Int): Either[AppError, Canvas] =
    requireCanvas(canvas).flatMap(current =>
      if current.contains(x, y) then Right(current.withPixel(x, y, Canvas.pointChar))
      else Left(AppError.OutOfBounds(x, y))
    )

  private def drawLine(canvas: Option[Canvas], x1: Int, y1: Int, x2: Int, y2: Int): Either[AppError, Canvas] =
    for
      current <- requireCanvas(canvas)
      points <- Drawing.linePoints(x1, y1, x2, y2).toRight(AppError.DiagonalLine)
      _ <- checkBounds(current, points)
    yield current.withPixels(points, Canvas.lineChar)

  private def checkBounds(canvas: Canvas, points: List[(Int, Int)]): Either[AppError, Unit] =
    points.find((x, y) => !canvas.contains(x, y)) match
      case Some((x, y)) => Left(AppError.OutOfBounds(x, y))
      case None         => Right(())

  private def requireCanvas(canvas: Option[Canvas]): Either[AppError, Canvas] =
    canvas.toRight(AppError.NoCanvas)

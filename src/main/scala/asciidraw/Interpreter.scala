package asciidraw

object Interpreter:
  case class Result(session: Session, output: Option[String])

  def interpret(session: Session, command: Command): Either[AppError, Result] =
    command match
      case Command.CreateCanvas(width, height) =>
        Canvas.create(width, height).map(created => updated(session, created))
      case Command.DrawPoint(x, y) =>
        draw(session, List((x, y)), Canvas.pointChar)
      case Command.DrawLine(x1, y1, x2, y2) =>
        linePoints(x1, y1, x2, y2).flatMap(points => draw(session, points, Canvas.lineChar))
      case Command.DrawRect(x, y, width, height) =>
        checkDimensions(width, height)
          .flatMap(_ => draw(session, Drawing.rectPoints(x, y, width, height), Canvas.lineChar))
      case Command.Fill(x, y, char) =>
        fill(session, x, y, char)
      case Command.SetChar(char) =>
        Right(Result(session.copy(drawChar = Some(char)), None))
      case Command.Clear =>
        requireCanvas(session).map(current => updated(session, current.cleared))
      case Command.Undo =>
        undo(session)
      case Command.Redo =>
        redo(session)
      case Command.Render =>
        requireCanvas(session).map(current => Result(session, Some(current.render)))
      case Command.Quit =>
        Right(Result(session, None))

  private def updated(session: Session, canvas: Canvas): Result =
    Result(
      session.copy(canvas = Some(canvas), undoStack = session.canvas :: session.undoStack, redoStack = Nil),
      None
    )

  private def undo(session: Session): Either[AppError, Result] =
    session.undoStack match
      case previous :: rest =>
        Right(
          Result(
            session.copy(canvas = previous, undoStack = rest, redoStack = session.canvas :: session.redoStack),
            None
          )
        )
      case Nil => Left(AppError.NothingToUndo)

  private def redo(session: Session): Either[AppError, Result] =
    session.redoStack match
      case next :: rest =>
        Right(
          Result(
            session.copy(canvas = next, undoStack = session.canvas :: session.undoStack, redoStack = rest),
            None
          )
        )
      case Nil => Left(AppError.NothingToRedo)

  private def draw(session: Session, points: List[(Int, Int)], defaultChar: Char): Either[AppError, Result] =
    for
      current <- requireCanvas(session)
      _ <- checkBounds(current, points)
    yield updated(session, current.withPixels(points, session.drawChar.getOrElse(defaultChar)))

  private def fill(session: Session, x: Int, y: Int, char: Char): Either[AppError, Result] =
    for
      current <- requireCanvas(session)
      _ <- checkBounds(current, List((x, y)))
    yield updated(session, current.withPixels(Drawing.fillZone(current, x, y).toList, char))

  private def linePoints(x1: Int, y1: Int, x2: Int, y2: Int): Either[AppError, List[(Int, Int)]] =
    Drawing.linePoints(x1, y1, x2, y2).toRight(AppError.DiagonalLine)

  private def checkDimensions(width: Int, height: Int): Either[AppError, Unit] =
    if width > 0 && height > 0 then Right(())
    else Left(AppError.InvalidDimensions(width, height))

  private def checkBounds(canvas: Canvas, points: List[(Int, Int)]): Either[AppError, Unit] =
    points.find((x, y) => !canvas.contains(x, y)) match
      case Some((x, y)) => Left(AppError.OutOfBounds(x, y))
      case None         => Right(())

  private def requireCanvas(session: Session): Either[AppError, Canvas] =
    session.canvas.toRight(AppError.NoCanvas)

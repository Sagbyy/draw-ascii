package asciidraw

case class Session(
    canvas: Option[Canvas],
    drawChar: Option[Char],
    undoStack: List[Option[Canvas]],
    redoStack: List[Option[Canvas]]
)

object Session:
  val initial: Session = Session(None, None, Nil, Nil)

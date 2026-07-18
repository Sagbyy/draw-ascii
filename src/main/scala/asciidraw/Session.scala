package asciidraw

case class Session(canvas: Option[Canvas], drawChar: Option[Char])

object Session:
  val initial: Session = Session(None, None)

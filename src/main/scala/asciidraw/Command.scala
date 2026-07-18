package asciidraw

sealed trait Command

object Command:
  case class CreateCanvas(width: Int, height: Int) extends Command
  case class DrawPoint(x: Int, y: Int) extends Command
  case object Render extends Command
  case object Quit extends Command

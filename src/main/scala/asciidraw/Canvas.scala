package asciidraw

case class Canvas(width: Int, height: Int, pixels: Map[(Int, Int), Char]):
  def contains(x: Int, y: Int): Boolean =
    x >= 0 && x < width && y >= 0 && y < height

  def withPixel(x: Int, y: Int, char: Char): Canvas =
    copy(pixels = pixels + ((x, y) -> char))

  def withPixels(points: List[(Int, Int)], char: Char): Canvas =
    points.foldLeft(this) { case (canvas, (x, y)) => canvas.withPixel(x, y, char) }

  def charAt(x: Int, y: Int): Char =
    pixels.getOrElse((x, y), Canvas.emptyChar)

  def render: String =
    (0 until height).map(renderRow).mkString("\n")

  private def renderRow(y: Int): String =
    (0 until width).map(x => charAt(x, y)).mkString

object Canvas:
  val emptyChar: Char = '.'
  val pointChar: Char = '*'
  val lineChar: Char = '#'

  def create(width: Int, height: Int): Either[AppError, Canvas] =
    if width > 0 && height > 0 then Right(Canvas(width, height, Map.empty))
    else Left(AppError.InvalidDimensions(width, height))

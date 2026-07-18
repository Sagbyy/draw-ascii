package asciidraw

object Drawing:
  def linePoints(x1: Int, y1: Int, x2: Int, y2: Int): Option[List[(Int, Int)]] =
    if x1 == x2 then Some(range(y1, y2).map(y => (x1, y)))
    else if y1 == y2 then Some(range(x1, x2).map(x => (x, y1)))
    else None

  def rectPoints(x: Int, y: Int, width: Int, height: Int): List[(Int, Int)] =
    val horizontal = range(x, x + width - 1).flatMap(px => List((px, y), (px, y + height - 1)))
    val vertical = range(y, y + height - 1).flatMap(py => List((x, py), (x + width - 1, py)))
    (horizontal ++ vertical).distinct

  private def range(from: Int, to: Int): List[Int] =
    ((from min to) to (from max to)).toList

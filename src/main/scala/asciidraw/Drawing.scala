package asciidraw

object Drawing:
  def linePoints(x1: Int, y1: Int, x2: Int, y2: Int): Option[List[(Int, Int)]] =
    if x1 == x2 then Some(range(y1, y2).map(y => (x1, y)))
    else if y1 == y2 then Some(range(x1, x2).map(x => (x, y1)))
    else None

  private def range(from: Int, to: Int): List[Int] =
    ((from min to) to (from max to)).toList
